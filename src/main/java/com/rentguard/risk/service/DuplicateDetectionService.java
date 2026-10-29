package com.rentguard.risk.service;

import com.rentguard.listing.model.Listing;
import com.rentguard.listing.model.PropertyImage;
import com.rentguard.listing.repository.ListingRepository;
import com.rentguard.listing.repository.PropertyImageRepository;
import com.rentguard.property.model.Property;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

@Service
public class DuplicateDetectionService {

    private final ListingRepository listingRepository;
    private final PropertyImageRepository propertyImageRepository;

    public DuplicateDetectionService(ListingRepository listingRepository,
                                     PropertyImageRepository propertyImageRepository) {
        this.listingRepository = listingRepository;
        this.propertyImageRepository = propertyImageRepository;
    }

    /**
     * Normalizes an address for duplicate detection:
     * lowercase, strip punctuation, standardize common terms.
     */
    public String normalizeAddress(String address) {
        if (address == null) return "";
        String normalized = address.toLowerCase().trim();
        // Replace common words with standard tokens
        normalized = normalized.replaceAll("\\b(street|str)\\b", "st");
        normalized = normalized.replaceAll("\\b(road|rd\\.)\\b", "rd");
        normalized = normalized.replaceAll("\\b(apartment|apt\\.|flat)\\b", "apt");
        normalized = normalized.replaceAll("\\b(floor|fl\\.)\\b", "fl");
        normalized = normalized.replaceAll("\\b(sector|sec\\.)\\b", "sec");
        normalized = normalized.replaceAll("\\b(block|blk\\.)\\b", "blk");
        normalized = normalized.replaceAll("\\b(avenue|ave\\.)\\b", "ave");
        // Remove punctuation and extra whitespace
        normalized = normalized.replaceAll("[^a-z0-9\\s]", " ");
        normalized = normalized.replaceAll("\\s+", " ").trim();
        return normalized;
    }

