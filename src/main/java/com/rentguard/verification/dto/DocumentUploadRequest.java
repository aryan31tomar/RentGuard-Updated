package com.rentguard.verification.dto;

import com.rentguard.verification.model.DocumentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class DocumentUploadRequest {

    private Long propertyId; // Optional if submitting owner identity doc

    @NotNull(message = "Document type is required")
    private DocumentType type;

    @NotBlank(message = "File path or identifier is required")
    private String filePath;

    public DocumentUploadRequest() {}

    public DocumentUploadRequest(Long propertyId, DocumentType type, String filePath) {
        this.propertyId = propertyId;
        this.type = type;
        this.filePath = filePath;
    }

    public Long getPropertyId() {
        return propertyId;
    }

    public void setPropertyId(Long propertyId) {
        this.propertyId = propertyId;
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
}
