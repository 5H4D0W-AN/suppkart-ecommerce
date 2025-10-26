package com.suppkart.service;

import com.suppkart.dto.admin.review.*;
import com.suppkart.dto.request.FakeReviewRequest;
import com.suppkart.exception.ResourceNotFoundException;
import com.suppkart.model.entity.Product;
import com.suppkart.model.entity.Review;
import com.suppkart.model.entity.User;
import com.suppkart.repository.ProductRepository;
import com.suppkart.repository.ReviewRepository;
import com.suppkart.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AdminReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final EmailNotificationService emailNotificationService;

    // Review settings (in a real application, these would be stored in database)
    private final ReviewSettingsDTO reviewSettings = createDefaultSettings();

    private ReviewSettingsDTO createDefaultSettings() {
        ReviewSettingsDTO settings = new ReviewSettingsDTO();
        settings.setAutoApprove(false);
        settings.setRequireVerifiedPurchase(true);
        settings.setMinimumReviewLength(10);
        settings.setModerationEmails(Arrays.asList("admin@suppkart.com"));
        return settings;
    }

    public Page<ReviewDTO> searchReviews(ReviewFilterRequest filter, Pageable pageable) {
        log.info("Searching reviews with filter: {}", filter);

        // Validate that at least one filter criteria is provided
        if (filter.getProductId() == null
                && (filter.getSearch() == null || filter.getSearch().trim().isEmpty())
                && filter.getCustomerId() == null
                && filter.getRating() == null
                && filter.getStatus() == null
                && filter.getVerified() == null
                && filter.getApproved() == null
                && filter.getIsVisible() == null
                && filter.getIsFake() == null) {
            throw new IllegalArgumentException("At least one filter criteria is required for review search");
        }

        // Get all reviews and apply filters manually
        List<Review> allReviews = reviewRepository.findAll();

        // Apply filters manually
        List<Review> filteredReviews = allReviews.stream()
                .filter(review -> matchesFilter(review, filter))
                .sorted((r1, r2) -> r2.getCreatedAt().compareTo(r1.getCreatedAt())) // Sort by newest first
                .collect(Collectors.toList());

        // Apply pagination manually
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), filteredReviews.size());
        List<Review> pageContent = start < filteredReviews.size()
                ? filteredReviews.subList(start, end) : new ArrayList<>();

        Page<Review> reviews = new PageImpl<>(pageContent, pageable, filteredReviews.size());
        return reviews.map(this::convertToReviewDTO);
    }

    public ReviewDetailDTO getReviewById(Long id) {
        log.info("Getting review by id: {}", id);

        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + id));

        return convertToReviewDetailDTO(review);
    }

    public ReviewDetailDTO updateReviewStatus(Long id, ReviewStatusUpdateRequest request) {
        log.info("Updating review status for id: {} to status: {}", id, request.getStatus());

        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + id));

        Boolean oldApproved = review.getApproved();
        Boolean newApproved = "APPROVED".equalsIgnoreCase(request.getStatus());

        review.setApproved(newApproved);
        review.setIsVisible(newApproved); // Set visibility based on approval
        review.setUpdatedAt(LocalDateTime.now());

        Review savedReview = reviewRepository.save(review);

        // Send notification if requested
        if (request.getNotifyCustomer() != null && request.getNotifyCustomer()) {
            sendStatusUpdateNotification(review, oldApproved, newApproved, request.getReason());
        }

        log.info("Review status updated successfully for id: {}", id);
        return convertToReviewDetailDTO(savedReview);
    }

    public void deleteReview(Long id) {
        log.info("Deleting review with id: {}", id);

        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + id));

        reviewRepository.delete(review);
        log.info("Review deleted successfully with id: {}", id);
    }

    public ReviewStatisticsDTO getReviewStatistics() {
        log.info("Getting review statistics");

        long totalReviews = reviewRepository.count();

        // Calculate average rating using existing method
        Double averageRating = null;
        try {
            Object[] stats = reviewRepository.getReviewStatistics();
            if (stats != null && stats.length > 4 && stats[4] != null) {
                averageRating = (Double) stats[4];
            }
        } catch (Exception e) {
            log.warn("Could not get review statistics, calculating manually", e);
            // Fallback calculation
            List<Review> allReviews = reviewRepository.findAll();
            if (!allReviews.isEmpty()) {
                averageRating = allReviews.stream()
                        .filter(r -> r.getApproved() && r.getIsVisible())
                        .mapToInt(Review::getRating)
                        .average()
                        .orElse(0.0);
            }
        }

        // Get rating distribution
        Map<Integer, Integer> ratingDistribution = new HashMap<>();
        for (int i = 1; i <= 5; i++) {
            final int rating = i; // Make it effectively final for lambda
            long count = reviewRepository.findAll().stream()
                    .filter(r -> r.getApproved() && r.getIsVisible() && r.getRating().equals(rating))
                    .count();
            ratingDistribution.put(i, (int) count);
        }

        long pendingReviews = reviewRepository.findAll().stream()
                .filter(r -> !r.getApproved())
                .count();

        long verifiedReviews = reviewRepository.findAll().stream()
                .filter(Review::getVerified)
                .count();

        double verifiedPercentage = totalReviews > 0 ? (verifiedReviews * 100.0) / totalReviews : 0.0;

        LocalDateTime lastMonth = LocalDateTime.now().minusMonths(1);
        long reviewsLastMonth = reviewRepository.findAll().stream()
                .filter(r -> r.getCreatedAt().isAfter(lastMonth))
                .count();

        ReviewStatisticsDTO stats = new ReviewStatisticsDTO();
        stats.setTotalReviews((int) totalReviews);
        stats.setAverageRating(averageRating != null ? averageRating : 0.0);
        stats.setRatingDistribution(ratingDistribution);
        stats.setPendingReviews((int) pendingReviews);
        stats.setVerifiedReviewsPercentage(verifiedPercentage);
        stats.setReviewsLastMonth((int) reviewsLastMonth);

        return stats;
    }

    public Page<ReviewDTO> getPendingReviews(Pageable pageable) {
        log.info("Getting pending reviews");

        Page<Review> pendingReviews = reviewRepository.findPendingReviewsOrderByCreatedAtAsc(pageable);
        return pendingReviews.map(this::convertToReviewDTO);
    }

    public ReviewDetailDTO createFakeReview(FakeReviewRequest request) {
        log.info("Creating fake review for product id: {}", request.getProductId());

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + request.getProductId()));

        Review fakeReview = new Review();
        fakeReview.setProduct(product);
        fakeReview.setRating(request.getRating());
        fakeReview.setTitle(request.getTitle());
        fakeReview.setContent(request.getContent());
        fakeReview.setDisplayName(request.getCustomerName());
        fakeReview.setVerified(request.getVerified() != null ? request.getVerified() : false);
        fakeReview.setApproved(true); // Admin reviews are auto-approved
        fakeReview.setCreatedByAdmin(true);
        fakeReview.setIsVisible(true);
        fakeReview.setCreatedAt(LocalDateTime.now());
        fakeReview.setUpdatedAt(LocalDateTime.now());

        Review savedReview = reviewRepository.save(fakeReview);

        log.info("Fake review created successfully with id: {}", savedReview.getReviewId());
        return convertToReviewDetailDTO(savedReview);
    }

    public ReviewSettingsDTO updateReviewSettings(ReviewSettingsRequest request) {
        log.info("Updating review settings");

        this.reviewSettings.setAutoApprove(request.getAutoApprove());
        this.reviewSettings.setRequireVerifiedPurchase(request.getRequireVerifiedPurchase());
        this.reviewSettings.setMinimumReviewLength(request.getMinimumReviewLength());
        this.reviewSettings.setModerationEmails(request.getModerationEmails());

        log.info("Review settings updated successfully");
        return this.reviewSettings;
    }

    public ReviewSettingsDTO getReviewSettings() {
        return this.reviewSettings;
    }

    public ReviewDetailDTO approveReview(Long id) {
        log.info("Approving review with id: {}", id);

        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + id));

        review.setApproved(true);
        review.setIsVisible(true); // Make visible when approved
        review.setUpdatedAt(LocalDateTime.now());

        Review savedReview = reviewRepository.save(review);

        log.info("Review approved successfully with id: {}", id);
        return convertToReviewDetailDTO(savedReview);
    }

    public ReviewDetailDTO toggleReviewVisibility(Long id) {
        log.info("Toggling visibility for review with id: {}", id);

        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + id));

        review.setIsVisible(!review.getIsVisible());
        review.setUpdatedAt(LocalDateTime.now());

        Review savedReview = reviewRepository.save(review);

        log.info("Review visibility toggled successfully for id: {}", id);
        return convertToReviewDetailDTO(savedReview);
    }

    public int bulkApproveReviews(List<Long> reviewIds) {
        log.info("Bulk approving {} reviews", reviewIds.size());

        int approvedCount = 0;
        for (Long reviewId : reviewIds) {
            try {
                Review review = reviewRepository.findById(reviewId).orElse(null);
                if (review != null) {
                    review.setApproved(true);
                    review.setIsVisible(true);
                    review.setUpdatedAt(LocalDateTime.now());
                    reviewRepository.save(review);
                    approvedCount++;
                }
            } catch (Exception e) {
                log.error("Failed to approve review with id: {}", reviewId, e);
            }
        }

        log.info("Successfully approved {} out of {} reviews", approvedCount, reviewIds.size());
        return approvedCount;
    }

    public Map<String, Integer> getReviewCounts(Long productId) {
        log.info("Getting review counts for product id: {}", productId);

        List<Review> reviews;
        if (productId != null) {
            reviews = reviewRepository.findAll().stream()
                    .filter(r -> r.getProduct().getProductId().equals(productId))
                    .collect(Collectors.toList());
        } else {
            reviews = reviewRepository.findAll();
        }

        Map<String, Integer> counts = new HashMap<>();
        counts.put("total", reviews.size());
        counts.put("approved", (int) reviews.stream().filter(Review::getApproved).count());
        counts.put("pending", (int) reviews.stream().filter(r -> !r.getApproved()).count());
        counts.put("visible", (int) reviews.stream().filter(Review::getIsVisible).count());
        counts.put("hidden", (int) reviews.stream().filter(r -> !r.getIsVisible()).count());
        counts.put("verified", (int) reviews.stream().filter(Review::getVerified).count());
        counts.put("fake", (int) reviews.stream().filter(Review::getCreatedByAdmin).count());

        return counts;
    }

    private boolean matchesFilter(Review review, ReviewFilterRequest filter) {
        // Search term filter - searches in title, content, display name, and product name
        if (filter.getSearch() != null && !filter.getSearch().trim().isEmpty()) {
            String searchTerm = filter.getSearch().toLowerCase();
            boolean matches = (review.getTitle() != null && review.getTitle().toLowerCase().contains(searchTerm))
                    || (review.getContent() != null && review.getContent().toLowerCase().contains(searchTerm))
                    || (review.getDisplayName() != null && review.getDisplayName().toLowerCase().contains(searchTerm))
                    || (review.getProduct().getName() != null && review.getProduct().getName().toLowerCase().contains(searchTerm));
            if (!matches) {
                return false;
            }
        }

        // Product ID filter
        if (filter.getProductId() != null) {
            if (!review.getProduct().getProductId().equals(filter.getProductId())) {
                return false;
            }
        }

        // Customer ID filter
        if (filter.getCustomerId() != null) {
            if (review.getUser() == null || !review.getUser().getUserId().equals(filter.getCustomerId())) {
                return false;
            }
        }

        // Rating filter
        if (filter.getRating() != null) {
            if (!review.getRating().equals(filter.getRating())) {
                return false;
            }
        }

        // Status filter (APPROVED/PENDING)
        if (filter.getStatus() != null && !filter.getStatus().trim().isEmpty()) {
            boolean approved = "APPROVED".equalsIgnoreCase(filter.getStatus());
            if (!review.getApproved().equals(approved)) {
                return false;
            }
        }

        // Date range filters
        if (filter.getStartDate() != null) {
            if (review.getCreatedAt().isBefore(filter.getStartDate())) {
                return false;
            }
        }

        if (filter.getEndDate() != null) {
            if (review.getCreatedAt().isAfter(filter.getEndDate())) {
                return false;
            }
        }

        // Verified purchase filter
        if (filter.getVerified() != null) {
            if (!review.getVerified().equals(filter.getVerified())) {
                return false;
            }
        }

        // Admin-created (fake) review filter
        if (filter.getIsFake() != null) {
            if (!review.getCreatedByAdmin().equals(filter.getIsFake())) {
                return false;
            }
        }

        // Visibility filter
        if (filter.getIsVisible() != null) {
            if (!review.getIsVisible().equals(filter.getIsVisible())) {
                return false;
            }
        }

        // Approved filter (additional to status filter for more granular control)
        if (filter.getApproved() != null) {
            if (!review.getApproved().equals(filter.getApproved())) {
                return false;
            }
        }

        return true;
    }

    private ReviewDTO convertToReviewDTO(Review review) {
        ReviewDTO dto = new ReviewDTO();
        dto.setId(review.getReviewId());
        dto.setProductId(review.getProduct().getProductId());
        dto.setProductName(review.getProduct().getName());
        dto.setCustomerName(review.getUserDisplayName());
        dto.setRating(review.getRating());
        dto.setTitle(review.getTitle());
        dto.setStatus(review.getApproved() ? "APPROVED" : "PENDING");
        dto.setCreatedAt(review.getCreatedAt());
        return dto;
    }

    private ReviewDetailDTO convertToReviewDetailDTO(Review review) {
        List<String> productImages = review.getProduct().getImages() != null
                ? review.getProduct().getImages().stream()
                        .map(img -> img.getImageUrl())
                        .collect(Collectors.toList()) : new ArrayList<>();

        String productImage = productImages.isEmpty() ? null : productImages.get(0);

        ReviewDetailDTO dto = new ReviewDetailDTO();
        dto.setId(review.getReviewId());
        dto.setProductId(review.getProduct().getProductId());
        dto.setProductName(review.getProduct().getName());
        dto.setProductImage(productImage);
        dto.setCustomerId(review.getUser() != null ? review.getUser().getUserId() : null);
        dto.setCustomerName(review.getUserDisplayName());
        dto.setVerified(review.getVerified());
        dto.setRating(review.getRating());
        dto.setTitle(review.getTitle());
        dto.setContent(review.getContent());
        dto.setStatus(review.getApproved() ? "APPROVED" : "PENDING");
        dto.setHelpfulVotes(review.getHelpfulVotes());
        dto.setCreatedAt(review.getCreatedAt());
        dto.setIsFake(review.getCreatedByAdmin());

        return dto;
    }

    private void sendStatusUpdateNotification(Review review, Boolean oldApproved, Boolean newApproved, String reason) {
        try {
            if (review.getUser() != null && review.getUser().getEmail() != null) {
                String status = newApproved ? "approved" : "rejected";
                String subject = "Review Status Updated - " + review.getProduct().getName();
                String message = String.format(
                        "Your review for %s has been %s. %s",
                        review.getProduct().getName(),
                        status,
                        reason != null ? "Reason: " + reason : ""
                );

                emailNotificationService.sendEmail(review.getUser().getEmail(), subject, message);
            }
        } catch (Exception e) {
            log.error("Failed to send review status notification", e);
        }
    }

    private void sendResponseNotification(Review review, String response) {
        try {
            if (review.getUser() != null && review.getUser().getEmail() != null) {
                String subject = "Admin Response to Your Review - " + review.getProduct().getName();
                String message = String.format(
                        "An admin has responded to your review for %s:\n\n%s",
                        review.getProduct().getName(),
                        response
                );

                emailNotificationService.sendEmail(review.getUser().getEmail(), subject, message);
            }
        } catch (Exception e) {
            log.error("Failed to send review response notification", e);
        }
    }
}
