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
 */
@RestController
@RequestMapping("/api/admin/reviews")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
public class AdminReviewController {

    private final AdminReviewService adminReviewService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ReviewDTO>>> getAllReviews(
            @ModelAttribute ReviewFilterRequest filter,
            @PageableDefault(size = 20) Pageable pageable) {
        
        log.info("Admin getting all reviews with filter: {}", filter);
        
        Page<ReviewDTO> reviews = adminReviewService.getAllReviews(filter, pageable);
        
        return ResponseEntity.ok(new ApiResponse<>(true, "Reviews retrieved successfully", reviews));
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

    @PostMapping("/{id}/respond")
    public ResponseEntity<ApiResponse<ReviewDetailDTO>> respondToReview(
            @PathVariable Long id,
            @Valid @RequestBody ReviewResponseRequest request) {
        
        log.info("Admin responding to review id: {}", id);
        
        ReviewDetailDTO updatedReview = adminReviewService.respondToReview(id, request);
        
        return ResponseEntity.ok(new ApiResponse<>(true, "Admin response added successfully", updatedReview));
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
