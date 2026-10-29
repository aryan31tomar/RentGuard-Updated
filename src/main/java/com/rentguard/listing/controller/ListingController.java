package com.rentguard.listing.controller;

import com.rentguard.common.dto.ApiResponse;
import com.rentguard.listing.dto.ListingRequestDto;
import com.rentguard.listing.dto.ListingResponseDto;
import com.rentguard.listing.dto.PropertyImageDto;
import com.rentguard.listing.service.ListingService;
import com.rentguard.property.model.PropertyType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/listings")
@Tag(name = "Listings", description = "Property listings and search with explainable risk evaluation")
public class ListingController {

    private final ListingService listingService;

    public ListingController(ListingService listingService) {
        this.listingService = listingService;
    }

    @GetMapping
    @Operation(summary = "Search and filter listings with risk level filters")
    public ResponseEntity<ApiResponse<List<ListingResponseDto>>> searchListings(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String locality,
            @RequestParam(required = false) PropertyType type,
            @RequestParam(required = false) Integer bedrooms,
            @RequestParam(required = false) Double minRent,
            @RequestParam(required = false) Double maxRent,
            @RequestParam(required = false) Integer maxRisk) {
        List<ListingResponseDto> listings = listingService.searchListings(
                city, locality, type, bedrooms, minRent, maxRent, maxRisk
        );
        return ResponseEntity.ok(ApiResponse.success(listings));
    }

    @PostMapping
    @Operation(summary = "Create a new listing and trigger instant risk calculation")
    public ResponseEntity<ApiResponse<ListingResponseDto>> createListing(
            @Valid @RequestBody ListingRequestDto request,
            @AuthenticationPrincipal UserDetails userDetails) {
        ListingResponseDto response = listingService.createListing(request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Listing published successfully"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get listing details with complete explainable risk breakdown")
    public ResponseEntity<ApiResponse<ListingResponseDto>> getListingById(@PathVariable Long id) {
        ListingResponseDto response = listingService.getListingById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing listing")
    public ResponseEntity<ApiResponse<ListingResponseDto>> updateListing(
            @PathVariable Long id,
            @Valid @RequestBody ListingRequestDto request,
            @AuthenticationPrincipal UserDetails userDetails) {
        ListingResponseDto response = listingService.updateListing(id, request, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(response, "Listing updated successfully"));
    }

    @GetMapping("/my")
    @Operation(summary = "Get all listings owned by authenticated user")
    public ResponseEntity<ApiResponse<List<ListingResponseDto>>> getMyListings(
            @AuthenticationPrincipal UserDetails userDetails) {
        List<ListingResponseDto> listings = listingService.getMyListings(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(listings));
    }

    @PostMapping("/{id}/images")
    @Operation(summary = "Upload image for listing (generates SHA-256 hash for duplicate image detection)")
    public ResponseEntity<ApiResponse<PropertyImageDto>> uploadImage(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "isCover", defaultValue = "false") Boolean isCover,
            @AuthenticationPrincipal UserDetails userDetails) throws IOException {
        byte[] bytes = file.getBytes();
        String originalFilename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "image.jpg";
        PropertyImageDto dto = listingService.addImageToListing(id, originalFilename, bytes, isCover, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(dto, "Image uploaded and hashed successfully"));
    }
}
