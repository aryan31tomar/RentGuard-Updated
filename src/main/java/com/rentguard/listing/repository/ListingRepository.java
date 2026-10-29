package com.rentguard.listing.repository;

import com.rentguard.listing.model.Listing;
import com.rentguard.listing.model.ListingStatus;
import com.rentguard.property.model.Property;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ListingRepository extends JpaRepository<Listing, Long> {
    List<Listing> findByStatus(ListingStatus status);
    List<Listing> findByProperty_Owner_User_Id(Long userId);
    List<Listing> findByProperty_Owner_Id(Long ownerId);
    List<Listing> findByProperty(Property property);

    long countByStatus(ListingStatus status);

    @Query("SELECT COUNT(l) FROM Listing l WHERE l.riskScore >= 60 AND l.status = 'ACTIVE'")
    long countHighRiskActiveListings();

    @Query("SELECT l FROM Listing l WHERE l.riskScore >= 60 ORDER BY l.riskScore DESC")
    List<Listing> findHighRiskListings();

    @Query("SELECT l FROM Listing l WHERE l.status = 'ACTIVE' AND " +
           "(:city IS NULL OR LOWER(l.property.city) LIKE LOWER(CONCAT('%', :city, '%'))) AND " +
           "(:locality IS NULL OR LOWER(l.property.locality) LIKE LOWER(CONCAT('%', :locality, '%'))) AND " +
           "(:type IS NULL OR l.property.type = :type) AND " +
           "(:bedrooms IS NULL OR l.property.bedrooms = :bedrooms) AND " +
           "(:minRent IS NULL OR l.rent >= :minRent) AND " +
           "(:maxRent IS NULL OR l.rent <= :maxRent) AND " +
           "(:maxRisk IS NULL OR l.riskScore <= :maxRisk) " +
           "ORDER BY l.createdAt DESC")
    List<Listing> searchListings(
            @Param("city") String city,
            @Param("locality") String locality,
            @Param("type") com.rentguard.property.model.PropertyType type,
            @Param("bedrooms") Integer bedrooms,
            @Param("minRent") Double minRent,
            @Param("maxRent") Double maxRent,
            @Param("maxRisk") Integer maxRisk
    );

    @Query("SELECT l FROM Listing l WHERE l.property.id IN :propertyIds AND l.status <> 'SUSPENDED'")
    List<Listing> findByPropertyIds(@Param("propertyIds") List<Long> propertyIds);
}
