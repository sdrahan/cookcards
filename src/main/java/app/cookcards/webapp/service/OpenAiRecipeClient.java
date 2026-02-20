package app.cookcards.webapp.service;

import app.cookcards.webapp.dto.CookcardsRecipeDTO;
import app.cookcards.webapp.user.TargetLanguage;
import app.cookcards.webapp.user.UnitsMode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Component
public class OpenAiRecipeClient {

    private static final String PARSE_PROMPT_TEMPLATE = """
            You convert an unstructured recipe into a single JSON object that matches this schema exactly (keys, nesting, types).
            Return ONLY the JSON object. No markdown, no commentary, no extra keys.

            OUTPUT SCHEMA (informal):
            {
              name: string (required, non-empty)
              images?: string[] (URLs found in input only)
              description?: string
              prepTime?: ISO-8601 duration (e.g. PT15M)
              cookTime?: ISO-8601 duration
              totalTime?: ISO-8601 duration
              yield?: string
              nutrition?: { calories?: string } (only if explicitly present)
              ingredients: string[] (required, >= 1, non-empty items)
              instructions: [
                {
                  name?: string
                  steps: [{ text: string (required), image?: string (URL found in input only) }]
                }
              ] (required, >= 1 section, each section >= 1 step)
            }

            OPTIONS:
            - unitsMode: one of ["metric", "imperial", "original"]
            - targetLanguage: string language name or IETF tag (e.g. "German" or "de") OR "original"

            RULES (strict):
            1) Do not invent facts:
               - Only include image URLs if present in the input.
               - Only include nutrition if explicitly present in the input.
               - Do not add missing ingredients/steps that are not present; if something is ambiguous, keep it as written.

            2) Ingredients:
               - Output as a flat list of strings, in the same order as the source.
               - Keep each ingredient as a single line.

            3) Instructions -> sections:
               - Always output instructions as an array of sections.
               - If headings/sections exist (e.g. "For the cake", "Sauce"), create multiple sections and set section.name.
               - If no clear headings exist, output exactly ONE section and set "name" to null.
               - Steps must be in correct order.
               - Each step object must have ONLY:
                 - text (required)
                 - image (required; set to null if no image URL is present)
               - Do NOT include step names, URLs, timers, etc.

            4) Time fields (prepTime/cookTime/totalTime):
               - If times are present, express as ISO-8601 duration:
                 - "10 minutes" -> "PT10M"
                 - "1 hour 30 minutes" -> "PT1H30M"
               - If a time is not clearly stated, omit that field.
               - If total time is clearly stated, use it.
               - If both prep and cook are clearly stated and total is not, you may compute totalTime = prep + cook.

            5) Unit conversion (only if unitsMode != "original"):
               - Convert quantities and units in BOTH ingredients and step text.
               - Keep ingredient names and instructions otherwise unchanged.
               - If unsure how to convert a specific item, keep the original phrasing for that part.
               - Rounding guidance:
                 - Metric: round grams to nearest 5g, milliliters to nearest 5ml, oven temp to nearest 5C.
                 - Imperial: round ounces to nearest 0.25oz, volumes to nearest 1 tsp / 1 tbsp / 1/4 cup where sensible, oven temp to nearest 5F.
               - Common conversions:
                 - 1 oz ~= 28.35 g; 1 lb ~= 453.6 g
                 - 1 fl oz ~= 29.57 ml
                 - 1 cup ~= 240 ml (US); 1 tbsp ~= 15 ml; 1 tsp ~= 5 ml
                 - C = (F - 32) x 5/9 ; F = (C x 9/5) + 32

            6) Translation (only if targetLanguage != "original"):
               - Translate ALL human-readable strings: name, description, ingredients lines, section names, step text, yield, nutrition strings.
               - Keep URLs unchanged.
               - Keep units consistent with unitsMode (do conversion first conceptually, then translate wording).

            7) Strict schema compliance:
               - Include all top-level keys.
               - For fields that are unknown/missing, use null (not omission).
               - For nutrition, use either null or {"calories": string|null}.

            INPUT:
            unitsMode: %s
            targetLanguage: %s

            UNSTRUCTURED RECIPE:
            %s
            """;

    private final RestClient openAi;
    private final ObjectMapper mapper;
    private final String model;

    public OpenAiRecipeClient(RestClient openAiRestClient,
                              ObjectMapper mapper,
                              @Value("${openai.model:gpt-4o-mini}") String model) {
        this.openAi = openAiRestClient;
        this.mapper = mapper;
        this.model = model;
    }

