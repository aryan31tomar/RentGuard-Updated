package com.rentguard.property.dto;

import com.rentguard.property.model.FurnishingStatus;
import com.rentguard.property.model.Property;
import com.rentguard.property.model.PropertyType;
import com.rentguard.user.model.VerificationStatus;

import java.time.LocalDateTime;

public class PropertyResponseDto {
    private Long id;
    private Long ownerId;
    private String ownerName;
    private String address;
    private String locality;
    private String city;
    private Double latitude;
    private Double longitude;
    private PropertyType type;
    private Integer bedrooms;
    private Double area;
    private FurnishingStatus furnishing;
    private VerificationStatus verificationStatus;
    private LocalDateTime createdAt;

    public PropertyResponseDto() {}

    public static PropertyResponseDto fromEntity(Property p) {
        PropertyResponseDto dto = new PropertyResponseDto();
        dto.setId(p.getId());
        if (p.getOwner() != null) {
            dto.setOwnerId(p.getOwner().getId());
            if (p.getOwner().getUser() != null) {
                dto.setOwnerName(p.getOwner().getUser().getName());
            }
        }
        dto.setAddress(p.getAddress());
        dto.setLocality(p.getLocality());
        dto.setCity(p.getCity());
        dto.setLatitude(p.getLatitude());
        dto.setLongitude(p.getLongitude());
        dto.setType(p.getType());
        dto.setBedrooms(p.getBedrooms());
        dto.setArea(p.getArea());
        dto.setFurnishing(p.getFurnishing());
        dto.setVerificationStatus(p.getVerificationStatus());
        dto.setCreatedAt(p.getCreatedAt());
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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getLocality() {
        return locality;
    }

    public void setLocality(String locality) {
        this.locality = locality;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public PropertyType getType() {
        return type;
    }

    public void setType(PropertyType type) {
        this.type = type;
    }

    public Integer getBedrooms() {
        return bedrooms;
    }

    public void setBedrooms(Integer bedrooms) {
        this.bedrooms = bedrooms;
    }

    public Double getArea() {
        return area;
    }

    public void setArea(Double area) {
        this.area = area;
    }

    public FurnishingStatus getFurnishing() {
        return furnishing;
    }

    public void setFurnishing(FurnishingStatus furnishing) {
        this.furnishing = furnishing;
    }

    public VerificationStatus getVerificationStatus() {
        return verificationStatus;
    }

    public void setVerificationStatus(VerificationStatus verificationStatus) {
        this.verificationStatus = verificationStatus;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
