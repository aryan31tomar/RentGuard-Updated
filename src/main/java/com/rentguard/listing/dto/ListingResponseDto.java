package com.rentguard.listing.dto;

import com.rentguard.listing.model.Listing;
import com.rentguard.listing.model.ListingStatus;
import com.rentguard.property.dto.PropertyResponseDto;
import com.rentguard.risk.dto.RiskEventDto;
import com.rentguard.risk.model.RiskLevel;
import com.rentguard.user.model.VerificationStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ListingResponseDto {
    private Long id;
    private PropertyResponseDto property;
    private Double rent;
    private Double deposit;
    private String description;
    private ListingStatus status;
    private Integer riskScore;
    private RiskLevel riskLevel;
    private String riskLabel;
    private String riskColorCode;
    private String suspensionReason;
    private List<PropertyImageDto> images = new ArrayList<>();
    private List<RiskEventDto> riskEvents = new ArrayList<>();
    private List<String> positiveSignals = new ArrayList<>();
    private Double averageRating;
    private long reviewCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ListingResponseDto() {}

    public static ListingResponseDto fromEntity(Listing l) {
        ListingResponseDto dto = new ListingResponseDto();
        dto.setId(l.getId());
        if (l.getProperty() != null) {
            dto.setProperty(PropertyResponseDto.fromEntity(l.getProperty()));
        }
        dto.setRent(l.getRent());
        dto.setDeposit(l.getDeposit());
        dto.setDescription(l.getDescription());
        dto.setStatus(l.getStatus());
        dto.setRiskScore(l.getRiskScore());
        RiskLevel level = RiskLevel.fromScore(l.getRiskScore());
        dto.setRiskLevel(level);
        dto.setRiskLabel(level.getLabel());
        dto.setRiskColorCode(level.getColorCode());
        dto.setSuspensionReason(l.getSuspensionReason());
        dto.setCreatedAt(l.getCreatedAt());
        dto.setUpdatedAt(l.getUpdatedAt());
        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public PropertyResponseDto getProperty() {
        return property;
    }

    public void setProperty(PropertyResponseDto property) {
        this.property = property;
    }

    public Double getRent() {
        return rent;
    }

    public void setRent(Double rent) {
        this.rent = rent;
    }

    public Double getDeposit() {
        return deposit;
    }

    public void setDeposit(Double deposit) {
        this.deposit = deposit;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ListingStatus getStatus() {
        return status;
    }

    public void setStatus(ListingStatus status) {
        this.status = status;
    }

    public Integer getRiskScore() {
        return riskScore;
    }

    public void setRiskScore(Integer riskScore) {
        this.riskScore = riskScore;
    }

    public RiskLevel getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(RiskLevel riskLevel) {
        this.riskLevel = riskLevel;
    }

    public String getRiskLabel() {
        return riskLabel;
    }

    public void setRiskLabel(String riskLabel) {
        this.riskLabel = riskLabel;
    }

    public String getRiskColorCode() {
        return riskColorCode;
    }

    public void setRiskColorCode(String riskColorCode) {
        this.riskColorCode = riskColorCode;
    }

    public String getSuspensionReason() {
        return suspensionReason;
    }

    public void setSuspensionReason(String suspensionReason) {
        this.suspensionReason = suspensionReason;
    }

    public List<PropertyImageDto> getImages() {
        return images;
    }

    public void setImages(List<PropertyImageDto> images) {
        this.images = images;
    }

    public List<RiskEventDto> getRiskEvents() {
        return riskEvents;
    }

    public void setRiskEvents(List<RiskEventDto> riskEvents) {
        this.riskEvents = riskEvents;
    }

    public List<String> getPositiveSignals() {
        return positiveSignals;
    }

    public void setPositiveSignals(List<String> positiveSignals) {
        this.positiveSignals = positiveSignals;
    }

    public Double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(Double averageRating) {
        this.averageRating = averageRating;
    }

    public long getReviewCount() {
        return reviewCount;
    }

    public void setReviewCount(long reviewCount) {
        this.reviewCount = reviewCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
