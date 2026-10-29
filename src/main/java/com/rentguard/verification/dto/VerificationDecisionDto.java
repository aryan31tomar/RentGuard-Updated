package com.rentguard.verification.dto;

import com.rentguard.verification.model.DocumentStatus;
import jakarta.validation.constraints.NotNull;

public class VerificationDecisionDto {

    @NotNull(message = "Decision status is required (APPROVED or REJECTED)")
    private DocumentStatus status;

    private String notes;

    public VerificationDecisionDto() {}

    public VerificationDecisionDto(DocumentStatus status, String notes) {
        this.status = status;
        this.notes = notes;
    }

    public DocumentStatus getStatus() {
        return status;
    }

    public void setStatus(DocumentStatus status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
