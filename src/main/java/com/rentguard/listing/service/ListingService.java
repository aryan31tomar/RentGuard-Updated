package com.rentguard.listing.service;

import com.rentguard.admin.model.AuditLog;
import com.rentguard.admin.repository.AuditLogRepository;
import com.rentguard.common.exception.BadRequestException;
import com.rentguard.common.exception.ResourceNotFoundException;
import com.rentguard.common.exception.UnauthorizedException;
import com.rentguard.listing.dto.ListingRequestDto;
import com.rentguard.listing.dto.ListingResponseDto;
import com.rentguard.listing.dto.PropertyImageDto;
import com.rentguard.listing.model.Listing;
import com.rentguard.listing.model.ListingStatus;
import com.rentguard.listing.model.PropertyImage;
import com.rentguard.listing.repository.ListingRepository;
import com.rentguard.listing.repository.PropertyImageRepository;
import com.rentguard.property.model.Property;
import com.rentguard.property.model.PropertyType;
import com.rentguard.property.repository.PropertyRepository;
import com.rentguard.review.repository.ReviewRepository;
import com.rentguard.risk.dto.RiskEvaluationResult;
import com.rentguard.risk.service.DuplicateDetectionService;
import com.rentguard.risk.service.RiskService;
import com.rentguard.user.model.Role;
import com.rentguard.user.model.User;
import com.rentguard.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ListingService {

    private final ListingRepository listingRepository;
    private final PropertyRepository propertyRepository;
    private final PropertyImageRepository propertyImageRepository;
    private final RiskService riskService;
    private final DuplicateDetectionService duplicateDetectionService;
    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;
    private final AuditLogRepository auditLogRepository;

    public ListingService(ListingRepository listingRepository,
                          PropertyRepository propertyRepository,
                          PropertyImageRepository propertyImageRepository,
                          RiskService riskService,
                          DuplicateDetectionService duplicateDetectionService,
                          UserRepository userRepository,
                          ReviewRepository reviewRepository,
                          AuditLogRepository auditLogRepository) {
        this.listingRepository = listingRepository;
        this.propertyRepository = propertyRepository;
        this.propertyImageRepository = propertyImageRepository;
        this.riskService = riskService;
        this.duplicateDetectionService = duplicateDetectionService;
        this.userRepository = userRepository;
        this.reviewRepository = reviewRepository;
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional
    public ListingResponseDto createListing(ListingRequestDto request, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Property property = propertyRepository.findById(request.getPropertyId())
                .orElseThrow(() -> new ResourceNotFoundException("Property not found with id: " + request.getPropertyId()));

        if (user.getRole() != Role.ROLE_ADMIN &&
                (property.getOwner() == null || property.getOwner().getUser() == null ||
                        !property.getOwner().getUser().getId().equals(user.getId()))) {
            throw new UnauthorizedException("You do not have permission to create listings for this property");
        }

        Listing listing = new Listing();
        listing.setProperty(property);
        listing.setRent(request.getRent());
        listing.setDeposit(request.getDeposit());
        listing.setDescription(request.getDescription());
        listing.setStatus(ListingStatus.ACTIVE);
        listing.setRiskScore(0);
        listing.setCreatedAt(LocalDateTime.now());
        listing.setUpdatedAt(LocalDateTime.now());

        listing = listingRepository.save(listing);

        // Add initial images if provided
        if (request.getImageUrls() != null && !request.getImageUrls().isEmpty()) {
            boolean isFirst = true;
            for (String imgUrl : request.getImageUrls()) {
                String hash = duplicateDetectionService.calculateSha256(imgUrl.getBytes(StandardCharsets.UTF_8));
                PropertyImage img = new PropertyImage(listing, imgUrl, hash, isFirst);
                propertyImageRepository.save(img);
                isFirst = false;
            }
        }

        // Run full explainable risk engine immediately!
        RiskEvaluationResult riskResult = riskService.evaluateListingRisk(listing);
        listing.setRiskScore(riskResult.getScore());
        listing = listingRepository.save(listing);

        return enrichListingResponse(listing, riskResult);
    }

    @Transactional
    public ListingResponseDto updateListing(Long id, ListingRequestDto request, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Listing listing = listingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Listing not found with id: " + id));

        if (user.getRole() != Role.ROLE_ADMIN &&
                !listing.getProperty().getOwner().getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("You do not have permission to modify this listing");
        }

        listing.setRent(request.getRent());
        listing.setDeposit(request.getDeposit());
        listing.setDescription(request.getDescription());
        listing.setUpdatedAt(LocalDateTime.now());

        listing = listingRepository.save(listing);

        // Re-evaluate risk score
        RiskEvaluationResult riskResult = riskService.evaluateListingRisk(listing);
        listing.setRiskScore(riskResult.getScore());
        listing = listingRepository.save(listing);

        return enrichListingResponse(listing, riskResult);
    }

    public ListingResponseDto getListingById(Long id) {
        Listing listing = listingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Listing not found with id: " + id));

        RiskEvaluationResult riskResult = riskService.evaluateListingRisk(listing);
        return enrichListingResponse(listing, riskResult);
    }

    public List<ListingResponseDto> searchListings(String city, String locality, PropertyType type,
                                                   Integer bedrooms, Double minRent, Double maxRent,
                                                   Integer maxRisk) {
        List<Listing> listings = listingRepository.searchListings(
                city, locality, type, bedrooms, minRent, maxRent, maxRisk
        );

        return listings.stream()
                .map(l -> {
                    ListingResponseDto dto = ListingResponseDto.fromEntity(l);
                    List<PropertyImage> images = propertyImageRepository.findByListingId(l.getId());
                    dto.setImages(images.stream().map(PropertyImageDto::fromEntity).collect(Collectors.toList()));
                    Double avgRating = reviewRepository.getAverageRatingForListing(l.getId());
                    dto.setAverageRating(avgRating != null ? Math.round(avgRating * 10.0) / 10.0 : null);
                    dto.setReviewCount(reviewRepository.countByListingId(l.getId()));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    public List<ListingResponseDto> getMyListings(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<Listing> listings;
        if (user.getRole() == Role.ROLE_ADMIN) {
            listings = listingRepository.findAll();
        } else {
            listings = listingRepository.findByProperty_Owner_User_Id(user.getId());
        }

        return listings.stream()
                .map(l -> {
                    ListingResponseDto dto = ListingResponseDto.fromEntity(l);
                    List<PropertyImage> images = propertyImageRepository.findByListingId(l.getId());
                    dto.setImages(images.stream().map(PropertyImageDto::fromEntity).collect(Collectors.toList()));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public PropertyImageDto addImageToListing(Long listingId, String imagePathOrData, byte[] imageBytes,
                                             Boolean isCover, String userEmail) {
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new ResourceNotFoundException("Listing not found with id: " + listingId));

        String hash = duplicateDetectionService.calculateSha256(imageBytes);

        PropertyImage image = new PropertyImage(listing, imagePathOrData, hash, isCover);
        image = propertyImageRepository.save(image);

        // Re-evaluate risk to detect any duplicate images
        riskService.evaluateListingRisk(listing);

        return PropertyImageDto.fromEntity(image);
    }

    @Transactional
    public ListingResponseDto suspendListing(Long id, String reason, String adminEmail) {
        User admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found"));

        Listing listing = listingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Listing not found with id: " + id));

        listing.setStatus(ListingStatus.SUSPENDED);
        listing.setSuspensionReason(reason);
        listing.setUpdatedAt(LocalDateTime.now());
        listing = listingRepository.save(listing);

        // Log admin audit action
        auditLogRepository.save(new AuditLog(
                admin.getId(), admin.getEmail(), "SUSPEND_LISTING", "LISTING", listing.getId(),
                "Listing #" + listing.getId() + " suspended. Reason: " + reason
        ));

        return enrichListingResponse(listing, null);
    }

    private ListingResponseDto enrichListingResponse(Listing listing, RiskEvaluationResult riskResult) {
        ListingResponseDto dto = ListingResponseDto.fromEntity(listing);

        List<PropertyImage> images = propertyImageRepository.findByListingId(listing.getId());
        dto.setImages(images.stream().map(PropertyImageDto::fromEntity).collect(Collectors.toList()));

        if (riskResult != null) {
            dto.setRiskEvents(riskResult.getEvents());
            dto.setPositiveSignals(riskResult.getPositiveSignals());
        } else {
            dto.setRiskEvents(riskService.getListingRiskEvents(listing.getId()));
        }

        Double avgRating = reviewRepository.getAverageRatingForListing(listing.getId());
        dto.setAverageRating(avgRating != null ? Math.round(avgRating * 10.0) / 10.0 : null);
        dto.setReviewCount(reviewRepository.countByListingId(listing.getId()));

        return dto;
    }
}
