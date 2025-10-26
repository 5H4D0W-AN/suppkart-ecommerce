package com.suppkart.controller;

import com.suppkart.dto.admin.review.*;
import com.suppkart.dto.request.FakeReviewRequest;
import com.suppkart.dto.response.ApiResponse;
import com.suppkart.service.AdminReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Admin controller for review moderation and management
 *
 * Key Features: - Search-based review retrieval (no default loading of all
 * reviews) - Comprehensive filtering by product, customer, rating, status, etc.
 * - Individual and bulk approval operations - Visibility management - Review
 * statistics and counts
 */
@RestController
@RequestMapping("/api/admin/reviews")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
public class AdminReviewController {

    private final AdminReviewService adminReviewService;

    /**
     * Search reviews with comprehensive filtering options Required: At least
     * one filter parameter (productId, search, customerId, etc.)
     *
     * Available filters: - productId: Filter by specific product - customerId:
     * Filter by specific customer - search: Text search in title, content,
     * display name, product name - rating: Filter by specific rating (1-5) -
     * status: APPROVED or PENDING - verified: true/false for verified purchases
     * - approved: true/false for approval status - isVisible: true/false for
     * visibility status - isFake: true/false for admin-created reviews -
     * startDate/endDate: Date range filtering
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<ReviewDTO>>> searchReviews(
            @ModelAttribute ReviewFilterRequest filter,
            @PageableDefault(size = 20) Pageable pageable) {

        log.info("Admin searching reviews with filter: {}", filter);

        Page<ReviewDTO> reviews = adminReviewService.searchReviews(filter, pageable);

        return ResponseEntity.ok(new ApiResponse<>(true, "Reviews retrieved successfully", reviews));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<ReviewStatisticsDTO>> getDefaultContent() {
        log.info("Admin accessing reviews section - returning statistics");

        ReviewStatisticsDTO statistics = adminReviewService.getReviewStatistics();

        return ResponseEntity.ok(new ApiResponse<>(true, "Review statistics retrieved successfully", statistics));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ReviewDetailDTO>> getReviewById(@PathVariable Long id) {

        log.info("Admin getting review details for id: {}", id);

        ReviewDetailDTO review = adminReviewService.getReviewById(id);

        return ResponseEntity.ok(new ApiResponse<>(true, "Review details retrieved successfully", review));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<ReviewDetailDTO>> updateReviewStatus(
            @PathVariable Long id,
            @Valid @RequestBody ReviewStatusUpdateRequest request) {

        log.info("Admin updating review status for id: {} to status: {}", id, request.getStatus());

        ReviewDetailDTO updatedReview = adminReviewService.updateReviewStatus(id, request);

        return ResponseEntity.ok(new ApiResponse<>(true, "Review status updated successfully", updatedReview));
    }

    @PatchMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<ReviewDetailDTO>> approveReview(@PathVariable Long id) {

        log.info("Admin approving review with id: {}", id);

        ReviewDetailDTO updatedReview = adminReviewService.approveReview(id);

        return ResponseEntity.ok(new ApiResponse<>(true, "Review approved successfully", updatedReview));
    }

    @PatchMapping("/{id}/visibility")
    public ResponseEntity<ApiResponse<ReviewDetailDTO>> toggleReviewVisibility(@PathVariable Long id) {

        log.info("Admin toggling visibility for review with id: {}", id);

        ReviewDetailDTO updatedReview = adminReviewService.toggleReviewVisibility(id);

        return ResponseEntity.ok(new ApiResponse<>(true, "Review visibility updated successfully", updatedReview));
    }

    @PostMapping("/bulk-approve")
    public ResponseEntity<ApiResponse<String>> bulkApproveReviews(@RequestBody java.util.List<Long> reviewIds) {

        log.info("Admin bulk approving {} reviews", reviewIds.size());

        int approvedCount = adminReviewService.bulkApproveReviews(reviewIds);

        return ResponseEntity.ok(new ApiResponse<>(true,
                String.format("Successfully approved %d out of %d reviews", approvedCount, reviewIds.size()),
                null));
    }

    /**
     * Get reviews for a specific product with optional filtering This is a
     * convenience endpoint for product-specific review management
     */
    @GetMapping("/product/{productId}")
    public ResponseEntity<ApiResponse<Page<ReviewDTO>>> getReviewsByProduct(
            @PathVariable Long productId,
            @RequestParam(required = false) Integer rating,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Boolean verified,
            @RequestParam(required = false) Boolean approved,
            @RequestParam(required = false) Boolean isVisible,
            @RequestParam(required = false) Boolean isFake,
            @RequestParam(required = false) Long customerId,
            @PageableDefault(size = 20) Pageable pageable) {

        log.info("Admin getting reviews for product id: {} with filters", productId);

        ReviewFilterRequest filter = ReviewFilterRequest.builder()
                .productId(productId)
                .rating(rating)
                .status(status)
                .verified(verified)
                .approved(approved)
                .isVisible(isVisible)
                .isFake(isFake)
                .customerId(customerId)
                .build();

        Page<ReviewDTO> reviews = adminReviewService.searchReviews(filter, pageable);

        return ResponseEntity.ok(new ApiResponse<>(true, "Product reviews retrieved successfully", reviews));
    }

