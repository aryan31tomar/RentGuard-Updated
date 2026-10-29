package com.rentguard.visit.service;

import com.rentguard.common.exception.ResourceNotFoundException;
import com.rentguard.common.exception.UnauthorizedException;
import com.rentguard.listing.model.Listing;
import com.rentguard.listing.repository.ListingRepository;
import com.rentguard.notification.model.Notification;
import com.rentguard.notification.repository.NotificationRepository;
import com.rentguard.user.model.Role;
import com.rentguard.user.model.User;
import com.rentguard.user.repository.UserRepository;
import com.rentguard.visit.dto.VisitRequestDto;
import com.rentguard.visit.dto.VisitResponseDto;
import com.rentguard.visit.dto.VisitStatusUpdateDto;
import com.rentguard.visit.model.VisitRequest;
import com.rentguard.visit.model.VisitStatus;
import com.rentguard.visit.repository.VisitRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class VisitService {

    private final VisitRequestRepository visitRequestRepository;
    private final ListingRepository listingRepository;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;

    public VisitService(VisitRequestRepository visitRequestRepository,
                        ListingRepository listingRepository,
                        UserRepository userRepository,
                        NotificationRepository notificationRepository) {
        this.visitRequestRepository = visitRequestRepository;
        this.listingRepository = listingRepository;
        this.userRepository = userRepository;
        this.notificationRepository = notificationRepository;
    }

    @Transactional
    public VisitResponseDto createVisitRequest(Long listingId, VisitRequestDto request, String userEmail) {
        User tenant = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new ResourceNotFoundException("Listing not found with id: " + listingId));

        VisitRequest visit = new VisitRequest(listing, tenant, request.getRequestedDate(), request.getNotes());
        visit = visitRequestRepository.save(visit);

        // Notify property owner
        if (listing.getProperty() != null && listing.getProperty().getOwner() != null
                && listing.getProperty().getOwner().getUser() != null) {
            notificationRepository.save(new Notification(
                    listing.getProperty().getOwner().getUser(),
                    "New property visit requested by " + tenant.getName() + " for listing #" + listing.getId() + " on " + request.getRequestedDate(),
                    "VISIT_REQUEST"
            ));
        }

        return VisitResponseDto.fromEntity(visit);
    }

    public List<VisitResponseDto> getTenantVisits(String userEmail) {
        User tenant = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return visitRequestRepository.findByTenantIdOrderByRequestedDateDesc(tenant.getId()).stream()
                .map(VisitResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    public List<VisitResponseDto> getOwnerVisits(String userEmail) {
        User owner = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return visitRequestRepository.findByListing_Property_Owner_User_IdOrderByRequestedDateDesc(owner.getId()).stream()
                .map(VisitResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public VisitResponseDto updateVisitStatus(Long visitId, VisitStatusUpdateDto update, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        VisitRequest visit = visitRequestRepository.findById(visitId)
                .orElseThrow(() -> new ResourceNotFoundException("Visit request not found with id: " + visitId));

        // Ensure user is owner of the listing or admin
        boolean isOwner = visit.getListing().getProperty().getOwner().getUser().getId().equals(user.getId());
        boolean isTenant = visit.getTenant().getId().equals(user.getId());
        boolean isAdmin = user.getRole() == Role.ROLE_ADMIN;

        if (!isOwner && !isTenant && !isAdmin) {
            throw new UnauthorizedException("You do not have permission to update this visit request");
        }

        visit.setStatus(update.getStatus());
        if (update.getResponseNotes() != null) {
            visit.setOwnerResponse(update.getResponseNotes());
        }
        visit = visitRequestRepository.save(visit);

        // Notify tenant
        notificationRepository.save(new Notification(
                visit.getTenant(),
                "Your visit request for listing #" + visit.getListing().getId() + " has been updated to: " + update.getStatus(),
                "VISIT_UPDATE"
        ));

        return VisitResponseDto.fromEntity(visit);
    }
}