    public boolean isValidFoodRecipe(String rawText) {
        Map<String, Object> requestBody = Map.of(
                "model", model,
                "store", false,
                "input", new Object[]{
                        Map.of("role", "system", "content", """
                                Decide if the user input is a legitimate food recipe.
                                Return JSON only.
                                """),
                        Map.of("role", "user", "content", "TEXT:\n" + rawText)
                },
                "text", Map.of("format", Map.of(
                        "type", "json_schema",
                        "name", "recipe_check_v1",
                        "strict", true,
                        "schema", Map.of(
                                "type", "object",
                                "additionalProperties", false,
                                "required", List.of("isRecipe"),
                                "properties", Map.of(
                                        "isRecipe", Map.of("type", "boolean")
                                )
                        )
                ))
        );

        String outputJson = callResponsesApi(requestBody);
        try {
            JsonNode node = mapper.readTree(outputJson);
            return node.path("isRecipe").asBoolean(false);
        } catch (JsonProcessingException e) {
            throw new OpenAiException("Failed to parse recipe validation response: " + e.getMessage(), e);
        }
    }

    public ParsedRecipeResult parseRecipeFromText(String rawText, UnitsMode unitsMode, TargetLanguage targetLanguage) {
        UnitsMode effectiveUnitsMode = unitsMode == null ? UnitsMode.ORIGINAL : unitsMode;
        TargetLanguage effectiveTargetLanguage = targetLanguage == null ? TargetLanguage.ORIGINAL : targetLanguage;

        String parsePrompt = PARSE_PROMPT_TEMPLATE.formatted(
                toUnitsModeValue(effectiveUnitsMode),
                toTargetLanguageValue(effectiveTargetLanguage),
                rawText
        );

        Map<String, Object> requestBody = Map.of(
                "model", model,
                "store", false,
                "input", new Object[]{
                        Map.of("role", "user", "content", parsePrompt)
                },
                "text", Map.of("format", schemaDefinition())
        );

        String outputJson = callResponsesApi(requestBody);
        try {
            CookcardsRecipeDTO recipe = mapper.readValue(outputJson, CookcardsRecipeDTO.class);
            return new ParsedRecipeResult(recipe, outputJson);
        } catch (JsonProcessingException e) {
            throw new OpenAiException("Failed to parse OpenAI response JSON into CookcardsRecipeDTO: " + e.getMessage(), e);
        }
    }

