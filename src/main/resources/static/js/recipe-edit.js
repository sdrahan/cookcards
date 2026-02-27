(() => {
    const recipeEditForm = document.getElementById("recipe-edit-form");
    if (!recipeEditForm) {
        return;
    }

    const ingredientsList = document.getElementById("ingredients-list");
    const instructionsList = document.getElementById("instructions-list");
    const ingredientTemplate = document.getElementById("ingredient-row-template");
    const sectionTemplate = document.getElementById("instruction-section-template");
    const stepTemplate = document.getElementById("instruction-step-template");
    const detailsToggle = document.getElementById("details-toggle");
    const detailsPanel = document.getElementById("details-panel");
    const detailsToggleIcon = document.getElementById("details-toggle-icon");
    const editPanel = document.getElementById("edit-panel");
    const previewPanel = document.getElementById("preview-panel");
    const modeEditButton = document.getElementById("mode-edit-button");
    const modePreviewButton = document.getElementById("mode-preview-button");

    let hasUnsavedChanges = false;
    let ignoreBeforeUnload = false;

    function markDirty() {
        hasUnsavedChanges = true;
    }

    function toArray(nodeList) {
        return Array.from(nodeList || []);
    }

    function reindexIngredients() {
        const rows = toArray(ingredientsList.querySelectorAll(".ingredient-row"));
        rows.forEach((row, index) => {
            const input = row.querySelector(".ingredient-input");
            if (input) {
                input.name = `ingredients[${index}]`;
            }
        });
    }

    function createIngredientRow(value = "") {
        const fragment = ingredientTemplate.content.cloneNode(true);
        const row = fragment.querySelector(".ingredient-row");
        const input = row.querySelector(".ingredient-input");
        input.value = value;
        return row;
    }

    function ensureAtLeastOneIngredientRow() {
        if (ingredientsList.querySelectorAll(".ingredient-row").length === 0) {
            ingredientsList.appendChild(createIngredientRow(""));
        }
        reindexIngredients();
    }

    function createStepRow(value = "") {
        const fragment = stepTemplate.content.cloneNode(true);
        const row = fragment.querySelector(".step-row");
        const input = row.querySelector(".step-text-input");
        input.value = value;
        return row;
    }

    function createSection(sectionName = "", steps = [""]) {
        const fragment = sectionTemplate.content.cloneNode(true);
        const section = fragment.querySelector(".instruction-section");
        const sectionNameInput = section.querySelector(".section-name-input");
        const stepsList = section.querySelector(".steps-list");
        sectionNameInput.value = sectionName;

        if (!steps.length) {
            stepsList.appendChild(createStepRow(""));
        } else {
            steps.forEach((stepValue) => stepsList.appendChild(createStepRow(stepValue)));
        }

        return section;
    }

    function reindexInstructions() {
        const sections = toArray(instructionsList.querySelectorAll(".instruction-section"));
        sections.forEach((section, sectionIndex) => {
            const sectionInput = section.querySelector(".section-name-input");
            if (sectionInput) {
                sectionInput.name = `instructions[${sectionIndex}].name`;
            }
            const steps = toArray(section.querySelectorAll(".step-row"));
            steps.forEach((step, stepIndex) => {
                const stepInput = step.querySelector(".step-text-input");
                if (stepInput) {
                    stepInput.name = `instructions[${sectionIndex}].steps[${stepIndex}].text`;
                }
            });
        });
    }

    function ensureAtLeastOneSectionAndStep() {
        if (instructionsList.querySelectorAll(".instruction-section").length === 0) {
            instructionsList.appendChild(createSection("", [""]));
        }

        toArray(instructionsList.querySelectorAll(".instruction-section")).forEach((section) => {
            const stepsList = section.querySelector(".steps-list");
            if (stepsList && stepsList.querySelectorAll(".step-row").length === 0) {
                stepsList.appendChild(createStepRow(""));
            }
        });

        reindexInstructions();
    }

    function setEditMode() {
        editPanel.classList.remove("hidden");
        previewPanel.classList.add("hidden");
        modeEditButton.classList.add("bg-slate-900", "text-white");
        modeEditButton.classList.remove("text-slate-700", "hover:bg-slate-100");
        modePreviewButton.classList.remove("bg-slate-900", "text-white");
        modePreviewButton.classList.add("text-slate-700", "hover:bg-slate-100");
    }

    function updatePreview() {
        const previewName = document.getElementById("preview-name");
        const previewDescription = document.getElementById("preview-description");
        const previewDetails = document.getElementById("preview-details");
        const previewDetailsBlock = document.getElementById("preview-details-block");
        const previewIngredients = document.getElementById("preview-ingredients");
        const previewInstructions = document.getElementById("preview-instructions");

        const nameValue = (recipeEditForm.querySelector("[name='name']")?.value || "").trim();
        const descriptionValue = (recipeEditForm.querySelector("[name='description']")?.value || "").trim();
        const recipeYieldValue = (recipeEditForm.querySelector("[name='recipeYield']")?.value || "").trim();
        const prepTimeValue = (recipeEditForm.querySelector("[name='prepTime']")?.value || "").trim();
        const cookTimeValue = (recipeEditForm.querySelector("[name='cookTime']")?.value || "").trim();
        const totalTimeValue = (recipeEditForm.querySelector("[name='totalTime']")?.value || "").trim();
        const caloriesValue = (recipeEditForm.querySelector("[name='calories']")?.value || "").trim();

        previewName.textContent = nameValue || "Untitled recipe";
        previewDescription.textContent = descriptionValue;
        previewDescription.classList.toggle("hidden", !descriptionValue);

        previewDetails.innerHTML = "";
        const detailsRows = [];
        if (recipeYieldValue) detailsRows.push(`Yield: ${recipeYieldValue}`);
        if (prepTimeValue) detailsRows.push(`Prep time: ${prepTimeValue}`);
        if (cookTimeValue) detailsRows.push(`Cook time: ${cookTimeValue}`);
        if (totalTimeValue) detailsRows.push(`Total time: ${totalTimeValue}`);
        if (caloriesValue) detailsRows.push(`Calories: ${caloriesValue}`);
        detailsRows.forEach((rowText) => {
            const li = document.createElement("li");
            li.textContent = rowText;
            previewDetails.appendChild(li);
        });
        previewDetailsBlock.classList.toggle("hidden", detailsRows.length === 0);

        previewIngredients.innerHTML = "";
        toArray(ingredientsList.querySelectorAll(".ingredient-input")).forEach((input) => {
            const value = input.value.trim();
            if (!value) {
                return;
            }
            const li = document.createElement("li");
            li.textContent = value;
            previewIngredients.appendChild(li);
        });

        previewInstructions.innerHTML = "";
        toArray(instructionsList.querySelectorAll(".instruction-section")).forEach((section) => {
            const sectionName = (section.querySelector(".section-name-input")?.value || "").trim();
            const stepValues = toArray(section.querySelectorAll(".step-text-input"))
                .map((stepInput) => stepInput.value.trim())
                .filter((value) => value.length > 0);
            if (!sectionName && stepValues.length === 0) {
                return;
            }

            const sectionWrap = document.createElement("div");
            sectionWrap.className = "space-y-2";
            if (sectionName) {
                const title = document.createElement("h4");
                title.className = "font-semibold font-lato text-slate-700";
                title.textContent = sectionName;
                sectionWrap.appendChild(title);
            }

            if (stepValues.length > 0) {
                const list = document.createElement("ol");
                list.className = "list-decimal pl-6 space-y-1 font-lato text-slate-700";
                stepValues.forEach((stepValue) => {
                    const li = document.createElement("li");
                    li.textContent = stepValue;
                    list.appendChild(li);
                });
                sectionWrap.appendChild(list);
            }
            previewInstructions.appendChild(sectionWrap);
        });
    }

    function setPreviewMode() {
        updatePreview();
        editPanel.classList.add("hidden");
        previewPanel.classList.remove("hidden");
        modePreviewButton.classList.add("bg-slate-900", "text-white");
        modePreviewButton.classList.remove("text-slate-700", "hover:bg-slate-100");
        modeEditButton.classList.remove("bg-slate-900", "text-white");
        modeEditButton.classList.add("text-slate-700", "hover:bg-slate-100");
    }

    function detailsHaveValues() {
        const names = ["recipeYield", "prepTime", "cookTime", "totalTime", "calories"];
        return names.some((name) => {
            const input = recipeEditForm.querySelector(`[name='${name}']`);
            return input && input.value.trim().length > 0;
        });
    }

    function setDetailsExpanded(expanded) {
        detailsPanel.classList.toggle("hidden", !expanded);
        detailsToggleIcon.textContent = expanded ? "Hide" : "Show";
    }

    document.getElementById("add-ingredient-button")?.addEventListener("click", () => {
        ingredientsList.appendChild(createIngredientRow(""));
        reindexIngredients();
        markDirty();
    });

    ingredientsList.addEventListener("click", (event) => {
        const button = event.target.closest("button[data-action]");
        if (!button) {
            return;
        }
        const row = button.closest(".ingredient-row");
        if (!row) {
            return;
        }
        const action = button.dataset.action;
        if (action === "ingredient-remove") {
            row.remove();
            ensureAtLeastOneIngredientRow();
            markDirty();
            return;
        }
        if (action === "ingredient-up" && row.previousElementSibling) {
            ingredientsList.insertBefore(row, row.previousElementSibling);
            reindexIngredients();
            markDirty();
            return;
        }
        if (action === "ingredient-down" && row.nextElementSibling) {
            ingredientsList.insertBefore(row.nextElementSibling, row);
            reindexIngredients();
            markDirty();
        }
    });

    document.getElementById("add-section-button")?.addEventListener("click", () => {
        instructionsList.appendChild(createSection("", [""]));
        reindexInstructions();
        markDirty();
    });

    instructionsList.addEventListener("click", (event) => {
        const button = event.target.closest("button[data-action]");
        if (!button) {
            return;
        }
        const action = button.dataset.action;
        const section = button.closest(".instruction-section");
        if (!section) {
            return;
        }

        if (action === "section-remove") {
            section.remove();
            ensureAtLeastOneSectionAndStep();
            markDirty();
            return;
        }
        if (action === "section-up" && section.previousElementSibling) {
            instructionsList.insertBefore(section, section.previousElementSibling);
            reindexInstructions();
            markDirty();
            return;
        }
        if (action === "section-down" && section.nextElementSibling) {
            instructionsList.insertBefore(section.nextElementSibling, section);
            reindexInstructions();
            markDirty();
            return;
        }

        const stepsList = section.querySelector(".steps-list");
        if (!stepsList) {
            return;
        }

        if (action === "add-step") {
            stepsList.appendChild(createStepRow(""));
            reindexInstructions();
            markDirty();
            return;
        }

        const stepRow = button.closest(".step-row");
        if (!stepRow) {
            return;
        }
        if (action === "step-remove") {
            stepRow.remove();
            if (stepsList.querySelectorAll(".step-row").length === 0) {
                stepsList.appendChild(createStepRow(""));
            }
            reindexInstructions();
            markDirty();
            return;
        }
        if (action === "step-up" && stepRow.previousElementSibling) {
            stepsList.insertBefore(stepRow, stepRow.previousElementSibling);
            reindexInstructions();
            markDirty();
            return;
        }
        if (action === "step-down" && stepRow.nextElementSibling) {
            stepsList.insertBefore(stepRow.nextElementSibling, stepRow);
            reindexInstructions();
            markDirty();
        }
    });

    recipeEditForm.addEventListener("input", () => {
        markDirty();
    });

    recipeEditForm.addEventListener("submit", () => {
        ignoreBeforeUnload = true;
    });

    document.getElementById("cancel-form")?.addEventListener("submit", () => {
        ignoreBeforeUnload = true;
    });

    document.querySelectorAll("[data-ignore-unsaved='true']").forEach((element) => {
        element.addEventListener("click", () => {
            ignoreBeforeUnload = true;
        });
    });

    window.addEventListener("beforeunload", (event) => {
        if (!hasUnsavedChanges || ignoreBeforeUnload) {
            return;
        }
        event.preventDefault();
        event.returnValue = "";
    });

    detailsToggle?.addEventListener("click", () => {
        const expanded = detailsPanel.classList.contains("hidden");
        setDetailsExpanded(expanded);
    });

    modeEditButton?.addEventListener("click", () => setEditMode());
    modePreviewButton?.addEventListener("click", () => setPreviewMode());

    ensureAtLeastOneIngredientRow();
    ensureAtLeastOneSectionAndStep();
    setDetailsExpanded(detailsHaveValues());
    setEditMode();
})();
