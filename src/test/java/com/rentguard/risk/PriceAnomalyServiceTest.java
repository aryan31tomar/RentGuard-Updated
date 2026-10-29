package com.rentguard.risk;

import com.rentguard.listing.model.Listing;
import com.rentguard.listing.repository.ListingRepository;
import com.rentguard.property.model.Property;
import com.rentguard.property.model.PropertyType;
import com.rentguard.risk.dto.PriceAnomalyResult;
import com.rentguard.risk.service.PriceAnomalyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PriceAnomalyServiceTest {

    @Mock
    private ListingRepository listingRepository;

    @InjectMocks
    private PriceAnomalyService priceAnomalyService;

    private List<Listing> mockListings;

    @BeforeEach
    void setUp() {
        mockListings = new ArrayList<>();

        // Create comparable listings in Sector 62, Noida, 2 BHK, APARTMENT
        // Comparable rents: 18000, 20000, 22000 -> Median = 20000
        mockListings.add(createListing(1L, "Sector 62", "Noida", 2, PropertyType.APARTMENT, 18000.0));
        mockListings.add(createListing(2L, "Sector 62", "Noida", 2, PropertyType.APARTMENT, 20000.0));
        mockListings.add(createListing(3L, "Sector 62", "Noida", 2, PropertyType.APARTMENT, 22000.0));
    }

    private Listing createListing(Long id, String locality, String city, int bhk, PropertyType type, double rent) {
        Property property = new Property();
        property.setLocality(locality);
        property.setCity(city);
        property.setBedrooms(bhk);
        property.setType(type);

        Listing listing = new Listing();
        listing.setId(id);
        listing.setProperty(property);
        listing.setRent(rent);
        return listing;
    }

    @Test
    @DisplayName("Should detect severe under-price anomaly when rent is 50% below median (as per spec example)")
    void testPriceAnomalyDetected_Underpriced() {
        when(listingRepository.findAll()).thenReturn(mockListings);

        // Listing with rent 10,000 when median is 20,000 (Deviation = 50% >= 35%)
        Listing scamListing = createListing(99L, "Sector 62", "Noida", 2, PropertyType.APARTMENT, 10000.0);

        PriceAnomalyResult result = priceAnomalyService.evaluatePriceAnomaly(scamListing);

        assertTrue(result.isAnomalyDetected(), "Should detect under-price anomaly");
        assertEquals(20000.0, result.getMedianRent(), 0.01);
        assertEquals(50.0, result.getDeviationPercentage(), 0.01);
        assertTrue(result.getExplanation().contains("substantially below"));
    }

    @Test
    @DisplayName("Should not flag price anomaly when rent is within normal range")
    void testPriceAnomalyNormal_RentWithinRange() {
        when(listingRepository.findAll()).thenReturn(mockListings);

        // Listing with rent 19,000 when median is 20,000 (Deviation = 5%)
        Listing normalListing = createListing(99L, "Sector 62", "Noida", 2, PropertyType.APARTMENT, 19000.0);

        PriceAnomalyResult result = priceAnomalyService.evaluatePriceAnomaly(normalListing);

        assertFalse(result.isAnomalyDetected(), "Normal price should not be flagged as anomaly");
        assertEquals(20000.0, result.getMedianRent(), 0.01);
        assertEquals(5.0, result.getDeviationPercentage(), 0.01);
        assertTrue(result.getExplanation().contains("within normal local range"));
    }
}
