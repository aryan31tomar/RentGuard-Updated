package com.rentguard.risk;

import com.rentguard.listing.model.Listing;
import com.rentguard.listing.repository.ListingRepository;
import com.rentguard.listing.repository.PropertyImageRepository;
import com.rentguard.property.model.Property;
import com.rentguard.report.repository.ReportRepository;
import com.rentguard.risk.dto.PriceAnomalyResult;
import com.rentguard.risk.dto.RiskEvaluationResult;
import com.rentguard.risk.model.RiskLevel;
import com.rentguard.risk.repository.RiskEventRepository;
import com.rentguard.risk.service.DuplicateDetectionService;
import com.rentguard.risk.service.PriceAnomalyService;
import com.rentguard.risk.service.RiskService;
import com.rentguard.user.model.Owner;
import com.rentguard.user.model.VerificationStatus;
import com.rentguard.verification.model.Document;
import com.rentguard.verification.model.DocumentStatus;
import com.rentguard.verification.repository.DocumentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RiskServiceTest {

    @Mock
    private RiskEventRepository riskEventRepository;
    @Mock
    private PriceAnomalyService priceAnomalyService;
    @Mock
    private DuplicateDetectionService duplicateDetectionService;
    @Mock
    private DocumentRepository documentRepository;
    @Mock
    private ReportRepository reportRepository;
    @Mock
    private PropertyImageRepository propertyImageRepository;
    @Mock
    private ListingRepository listingRepository;

    @InjectMocks
    private RiskService riskService;

    @Test
    @DisplayName("Verified owner with approved docs and normal price should have LOW risk (score < 30)")
    void testLowRiskEvaluation() {
        Owner owner = new Owner();
        owner.setId(1L);
        owner.setVerificationStatus(VerificationStatus.VERIFIED);

        Property property = new Property();
        property.setOwner(owner);

        Listing listing = new Listing();
        listing.setId(100L);
        listing.setProperty(property);
        listing.setRent(18000.0);
        listing.setDeposit(36000.0);

        Document approvedDoc = new Document();
        approvedDoc.setStatus(DocumentStatus.APPROVED);

        when(documentRepository.findByOwnerId(1L)).thenReturn(List.of(approvedDoc));
        when(priceAnomalyService.evaluatePriceAnomaly(any())).thenReturn(
                new PriceAnomalyResult(false, 18000.0, 19000.0, 5.0, 3, "Price within range")
        );
        when(duplicateDetectionService.checkDuplicateProperty(any())).thenReturn(
                new DuplicateDetectionService.DuplicatePropertyCheckResult(false, "No duplicate")
        );
        when(propertyImageRepository.findByListingId(100L)).thenReturn(Collections.emptyList());
        when(reportRepository.countByListingIdAndStatusIn(anyLong(), any())).thenReturn(0L);

        RiskEvaluationResult result = riskService.evaluateListingRisk(listing);

        assertNotNull(result);
        assertEquals(0, result.getScore(), "Verified listing with no suspicious signals should score 0 points");
        assertEquals(RiskLevel.LOW, result.getRiskLevel());
        assertTrue(result.getEvents().isEmpty(), "No risk events should be triggered");
        assertTrue(result.getPositiveSignals().size() >= 4, "Should have multiple positive reassurance signals");
    }

    @Test
    @DisplayName("Unverified owner with missing docs, price anomaly, and reports should produce HIGH risk (score >= 60)")
    void testHighRiskEvaluation_ScamSignals() {
        Owner owner = new Owner();
        owner.setId(2L);
        owner.setVerificationStatus(VerificationStatus.REGISTERED); // Unverified (+20)

        Property property = new Property();
        property.setOwner(owner);

        Listing listing = new Listing();
        listing.setId(200L);
        listing.setProperty(property);
        listing.setRent(6000.0);
        listing.setDeposit(1000.0); // low deposit (+10)

        when(documentRepository.findByOwnerId(2L)).thenReturn(Collections.emptyList()); // Docs missing (+20)
        when(priceAnomalyService.evaluatePriceAnomaly(any())).thenReturn(
                new PriceAnomalyResult(true, 6000.0, 20000.0, 70.0, 4, "Rent substantially below median") // (+25)
        );
        when(duplicateDetectionService.checkDuplicateProperty(any())).thenReturn(
                new DuplicateDetectionService.DuplicatePropertyCheckResult(false, "No duplicate")
        );
        when(propertyImageRepository.findByListingId(200L)).thenReturn(Collections.emptyList());
        when(reportRepository.countByListingIdAndStatusIn(anyLong(), any())).thenReturn(2L); // complaints (+10)

        RiskEvaluationResult result = riskService.evaluateListingRisk(listing);

        // Expected score: +20 (unverified) + 20 (no docs) + 25 (price anomaly) + 10 (complaints) + 10 (suspicious terms) = 85
        assertEquals(85, result.getScore());
        assertEquals(RiskLevel.HIGH, result.getRiskLevel());
        assertEquals(5, result.getEvents().size());
    }
}
