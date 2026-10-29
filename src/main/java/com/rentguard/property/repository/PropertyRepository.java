package com.rentguard.property.repository;

import com.rentguard.property.model.Property;
import com.rentguard.property.model.PropertyType;
import com.rentguard.user.model.Owner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PropertyRepository extends JpaRepository<Property, Long> {
    List<Property> findByOwner(Owner owner);
    List<Property> findByOwnerId(Long ownerId);

    @Query("SELECT p FROM Property p WHERE LOWER(p.city) = LOWER(:city) AND LOWER(p.locality) = LOWER(:locality) AND p.type = :type AND p.bedrooms = :bedrooms")
    List<Property> findComparableProperties(
            @Param("city") String city,
            @Param("locality") String locality,
            @Param("type") PropertyType type,
            @Param("bedrooms") Integer bedrooms
    );

    @Query("SELECT p FROM Property p WHERE p.id <> :excludeId AND " +
           "(6371 * acos(cos(radians(:lat)) * cos(radians(p.latitude)) * cos(radians(p.longitude) - radians(:lng)) + sin(radians(:lat)) * sin(radians(p.latitude)))) < :distanceKm")
    List<Property> findNearbyProperties(
            @Param("lat") Double lat,
            @Param("lng") Double lng,
            @Param("distanceKm") Double distanceKm,
            @Param("excludeId") Long excludeId
    );
}
