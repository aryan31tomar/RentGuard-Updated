package com.rentguard.verification.controller;

import com.rentguard.common.dto.ApiResponse;
import com.rentguard.verification.dto.DocumentResponseDto;
import com.rentguard.verification.dto.DocumentUploadRequest;
import com.rentguard.verification.dto.VerificationDecisionDto;
import com.rentguard.verification.service.VerificationService;
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
@RequestMapping("/api/verification")
@Tag(name = "Verification", description = "Owner & property KYC and ownership verification workflow")
public class VerificationController {

    private final VerificationService verificationService;

    public VerificationController(VerificationService verificationService) {
        this.verificationService = verificationService;
    }

    @PostMapping("/submit")
    @Operation(summary = "Submit an owner KYC or property ownership document")
    public ResponseEntity<ApiResponse<DocumentResponseDto>> submitDocument(
            @Valid @RequestBody DocumentUploadRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        DocumentResponseDto response = verificationService.submitDocument(request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Document submitted successfully and marked under review"));
    }

    @GetMapping("/my")
    @Operation(summary = "Get all documents submitted by the current owner")
    public ResponseEntity<ApiResponse<List<DocumentResponseDto>>> getMyDocuments(
            @AuthenticationPrincipal UserDetails userDetails) {
        List<DocumentResponseDto> documents = verificationService.getMyDocuments(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(documents));
    }
}