    /**
     * Calculates distance in kilometers between two geo-coordinates using Haversine formula.
     */
    public double calculateDistanceKm(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Radius of the Earth in km
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    /**
     * Calculates Levenshtein Similarity Ratio (0.0 to 1.0).
     */
    public double calculateStringSimilarity(String s1, String s2) {
        if (s1 == null || s2 == null) return 0.0;
        if (s1.equals(s2)) return 1.0;
        int longer = Math.max(s1.length(), s2.length());
        if (longer == 0) return 1.0;
        int editDist = computeLevenshteinDistance(s1, s2);
        return (longer - editDist) / (double) longer;
    }

    private int computeLevenshteinDistance(String s1, String s2) {
        int[] costs = new int[s2.length() + 1];
        for (int i = 0; i <= s1.length(); i++) {
            int lastValue = i;
            for (int j = 0; j <= s2.length(); j++) {
                if (i == 0) {
                    costs[j] = j;
                } else {
                    if (j > 0) {
                        int newValue = costs[j - 1];
                        if (s1.charAt(i - 1) != s2.charAt(j - 1)) {
                            newValue = Math.min(Math.min(newValue, lastValue), costs[j]) + 1;
                        }
                        costs[j - 1] = lastValue;
                        lastValue = newValue;
                    }
                }
            }
            if (i > 0) costs[s2.length()] = lastValue;
        }
        return costs[s2.length()];
    }

    /**
     * Checks if listing has duplicate properties elsewhere in database.
     */
    public DuplicatePropertyCheckResult checkDuplicateProperty(Listing listing) {
        Property property = listing.getProperty();
        if (property == null) {
            return new DuplicatePropertyCheckResult(false, "No property data available");
        }

        String normCurrent = normalizeAddress(property.getAddress());
        List<Listing> allListings = listingRepository.findAll();

        for (Listing other : allListings) {
            if (other.getId() != null && other.getId().equals(listing.getId())) {
                continue;
            }
            Property otherProp = other.getProperty();
            if (otherProp == null) continue;

            // Check 1: Address string similarity
            String normOther = normalizeAddress(otherProp.getAddress());
            double addressSimilarity = calculateStringSimilarity(normCurrent, normOther);

            // Check 2: Geo distance tolerance (< 150m)
            double distanceKm = calculateDistanceKm(
                    property.getLatitude(), property.getLongitude(),
                    otherProp.getLatitude(), otherProp.getLongitude()
            );

            boolean sameBhk = property.getBedrooms() != null && property.getBedrooms().equals(otherProp.getBedrooms());
            boolean sameType = property.getType() != null && property.getType().equals(otherProp.getType());

            // If addresses match > 85% or coordinates within 150m with same BHK & property type
            if ((addressSimilarity >= 0.85 || distanceKm <= 0.15) && sameBhk && sameType) {
                String explanation = String.format(
                        "Potential duplicate property detected. Matches listing #%d (%s) with %.0f%% address similarity and %.0fm distance.",
                        other.getId(), otherProp.getAddress(), addressSimilarity * 100, distanceKm * 1000
                );
                return new DuplicatePropertyCheckResult(true, explanation, other.getId());
            }
        }

        return new DuplicatePropertyCheckResult(false, "No duplicate property detected across active database.");
    }

    /**
     * Calculates SHA-256 hash for byte array using MessageDigest.
     */
    public String calculateSha256(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedHash = digest.digest(data);
            StringBuilder hexString = new StringBuilder();
            for (byte b : encodedHash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    /**
     * Checks if image hash exists on another listing.
     */
    public DuplicateImageCheckResult checkDuplicateImage(String imageHash, Long currentListingId) {
        if (imageHash == null || imageHash.isBlank()) {
            return new DuplicateImageCheckResult(false, "No image hash provided");
        }

        List<PropertyImage> duplicates = propertyImageRepository.findDuplicateImages(
                imageHash,
                currentListingId != null ? currentListingId : -1L
        );

        if (!duplicates.isEmpty()) {
            PropertyImage matched = duplicates.get(0);
            Long matchedListingId = matched.getListing() != null ? matched.getListing().getId() : null;
            String explanation = String.format(
                    "Image hash collision (SHA-256: %s...): identical image is already published on listing #%d.",
                    imageHash.substring(0, Math.min(12, imageHash.length())),
                    matchedListingId
            );
            return new DuplicateImageCheckResult(true, explanation, matchedListingId);
        }

        return new DuplicateImageCheckResult(false, "No duplicate image detected.");
    }

    public static class DuplicatePropertyCheckResult {
        private final boolean duplicate;
        private final String explanation;
        private final Long matchingListingId;

        public DuplicatePropertyCheckResult(boolean duplicate, String explanation) {
            this(duplicate, explanation, null);
        }

        public DuplicatePropertyCheckResult(boolean duplicate, String explanation, Long matchingListingId) {
            this.duplicate = duplicate;
            this.explanation = explanation;
            this.matchingListingId = matchingListingId;
        }

        public boolean isDuplicate() {
            return duplicate;
        }

        public String getExplanation() {
            return explanation;
        }

        public Long getMatchingListingId() {
            return matchingListingId;
        }
    }

    public static class DuplicateImageCheckResult {
        private final boolean duplicate;
        private final String explanation;
        private final Long matchingListingId;

        public DuplicateImageCheckResult(boolean duplicate, String explanation) {
            this(duplicate, explanation, null);
        }

        public DuplicateImageCheckResult(boolean duplicate, String explanation, Long matchingListingId) {
            this.duplicate = duplicate;
            this.explanation = explanation;
            this.matchingListingId = matchingListingId;
        }

        public boolean isDuplicate() {
            return duplicate;
        }

        public String getExplanation() {
            return explanation;
        }

        public Long getMatchingListingId() {
            return matchingListingId;
        }
    }
}
