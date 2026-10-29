package com.rentguard.risk;

import com.rentguard.listing.model.Listing;
import com.rentguard.listing.model.PropertyImage;
import com.rentguard.listing.repository.ListingRepository;
import com.rentguard.listing.repository.PropertyImageRepository;
import com.rentguard.property.model.Property;
import com.rentguard.property.model.PropertyType;
import com.rentguard.risk.service.DuplicateDetectionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DuplicateDetectionServiceTest {

    @Mock
    private ListingRepository listingRepository;

    @Mock
    private PropertyImageRepository propertyImageRepository;

    @InjectMocks
    private DuplicateDetectionService duplicateDetectionService;

    @Test
    @DisplayName("Address normalization should standardize abbreviations, lowercase and strip punctuation")
    void testNormalizeAddress() {
        String rawAddress = "Flat 402, Block-C, Green Valley Road, Sector 62.";
        String normalized = duplicateDetectionService.normalizeAddress(rawAddress);

        assertEquals("apt 402 blk c green valley rd sec 62", normalized);
    }

    @Test
    @DisplayName("Should generate deterministic SHA-256 hash for image bytes")
    void testCalculateSha256() {
        byte[] sampleBytes = "test_image_binary_data".getBytes(StandardCharsets.UTF_8);
        String hash1 = duplicateDetectionService.calculateSha256(sampleBytes);
        String hash2 = duplicateDetectionService.calculateSha256(sampleBytes);

        assertNotNull(hash1);
        assertEquals(64, hash1.length(), "SHA-256 must produce 64 hex characters");
        assertEquals(hash1, hash2, "Identical inputs must produce identical hashes");
    }

    @Test
    @DisplayName("Should detect duplicate image when same SHA-256 hash exists on another listing")
    void testCheckDuplicateImage_Found() {
        String testHash = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855";
        Long currentListingId = 10L;

        Listing otherListing = new Listing();
        otherListing.setId(5L);

        PropertyImage duplicateImg = new PropertyImage(otherListing, "img.jpg", testHash, true);
        when(propertyImageRepository.findDuplicateImages(testHash, currentListingId))
                .thenReturn(List.of(duplicateImg));

        DuplicateDetectionService.DuplicateImageCheckResult result =
                duplicateDetectionService.checkDuplicateImage(testHash, currentListingId);

        assertTrue(result.isDuplicate());
        assertEquals(5L, result.getMatchingListingId());
        assertTrue(result.getExplanation().contains("hash collision"));
    }

    @Test
    @DisplayName("Should detect duplicate property when nearby coordinates and same specs match")
    void testCheckDuplicateProperty_GeoAndSpecsMatch() {
        Property existingProp = new Property();
        existingProp.setAddress("Tower B, Flat 402, Sector 62");
        existingProp.setLatitude(28.6280);
        existingProp.setLongitude(77.3649);
        existingProp.setBedrooms(2);
        existingProp.setType(PropertyType.APARTMENT);

        Listing existingListing = new Listing();
        existingListing.setId(1L);
        existingListing.setProperty(existingProp);

        when(listingRepository.findAll()).thenReturn(List.of(existingListing));

        // New listing with coordinates ~10 meters away and same BHK/type
        Property newProp = new Property();
        newProp.setAddress("Tower B, Flat 402, Sec 62");
        newProp.setLatitude(28.62805);
        newProp.setLongitude(77.36492);
        newProp.setBedrooms(2);
        newProp.setType(PropertyType.APARTMENT);

        Listing newListing = new Listing();
        newListing.setId(2L);
        newListing.setProperty(newProp);

        DuplicateDetectionService.DuplicatePropertyCheckResult result =
                duplicateDetectionService.checkDuplicateProperty(newListing);

        assertTrue(result.isDuplicate(), "Should detect duplicate property");
        assertEquals(1L, result.getMatchingListingId());
    }
}