    @GetMapping("/counts")
    public ResponseEntity<ApiResponse<java.util.Map<String, Integer>>> getReviewCounts(
            @RequestParam(required = false) Long productId) {

        log.info("Admin getting review counts for product id: {}", productId);

        java.util.Map<String, Integer> counts = adminReviewService.getReviewCounts(productId);

        return ResponseEntity.ok(new ApiResponse<>(true, "Review counts retrieved successfully", counts));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteReview(@PathVariable Long id) {

        log.info("Admin deleting review with id: {}", id);

        adminReviewService.deleteReview(id);

        return ResponseEntity.ok(new ApiResponse<>(true, "Review deleted successfully", null));
    }

    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<ReviewStatisticsDTO>> getReviewStatistics() {

        log.info("Admin getting review statistics");

        ReviewStatisticsDTO statistics = adminReviewService.getReviewStatistics();

        return ResponseEntity.ok(new ApiResponse<>(true, "Review statistics retrieved successfully", statistics));
    }

    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<Page<ReviewDTO>>> getPendingReviews(
            @PageableDefault(size = 20) Pageable pageable) {

        log.info("Admin getting pending reviews");

        Page<ReviewDTO> pendingReviews = adminReviewService.getPendingReviews(pageable);

        return ResponseEntity.ok(new ApiResponse<>(true, "Pending reviews retrieved successfully", pendingReviews));
    }

    @PostMapping("/fake")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<ReviewDetailDTO>> createFakeReview(
            @Valid @RequestBody FakeReviewRequest request) {

        log.info("Admin creating fake review for product id: {}", request.getProductId());

        ReviewDetailDTO fakeReview = adminReviewService.createFakeReview(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Fake review created successfully", fakeReview));
    }

    @GetMapping("/settings")
    public ResponseEntity<ApiResponse<ReviewSettingsDTO>> getReviewSettings() {

        log.info("Admin getting review settings");

        ReviewSettingsDTO settings = adminReviewService.getReviewSettings();

        return ResponseEntity.ok(new ApiResponse<>(true, "Review settings retrieved successfully", settings));
    }

    @PutMapping("/settings")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<ReviewSettingsDTO>> updateReviewSettings(
            @Valid @RequestBody ReviewSettingsRequest request) {

        log.info("Admin updating review settings");

        ReviewSettingsDTO updatedSettings = adminReviewService.updateReviewSettings(request);

        return ResponseEntity.ok(new ApiResponse<>(true, "Review settings updated successfully", updatedSettings));
    }
}
