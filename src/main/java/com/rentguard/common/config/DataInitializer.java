package com.rentguard.common.config;

import com.rentguard.listing.model.Listing;
import com.rentguard.listing.model.ListingStatus;
import com.rentguard.listing.model.PropertyImage;
import com.rentguard.listing.repository.ListingRepository;
import com.rentguard.listing.repository.PropertyImageRepository;
import com.rentguard.property.model.FurnishingStatus;
import com.rentguard.property.model.Property;
import com.rentguard.property.model.PropertyType;
import com.rentguard.property.repository.PropertyRepository;
import com.rentguard.report.model.Report;
import com.rentguard.report.model.ReportReason;
import com.rentguard.report.model.ReportStatus;
import com.rentguard.report.repository.ReportRepository;
import com.rentguard.review.model.Review;
import com.rentguard.review.repository.ReviewRepository;
import com.rentguard.risk.service.DuplicateDetectionService;
import com.rentguard.risk.service.RiskService;
import com.rentguard.user.model.Owner;
import com.rentguard.user.model.Role;
import com.rentguard.user.model.User;
import com.rentguard.user.model.VerificationStatus;
import com.rentguard.user.repository.OwnerRepository;
import com.rentguard.user.repository.UserRepository;
import com.rentguard.verification.model.Document;
import com.rentguard.verification.model.DocumentStatus;
import com.rentguard.verification.model.DocumentType;
import com.rentguard.verification.repository.DocumentRepository;
import com.rentguard.visit.model.VisitRequest;
import com.rentguard.visit.model.VisitStatus;
import com.rentguard.visit.repository.VisitRequestRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final OwnerRepository ownerRepository;
    private final PropertyRepository propertyRepository;
    private final ListingRepository listingRepository;
    private final PropertyImageRepository propertyImageRepository;
    private final DocumentRepository documentRepository;
    private final ReportRepository reportRepository;
    private final ReviewRepository reviewRepository;
    private final VisitRequestRepository visitRequestRepository;
    private final RiskService riskService;
    private final DuplicateDetectionService duplicateDetectionService;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           OwnerRepository ownerRepository,
                           PropertyRepository propertyRepository,
                           ListingRepository listingRepository,
                           PropertyImageRepository propertyImageRepository,
                           DocumentRepository documentRepository,
                           ReportRepository reportRepository,
                           ReviewRepository reviewRepository,
                           VisitRequestRepository visitRequestRepository,
                           RiskService riskService,
                           DuplicateDetectionService duplicateDetectionService,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.ownerRepository = ownerRepository;
        this.propertyRepository = propertyRepository;
        this.listingRepository = listingRepository;
        this.propertyImageRepository = propertyImageRepository;
        this.documentRepository = documentRepository;
        this.reportRepository = reportRepository;
        this.reviewRepository = reviewRepository;
        this.visitRequestRepository = visitRequestRepository;
        this.riskService = riskService;
        this.duplicateDetectionService = duplicateDetectionService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            return; // Seed data already loaded
        }

        System.out.println(">>> Initializing RentGuard Demo Seed Data...");

        // 1. Users
        String encodedPassword = passwordEncoder.encode("Password123!");

        User adminUser = new User("Platform Administrator", "admin@rentguard.com", encodedPassword, "+91 98765 00001", Role.ROLE_ADMIN);
        userRepository.save(adminUser);

        User verifiedOwnerUser = new User("Rahul Verma (Verified Owner)", "owner@rentguard.com", encodedPassword, "+91 98765 00002", Role.ROLE_OWNER);
        userRepository.save(verifiedOwnerUser);

        User unverifiedOwnerUser = new User("Vikram Singh (Unverified)", "vikram@rentguard.com", encodedPassword, "+91 98765 00003", Role.ROLE_OWNER);
        userRepository.save(unverifiedOwnerUser);

        User tenant1 = new User("Priya Sharma (Tenant)", "tenant@rentguard.com", encodedPassword, "+91 98765 00004", Role.ROLE_TENANT);
        userRepository.save(tenant1);

        User tenant2 = new User("Amit Kumar", "amit@rentguard.com", encodedPassword, "+91 98765 00005", Role.ROLE_TENANT);
        userRepository.save(tenant2);

        // 2. Owners
        Owner verifiedOwner = new Owner(verifiedOwnerUser);
        verifiedOwner.setVerificationStatus(VerificationStatus.VERIFIED);
        verifiedOwner.setTrustScore(95);
        verifiedOwner.setVerificationNotes("Government ID and Electricity bill verified by Admin");
        ownerRepository.save(verifiedOwner);

        Owner unverifiedOwner = new Owner(unverifiedOwnerUser);
        unverifiedOwner.setVerificationStatus(VerificationStatus.REGISTERED);
        unverifiedOwner.setTrustScore(40);
        unverifiedOwner.setVerificationNotes("No verification documents submitted yet");
        ownerRepository.save(unverifiedOwner);

        // 3. Documents for Verified Owner
        Document doc1 = new Document();
        doc1.setOwner(verifiedOwner);
        doc1.setType(DocumentType.GOVERNMENT_ID);
        doc1.setFilePath("aadhaar_rahul_verma.pdf");
        doc1.setStatus(DocumentStatus.APPROVED);
        doc1.setReviewedBy("admin@rentguard.com");
        doc1.setReviewedAt(LocalDateTime.now().minusDays(10));
        documentRepository.save(doc1);

        Document doc2 = new Document();
        doc2.setOwner(verifiedOwner);
        doc2.setType(DocumentType.PROPERTY_TAX_RECEIPT);
        doc2.setFilePath("tax_receipt_sec62_noida.pdf");
        doc2.setStatus(DocumentStatus.APPROVED);
        doc2.setReviewedBy("admin@rentguard.com");
        doc2.setReviewedAt(LocalDateTime.now().minusDays(9));
        documentRepository.save(doc2);

        // Document for Unverified Owner (Pending in admin verification queue for demo!)
        Document docPending = new Document();
        docPending.setOwner(unverifiedOwner);
        docPending.setType(DocumentType.GOVERNMENT_ID);
        docPending.setFilePath("kyc_vikram_passport.pdf");
        docPending.setStatus(DocumentStatus.PENDING);
        documentRepository.save(docPending);

        // 4. Properties
        // Property 1: Example Property from Page 4 Specification
        Property p1 = new Property();
        p1.setOwner(verifiedOwner);
        p1.setAddress("Tower B, Flat 402, Green Valley Apartments, Sector 62");
        p1.setLocality("Sector 62");
        p1.setCity("Noida");
        p1.setLatitude(28.6280);
        p1.setLongitude(77.3649);
        p1.setType(PropertyType.APARTMENT);
        p1.setBedrooms(2);
        p1.setArea(1150.0);
        p1.setFurnishing(FurnishingStatus.SEMI_FURNISHED);
        p1.setVerificationStatus(VerificationStatus.VERIFIED);
        p1.setNormalizedAddress(duplicateDetectionService.normalizeAddress(p1.getAddress()));
        propertyRepository.save(p1);

        // Property 2: Comparable 2 BHK in Sector 62
        Property p2 = new Property();
        p2.setOwner(verifiedOwner);
        p2.setAddress("House 105, Block C, Stellar Park, Sector 62");
        p2.setLocality("Sector 62");
        p2.setCity("Noida");
        p2.setLatitude(28.6295);
        p2.setLongitude(77.3670);
        p2.setType(PropertyType.APARTMENT);
        p2.setBedrooms(2);
        p2.setArea(1200.0);
        p2.setFurnishing(FurnishingStatus.FULLY_FURNISHED);
        p2.setVerificationStatus(VerificationStatus.VERIFIED);
        p2.setNormalizedAddress(duplicateDetectionService.normalizeAddress(p2.getAddress()));
        propertyRepository.save(p2);

        // Property 3: Another comparable in Sector 62
        Property p3 = new Property();
        p3.setOwner(verifiedOwner);
        p3.setAddress("Flat 201, Royal Heights, Sector 62");
        p3.setLocality("Sector 62");
        p3.setCity("Noida");
        p3.setLatitude(28.6250);
        p3.setLongitude(77.3620);
        p3.setType(PropertyType.APARTMENT);
        p3.setBedrooms(2);
        p3.setArea(1100.0);
        p3.setFurnishing(FurnishingStatus.SEMI_FURNISHED);
        p3.setVerificationStatus(VerificationStatus.VERIFIED);
        p3.setNormalizedAddress(duplicateDetectionService.normalizeAddress(p3.getAddress()));
        propertyRepository.save(p3);

        // Property 4: Suspect Property by Vikram (Price anomaly scam)
        Property p4 = new Property();
        p4.setOwner(unverifiedOwner);
        p4.setAddress("A-12, Near Metro Station, Sector 62");
        p4.setLocality("Sector 62");
        p4.setCity("Noida");
        p4.setLatitude(28.6275);
        p4.setLongitude(77.3640);
        p4.setType(PropertyType.APARTMENT);
        p4.setBedrooms(2);
        p4.setArea(1150.0);
        p4.setFurnishing(FurnishingStatus.FULLY_FURNISHED);
        p4.setVerificationStatus(VerificationStatus.REGISTERED);
        p4.setNormalizedAddress(duplicateDetectionService.normalizeAddress(p4.getAddress()));
        propertyRepository.save(p4);

        // Property 5: Duplicate listing attempt of Property 1 (Duplicate address & coordinates)
        Property p5 = new Property();
        p5.setOwner(unverifiedOwner);
        p5.setAddress("Tower B, Apt 402, Green Valley Apts, Sec 62");
        p5.setLocality("Sector 62");
        p5.setCity("Noida");
        p5.setLatitude(28.6281);
        p5.setLongitude(77.3648);
        p5.setType(PropertyType.APARTMENT);
        p5.setBedrooms(2);
        p5.setArea(1150.0);
        p5.setFurnishing(FurnishingStatus.SEMI_FURNISHED);
        p5.setVerificationStatus(VerificationStatus.REGISTERED);
        p5.setNormalizedAddress(duplicateDetectionService.normalizeAddress(p5.getAddress()));
        propertyRepository.save(p5);

        // Property 6: 3 BHK in Bengaluru (Medium Risk)
        Property p6 = new Property();
        p6.setOwner(unverifiedOwner);
        p6.setAddress("12th Main, 100ft Road, Indiranagar");
        p6.setLocality("Indiranagar");
        p6.setCity("Bengaluru");
        p6.setLatitude(12.9784);
        p6.setLongitude(77.6408);
        p6.setType(PropertyType.APARTMENT);
        p6.setBedrooms(3);
        p6.setArea(1650.0);
        p6.setFurnishing(FurnishingStatus.SEMI_FURNISHED);
        p6.setVerificationStatus(VerificationStatus.REGISTERED);
        p6.setNormalizedAddress(duplicateDetectionService.normalizeAddress(p6.getAddress()));
        propertyRepository.save(p6);

        // 5. Listings
        // Listing 1: Exactly matches Page 4 specification (₹18,000/month, 2 BHK, Low Risk 22/100)
        Listing l1 = new Listing();
        l1.setProperty(p1);
        l1.setRent(18000.0);
        l1.setDeposit(36000.0);
        l1.setDescription("Spacious, well-ventilated 2 BHK apartment in Sector 62 Noida. Fully modular kitchen, 24/7 security, power backup, covered parking, and prime metro connectivity.");
        l1.setStatus(ListingStatus.ACTIVE);
        l1 = listingRepository.save(l1);

        String sharedImage1 = "https://images.unsplash.com/photo-1545324418-cc1a3fa10c00?w=800&auto=format&fit=crop";
        String sharedImageHash = duplicateDetectionService.calculateSha256(sharedImage1.getBytes(StandardCharsets.UTF_8));
        PropertyImage img1 = new PropertyImage(l1, sharedImage1, sharedImageHash, true);
        propertyImageRepository.save(img1);

        // Listing 2: Comparable 2 BHK (₹20,000/month)
        Listing l2 = new Listing();
        l2.setProperty(p2);
        l2.setRent(20000.0);
        l2.setDeposit(40000.0);
        l2.setDescription("Premium 2 BHK in Stellar Park Sector 62. Park-facing balcony, wooden flooring, close to tech parks and schools.");
        l2.setStatus(ListingStatus.ACTIVE);
        l2 = listingRepository.save(l2);

        String imgUrl2 = "https://images.unsplash.com/photo-1502672260266-1c1ef2d93688?w=800&auto=format&fit=crop";
        PropertyImage img2 = new PropertyImage(l2, imgUrl2, duplicateDetectionService.calculateSha256(imgUrl2.getBytes(StandardCharsets.UTF_8)), true);
        propertyImageRepository.save(img2);

        // Listing 3: Another comparable (₹19,000/month)
        Listing l3 = new Listing();
        l3.setProperty(p3);
        l3.setRent(19000.0);
        l3.setDeposit(38000.0);
        l3.setDescription("Comfortable 2 BHK in Royal Heights Sector 62. High-speed lift, gated community with gym and clubhouse.");
        l3.setStatus(ListingStatus.ACTIVE);
        l3 = listingRepository.save(l3);

        String imgUrl3 = "https://images.unsplash.com/photo-1560448204-e02f11c3d0e2?w=800&auto=format&fit=crop";
        PropertyImage img3 = new PropertyImage(l3, imgUrl3, duplicateDetectionService.calculateSha256(imgUrl3.getBytes(StandardCharsets.UTF_8)), true);
        propertyImageRepository.save(img3);

        // Listing 4: High Risk — Price Anomaly Scam (₹6,500/month when median is ₹19,000!)
        Listing l4 = new Listing();
        l4.setProperty(p4);
        l4.setRent(6500.0); // 65% below median ₹19,000
        l4.setDeposit(2000.0); // Suspiciously low deposit
        l4.setDescription("URGENT RENTAL: Luxurious 2 BHK fully furnished apartment at throwaway price! Owner relocating abroad immediately. First-come first-served, token deposit required to book slot.");
        l4.setStatus(ListingStatus.ACTIVE);
        l4 = listingRepository.save(l4);

        String imgUrl4 = "https://images.unsplash.com/photo-1522708323590-d24dbb6b0267?w=800&auto=format&fit=crop";
        PropertyImage img4 = new PropertyImage(l4, imgUrl4, duplicateDetectionService.calculateSha256(imgUrl4.getBytes(StandardCharsets.UTF_8)), true);
        propertyImageRepository.save(img4);

        // Listing 5: High Risk — Duplicate Image Reuse & Duplicate Property
        Listing l5 = new Listing();
        l5.setProperty(p5);
        l5.setRent(16500.0);
        l5.setDeposit(33000.0);
        l5.setDescription("Stolen listing copy: Re-listed by unauthorized third-party broker copying Property 1 details.");
        l5.setStatus(ListingStatus.ACTIVE);
        l5 = listingRepository.save(l5);

        // Intentionally reuse sharedImageHash from Listing 1 to demonstrate SHA-256 duplicate image detection!
        PropertyImage img5 = new PropertyImage(l5, sharedImage1, sharedImageHash, true);
        propertyImageRepository.save(img5);

        // Listing 6: Indiranagar 3 BHK (Medium Risk)
        Listing l6 = new Listing();
        l6.setProperty(p6);
        l6.setRent(38000.0);
        l6.setDeposit(100000.0);
        l6.setDescription("Chic 3 BHK flat in Indiranagar Bengaluru. Prime location with vibrant nightlife and metro accessibility.");
        l6.setStatus(ListingStatus.ACTIVE);
        l6 = listingRepository.save(l6);

        String imgUrl6 = "https://images.unsplash.com/photo-1600585154340-be6161a56a0c?w=800&auto=format&fit=crop";
        PropertyImage img6 = new PropertyImage(l6, imgUrl6, duplicateDetectionService.calculateSha256(imgUrl6.getBytes(StandardCharsets.UTF_8)), true);
        propertyImageRepository.save(img6);

        // 6. Reports (Tenants report the scam listing l4)
        Report r1 = new Report(l4, tenant1, ReportReason.PRICE_SCAM,
                "Listing rent is absurdly low (₹6,500 vs normal ₹19,000). When I called, owner asked for ₹2,000 advance Google Pay before viewing.");
        reportRepository.save(r1);

        Report r2 = new Report(l4, tenant2, ReportReason.ADVANCE_FEE_FRAUD,
                "Owner claimed to be an army officer posted in Ladakh and insisted on urgent security deposit transfer before key dispatch.");
        reportRepository.save(r2);

        // Report on duplicate listing l5
        Report r3 = new Report(l5, tenant1, ReportReason.DUPLICATE_LISTING,
                "Identical flat photos and address copied from Rahul Verma's verified listing #1.");
        reportRepository.save(r3);

        // 7. Reviews for Listing 1
        Review rev1 = new Review(l1, tenant1, 5,
                "Visited the property in Sector 62. Rahul is a verified owner and showed all original registry papers. Clean transaction!");
        reviewRepository.save(rev1);

        Review rev2 = new Review(l1, tenant2, 4,
                "Very genuine property matching the description. Verification badge gave immense confidence.");
        reviewRepository.save(rev2);

        // 8. Visit Requests
        VisitRequest v1 = new VisitRequest(l1, tenant1, LocalDate.now().plusDays(2), "Looking to move next month. Available on Saturday morning.");
        v1.setStatus(VisitStatus.ACCEPTED);
        v1.setOwnerResponse("Confirmed! Please visit between 11 AM - 1 PM. Contact me on phone when at the gate.");
        visitRequestRepository.save(v1);

        VisitRequest v2 = new VisitRequest(l2, tenant2, LocalDate.now().plusDays(4), "Need to inspect parking and modular kitchen.");
        v2.setStatus(VisitStatus.PENDING);
        visitRequestRepository.save(v2);

        // 9. Run Risk Engine on All Listings to populate risk_events table
        riskService.evaluateListingRisk(l1);
        riskService.evaluateListingRisk(l2);
        riskService.evaluateListingRisk(l3);
        riskService.evaluateListingRisk(l4);
        riskService.evaluateListingRisk(l5);
        riskService.evaluateListingRisk(l6);

        System.out.println(">>> RentGuard Demo Seed Data successfully loaded!");
    }
}
