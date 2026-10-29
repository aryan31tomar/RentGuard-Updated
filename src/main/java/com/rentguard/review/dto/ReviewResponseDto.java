package com.rentguard.review.dto;

import com.rentguard.review.model.Review;

import java.time.LocalDateTime;

public class ReviewResponseDto {
    private Long id;
    private Long listingId;
    private Long tenantId;
    private String tenantName;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;

    public ReviewResponseDto() {}

    public static ReviewResponseDto fromEntity(Review r) {
        ReviewResponseDto dto = new ReviewResponseDto();
        dto.setId(r.getId());
        if (r.getListing() != null) {
            dto.setListingId(r.getListing().getId());
        }
        if (r.getTenant() != null) {
            dto.setTenantId(r.getTenant().getId());
            dto.setTenantName(r.getTenant().getName());
        }
        dto.setRating(r.getRating());
        dto.setComment(r.getComment());
        dto.setCreatedAt(r.getCreatedAt());
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

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
