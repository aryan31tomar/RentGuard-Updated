package com.rentguard.review.repository;

import com.rentguard.review.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByListingIdOrderByCreatedAtDesc(Long listingId);
    List<Review> findByTenantIdOrderByCreatedAtDesc(Long tenantId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.listing.id = :listingId")
    Double getAverageRatingForListing(@Param("listingId") Long listingId);

    long countByListingId(Long listingId);
}
