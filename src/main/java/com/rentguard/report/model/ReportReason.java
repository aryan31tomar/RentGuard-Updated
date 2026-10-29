package com.rentguard.report.model;

public enum ReportReason {
    PRICE_SCAM("Price too low / Advance fee requested"),
    DUPLICATE_LISTING("Duplicate listing of another property"),
    FAKE_PHOTOS("Fake or stolen property photos"),
    UNAVAILABLE_PROPERTY("Property does not exist or unavailable"),
    ADVANCE_FEE_FRAUD("Owner demanding advance deposit before viewing"),
    SUSPICIOUS_IDENTITY("Owner identity appears fake or unverified"),
    OTHER("Other scam or safety concern");

    private final String description;

    ReportReason(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
