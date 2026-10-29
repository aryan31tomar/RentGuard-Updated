package com.rentguard.review.controller;

import com.rentguard.common.dto.ApiResponse;
import com.rentguard.review.dto.ReviewRequestDto;
import com.rentguard.review.dto.ReviewResponseDto;
import com.rentguard.review.service.ReviewService;
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
@RequestMapping("/api/listings")
@Tag(name = "Reviews", description = "Property and owner ratings and reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping("/{id}/review")
    @Operation(summary = "Submit a rating and review for a listing")
    public ResponseEntity<ApiResponse<ReviewResponseDto>> createReview(
            @PathVariable Long id,
            @Valid @RequestBody ReviewRequestDto request,
            @AuthenticationPrincipal UserDetails userDetails) {
        ReviewResponseDto response = reviewService.createReview(id, request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Review posted successfully"));
    }

    @GetMapping("/{id}/reviews")
    @Operation(summary = "Get all reviews for a listing")
    public ResponseEntity<ApiResponse<List<ReviewResponseDto>>> getListingReviews(@PathVariable Long id) {
        List<ReviewResponseDto> reviews = reviewService.getListingReviews(id);
        return ResponseEntity.ok(ApiResponse.success(reviews));
    }
}
