package com.rentguard.property.service;

import com.rentguard.common.exception.ResourceNotFoundException;
import com.rentguard.common.exception.UnauthorizedException;
import com.rentguard.property.dto.PropertyRequestDto;
import com.rentguard.property.dto.PropertyResponseDto;
import com.rentguard.property.model.Property;
import com.rentguard.property.repository.PropertyRepository;
import com.rentguard.risk.service.DuplicateDetectionService;
import com.rentguard.user.model.Owner;
import com.rentguard.user.model.Role;
import com.rentguard.user.model.User;
import com.rentguard.user.repository.OwnerRepository;
import com.rentguard.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PropertyService {

    private final PropertyRepository propertyRepository;
    private final OwnerRepository ownerRepository;
    private final UserRepository userRepository;
    private final DuplicateDetectionService duplicateDetectionService;

    public PropertyService(PropertyRepository propertyRepository,
                           OwnerRepository ownerRepository,
                           UserRepository userRepository,
                           DuplicateDetectionService duplicateDetectionService) {
        this.propertyRepository = propertyRepository;
        this.ownerRepository = ownerRepository;
        this.userRepository = userRepository;
        this.duplicateDetectionService = duplicateDetectionService;
    }

    @Transactional
    public PropertyResponseDto createProperty(PropertyRequestDto request, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userEmail));

        Owner owner = ownerRepository.findByUserId(user.getId())
                .orElseThrow(() -> new UnauthorizedException("User is not registered as an Owner"));

        Property property = new Property();
        property.setOwner(owner);
        property.setAddress(request.getAddress());
        property.setLocality(request.getLocality());
        property.setCity(request.getCity());
        property.setLatitude(request.getLatitude());
        property.setLongitude(request.getLongitude());
        property.setType(request.getType());
        property.setBedrooms(request.getBedrooms());
        property.setArea(request.getArea());
        property.setFurnishing(request.getFurnishing());
        property.setNormalizedAddress(duplicateDetectionService.normalizeAddress(request.getAddress()));

        property = propertyRepository.save(property);
        return PropertyResponseDto.fromEntity(property);
    }

    public List<PropertyResponseDto> getPropertiesByOwner(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getRole() == Role.ROLE_ADMIN) {
            return propertyRepository.findAll().stream()
                    .map(PropertyResponseDto::fromEntity)
                    .collect(Collectors.toList());
        }

        Owner owner = ownerRepository.findByUserId(user.getId())
                .orElseThrow(() -> new UnauthorizedException("Owner profile not found"));

        return propertyRepository.findByOwner(owner).stream()
                .map(PropertyResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    public PropertyResponseDto getPropertyById(Long id) {
        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found with id: " + id));
        return PropertyResponseDto.fromEntity(property);
    }
}
