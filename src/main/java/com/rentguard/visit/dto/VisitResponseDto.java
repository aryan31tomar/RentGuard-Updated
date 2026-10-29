package com.rentguard.visit.dto;

import com.rentguard.visit.model.VisitRequest;
import com.rentguard.visit.model.VisitStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class VisitResponseDto {
    private Long id;
    private Long listingId;
    private String propertyAddress;
    private Double rent;
    private Long tenantId;
    private String tenantName;
    private String tenantEmail;
    private String tenantPhone;
    private LocalDate requestedDate;
    private VisitStatus status;
    private String notes;
    private String ownerResponse;
    private LocalDateTime createdAt;

    public VisitResponseDto() {}

    public static VisitResponseDto fromEntity(VisitRequest v) {
        VisitResponseDto dto = new VisitResponseDto();
        dto.setId(v.getId());
        if (v.getListing() != null) {
            dto.setListingId(v.getListing().getId());
            dto.setRent(v.getListing().getRent());
            if (v.getListing().getProperty() != null) {
                dto.setPropertyAddress(v.getListing().getProperty().getAddress());
            }
        }
        if (v.getTenant() != null) {
            dto.setTenantId(v.getTenant().getId());
            dto.setTenantName(v.getTenant().getName());
            dto.setTenantEmail(v.getTenant().getEmail());
            dto.setTenantPhone(v.getTenant().getPhone());
        }
        dto.setRequestedDate(v.getRequestedDate());
        dto.setStatus(v.getStatus());
        dto.setNotes(v.getNotes());
        dto.setOwnerResponse(v.getOwnerResponse());
        dto.setCreatedAt(v.getCreatedAt());
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

    public String getPropertyAddress() {
        return propertyAddress;
    }

    public void setPropertyAddress(String propertyAddress) {
        this.propertyAddress = propertyAddress;
    }

    public Double getRent() {
        return rent;
    }

    public void setRent(Double rent) {
        this.rent = rent;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    public String getTenantName() {
        return tenantName;
    }

    public void setTenantName(String tenantName) {
        this.tenantName = tenantName;
    }

    public String getTenantEmail() {
        return tenantEmail;
    }

    public void setTenantEmail(String tenantEmail) {
        this.tenantEmail = tenantEmail;
    }

    public String getTenantPhone() {
        return tenantPhone;
    }

    public void setTenantPhone(String tenantPhone) {
        this.tenantPhone = tenantPhone;
    }

    public LocalDate getRequestedDate() {
        return requestedDate;
    }

    public void setRequestedDate(LocalDate requestedDate) {
        this.requestedDate = requestedDate;
    }

    public VisitStatus getStatus() {
        return status;
    }

    public void setStatus(VisitStatus status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getOwnerResponse() {
        return ownerResponse;
    }

    public void setOwnerResponse(String ownerResponse) {
        this.ownerResponse = ownerResponse;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
