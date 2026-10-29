package com.rentguard.property.controller;

import com.rentguard.common.dto.ApiResponse;
import com.rentguard.property.dto.PropertyRequestDto;
import com.rentguard.property.dto.PropertyResponseDto;
import com.rentguard.property.service.PropertyService;
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
@RequestMapping("/api/properties")
@Tag(name = "Properties", description = "Property registration and management")
public class PropertyController {

    private final PropertyService propertyService;

    public PropertyController(PropertyService propertyService) {
        this.propertyService = propertyService;
    }

    @PostMapping
    @Operation(summary = "Register a new property (Owners and Admins)")
    public ResponseEntity<ApiResponse<PropertyResponseDto>> createProperty(
            @Valid @RequestBody PropertyRequestDto request,
            @AuthenticationPrincipal UserDetails userDetails) {
        PropertyResponseDto created = propertyService.createProperty(request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(created, "Property registered successfully"));
    }

    @GetMapping
    @Operation(summary = "Get properties belonging to the current owner")
    public ResponseEntity<ApiResponse<List<PropertyResponseDto>>> getMyProperties(
            @AuthenticationPrincipal UserDetails userDetails) {
        List<PropertyResponseDto> properties = propertyService.getPropertiesByOwner(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(properties));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get property details by id")
    public ResponseEntity<ApiResponse<PropertyResponseDto>> getPropertyById(@PathVariable Long id) {
        PropertyResponseDto property = propertyService.getPropertyById(id);
        return ResponseEntity.ok(ApiResponse.success(property));
    }
}
