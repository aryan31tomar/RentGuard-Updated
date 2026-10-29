package com.rentguard.visit.dto;

import com.rentguard.visit.model.VisitStatus;
import jakarta.validation.constraints.NotNull;

public class VisitStatusUpdateDto {

    @NotNull(message = "Status is required (ACCEPTED, DECLINED, COMPLETED, CANCELLED)")
    private VisitStatus status;

    private String responseNotes;

    public VisitStatusUpdateDto() {}

    public VisitStatusUpdateDto(VisitStatus status, String responseNotes) {
        this.status = status;
        this.responseNotes = responseNotes;
    }

    public VisitStatus getStatus() {
        return status;
    }

    public void setStatus(VisitStatus status) {
        this.status = status;
    }

    public String getResponseNotes() {
        return responseNotes;
    }

    public void setResponseNotes(String responseNotes) {
        this.responseNotes = responseNotes;
    }
}
