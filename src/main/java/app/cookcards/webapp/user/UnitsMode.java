package app.cookcards.webapp.user;

public enum UnitsMode {
    METRIC("Metric"),
    IMPERIAL("Imperial"),
    ORIGINAL("Original");

    private final String label;

    UnitsMode(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
