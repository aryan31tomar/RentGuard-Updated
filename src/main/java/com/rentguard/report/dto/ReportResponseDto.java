package com.rentguard.report.dto;

import com.rentguard.report.model.Report;
import com.rentguard.report.model.ReportReason;
import com.rentguard.report.model.ReportStatus;

import java.time.LocalDateTime;

public class ReportResponseDto {
    private Long id;
    private Long listingId;
    private String listingAddress;
    private Long reporterId;
    private String reporterName;
    private String reporterEmail;
    private ReportReason reason;
    private String reasonDescription;
    private String description;
    private ReportStatus status;
    private String adminNotes;
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;

    public ReportResponseDto() {}

    public static ReportResponseDto fromEntity(Report r) {
        ReportResponseDto dto = new ReportResponseDto();
        dto.setId(r.getId());
        if (r.getListing() != null) {
            dto.setListingId(r.getListing().getId());
            if (r.getListing().getProperty() != null) {
                dto.setListingAddress(r.getListing().getProperty().getAddress());
            }
        }
        if (r.getReporter() != null) {
            dto.setReporterId(r.getReporter().getId());
            dto.setReporterName(r.getReporter().getName());
            dto.setReporterEmail(r.getReporter().getEmail());
        }
        dto.setReason(r.getReason());
        dto.setReasonDescription(r.getReason() != null ? r.getReason().getDescription() : "");
        dto.setDescription(r.getDescription());
        dto.setStatus(r.getStatus());
        dto.setAdminNotes(r.getAdminNotes());
        dto.setCreatedAt(r.getCreatedAt());
        dto.setResolvedAt(r.getResolvedAt());
        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getListingId() {
        return listingId;
    }

    public void setListingId(Long listingId) {
        this.listingId = listingId;
    }

    public String getListingAddress() {
        return listingAddress;
    }

    public void setListingAddress(String listingAddress) {
        this.listingAddress = listingAddress;
    }

    public Long getReporterId() {
        return reporterId;
    }

    public void setReporterId(Long reporterId) {
        this.reporterId = reporterId;
    }

    public String getReporterName() {
        return reporterName;
    }

    public void setReporterName(String reporterName) {
        this.reporterName = reporterName;
    }

    public String getReporterEmail() {
        return reporterEmail;
    }

    public void setReporterEmail(String reporterEmail) {
        this.reporterEmail = reporterEmail;
    }

    public ReportReason getReason() {
        return reason;
    }

    public void setReason(ReportReason reason) {
        this.reason = reason;
    }

    public String getReasonDescription() {
        return reasonDescription;
    }

    public void setReasonDescription(String reasonDescription) {
        this.reasonDescription = reasonDescription;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(LocalDateTime resolvedAt) {
        this.resolvedAt = resolvedAt;
    }
}
