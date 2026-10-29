package com.rentguard.listing.dto;

import com.rentguard.listing.model.PropertyImage;

public class PropertyImageDto {
    private Long id;
    private String filePath;
    private String imageHash;
    private Boolean isCover;

    public PropertyImageDto() {}

    public static PropertyImageDto fromEntity(PropertyImage img) {
        PropertyImageDto dto = new PropertyImageDto();
        dto.setId(img.getId());
        dto.setFilePath(img.getFilePath());
        dto.setImageHash(img.getImageHash());
        dto.setIsCover(img.getIsCover());
        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getImageHash() {
        return imageHash;
    }

    public void setImageHash(String imageHash) {
        this.imageHash = imageHash;
    }

    public Boolean getIsCover() {
        return isCover;
    }

    public void setIsCover(Boolean isCover) {
        this.isCover = isCover;
    }
}
