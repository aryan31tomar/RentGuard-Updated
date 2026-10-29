package com.rentguard.listing.repository;

import com.rentguard.listing.model.PropertyImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PropertyImageRepository extends JpaRepository<PropertyImage, Long> {
    List<PropertyImage> findByListingId(Long listingId);

    @Query("SELECT pi FROM PropertyImage pi WHERE pi.imageHash = :imageHash AND pi.listing.id <> :excludeListingId AND pi.listing.status <> 'SUSPENDED'")
    List<PropertyImage> findDuplicateImages(
            @Param("imageHash") String imageHash,
            @Param("excludeListingId") Long excludeListingId
    );

    @Query("SELECT pi FROM PropertyImage pi WHERE pi.imageHash = :imageHash")
    List<PropertyImage> findByImageHash(@Param("imageHash") String imageHash);
}
