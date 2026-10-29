package com.rentguard.risk.dto;

public class PriceAnomalyResult {
    private boolean anomalyDetected;
    private Double listingRent;
    private Double medianRent;
    private Double deviationPercentage;
    private int comparableCount;
    private String explanation;

    public PriceAnomalyResult() {}

    public PriceAnomalyResult(boolean anomalyDetected, Double listingRent, Double medianRent,
                              Double deviationPercentage, int comparableCount, String explanation) {
        this.anomalyDetected = anomalyDetected;
        this.listingRent = listingRent;
        this.medianRent = medianRent;
        this.deviationPercentage = deviationPercentage;
        this.comparableCount = comparableCount;
        this.explanation = explanation;
    }

    public boolean isAnomalyDetected() {
        return anomalyDetected;
    }

    public void setAnomalyDetected(boolean anomalyDetected) {
        this.anomalyDetected = anomalyDetected;
    }

    public Double getListingRent() {
        return listingRent;
    }

    public void setListingRent(Double listingRent) {
        this.listingRent = listingRent;
    }

    public Double getMedianRent() {
        return medianRent;
    }

    public void setMedianRent(Double medianRent) {
        this.medianRent = medianRent;
    }

    public Double getDeviationPercentage() {
        return deviationPercentage;
    }

    public void setDeviationPercentage(Double deviationPercentage) {
        this.deviationPercentage = deviationPercentage;
    }

    public int getComparableCount() {
        return comparableCount;
    }

    public void setComparableCount(int comparableCount) {
        this.comparableCount = comparableCount;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }
}
