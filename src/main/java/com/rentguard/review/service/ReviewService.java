package com.rentguard.review.service;

import com.rentguard.common.exception.ResourceNotFoundException;
import com.rentguard.listing.model.Listing;
import com.rentguard.listing.repository.ListingRepository;
import com.rentguard.review.dto.ReviewRequestDto;
import com.rentguard.review.dto.ReviewResponseDto;
import com.rentguard.review.model.Review;
import com.rentguard.review.repository.ReviewRepository;
import com.rentguard.user.model.User;
import com.rentguard.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ListingRepository listingRepository;
    private final UserRepository userRepository;

    public ReviewService(ReviewRepository reviewRepository,
                         ListingRepository listingRepository,
                         UserRepository userRepository) {
        this.reviewRepository = reviewRepository;
        this.listingRepository = listingRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public ReviewResponseDto createReview(Long listingId, ReviewRequestDto request, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new ResourceNotFoundException("Listing not found with id: " + listingId));

        Review review = new Review(listing, user, request.getRating(), request.getComment());
        review = reviewRepository.save(review);

        return ReviewResponseDto.fromEntity(review);
    }

    public List<ReviewResponseDto> getListingReviews(Long listingId) {
        return reviewRepository.findByListingIdOrderByCreatedAtDesc(listingId).stream()
                .map(ReviewResponseDto::fromEntity)
                .collect(Collectors.toList());
    }
}
