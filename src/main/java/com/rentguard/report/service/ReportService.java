package com.rentguard.report.service;

import com.rentguard.admin.model.AuditLog;
import com.rentguard.admin.repository.AuditLogRepository;
import com.rentguard.common.exception.ResourceNotFoundException;
import com.rentguard.listing.model.Listing;
import com.rentguard.listing.model.ListingStatus;
import com.rentguard.listing.repository.ListingRepository;
import com.rentguard.report.dto.ReportRequestDto;
import com.rentguard.report.dto.ReportResolutionDto;
import com.rentguard.report.dto.ReportResponseDto;
import com.rentguard.report.model.Report;
import com.rentguard.report.model.ReportStatus;
import com.rentguard.report.repository.ReportRepository;
import com.rentguard.risk.service.RiskService;
import com.rentguard.user.model.User;
import com.rentguard.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReportService {

    private final ReportRepository reportRepository;
    private final ListingRepository listingRepository;
    private final UserRepository userRepository;
    private final RiskService riskService;
    private final AuditLogRepository auditLogRepository;

    public ReportService(ReportRepository reportRepository,
                         ListingRepository listingRepository,
                         UserRepository userRepository,
                         RiskService riskService,
                         AuditLogRepository auditLogRepository) {
        this.reportRepository = reportRepository;
        this.listingRepository = listingRepository;
        this.userRepository = userRepository;
        this.riskService = riskService;
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional
    public ReportResponseDto createReport(Long listingId, ReportRequestDto request, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new ResourceNotFoundException("Listing not found with id: " + listingId));

        Report report = new Report(listing, user, request.getReason(), request.getDescription());
        report = reportRepository.save(report);

        // Re-evaluate risk score immediately! Multiple complaints increase risk score (+10 to +20 points)
        riskService.evaluateListingRisk(listing);

        return ReportResponseDto.fromEntity(report);
    }

    public List<ReportResponseDto> getAllReports(ReportStatus status) {
        List<Report> reports = (status != null)
                ? reportRepository.findByStatus(status)
                : reportRepository.findAll();

        return reports.stream()
                .map(ReportResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    public List<ReportResponseDto> getMyReports(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return reportRepository.findByReporterId(user.getId()).stream()
                .map(ReportResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public ReportResponseDto resolveReport(Long reportId, ReportResolutionDto resolution, String adminEmail) {
        User admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found"));

        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found with id: " + reportId));

        report.setStatus(resolution.getStatus());
        report.setAdminNotes(resolution.getAdminNotes());
        if (resolution.getStatus() == ReportStatus.RESOLVED || resolution.getStatus() == ReportStatus.DISMISSED) {
            report.setResolvedAt(LocalDateTime.now());
        }
        report = reportRepository.save(report);

        Listing listing = report.getListing();
        if (Boolean.TRUE.equals(resolution.getSuspendListing()) && listing != null) {
            listing.setStatus(ListingStatus.SUSPENDED);
            listing.setSuspensionReason("Suspended due to substantiated scam report: " + resolution.getAdminNotes());
            listingRepository.save(listing);
        }

        // Re-evaluate risk score
        if (listing != null) {
            riskService.evaluateListingRisk(listing);
        }

        // Audit log
        auditLogRepository.save(new AuditLog(
                admin.getId(), admin.getEmail(),
                "REPORT_RESOLUTION", "REPORT", report.getId(),
                "Report #" + report.getId() + " status updated to " + resolution.getStatus() +
                        (Boolean.TRUE.equals(resolution.getSuspendListing()) ? " [Listing Suspended]" : "")
        ));

        return ReportResponseDto.fromEntity(report);
    }
}
