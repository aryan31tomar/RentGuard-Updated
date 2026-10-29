package com.rentguard.visit.repository;

import com.rentguard.visit.model.VisitRequest;
import com.rentguard.visit.model.VisitStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VisitRequestRepository extends JpaRepository<VisitRequest, Long> {
    List<VisitRequest> findByListingIdOrderByRequestedDateDesc(Long listingId);
    List<VisitRequest> findByTenantIdOrderByRequestedDateDesc(Long tenantId);
    List<VisitRequest> findByListing_Property_Owner_User_IdOrderByRequestedDateDesc(Long ownerUserId);
    List<VisitRequest> findByStatus(VisitStatus status);
}
