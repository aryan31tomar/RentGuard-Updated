package com.rentguard.risk.dto;

public class RiskEventDto {
    private String ruleCode;
    private Integer points;
    private String explanation;

    public RiskEventDto() {}

    public RiskEventDto(String ruleCode, Integer points, String explanation) {
        this.ruleCode = ruleCode;
        this.points = points;
        this.explanation = explanation;
    }

    public String getRuleCode() {
        return ruleCode;
    }

    public void setRuleCode(String ruleCode) {
        this.ruleCode = ruleCode;
    }

    public Integer getPoints() {
        return points;
    }

    public void setPoints(Integer points) {
        this.points = points;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }
}