    private String callResponsesApi(Map<String, Object> requestBody) {
        String responseJson = openAi.post()
                .uri("/responses")
                .body(requestBody)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, res) -> {
                    String body;
                    try {
                        body = new String(res.getBody().readAllBytes());
                    } catch (Exception e) {
                        body = "<unable to read error body>";
                    }
                    throw new OpenAiException("OpenAI API error: HTTP " + res.getStatusCode() + " body=" + body);
                })
                .body(String.class);

        if (responseJson == null || responseJson.isBlank()) {
            throw new OpenAiException("OpenAI API returned empty response body.");
        }

        return extractOutputJsonText(responseJson);
    }

    private String extractOutputJsonText(String responseJson) {
        try {
            JsonNode root = mapper.readTree(responseJson);
            JsonNode output = root.path("output");
            if (!output.isArray()) {
                throw new OpenAiException("Unexpected OpenAI response format: missing output array.");
            }

            for (JsonNode outItem : output) {
                if (!"message".equals(outItem.path("type").asText())) {
                    continue;
                }
                JsonNode content = outItem.path("content");
                if (!content.isArray()) {
                    continue;
                }
                for (JsonNode item : content) {
                    if ("output_text".equals(item.path("type").asText())) {
                        String text = item.path("text").asText(null);
                        if (text != null && !text.isBlank()) {
                            return text;
                        }
                    }
                }
            }

            throw new OpenAiException("OpenAI response did not contain output_text with JSON.");
        } catch (JsonProcessingException e) {
            throw new OpenAiException("Failed to parse OpenAI response envelope: " + e.getMessage(), e);
        }
    }

    private String toUnitsModeValue(UnitsMode unitsMode) {
        return switch (unitsMode) {
            case METRIC -> "metric";
            case IMPERIAL -> "imperial";
            case ORIGINAL -> "original";
        };
    }

    private String toTargetLanguageValue(TargetLanguage language) {
        return switch (language) {
            case ENGLISH -> "en";
            case GERMAN -> "de";
            case RUSSIAN -> "ru";
            case ORIGINAL -> "original";
        };
    }

    private Map<String, Object> schemaDefinition() {
        return Map.of(
                "type", "json_schema",
                "name", "cookcards_recipe_v1",
                "strict", true,
                "schema", Map.of(
                        "type", "object",
                        "additionalProperties", false,
                        "required", List.of(
                                "name",
                                "images",
                                "description",
                                "prepTime",
                                "cookTime",
                                "totalTime",
                                "yield",
                                "nutrition",
                                "ingredients",
                                "instructions"
                        ),
                        "properties", Map.of(
                                        "name", Map.of(
                                                "type", "string",
                                                "description", "Recipe title.",
                                                "pattern", ".*\\S.*"
                                        ),
                                        "images", Map.of(
                                                "type", List.of("array", "null"),
                                                "description", "Optional image URLs (recipe hero images). Only include URLs that are explicitly present in the input.",
                                                "items", Map.of(
                                                        "type", "string",
                                                        "pattern", "^https?://\\S+$"
                                                ),
                                                "minItems", 1,
                                                "maxItems", 20
                                        ),
                                        "description", Map.of(
                                                "type", List.of("string", "null"),
                                                "description", "Optional short description of the recipe.",
                                                "pattern", ".*\\S.*"
                                        ),
                                        "prepTime", Map.of(
                                                "type", List.of("string", "null"),
                                                "description", "Optional ISO 8601 duration for prep time (e.g. PT15M).",
                                                "format", "duration"
                                        ),
                                        "cookTime", Map.of(
                                                "type", List.of("string", "null"),
                                                "description", "Optional ISO 8601 duration for cook time (e.g. PT1H).",
                                                "format", "duration"
                                        ),
                                        "totalTime", Map.of(
                                                "type", List.of("string", "null"),
                                                "description", "Optional ISO 8601 duration for total time (e.g. PT1H30M).",
                                                "format", "duration"
                                        ),
                                        "yield", Map.of(
                                                "type", List.of("string", "null"),
                                                "description", "Optional yield/servings string (e.g. '4 servings', 'Makes 1 loaf').",
                                                "pattern", ".*\\S.*"
                                        ),
                                        "nutrition", Map.of(
                                                "type", List.of("object", "null"),
                                                "description", "Optional nutrition information if explicitly present in the input.",
                                                "additionalProperties", false,
                                                "required", List.of("calories"),
                                                "properties", Map.of(
                                                        "calories", Map.of(
                                                                "type", List.of("string", "null"),
                                                                "description", "Calories string as shown or derived only if explicitly present (e.g. '120 calories').",
                                                                "pattern", ".*\\S.*"
                                                        )
                                                )
                                        ),
                                        "ingredients", Map.of(
                                                "type", "array",
                                                "description", "Flat list of ingredient lines, in original order.",
                                                "items", Map.of(
                                                        "type", "string",
                                                        "pattern", ".*\\S.*"
                                                ),
                                                "minItems", 1,
                                                "maxItems", 200
                                        ),
                                        "instructions", Map.of(
                                                "type", "array",
                                                "description", "Instructions split into mandatory sections. If the recipe has no obvious sectioning, return exactly one section and omit its name.",
                                                "minItems", 1,
                                                "maxItems", 50,
                                                "items", Map.of(
                                                        "type", "object",
                                                        "additionalProperties", false,
                                                        "required", List.of("name", "steps"),
                                                        "properties", Map.of(
                                                                "name", Map.of(
                                                                        "type", List.of("string", "null"),
                                                                        "description", "Optional section title (e.g. 'Make the sauce'). Omit if there is only one unnamed section.",
                                                                        "pattern", ".*\\S.*"
                                                                ),
                                                                "steps", Map.of(
                                                                        "type", "array",
                                                                        "minItems", 1,
                                                                        "maxItems", 200,
                                                                        "items", Map.of(
                                                                                "type", "object",
                                                                                "additionalProperties", false,
                                                                                "required", List.of("text", "image"),
                                                                                "properties", Map.of(
                                                                                        "text", Map.of(
                                                                                                "type", "string",
                                                                                                "description", "Step text, in order.",
                                                                                                "pattern", ".*\\S.*"
                                                                                        ),
                                                                                        "image", Map.of(
                                                                                                "type", List.of("string", "null"),
                                                                                                "description", "Optional step image URL, only if explicitly present in the input.",
                                                                                                "pattern", "^https?://\\S+$"
                                                                                        )
                                                                                )
                                                                        )
                                                                )
                                                        )
                                                )
                                )
                        )
                )
        );
    }

    public static class OpenAiException extends RuntimeException {
        public OpenAiException(String message) {
            super(message);
        }

        public OpenAiException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
