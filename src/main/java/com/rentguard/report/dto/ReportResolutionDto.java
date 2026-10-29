package com.rentguard.report.dto;

import com.rentguard.report.model.ReportStatus;
import jakarta.validation.constraints.NotNull;

public class ReportResolutionDto {

    @NotNull(message = "Resolution status is required (INVESTIGATING, RESOLVED, DISMISSED)")
    private ReportStatus status;

    private String adminNotes;

    private Boolean suspendListing = false;

    public ReportResolutionDto() {}

    public ReportStatus getStatus() {
        return status;
    }

    public void setStatus(ReportStatus status) {
        this.status = status;
    }

    public String getAdminNotes() {
        return adminNotes;
    }

    public void setAdminNotes(String adminNotes) {
        this.adminNotes = adminNotes;
    }

    public Boolean getSuspendListing() {
        return suspendListing;
    }

    public void setSuspendListing(Boolean suspendListing) {
        this.suspendListing = suspendListing;
    }
}
