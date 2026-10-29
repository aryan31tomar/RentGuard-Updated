package com.rentguard.report.controller;

import com.rentguard.common.dto.ApiResponse;
import com.rentguard.report.dto.ReportRequestDto;
import com.rentguard.report.dto.ReportResponseDto;
import com.rentguard.report.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@Tag(name = "Reports & Moderation", description = "Scam reports and listing complaints")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @PostMapping("/listings/{id}/report")
    @Operation(summary = "Report a suspicious or fraudulent listing")
    public ResponseEntity<ApiResponse<ReportResponseDto>> reportListing(
            @PathVariable Long id,
            @Valid @RequestBody ReportRequestDto request,
            @AuthenticationPrincipal UserDetails userDetails) {
        ReportResponseDto response = reportService.createReport(id, request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Report submitted successfully for admin moderation"));
    }

    @GetMapping("/reports/my")
    @Operation(summary = "Get scam reports submitted by authenticated user")
    public ResponseEntity<ApiResponse<List<ReportResponseDto>>> getMyReports(
            @AuthenticationPrincipal UserDetails userDetails) {
        List<ReportResponseDto> reports = reportService.getMyReports(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(reports));
    }
}
