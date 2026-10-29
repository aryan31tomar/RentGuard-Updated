package com.rentguard.admin.dto;

import com.rentguard.listing.dto.ListingResponseDto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminAnalyticsDto {
    private long totalListings;
    private long activeListings;
    private long verifiedOwners;
    private long pendingVerifications;
    private long highRiskListings;
    private long openReports;
    private long suspendedListings;
    private Map<String, Long> complaintCategories = new HashMap<>();
    private Map<String, Long> riskScoreDistribution = new HashMap<>();
    private List<ListingResponseDto> recentSuspiciousListings;

    public AdminAnalyticsDto() {}

    public long getTotalListings() {
        return totalListings;
    }

    public void setTotalListings(long totalListings) {
        this.totalListings = totalListings;
    }

    public long getActiveListings() {
        return activeListings;
    }

    public void setActiveListings(long activeListings) {
        this.activeListings = activeListings;
    }

    public long getVerifiedOwners() {
        return verifiedOwners;
    }

    public void setVerifiedOwners(long verifiedOwners) {
        this.verifiedOwners = verifiedOwners;
    }

    public long getPendingVerifications() {
        return pendingVerifications;
    }

    public void setPendingVerifications(long pendingVerifications) {
        this.pendingVerifications = pendingVerifications;
    }

    public long getHighRiskListings() {
        return highRiskListings;
    }

    public void setHighRiskListings(long highRiskListings) {
        this.highRiskListings = highRiskListings;
    }

    public long getOpenReports() {
        return openReports;
    }

    public void setOpenReports(long openReports) {
        this.openReports = openReports;
    }

    public long getSuspendedListings() {
        return suspendedListings;
    }

    public void setSuspendedListings(long suspendedListings) {
        this.suspendedListings = suspendedListings;
    }

    public Map<String, Long> getComplaintCategories() {
        return complaintCategories;
    }

    public void setComplaintCategories(Map<String, Long> complaintCategories) {
        this.complaintCategories = complaintCategories;
    }

    public Map<String, Long> getRiskScoreDistribution() {
        return riskScoreDistribution;
    }

    public void setRiskScoreDistribution(Map<String, Long> riskScoreDistribution) {
        this.riskScoreDistribution = riskScoreDistribution;
    }

    public List<ListingResponseDto> getRecentSuspiciousListings() {
        return recentSuspiciousListings;
    }

    public void setRecentSuspiciousListings(List<ListingResponseDto> recentSuspiciousListings) {
        this.recentSuspiciousListings = recentSuspiciousListings;
    }
}
