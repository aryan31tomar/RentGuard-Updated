package com.rentguard.verification.dto;

import com.rentguard.verification.model.Document;
import com.rentguard.verification.model.DocumentStatus;
import com.rentguard.verification.model.DocumentType;

import java.time.LocalDateTime;

public class DocumentResponseDto {
    private Long id;
    private Long ownerId;
    private String ownerName;
    private String ownerEmail;
    private Long propertyId;
    private String propertyAddress;
    private DocumentType type;
    private String filePath;
    private DocumentStatus status;
    private String reviewedBy;
    private String rejectionReason;
    private LocalDateTime createdAt;
    private LocalDateTime reviewedAt;

    public DocumentResponseDto() {}

    public static DocumentResponseDto fromEntity(Document doc) {
        DocumentResponseDto dto = new DocumentResponseDto();
        dto.setId(doc.getId());
        if (doc.getOwner() != null) {
            dto.setOwnerId(doc.getOwner().getId());
            if (doc.getOwner().getUser() != null) {
                dto.setOwnerName(doc.getOwner().getUser().getName());
                dto.setOwnerEmail(doc.getOwner().getUser().getEmail());
            }
        }
        if (doc.getProperty() != null) {
            dto.setPropertyId(doc.getProperty().getId());
            dto.setPropertyAddress(doc.getProperty().getAddress());
        }
        dto.setType(doc.getType());
        dto.setFilePath(doc.getFilePath());
        dto.setStatus(doc.getStatus());
        dto.setReviewedBy(doc.getReviewedBy());
        dto.setRejectionReason(doc.getRejectionReason());
        dto.setCreatedAt(doc.getCreatedAt());
        dto.setReviewedAt(doc.getReviewedAt());
        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getOwnerEmail() {
        return ownerEmail;
    }

    public void setOwnerEmail(String ownerEmail) {
        this.ownerEmail = ownerEmail;
    }

    public Long getPropertyId() {
        return propertyId;
    }

    public void setPropertyId(Long propertyId) {
        this.propertyId = propertyId;
    }

    public String getPropertyAddress() {
        return propertyAddress;
    }

    public void setPropertyAddress(String propertyAddress) {
        this.propertyAddress = propertyAddress;
    }

    public DocumentType getType() {
        return type;
    }

    public void setType(DocumentType type) {
        this.type = type;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public DocumentStatus getStatus() {
        return status;
    }

    public void setStatus(DocumentStatus status) {
        this.status = status;
    }

    public String getReviewedBy() {
        return reviewedBy;
    }

    public void setReviewedBy(String reviewedBy) {
        this.reviewedBy = reviewedBy;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getReviewedAt() {
        return reviewedAt;
    }

    public void setReviewedAt(LocalDateTime reviewedAt) {
        this.reviewedAt = reviewedAt;
    }
}
