package app.cookcards.webapp.service;

import app.cookcards.webapp.dto.RecipeDTO;
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

    public RecipeDTO parseRecipeFromText(String rawText, UnitsMode unitsMode, TargetLanguage targetLanguage) {
        UnitsMode effectiveUnitsMode = unitsMode == null ? UnitsMode.ORIGINAL : unitsMode;
        TargetLanguage effectiveTargetLanguage = targetLanguage == null ? TargetLanguage.ORIGINAL : targetLanguage;

        Map<String, Object> requestBody = buildResponsesRequest(rawText, effectiveUnitsMode, effectiveTargetLanguage);

        String responseJson = openAi.post()
                .uri("/responses")
                .body(requestBody)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, res) -> {
                    // best-effort error body read
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

        return extractRecipeDto(responseJson);
    }

    /**
     * Build Responses API request with Structured Outputs (strict json_schema).
     * Docs: Responses API + text.format json_schema. :contentReference[oaicite:2]{index=2}
     */
    private Map<String, Object> buildResponsesRequest(String rawText, UnitsMode unitsMode, TargetLanguage targetLanguage) {
        String unitsInstruction = switch (unitsMode) {
            case METRIC -> "Convert all measurable ingredient quantities and cooking temperatures to metric units.";
            case IMPERIAL -> "Convert all measurable ingredient quantities and cooking temperatures to imperial units.";
            case ORIGINAL -> "Keep all units exactly as in source text. Do not convert units.";
        };
        String languageInstruction = switch (targetLanguage) {
            case ENGLISH -> "Translate title, ingredients, and steps to English.";
            case GERMAN -> "Translate title, ingredients, and steps to German.";
            case RUSSIAN -> "Translate title, ingredients, and steps to Russian.";
            case ORIGINAL -> "Keep text in original source language. Do not translate.";
        };

        String system = """
                You are a careful recipe parser.
                The recipe can be in ANY language (e.g., Russian, Ukrainian, English).
                %s
                %s
                Return ONLY JSON matching the schema. No markdown. No commentary.
                """.formatted(unitsInstruction, languageInstruction);

        // User instructions: just provide raw text as-is
        String user = "RAW_TEXT:\n" + rawText;

        // Minimal JSON Schema for your RecipeDto (adjust if your DTO differs)
        Map<String, Object> schema = Map.of(
                "type", "object",
                "additionalProperties", false,
                "properties", Map.of(
                        "title", Map.of("type", "string"),
                        "language", Map.of("type", List.of("string", "null")),
                        "servingsAndYield", Map.of(
                                "type", "array",
                                "items", Map.of("type", "string")
                        ),
                        "times", Map.of(
                                "type", "array",
                                "items", Map.of("type", "string")
                        ),
                        "ingredients", Map.of(
                                "type", "array",
                                "items", Map.of(
                                        "type", "object",
                                        "additionalProperties", false,
                                        "properties", Map.of(
                                                "section", Map.of("type", List.of("string", "null")),
                                                "quantity", Map.of("type", List.of("string", "null")),
                                                "unit", Map.of("type", List.of("string", "null")),
                                                "item", Map.of("type", "string"),
                                                "preparation", Map.of("type", List.of("string", "null"))
                                        ),
                                        "required", List.of("section","quantity","unit","item","preparation")
                                )
                        ),
                        "steps", Map.of(
                                "type", "array",
                                "items", Map.of(
                                        "type", "object",
                                        "additionalProperties", false,
                                        "properties", Map.of(
                                                "number", Map.of("type", "integer", "minimum", 1),
                                                "text", Map.of("type", "string")
                                        ),
                                        "required", List.of("number", "text")
                                )
                        ),
                        "notes", Map.of(
                                "type", "array",
                                "items", Map.of("type", "string")
                        ),
                        "tags", Map.of(
                                "type", "array",
                                "items", Map.of("type", "string")
                        ),
                        "source", Map.of("type", List.of("string", "null"))
                ),
                "required", List.of(
                        "title",
                        "language",
                        "servingsAndYield",
                        "times",
                        "ingredients",
                        "steps",
                        "notes",
                        "tags",
                        "source"
                )
        );

        Map<String, Object> textFormat = Map.of(
                "type", "json_schema",
                "name", "recipe",
                "strict", true,
                "schema", schema
        );

        // Responses API body shape:
        // {
        //   "model": "...",
        //   "input": [ {role, content}, ... ],
        //   "text": { "format": { ...json_schema... } },
        //   "store": false
        // }
        return Map.of(
                "model", model,
                "store", false,
                "input", new Object[]{
                        Map.of("role", "system", "content", system),
                        Map.of("role", "user", "content", user)
                },
                "text", Map.of("format", textFormat)
        );
    }

    /**
     * Extract the assistant text output and parse it as RecipeDto.
     * Responses API returns output as an array; the message contains content items with type output_text.
     */
    private RecipeDTO extractRecipeDto(String responseJson) {
        try {
            JsonNode root = mapper.readTree(responseJson);

            // Find first output_text node anywhere in output -> content -> text
            JsonNode output = root.path("output");
            if (!output.isArray()) {
                throw new OpenAiException("Unexpected OpenAI response format: missing output array.");
            }

            String jsonText = null;

            for (JsonNode outItem : output) {
                if (!"message".equals(outItem.path("type").asText())) continue;

                JsonNode content = outItem.path("content");
                if (!content.isArray()) continue;

                for (JsonNode c : content) {
                    if ("output_text".equals(c.path("type").asText())) {
                        jsonText = c.path("text").asText(null);
                        break;
                    }
                }
                if (jsonText != null) break;
            }

            if (jsonText == null || jsonText.isBlank()) {
                throw new OpenAiException("OpenAI response did not contain output_text with JSON.");
            }

            return mapper.readValue(jsonText, RecipeDTO.class);

        } catch (JsonProcessingException e) {
            throw new OpenAiException("Failed to parse OpenAI response JSON into RecipeDto: " + e.getMessage(), e);
        }
    }

    public static class OpenAiException extends RuntimeException {
        public OpenAiException(String message) { super(message); }
        public OpenAiException(String message, Throwable cause) { super(message, cause); }
    }
}
