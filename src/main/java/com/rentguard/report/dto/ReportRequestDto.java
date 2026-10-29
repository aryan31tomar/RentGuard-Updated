package com.rentguard.report.dto;

import com.rentguard.report.model.ReportReason;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ReportRequestDto {

    @NotNull(message = "Reason is required")
    private ReportReason reason;

    @NotBlank(message = "Description of the scam or issue is required")
    private String description;

    public ReportRequestDto() {}

    public ReportRequestDto(ReportReason reason, String description) {
        this.reason = reason;
        this.description = description;
    }

    public ReportReason getReason() {
        return reason;
    }

    public void setReason(ReportReason reason) {
        this.reason = reason;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
