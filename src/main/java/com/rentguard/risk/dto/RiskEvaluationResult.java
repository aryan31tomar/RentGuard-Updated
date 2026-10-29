package com.rentguard.risk.dto;

import com.rentguard.risk.model.RiskLevel;

import java.util.ArrayList;
import java.util.List;

public class RiskEvaluationResult {
    private Long listingId;
    private int score;
    private RiskLevel riskLevel;
    private String riskLabel;
    private String colorCode;
    private List<RiskEventDto> events = new ArrayList<>();
    private List<String> positiveSignals = new ArrayList<>();

    public RiskEvaluationResult() {}

    public RiskEvaluationResult(Long listingId, int score, RiskLevel riskLevel) {
        this.listingId = listingId;
        this.score = score;
        this.riskLevel = riskLevel;
        this.riskLabel = riskLevel.getLabel();
        this.colorCode = riskLevel.getColorCode();
    }

    public Long getListingId() {
        return listingId;
    }

    public void setListingId(Long listingId) {
        this.listingId = listingId;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public RiskLevel getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(RiskLevel riskLevel) {
        this.riskLevel = riskLevel;
        if (riskLevel != null) {
            this.riskLabel = riskLevel.getLabel();
            this.colorCode = riskLevel.getColorCode();
        }
    }

    public String getRiskLabel() {
        return riskLabel;
    }

    public void setRiskLabel(String riskLabel) {
        this.riskLabel = riskLabel;
    }

    public String getColorCode() {
        return colorCode;
    }

    public void setColorCode(String colorCode) {
        this.colorCode = colorCode;
    }

    public List<RiskEventDto> getEvents() {
        return events;
    }

    public void setEvents(List<RiskEventDto> events) {
        this.events = events;
    }

    public List<String> getPositiveSignals() {
        return positiveSignals;
    }

    public void setPositiveSignals(List<String> positiveSignals) {
        this.positiveSignals = positiveSignals;
    }
}
