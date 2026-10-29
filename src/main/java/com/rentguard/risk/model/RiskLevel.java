package com.rentguard.risk.model;

public enum RiskLevel {
    LOW(0, 29, "Low Risk", "#10b981"),
    MEDIUM(30, 59, "Medium Risk", "#f59e0b"),
    HIGH(60, 100, "High Risk", "#ef4444");

    private final int minScore;
    private final int maxScore;
    private final String label;
    private final String colorCode;

    RiskLevel(int minScore, int maxScore, String label, String colorCode) {
        this.minScore = minScore;
        this.maxScore = maxScore;
        this.label = label;
        this.colorCode = colorCode;
    }

    public static RiskLevel fromScore(int score) {
        if (score <= 29) {
            return LOW;
        } else if (score <= 59) {
            return MEDIUM;
        } else {
            return HIGH;
        }
    }

    public int getMinScore() {
        return minScore;
    }

    public int getMaxScore() {
        return maxScore;
    }

    public String getLabel() {
        return label;
    }

    public String getColorCode() {
        return colorCode;
    }
}
