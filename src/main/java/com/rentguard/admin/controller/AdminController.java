package com.rentguard.admin.controller;

import com.rentguard.admin.dto.AdminAnalyticsDto;
import com.rentguard.admin.dto.SuspendListingDto;
import com.rentguard.admin.model.AuditLog;
import com.rentguard.admin.service.AdminService;
import com.rentguard.common.dto.ApiResponse;
import com.rentguard.listing.dto.ListingResponseDto;
import com.rentguard.listing.service.ListingService;
import com.rentguard.report.dto.ReportResolutionDto;
import com.rentguard.report.dto.ReportResponseDto;
import com.rentguard.report.model.ReportStatus;
import com.rentguard.report.service.ReportService;
import com.rentguard.verification.dto.DocumentResponseDto;
import com.rentguard.verification.dto.VerificationDecisionDto;
import com.rentguard.verification.service.VerificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Operations", description = "Administrative dashboard, moderation, verifications, and audit")
public class AdminController {

    private final AdminService adminService;
    private final VerificationService verificationService;
    private final ReportService reportService;
    private final ListingService listingService;

    public AdminController(AdminService adminService,
                           VerificationService verificationService,
                           ReportService reportService,
                           ListingService listingService) {
        this.adminService = adminService;
        this.verificationService = verificationService;
        this.reportService = reportService;
        this.listingService = listingService;
    }

    @GetMapping("/analytics")
    @Operation(summary = "Get platform scam analytics, metrics, and risk distributions")
    public ResponseEntity<ApiResponse<AdminAnalyticsDto>> getAnalytics() {
        AdminAnalyticsDto analytics = adminService.getAnalytics();
        return ResponseEntity.ok(ApiResponse.success(analytics));
    }

    @GetMapping("/verifications")
    @Operation(summary = "Get pending owner and property KYC verification queue")
    public ResponseEntity<ApiResponse<List<DocumentResponseDto>>> getPendingVerifications() {
        List<DocumentResponseDto> pending = verificationService.getPendingVerifications();
        return ResponseEntity.ok(ApiResponse.success(pending));
    }

    @PutMapping("/verifications/{id}")
    @Operation(summary = "Approve or reject a verification document")
    public ResponseEntity<ApiResponse<DocumentResponseDto>> processVerification(
            @PathVariable Long id,
            @Valid @RequestBody VerificationDecisionDto decision,
            @AuthenticationPrincipal UserDetails userDetails) {
        DocumentResponseDto response = verificationService.processDecision(id, decision, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(response, "Verification decision recorded"));
    }

    @GetMapping("/reports")
    @Operation(summary = "Get listing reports filtered by status")
    public ResponseEntity<ApiResponse<List<ReportResponseDto>>> getReports(
            @RequestParam(required = false) ReportStatus status) {
        List<ReportResponseDto> reports = reportService.getAllReports(status);
        return ResponseEntity.ok(ApiResponse.success(reports));
    }

    @PutMapping("/reports/{id}")
    @Operation(summary = "Update or resolve a tenant scam report")
    public ResponseEntity<ApiResponse<ReportResponseDto>> resolveReport(
            @PathVariable Long id,
            @Valid @RequestBody ReportResolutionDto resolution,
            @AuthenticationPrincipal UserDetails userDetails) {
        ReportResponseDto response = reportService.resolveReport(id, resolution, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(response, "Report updated successfully"));
    }

    @PutMapping("/listings/{id}/suspend")
    @Operation(summary = "Suspend a scam or high-risk listing")
    public ResponseEntity<ApiResponse<ListingResponseDto>> suspendListing(
            @PathVariable Long id,
            @Valid @RequestBody SuspendListingDto request,
            @AuthenticationPrincipal UserDetails userDetails) {
        ListingResponseDto response = listingService.suspendListing(id, request.getReason(), userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(response, "Listing suspended successfully"));
    }

    @GetMapping("/audit-logs")
    @Operation(summary = "Get system audit logs")
    public ResponseEntity<ApiResponse<List<AuditLog>>> getAuditLogs() {
        List<AuditLog> logs = adminService.getRecentAuditLogs();
        return ResponseEntity.ok(ApiResponse.success(logs));
    }
}
