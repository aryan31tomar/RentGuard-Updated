package com.rentguard.visit.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public class VisitRequestDto {

    @NotNull(message = "Requested visit date is required")
    @FutureOrPresent(message = "Requested date must be today or in the future")
    private LocalDate requestedDate;

    private String notes;

    public VisitRequestDto() {}

    public VisitRequestDto(LocalDate requestedDate, String notes) {
        this.requestedDate = requestedDate;
        this.notes = notes;
    }

    public LocalDate getRequestedDate() {
        return requestedDate;
    }

    public void setRequestedDate(LocalDate requestedDate) {
        this.requestedDate = requestedDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
