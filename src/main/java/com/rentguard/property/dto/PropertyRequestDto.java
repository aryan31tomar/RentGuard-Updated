package com.rentguard.property.dto;

import com.rentguard.property.model.FurnishingStatus;
import com.rentguard.property.model.PropertyType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class PropertyRequestDto {

    @NotBlank(message = "Address is required")
    private String address;

    @NotBlank(message = "Locality is required")
    private String locality;

    @NotBlank(message = "City is required")
    private String city;

    @NotNull(message = "Latitude is required")
    private Double latitude;

    @NotNull(message = "Longitude is required")
    private Double longitude;

    @NotNull(message = "Property type is required")
    private PropertyType type;

    @NotNull(message = "Number of bedrooms is required")
    @Positive(message = "Bedrooms must be positive")
    private Integer bedrooms;

    @NotNull(message = "Area (sq ft) is required")
    @Positive(message = "Area must be positive")
    private Double area;

    private FurnishingStatus furnishing = FurnishingStatus.SEMI_FURNISHED;

    public PropertyRequestDto() {}

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
}
