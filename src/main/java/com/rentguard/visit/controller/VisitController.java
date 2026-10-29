package com.rentguard.visit.controller;

import com.rentguard.common.dto.ApiResponse;
import com.rentguard.visit.dto.VisitRequestDto;
import com.rentguard.visit.dto.VisitResponseDto;
import com.rentguard.visit.dto.VisitStatusUpdateDto;
import com.rentguard.visit.service.VisitService;
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
@Tag(name = "Visits", description = "Property visit scheduling and management")
public class VisitController {

    private final VisitService visitService;

    public VisitController(VisitService visitService) {
        this.visitService = visitService;
    }

    @PostMapping("/listings/{id}/visits")
    @Operation(summary = "Request a physical or virtual visit for a property")
    public ResponseEntity<ApiResponse<VisitResponseDto>> requestVisit(
            @PathVariable Long id,
            @Valid @RequestBody VisitRequestDto request,
            @AuthenticationPrincipal UserDetails userDetails) {
        VisitResponseDto response = visitService.createVisitRequest(id, request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Visit request sent to property owner"));
    }

    @GetMapping("/visits/tenant")
    @Operation(summary = "Get visits requested by the current tenant")
    public ResponseEntity<ApiResponse<List<VisitResponseDto>>> getTenantVisits(
            @AuthenticationPrincipal UserDetails userDetails) {
        List<VisitResponseDto> visits = visitService.getTenantVisits(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(visits));
    }

    @GetMapping("/visits/owner")
    @Operation(summary = "Get visit requests received by the current owner")
    public ResponseEntity<ApiResponse<List<VisitResponseDto>>> getOwnerVisits(
            @AuthenticationPrincipal UserDetails userDetails) {
        List<VisitResponseDto> visits = visitService.getOwnerVisits(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(visits));
    }

    @PutMapping("/visits/{id}/status")
    @Operation(summary = "Update visit request status (Accept / Decline / Complete)")
    public ResponseEntity<ApiResponse<VisitResponseDto>> updateVisitStatus(
            @PathVariable Long id,
            @Valid @RequestBody VisitStatusUpdateDto update,
            @AuthenticationPrincipal UserDetails userDetails) {
        VisitResponseDto response = visitService.updateVisitStatus(id, update, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(response, "Visit status updated successfully"));
    }
}
