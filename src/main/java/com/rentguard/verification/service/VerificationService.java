package com.rentguard.verification.service;

import com.rentguard.admin.model.AuditLog;
import com.rentguard.admin.repository.AuditLogRepository;
import com.rentguard.common.exception.ResourceNotFoundException;
import com.rentguard.common.exception.UnauthorizedException;
import com.rentguard.listing.model.Listing;
import com.rentguard.listing.repository.ListingRepository;
import com.rentguard.notification.model.Notification;
import com.rentguard.notification.repository.NotificationRepository;
import com.rentguard.property.model.Property;
import com.rentguard.property.repository.PropertyRepository;
import com.rentguard.risk.service.RiskService;
import com.rentguard.user.model.Owner;
import com.rentguard.user.model.User;
import com.rentguard.user.model.VerificationStatus;
import com.rentguard.user.repository.OwnerRepository;
import com.rentguard.user.repository.UserRepository;
import com.rentguard.verification.dto.DocumentResponseDto;
import com.rentguard.verification.dto.DocumentUploadRequest;
import com.rentguard.verification.dto.VerificationDecisionDto;
import com.rentguard.verification.model.Document;
import com.rentguard.verification.model.DocumentStatus;
import com.rentguard.verification.repository.DocumentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class VerificationService {

    private final DocumentRepository documentRepository;
    private final OwnerRepository ownerRepository;
    private final UserRepository userRepository;
    private final PropertyRepository propertyRepository;
    private final ListingRepository listingRepository;
    private final RiskService riskService;
    private final NotificationRepository notificationRepository;
    private final AuditLogRepository auditLogRepository;

    public VerificationService(DocumentRepository documentRepository,
                               OwnerRepository ownerRepository,
                               UserRepository userRepository,
                               PropertyRepository propertyRepository,
                               ListingRepository listingRepository,
                               RiskService riskService,
                               NotificationRepository notificationRepository,
                               AuditLogRepository auditLogRepository) {
        this.documentRepository = documentRepository;
        this.ownerRepository = ownerRepository;
        this.userRepository = userRepository;
        this.propertyRepository = propertyRepository;
        this.listingRepository = listingRepository;
        this.riskService = riskService;
        this.notificationRepository = notificationRepository;
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional
    public DocumentResponseDto submitDocument(DocumentUploadRequest request, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userEmail));

        Owner owner = ownerRepository.findByUserId(user.getId())
                .orElseThrow(() -> new UnauthorizedException("Authenticated user is not registered as an Owner"));

        Property property = null;
        if (request.getPropertyId() != null) {
            property = propertyRepository.findById(request.getPropertyId())
                    .orElseThrow(() -> new ResourceNotFoundException("Property not found with id: " + request.getPropertyId()));
        }

        Document document = new Document();
        document.setOwner(owner);
        document.setProperty(property);
        document.setType(request.getType());
        document.setFilePath(request.getFilePath());
        document.setStatus(DocumentStatus.PENDING);
        document.setCreatedAt(LocalDateTime.now());

        document = documentRepository.save(document);

        // Advance Owner status: REGISTERED -> DOCUMENT_SUBMITTED -> UNDER_REVIEW
        owner.setVerificationStatus(VerificationStatus.UNDER_REVIEW);
        ownerRepository.save(owner);

        // Notify owner
        notificationRepository.save(new Notification(
                user,
                "Your verification document (" + request.getType() + ") has been submitted for admin review.",
                "VERIFICATION"
        ));

        return DocumentResponseDto.fromEntity(document);
    }

    public List<DocumentResponseDto> getPendingVerifications() {
        return documentRepository.findByStatus(DocumentStatus.PENDING).stream()
                .map(DocumentResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    public List<DocumentResponseDto> getMyDocuments(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Owner owner = ownerRepository.findByUserId(user.getId())
                .orElseThrow(() -> new UnauthorizedException("Owner profile not found"));

        return documentRepository.findByOwnerId(owner.getId()).stream()
                .map(DocumentResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public DocumentResponseDto processDecision(Long documentId, VerificationDecisionDto decision, String adminEmail) {
        User admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found: " + adminEmail));

        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found with id: " + documentId));

        document.setStatus(decision.getStatus());
        document.setReviewedBy(admin.getEmail());
        document.setRejectionReason(decision.getStatus() == DocumentStatus.REJECTED ? decision.getNotes() : null);
        document.setReviewedAt(LocalDateTime.now());
        document = documentRepository.save(document);

        Owner owner = document.getOwner();
        if (owner != null) {
            if (decision.getStatus() == DocumentStatus.APPROVED) {
                owner.setVerificationStatus(VerificationStatus.VERIFIED);
                owner.setTrustScore(Math.min(100, owner.getTrustScore() + 30));
                owner.setVerificationNotes(decision.getNotes());
                ownerRepository.save(owner);

                // Notify Owner
                if (owner.getUser() != null) {
                    notificationRepository.save(new Notification(
                            owner.getUser(),
                            "Congratulations! Your verification document (" + document.getType() + ") was approved. Your profile is now VERIFIED.",
                            "VERIFICATION"
                    ));
                }

                // If property document approved, verify property
                if (document.getProperty() != null) {
                    document.getProperty().setVerificationStatus(VerificationStatus.VERIFIED);
                    propertyRepository.save(document.getProperty());
                }
            } else {
                owner.setVerificationStatus(VerificationStatus.REJECTED);
                owner.setVerificationNotes(decision.getNotes());
                ownerRepository.save(owner);

                if (owner.getUser() != null) {
                    notificationRepository.save(new Notification(
                            owner.getUser(),
                            "Your verification document was rejected. Reason: " + decision.getNotes(),
                            "VERIFICATION"
                    ));
                }
            }

            // Re-evaluate risk for all listings of this owner!
            List<Listing> ownerListings = listingRepository.findByProperty_Owner_Id(owner.getId());
            for (Listing listing : ownerListings) {
                riskService.evaluateListingRisk(listing);
            }
        }

        // Audit log
        auditLogRepository.save(new AuditLog(
                admin.getId(), admin.getEmail(),
                "VERIFICATION_DECISION", "DOCUMENT", document.getId(),
                "Document #" + document.getId() + " decided as " + decision.getStatus() + ". Notes: " + decision.getNotes()
        ));

        return DocumentResponseDto.fromEntity(document);
    }
}
