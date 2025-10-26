package com.suppkart.controller;

import com.suppkart.dto.admin.review.*;
import com.suppkart.dto.response.ApiResponse;
import com.suppkart.service.AdminReviewService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminReviewControllerTest {

    @Mock
    private AdminReviewService adminReviewService;

    @InjectMocks
    private AdminReviewController adminReviewController;

    @BeforeEach
    void setUp() {
        // Setup is handled by Mockito
    }

    @Test
    void getDefaultContent_ShouldReturnStatistics() {
        // Given
        ReviewStatisticsDTO statistics = new ReviewStatisticsDTO();
        statistics.setTotalReviews(100);
        statistics.setAverageRating(4.5);
        statistics.setRatingDistribution(Map.of(5, 50, 4, 30, 3, 15, 2, 3, 1, 2));
        statistics.setPendingReviews(10);
        statistics.setVerifiedReviewsPercentage(85.0);
        statistics.setReviewsLastMonth(25);

        when(adminReviewService.getReviewStatistics()).thenReturn(statistics);

        // When
        ResponseEntity<ApiResponse<ReviewStatisticsDTO>> response = adminReviewController.getDefaultContent();

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Review statistics retrieved successfully", response.getBody().getMessage());
        assertEquals(100, response.getBody().getData().getTotalReviews());
        assertEquals(4.5, response.getBody().getData().getAverageRating());
        assertEquals(10, response.getBody().getData().getPendingReviews());

        verify(adminReviewService).getReviewStatistics();
    }

    @Test
    void searchReviews_ShouldReturnFilteredReviews() {
        // Given
        ReviewDTO reviewDTO = new ReviewDTO();
        reviewDTO.setId(1L);
        reviewDTO.setProductId(123L);
        reviewDTO.setProductName("Test Product");
        reviewDTO.setCustomerName("John Doe");
        reviewDTO.setRating(5);
        reviewDTO.setTitle("Great product");
        reviewDTO.setStatus("APPROVED");
        reviewDTO.setCreatedAt(LocalDateTime.now());

        Page<ReviewDTO> reviewPage = new PageImpl<>(List.of(reviewDTO), PageRequest.of(0, 20), 1);
        when(adminReviewService.searchReviews(any(ReviewFilterRequest.class), any(Pageable.class)))
                .thenReturn(reviewPage);

        ReviewFilterRequest filter = new ReviewFilterRequest();
        filter.setProductId(123L);
        filter.setStatus("APPROVED");

        // When
        ResponseEntity<ApiResponse<Page<ReviewDTO>>> response = adminReviewController.searchReviews(filter, PageRequest.of(0, 20));

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Reviews retrieved successfully", response.getBody().getMessage());
        assertEquals(1, response.getBody().getData().getContent().get(0).getId());
        assertEquals("Test Product", response.getBody().getData().getContent().get(0).getProductName());

        verify(adminReviewService).searchReviews(any(ReviewFilterRequest.class), any(Pageable.class));
    }

    @Test
    void getReviewById_ShouldReturnReviewDetail() {
        // Given
        ReviewDetailDTO reviewDetail = new ReviewDetailDTO();
        reviewDetail.setId(1L);
        reviewDetail.setProductId(123L);
        reviewDetail.setProductName("Test Product");
        reviewDetail.setCustomerName("John Doe");
        reviewDetail.setRating(5);
        reviewDetail.setTitle("Great product");
        reviewDetail.setContent("This is a great product!");
        reviewDetail.setStatus("APPROVED");
        reviewDetail.setVerified(true);
        reviewDetail.setHelpfulVotes(5);
        reviewDetail.setCreatedAt(LocalDateTime.now());
        reviewDetail.setIsFake(false);

        when(adminReviewService.getReviewById(1L)).thenReturn(reviewDetail);

        // When
        ResponseEntity<ApiResponse<ReviewDetailDTO>> response = adminReviewController.getReviewById(1L);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Review details retrieved successfully", response.getBody().getMessage());
        assertEquals(1L, response.getBody().getData().getId());
        assertEquals("Test Product", response.getBody().getData().getProductName());
        assertTrue(response.getBody().getData().getVerified());

        verify(adminReviewService).getReviewById(1L);
    }

    @Test
    void approveReview_ShouldReturnUpdatedReview() {
        // Given
        ReviewDetailDTO updatedReview = new ReviewDetailDTO();
        updatedReview.setId(1L);
        updatedReview.setStatus("APPROVED");

        when(adminReviewService.approveReview(1L)).thenReturn(updatedReview);

        // When
        ResponseEntity<ApiResponse<ReviewDetailDTO>> response = adminReviewController.approveReview(1L);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Review approved successfully", response.getBody().getMessage());
        assertEquals("APPROVED", response.getBody().getData().getStatus());

        verify(adminReviewService).approveReview(1L);
    }

    @Test
    void toggleReviewVisibility_ShouldReturnUpdatedReview() {
        // Given
        ReviewDetailDTO updatedReview = new ReviewDetailDTO();
        updatedReview.setId(1L);
        updatedReview.setStatus("APPROVED");

        when(adminReviewService.toggleReviewVisibility(1L)).thenReturn(updatedReview);

        // When
        ResponseEntity<ApiResponse<ReviewDetailDTO>> response = adminReviewController.toggleReviewVisibility(1L);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Review visibility updated successfully", response.getBody().getMessage());

        verify(adminReviewService).toggleReviewVisibility(1L);
    }

    @Test
    void bulkApproveReviews_ShouldReturnSuccessMessage() {
        // Given
        List<Long> reviewIds = Arrays.asList(1L, 2L, 3L);
        when(adminReviewService.bulkApproveReviews(reviewIds)).thenReturn(3);

        // When
        ResponseEntity<ApiResponse<String>> response = adminReviewController.bulkApproveReviews(reviewIds);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Successfully approved 3 out of 3 reviews", response.getBody().getMessage());

        verify(adminReviewService).bulkApproveReviews(reviewIds);
    }

    @Test
    void getReviewsByProduct_ShouldReturnProductReviews() {
        // Given
        ReviewDTO reviewDTO = new ReviewDTO();
        reviewDTO.setId(1L);
        reviewDTO.setProductId(123L);
        reviewDTO.setProductName("Test Product");
        reviewDTO.setCustomerName("John Doe");
        reviewDTO.setRating(5);
        reviewDTO.setTitle("Great product");
        reviewDTO.setStatus("APPROVED");
        reviewDTO.setCreatedAt(LocalDateTime.now());

        Page<ReviewDTO> reviewPage = new PageImpl<>(List.of(reviewDTO), PageRequest.of(0, 20), 1);
        when(adminReviewService.searchReviews(any(ReviewFilterRequest.class), any(Pageable.class)))
                .thenReturn(reviewPage);

        // When
        ResponseEntity<ApiResponse<Page<ReviewDTO>>> response = adminReviewController.getReviewsByProduct(
                123L, 5, "APPROVED", true, true, true, false, 1L, PageRequest.of(0, 20));

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Product reviews retrieved successfully", response.getBody().getMessage());
        assertEquals(123L, response.getBody().getData().getContent().get(0).getProductId());

        verify(adminReviewService).searchReviews(any(ReviewFilterRequest.class), any(Pageable.class));
    }

    @Test
    void getReviewCounts_ShouldReturnCounts() {
        // Given
        Map<String, Integer> counts = Map.of(
                "total", 100,
                "approved", 85,
                "pending", 15,
                "visible", 90,
                "hidden", 10,
                "verified", 70,
                "fake", 5
        );

        when(adminReviewService.getReviewCounts(123L)).thenReturn(counts);

        // When
        ResponseEntity<ApiResponse<Map<String, Integer>>> response = adminReviewController.getReviewCounts(123L);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Review counts retrieved successfully", response.getBody().getMessage());
        assertEquals(100, response.getBody().getData().get("total"));
        assertEquals(85, response.getBody().getData().get("approved"));
        assertEquals(15, response.getBody().getData().get("pending"));

        verify(adminReviewService).getReviewCounts(123L);
    }

    @Test
    void updateReviewStatus_ShouldReturnUpdatedReview() {
        // Given
        ReviewStatusUpdateRequest request = new ReviewStatusUpdateRequest();
        request.setStatus("APPROVED");
        request.setReason("Good review");
        request.setNotifyCustomer(true);

        ReviewDetailDTO updatedReview = new ReviewDetailDTO();
        updatedReview.setId(1L);
        updatedReview.setStatus("APPROVED");

        when(adminReviewService.updateReviewStatus(eq(1L), any(ReviewStatusUpdateRequest.class)))
                .thenReturn(updatedReview);

        // When
        ResponseEntity<ApiResponse<ReviewDetailDTO>> response = adminReviewController.updateReviewStatus(1L, request);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Review status updated successfully", response.getBody().getMessage());
        assertEquals("APPROVED", response.getBody().getData().getStatus());

        verify(adminReviewService).updateReviewStatus(eq(1L), any(ReviewStatusUpdateRequest.class));
    }

    @Test
    void deleteReview_ShouldReturnSuccessMessage() {
        // Given
        doNothing().when(adminReviewService).deleteReview(1L);

        // When
        ResponseEntity<ApiResponse<Void>> response = adminReviewController.deleteReview(1L);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Review deleted successfully", response.getBody().getMessage());

        verify(adminReviewService).deleteReview(1L);
    }

    @Test
    void getPendingReviews_ShouldReturnPendingReviews() {
        // Given
        ReviewDTO reviewDTO = new ReviewDTO();
        reviewDTO.setId(1L);
        reviewDTO.setProductId(123L);
        reviewDTO.setProductName("Test Product");
        reviewDTO.setCustomerName("John Doe");
        reviewDTO.setRating(5);
        reviewDTO.setTitle("Great product");
        reviewDTO.setStatus("PENDING");
        reviewDTO.setCreatedAt(LocalDateTime.now());

        Page<ReviewDTO> reviewPage = new PageImpl<>(List.of(reviewDTO), PageRequest.of(0, 20), 1);
        when(adminReviewService.getPendingReviews(any(Pageable.class))).thenReturn(reviewPage);

        // When
        ResponseEntity<ApiResponse<Page<ReviewDTO>>> response = adminReviewController.getPendingReviews(PageRequest.of(0, 20));

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Pending reviews retrieved successfully", response.getBody().getMessage());
        assertEquals("PENDING", response.getBody().getData().getContent().get(0).getStatus());

        verify(adminReviewService).getPendingReviews(any(Pageable.class));
    }

    @Test
    void getReviewSettings_ShouldReturnSettings() {
        // Given
        ReviewSettingsDTO settings = new ReviewSettingsDTO();
        settings.setAutoApprove(false);
        settings.setRequireVerifiedPurchase(true);
        settings.setMinimumReviewLength(10);
        settings.setModerationEmails(Arrays.asList("admin@suppkart.com"));

        when(adminReviewService.getReviewSettings()).thenReturn(settings);

        // When
        ResponseEntity<ApiResponse<ReviewSettingsDTO>> response = adminReviewController.getReviewSettings();

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Review settings retrieved successfully", response.getBody().getMessage());
        assertFalse(response.getBody().getData().getAutoApprove());
        assertTrue(response.getBody().getData().getRequireVerifiedPurchase());

        verify(adminReviewService).getReviewSettings();
    }

    @Test
    void updateReviewSettings_ShouldReturnUpdatedSettings() {
        // Given
        ReviewSettingsRequest request = new ReviewSettingsRequest();
        request.setAutoApprove(true);
        request.setRequireVerifiedPurchase(false);
        request.setMinimumReviewLength(5);
        request.setModerationEmails(Arrays.asList("admin@suppkart.com", "moderator@suppkart.com"));

        ReviewSettingsDTO updatedSettings = new ReviewSettingsDTO();
        updatedSettings.setAutoApprove(true);
        updatedSettings.setRequireVerifiedPurchase(false);
        updatedSettings.setMinimumReviewLength(5);
        updatedSettings.setModerationEmails(Arrays.asList("admin@suppkart.com", "moderator@suppkart.com"));

        when(adminReviewService.updateReviewSettings(any(ReviewSettingsRequest.class)))
                .thenReturn(updatedSettings);

        // When
        ResponseEntity<ApiResponse<ReviewSettingsDTO>> response = adminReviewController.updateReviewSettings(request);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Review settings updated successfully", response.getBody().getMessage());
        assertTrue(response.getBody().getData().getAutoApprove());
        assertFalse(response.getBody().getData().getRequireVerifiedPurchase());

        verify(adminReviewService).updateReviewSettings(any(ReviewSettingsRequest.class));
    }
}