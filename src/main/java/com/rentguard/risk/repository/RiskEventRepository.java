package com.rentguard.risk.repository;

import com.rentguard.risk.model.RiskEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RiskEventRepository extends JpaRepository<RiskEvent, Long> {
    List<RiskEvent> findByListingIdOrderByPointsDesc(Long listingId);
    void deleteByListingId(Long listingId);
}
