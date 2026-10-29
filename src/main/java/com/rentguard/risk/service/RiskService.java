package com.rentguard.risk.service;

import com.rentguard.listing.model.Listing;
import com.rentguard.listing.model.PropertyImage;
import com.rentguard.listing.repository.ListingRepository;
import com.rentguard.listing.repository.PropertyImageRepository;
import com.rentguard.property.model.Property;
import com.rentguard.report.model.ReportStatus;
import com.rentguard.report.repository.ReportRepository;
import com.rentguard.risk.dto.PriceAnomalyResult;
import com.rentguard.risk.dto.RiskEvaluationResult;
import com.rentguard.risk.dto.RiskEventDto;
import com.rentguard.risk.model.RiskEvent;
import com.rentguard.risk.model.RiskLevel;
import com.rentguard.risk.repository.RiskEventRepository;
import com.rentguard.user.model.Owner;
import com.rentguard.user.model.VerificationStatus;
import com.rentguard.verification.model.Document;
import com.rentguard.verification.model.DocumentStatus;
import com.rentguard.verification.repository.DocumentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class RiskService {

    public static final String RULE_OWNER_UNVERIFIED = "OWNER_UNVERIFIED";
    public static final String RULE_DOCS_MISSING_OR_REJECTED = "DOCS_MISSING_OR_REJECTED";
    public static final String RULE_PRICE_SUBSTANTIALLY_LOW = "PRICE_SUBSTANTIALLY_LOW";
    public static final String RULE_DUPLICATE_PROPERTY = "DUPLICATE_PROPERTY";
    public static final String RULE_DUPLICATE_IMAGE = "DUPLICATE_IMAGE";
    public static final String RULE_MULTIPLE_COMPLAINTS = "MULTIPLE_COMPLAINTS";
    public static final String RULE_SUSPICIOUS_BEHAVIOUR = "SUSPICIOUS_BEHAVIOUR";

    private final RiskEventRepository riskEventRepository;
    private final PriceAnomalyService priceAnomalyService;
    private final DuplicateDetectionService duplicateDetectionService;
    private final DocumentRepository documentRepository;
    private final ReportRepository reportRepository;
    private final PropertyImageRepository propertyImageRepository;
    private final ListingRepository listingRepository;

    public RiskService(RiskEventRepository riskEventRepository,
                       PriceAnomalyService priceAnomalyService,
                       DuplicateDetectionService duplicateDetectionService,
                       DocumentRepository documentRepository,
                       ReportRepository reportRepository,
                       PropertyImageRepository propertyImageRepository,
                       ListingRepository listingRepository) {
        this.riskEventRepository = riskEventRepository;
        this.priceAnomalyService = priceAnomalyService;
        this.duplicateDetectionService = duplicateDetectionService;
        this.documentRepository = documentRepository;
        this.reportRepository = reportRepository;
        this.propertyImageRepository = propertyImageRepository;
        this.listingRepository = listingRepository;
    }

    @Transactional
    public RiskEvaluationResult evaluateListingRisk(Listing listing) {
        List<RiskEventDto> triggeredEvents = new ArrayList<>();
        List<String> positiveSignals = new ArrayList<>();
        int totalScore = 0;

        Property property = listing.getProperty();
        Owner owner = property != null ? property.getOwner() : null;

        // 1. Owner verification (+20 pts)
        if (owner == null || owner.getVerificationStatus() != VerificationStatus.VERIFIED) {
            triggeredEvents.add(new RiskEventDto(
                    RULE_OWNER_UNVERIFIED, 20,
                    "Owner identity is unverified on the platform."
            ));
            totalScore += 20;
        } else {
            positiveSignals.add("Owner identity is verified");
        }

        // 2. Documents missing/rejected (+20 pts)
        boolean hasApprovedDocs = false;
        if (owner != null) {
            List<Document> ownerDocs = documentRepository.findByOwnerId(owner.getId());
            hasApprovedDocs = ownerDocs.stream()
                    .anyMatch(d -> d.getStatus() == DocumentStatus.APPROVED);
        }
        if (!hasApprovedDocs) {
            triggeredEvents.add(new RiskEventDto(
                    RULE_DOCS_MISSING_OR_REJECTED, 20,
                    "Ownership and government ID verification documents are missing or pending review."
            ));
            totalScore += 20;
        } else {
            positiveSignals.add("Property ownership documents verified");
        }

        // 3. Rent substantially below comparable listings (+25 pts)
        PriceAnomalyResult priceResult = priceAnomalyService.evaluatePriceAnomaly(listing);
        if (priceResult.isAnomalyDetected()) {
            triggeredEvents.add(new RiskEventDto(
                    RULE_PRICE_SUBSTANTIALLY_LOW, 25,
                    priceResult.getExplanation()
            ));
            totalScore += 25;
        } else {
            positiveSignals.add(priceResult.getExplanation());
        }

        // 4. Duplicate property detection (+20 pts)
        DuplicateDetectionService.DuplicatePropertyCheckResult dupPropResult =
                duplicateDetectionService.checkDuplicateProperty(listing);
        if (dupPropResult.isDuplicate()) {
            triggeredEvents.add(new RiskEventDto(
                    RULE_DUPLICATE_PROPERTY, 20,
                    dupPropResult.getExplanation()
            ));
            totalScore += 20;
        } else {
            positiveSignals.add("No duplicate property detected");
        }

        // 5. Duplicate image detection (+15 pts)
        boolean duplicateImageFound = false;
        List<PropertyImage> images = propertyImageRepository.findByListingId(listing.getId());
        for (PropertyImage img : images) {
            DuplicateDetectionService.DuplicateImageCheckResult imgResult =
                    duplicateDetectionService.checkDuplicateImage(img.getImageHash(), listing.getId());
            if (imgResult.isDuplicate()) {
                triggeredEvents.add(new RiskEventDto(
                        RULE_DUPLICATE_IMAGE, 15,
                        imgResult.getExplanation()
                ));
                totalScore += 15;
                duplicateImageFound = true;
                break; // add once per listing
            }
        }
        if (!duplicateImageFound) {
            positiveSignals.add("No duplicate image reuse detected");
        }

        // 6. Multiple complaints (+10 to +20 pts)
        if (listing.getId() != null) {
            long openReports = reportRepository.countByListingIdAndStatusIn(
                    listing.getId(),
                    Arrays.asList(ReportStatus.OPEN, ReportStatus.INVESTIGATING)
            );
            if (openReports >= 3) {
                triggeredEvents.add(new RiskEventDto(
                        RULE_MULTIPLE_COMPLAINTS, 20,
                        "Multiple tenant complaints recorded (" + openReports + " active reports under investigation)."
                ));
                totalScore += 20;
            } else if (openReports >= 1) {
                triggeredEvents.add(new RiskEventDto(
                        RULE_MULTIPLE_COMPLAINTS, 10,
                        "Tenant complaint recorded (" + openReports + " report under investigation)."
                ));
                totalScore += 10;
            } else {
                positiveSignals.add("No active complaints or scam reports");
            }
        } else {
            positiveSignals.add("No active complaints or scam reports");
        }

        // 7. Suspicious behaviour (+10 pts)
        boolean suspicious = false;
        if (listing.getRent() != null && listing.getDeposit() != null && listing.getDeposit() < (listing.getRent() * 0.5)) {
            // Unusually low or zero deposit designed to lure victims into fast payments
            suspicious = true;
            triggeredEvents.add(new RiskEventDto(
                    RULE_SUSPICIOUS_BEHAVIOUR, 10,
                    "Suspicious terms: Zero or unusually low security deposit (<50% rent), often used in advance deposit phishing."
            ));
            totalScore += 10;
        } else if (owner != null && owner.getUser() != null && owner.getUser().getCreatedAt() != null) {
            // Newly created owner account created within 24 hours
            if (owner.getUser().getCreatedAt().isAfter(LocalDateTime.now().minusHours(24))
                    && owner.getVerificationStatus() != VerificationStatus.VERIFIED) {
                suspicious = true;
                triggeredEvents.add(new RiskEventDto(
                        RULE_SUSPICIOUS_BEHAVIOUR, 10,
                        "New owner account registered within last 24 hours without prior history."
                ));
                totalScore += 10;
            }
        }
        if (!suspicious) {
            positiveSignals.add("Standard lease terms and deposit verified");
        }

        // Capped at 100
        int finalScore = Math.min(100, Math.max(0, totalScore));
        RiskLevel riskLevel = RiskLevel.fromScore(finalScore);

        // Persist events if listing is already saved
        if (listing.getId() != null) {
            riskEventRepository.deleteByListingId(listing.getId());
            for (RiskEventDto dto : triggeredEvents) {
                RiskEvent event = new RiskEvent(listing, dto.getRuleCode(), dto.getPoints(), dto.getExplanation());
                riskEventRepository.save(event);
            }
            listing.setRiskScore(finalScore);
            listingRepository.save(listing);
        }

        RiskEvaluationResult result = new RiskEvaluationResult(listing.getId(), finalScore, riskLevel);
        result.setEvents(triggeredEvents);
        result.setPositiveSignals(positiveSignals);
        return result;
    }

    public List<RiskEventDto> getListingRiskEvents(Long listingId) {
        List<RiskEvent> events = riskEventRepository.findByListingIdOrderByPointsDesc(listingId);
        List<RiskEventDto> dtos = new ArrayList<>();
        for (RiskEvent e : events) {
            dtos.add(new RiskEventDto(e.getRuleCode(), e.getPoints(), e.getExplanation()));
        }
        return dtos;
    }
}
