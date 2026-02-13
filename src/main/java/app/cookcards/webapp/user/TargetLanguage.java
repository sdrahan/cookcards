package app.cookcards.webapp.user;

public enum TargetLanguage {
    ENGLISH("English"),
    GERMAN("German"),
    RUSSIAN("Russian"),
    ORIGINAL("Original");

    private final String label;

    TargetLanguage(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
