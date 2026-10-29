package com.rentguard.admin.service;

import com.rentguard.admin.dto.AdminAnalyticsDto;
import com.rentguard.admin.model.AuditLog;
import com.rentguard.admin.repository.AuditLogRepository;
import com.rentguard.listing.dto.ListingResponseDto;
import com.rentguard.listing.model.Listing;
import com.rentguard.listing.model.ListingStatus;
import com.rentguard.listing.repository.ListingRepository;
import com.rentguard.listing.repository.PropertyImageRepository;
import com.rentguard.report.model.ReportReason;
import com.rentguard.report.model.ReportStatus;
import com.rentguard.report.repository.ReportRepository;
import com.rentguard.risk.model.RiskLevel;
import com.rentguard.user.model.VerificationStatus;
import com.rentguard.user.repository.OwnerRepository;
import com.rentguard.verification.model.DocumentStatus;
import com.rentguard.verification.repository.DocumentRepository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AdminService {

    private final ListingRepository listingRepository;
    private final OwnerRepository ownerRepository;
    private final DocumentRepository documentRepository;
    private final ReportRepository reportRepository;
    private final AuditLogRepository auditLogRepository;
    private final PropertyImageRepository propertyImageRepository;

    public AdminService(ListingRepository listingRepository,
                        OwnerRepository ownerRepository,
                        DocumentRepository documentRepository,
                        ReportRepository reportRepository,
                        AuditLogRepository auditLogRepository,
                        PropertyImageRepository propertyImageRepository) {
        this.listingRepository = listingRepository;
        this.ownerRepository = ownerRepository;
        this.documentRepository = documentRepository;
        this.reportRepository = reportRepository;
        this.auditLogRepository = auditLogRepository;
        this.propertyImageRepository = propertyImageRepository;
    }

    public AdminAnalyticsDto getAnalytics() {
        AdminAnalyticsDto dto = new AdminAnalyticsDto();

        dto.setTotalListings(listingRepository.count());
        dto.setActiveListings(listingRepository.countByStatus(ListingStatus.ACTIVE));
        dto.setVerifiedOwners(ownerRepository.countByVerificationStatus(VerificationStatus.VERIFIED));
        dto.setPendingVerifications(documentRepository.countByStatus(DocumentStatus.PENDING));
        dto.setHighRiskListings(listingRepository.countHighRiskActiveListings());
        dto.setOpenReports(reportRepository.countByStatus(ReportStatus.OPEN) + reportRepository.countByStatus(ReportStatus.INVESTIGATING));
        dto.setSuspendedListings(listingRepository.countByStatus(ListingStatus.SUSPENDED));

        // Complaint categories
        Map<String, Long> complaints = new HashMap<>();
        List<Object[]> reportCounts = reportRepository.countReportsByReason();
        for (Object[] row : reportCounts) {
            ReportReason reason = (ReportReason) row[0];
            Long count = (Long) row[1];
            complaints.put(reason.name(), count);
        }
        dto.setComplaintCategories(complaints);

        // Risk distribution
        List<Listing> allListings = listingRepository.findAll();
        long lowCount = allListings.stream().filter(l -> l.getRiskScore() <= 29).count();
        long medCount = allListings.stream().filter(l -> l.getRiskScore() >= 30 && l.getRiskScore() <= 59).count();
        long highCount = allListings.stream().filter(l -> l.getRiskScore() >= 60).count();

        Map<String, Long> riskDist = new HashMap<>();
        riskDist.put("LOW", lowCount);
        riskDist.put("MEDIUM", medCount);
        riskDist.put("HIGH", highCount);
        dto.setRiskScoreDistribution(riskDist);

        // Recent high-risk listings
        List<Listing> highRiskListings = listingRepository.findHighRiskListings().stream()
                .limit(5)
                .collect(Collectors.toList());

        List<ListingResponseDto> highRiskDtos = highRiskListings.stream()
                .map(ListingResponseDto::fromEntity)
                .collect(Collectors.toList());
        dto.setRecentSuspiciousListings(highRiskDtos);

        return dto;
    }

    public List<AuditLog> getRecentAuditLogs() {
        return auditLogRepository.findTop50ByOrderByTimestampDesc();
    }
}
