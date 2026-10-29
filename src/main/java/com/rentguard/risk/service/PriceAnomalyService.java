package com.rentguard.risk.service;

import com.rentguard.listing.model.Listing;
import com.rentguard.listing.repository.ListingRepository;
import com.rentguard.property.model.Property;
import com.rentguard.risk.dto.PriceAnomalyResult;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class PriceAnomalyService {

    private final ListingRepository listingRepository;

    public PriceAnomalyService(ListingRepository listingRepository) {
        this.listingRepository = listingRepository;
    }

    public PriceAnomalyResult evaluatePriceAnomaly(Listing listing) {
        Property property = listing.getProperty();
        Double currentRent = listing.getRent();

        if (currentRent == null || currentRent <= 0) {
            return new PriceAnomalyResult(true, currentRent, 0.0, 100.0, 0,
                    "Invalid or zero rent detected");
        }

        // Find listings in same city, type, bedrooms
        List<Listing> allListings = listingRepository.findAll();
        List<Double> comparableRents = new ArrayList<>();

        for (Listing other : allListings) {
            // Exclude current listing
            if (other.getId() != null && other.getId().equals(listing.getId())) {
                continue;
            }
            Property p = other.getProperty();
            if (p == null) continue;

            // Match locality or city + bedrooms + type
            boolean sameCity = property.getCity() != null && property.getCity().equalsIgnoreCase(p.getCity());
            boolean sameLocality = property.getLocality() != null && property.getLocality().equalsIgnoreCase(p.getLocality());
            boolean sameType = property.getType() != null && property.getType().equals(p.getType());
            boolean sameBhk = property.getBedrooms() != null && property.getBedrooms().equals(p.getBedrooms());

            if (sameCity && sameBhk && sameType) {
                // If locality matches, strong match; else still comparable in same city
                comparableRents.add(other.getRent());
            }
        }

        // If fewer than 2 comparables found, fall back to city + bedrooms
        if (comparableRents.size() < 2) {
            for (Listing other : allListings) {
                if (other.getId() != null && other.getId().equals(listing.getId())) continue;
                Property p = other.getProperty();
                if (p != null && property.getCity() != null && property.getCity().equalsIgnoreCase(p.getCity())
                        && property.getBedrooms() != null && property.getBedrooms().equals(p.getBedrooms())) {
                    if (!comparableRents.contains(other.getRent())) {
                        comparableRents.add(other.getRent());
                    }
                }
            }
        }

        // If no comparables found in DB, return normal baseline
        if (comparableRents.isEmpty()) {
            return new PriceAnomalyResult(false, currentRent, currentRent, 0.0, 0,
                    "No comparable listings found in this locality yet. Price appears acceptable.");
        }

        Collections.sort(comparableRents);
        double medianRent = calculateMedian(comparableRents);

        double deviation = Math.abs(currentRent - medianRent) / medianRent * 100.0;

        // Large under-price deviation contributes risk (> 35% below median)
        // E.g. Spec example: median 20,000, listing 10,000 => deviation 50%
        if (currentRent < medianRent && deviation >= 35.0) {
            String explanation = String.format(
                    "Rent substantially below comparable listings. Listing rent (₹%,.0f) is %.1f%% below locality median (₹%,.0f) across %d comparable listings.",
                    currentRent, deviation, medianRent, comparableRents.size()
            );
            return new PriceAnomalyResult(true, currentRent, medianRent, deviation, comparableRents.size(), explanation);
        }

        String explanation = String.format(
                "Rent is within normal local range. Median rent: ₹%,.0f (deviation: %.1f%% across %d comparable listings).",
                medianRent, deviation, comparableRents.size()
        );
        return new PriceAnomalyResult(false, currentRent, medianRent, deviation, comparableRents.size(), explanation);
    }

    private double calculateMedian(List<Double> sortedValues) {
        int n = sortedValues.size();
        if (n % 2 == 1) {
            return sortedValues.get(n / 2);
        } else {
            return (sortedValues.get((n / 2) - 1) + sortedValues.get(n / 2)) / 2.0;
        }
    }
}
