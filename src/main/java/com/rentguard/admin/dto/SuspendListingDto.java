package com.rentguard.admin.dto;

import jakarta.validation.constraints.NotBlank;

public class SuspendListingDto {

    @NotBlank(message = "Suspension reason is required")
    private String reason;

    public SuspendListingDto() {}

    public SuspendListingDto(String reason) {
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
