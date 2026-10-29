package com.rentguard.user.controller;

import com.rentguard.common.dto.ApiResponse;
import com.rentguard.common.exception.ResourceNotFoundException;
import com.rentguard.user.model.Owner;
import com.rentguard.user.model.User;
import com.rentguard.user.repository.OwnerRepository;
import com.rentguard.user.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@Tag(name = "User Profile", description = "User account and profile details")
public class UserController {

    private final UserRepository userRepository;
    private final OwnerRepository ownerRepository;

    public UserController(UserRepository userRepository, OwnerRepository ownerRepository) {
        this.userRepository = userRepository;
        this.ownerRepository = ownerRepository;
    }

    @GetMapping("/profile")
    @Operation(summary = "Get authenticated user profile details")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getProfile(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Map<String, Object> profile = new HashMap<>();
        profile.put("id", user.getId());
        profile.put("name", user.getName());
        profile.put("email", user.getEmail());
        profile.put("phone", user.getPhone());
        profile.put("role", user.getRole());
        profile.put("status", user.getStatus());
        profile.put("createdAt", user.getCreatedAt());

        Owner owner = ownerRepository.findByUserId(user.getId()).orElse(null);
        if (owner != null) {
            Map<String, Object> ownerData = new HashMap<>();
            ownerData.put("id", owner.getId());
            ownerData.put("verificationStatus", owner.getVerificationStatus());
            ownerData.put("trustScore", owner.getTrustScore());
            ownerData.put("verificationNotes", owner.getVerificationNotes());
            profile.put("ownerProfile", ownerData);
        }

        return ResponseEntity.ok(ApiResponse.success(profile));
    }
}
