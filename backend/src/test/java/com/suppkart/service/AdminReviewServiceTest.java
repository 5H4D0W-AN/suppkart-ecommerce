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

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailNotificationService emailNotificationService;

    @InjectMocks
    private AdminReviewService adminReviewService;

    private Review testReview;
    private Product testProduct;
    private User testUser;

    @BeforeEach
    void setUp() {
        testProduct = new Product();
        testProduct.setProductId(1L);
        testProduct.setName("Test Product");

        testUser = new User();
        testUser.setUserId(1L);
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setEmail("john.doe@example.com");

        testReview = new Review();
        testReview.setReviewId(1L);
        testReview.setProduct(testProduct);
        testReview.setUser(testUser);
        testReview.setRating(5);
        testReview.setTitle("Great product");
        testReview.setContent("This is a great product!");
        testReview.setApproved(false);
        testReview.setIsVisible(true);
        testReview.setVerified(true);
        testReview.setCreatedByAdmin(false);
        testReview.setHelpfulVotes(0);
        testReview.setCreatedAt(LocalDateTime.now());
        testReview.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void searchReviews_WithValidFilter_ShouldReturnFilteredReviews() {
        // Given
        ReviewFilterRequest filter = ReviewFilterRequest.builder()
                .productId(1L)
                .status("PENDING")
                .build();

        List<Review> reviews = Arrays.asList(testReview);
        when(reviewRepository.findAll()).thenReturn(reviews);

        Pageable pageable = PageRequest.of(0, 20);

        // When
        Page<ReviewDTO> result = adminReviewService.searchReviews(filter, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1L, result.getContent().get(0).getId());
        assertEquals("Test Product", result.getContent().get(0).getProductName());
        verify(reviewRepository).findAll();
    }

    @Test
    void searchReviews_WithNoFilterCriteria_ShouldThrowException() {
        // Given
        ReviewFilterRequest filter = ReviewFilterRequest.builder().build();
        Pageable pageable = PageRequest.of(0, 20);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> adminReviewService.searchReviews(filter, pageable));
        
        assertEquals("At least one filter criteria is required for review search", exception.getMessage());
    }

    @Test
    void getReviewById_WithValidId_ShouldReturnReviewDetail() {
        // Given
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(testReview));

        // When
        ReviewDetailDTO result = adminReviewService.getReviewById(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Test Product", result.getProductName());
        assertEquals("John D.", result.getCustomerName());
        assertEquals(5, result.getRating());
        assertEquals("Great product", result.getTitle());
        assertEquals("This is a great product!", result.getContent());
        assertEquals("PENDING", result.getStatus());
        assertFalse(result.getIsFake());
        verify(reviewRepository).findById(1L);
    }

    @Test
    void getReviewById_WithInvalidId_ShouldThrowException() {
        // Given
        when(reviewRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
            () -> adminReviewService.getReviewById(999L));
        
        assertEquals("Review not found with id: 999", exception.getMessage());
        verify(reviewRepository).findById(999L);
    }

    @Test
    void approveReview_WithValidId_ShouldApproveAndMakeVisible() {
        // Given
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(testReview));
        when(reviewRepository.save(any(Review.class))).thenReturn(testReview);

        // When
        ReviewDetailDTO result = adminReviewService.approveReview(1L);

        // Then
        assertNotNull(result);
        assertTrue(testReview.getApproved());
        assertTrue(testReview.getIsVisible());
        verify(reviewRepository).findById(1L);
        verify(reviewRepository).save(testReview);
    }

    @Test
    void toggleReviewVisibility_ShouldToggleVisibility() {
        // Given
        testReview.setIsVisible(true);
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(testReview));
        when(reviewRepository.save(any(Review.class))).thenReturn(testReview);

        // When
        ReviewDetailDTO result = adminReviewService.toggleReviewVisibility(1L);

        // Then
        assertNotNull(result);
        assertFalse(testReview.getIsVisible());
        verify(reviewRepository).findById(1L);
        verify(reviewRepository).save(testReview);
    }

    @Test
    void bulkApproveReviews_ShouldApproveAllValidReviews() {
        // Given
        List<Long> reviewIds = Arrays.asList(1L, 2L, 3L);
        Review review2 = new Review();
        review2.setReviewId(2L);
        Review review3 = new Review();
        review3.setReviewId(3L);

        when(reviewRepository.findById(1L)).thenReturn(Optional.of(testReview));
        when(reviewRepository.findById(2L)).thenReturn(Optional.of(review2));
        when(reviewRepository.findById(3L)).thenReturn(Optional.of(review3));
        when(reviewRepository.save(any(Review.class))).thenReturn(testReview);

        // When
        int result = adminReviewService.bulkApproveReviews(reviewIds);

        // Then
        assertEquals(3, result);
        verify(reviewRepository, times(3)).save(any(Review.class));
    }

    @Test
    void bulkApproveReviews_WithSomeInvalidIds_ShouldApproveOnlyValidOnes() {
        // Given
        List<Long> reviewIds = Arrays.asList(1L, 999L, 2L);
        Review review2 = new Review();
        review2.setReviewId(2L);

        when(reviewRepository.findById(1L)).thenReturn(Optional.of(testReview));
        when(reviewRepository.findById(999L)).thenReturn(Optional.empty());
        when(reviewRepository.findById(2L)).thenReturn(Optional.of(review2));
        when(reviewRepository.save(any(Review.class))).thenReturn(testReview);

        // When
        int result = adminReviewService.bulkApproveReviews(reviewIds);

        // Then
        assertEquals(2, result);
        verify(reviewRepository, times(2)).save(any(Review.class));
    }

    @Test
    void updateReviewStatus_WithApprovalStatus_ShouldUpdateCorrectly() {
        // Given
        ReviewStatusUpdateRequest request = new ReviewStatusUpdateRequest();
        request.setStatus("APPROVED");
        request.setNotifyCustomer(false);

        when(reviewRepository.findById(1L)).thenReturn(Optional.of(testReview));
        when(reviewRepository.save(any(Review.class))).thenReturn(testReview);

        // When
        ReviewDetailDTO result = adminReviewService.updateReviewStatus(1L, request);

        // Then
        assertNotNull(result);
        assertTrue(testReview.getApproved());
        assertTrue(testReview.getIsVisible());
        verify(reviewRepository).findById(1L);
        verify(reviewRepository).save(testReview);
    }

    @Test
    void updateReviewStatus_WithNotification_ShouldSendEmail() {
        // Given
        ReviewStatusUpdateRequest request = new ReviewStatusUpdateRequest();
        request.setStatus("APPROVED");
        request.setNotifyCustomer(true);
        request.setReason("Good review");

        when(reviewRepository.findById(1L)).thenReturn(Optional.of(testReview));
        when(reviewRepository.save(any(Review.class))).thenReturn(testReview);

        // When
        adminReviewService.updateReviewStatus(1L, request);

        // Then
        verify(emailNotificationService).sendEmail(
            eq("john.doe@example.com"),
            anyString(),
            anyString()
        );
    }

    @Test
    void deleteReview_WithValidId_ShouldDeleteReview() {
        // Given
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(testReview));

        // When
        adminReviewService.deleteReview(1L);

        // Then
        verify(reviewRepository).findById(1L);
        verify(reviewRepository).delete(testReview);
    }

    @Test
    void getReviewStatistics_ShouldReturnCorrectStatistics() {
        // Given
        List<Review> reviews = Arrays.asList(
            createReviewWithRating(5, true, true),
            createReviewWithRating(4, true, true),
            createReviewWithRating(3, false, true),
            createReviewWithRating(5, true, false)
        );

        when(reviewRepository.findAll()).thenReturn(reviews);
        when(reviewRepository.count()).thenReturn(4L);
        when(reviewRepository.getReviewStatistics()).thenThrow(new RuntimeException("Method not found"));

        // When
        ReviewStatisticsDTO result = adminReviewService.getReviewStatistics();

        // Then
        assertNotNull(result);
        assertEquals(4, result.getTotalReviews());
        assertEquals(1, result.getPendingReviews());
        assertEquals(4.67, result.getAverageRating(), 0.1);
        assertTrue(result.getVerifiedReviewsPercentage() > 0);
    }

    @Test
    void createFakeReview_WithValidRequest_ShouldCreateReview() {
        // Given
        FakeReviewRequest request = new FakeReviewRequest();
        request.setProductId(1L);
        request.setRating(5);
        request.setTitle("Great product");
        request.setContent("This is a great product!");
        request.setCustomerName("John Doe");
        request.setVerified(true);

        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(reviewRepository.save(any(Review.class))).thenReturn(testReview);

        // When
        ReviewDetailDTO result = adminReviewService.createFakeReview(request);

        // Then
        assertNotNull(result);
        verify(productRepository).findById(1L);
        verify(reviewRepository).save(any(Review.class));
    }

    @Test
    void createFakeReview_WithInvalidProductId_ShouldThrowException() {
        // Given
        FakeReviewRequest request = new FakeReviewRequest();
        request.setProductId(999L);

        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
            () -> adminReviewService.createFakeReview(request));
        
        assertEquals("Product not found with id: 999", exception.getMessage());
    }

    @Test
    void getReviewCounts_WithProductId_ShouldReturnProductSpecificCounts() {
        // Given
        List<Review> allReviews = Arrays.asList(
            createReviewForProduct(1L, true, true, true, false),
            createReviewForProduct(1L, false, true, false, false),
            createReviewForProduct(2L, true, true, true, false),
            createReviewForProduct(1L, true, false, true, true)
        );

        when(reviewRepository.findAll()).thenReturn(allReviews);

        // When
        Map<String, Integer> result = adminReviewService.getReviewCounts(1L);

        // Then
        assertNotNull(result);
        assertEquals(3, result.get("total"));
        assertEquals(2, result.get("approved"));
        assertEquals(1, result.get("pending"));
        assertEquals(2, result.get("visible"));
        assertEquals(1, result.get("hidden"));
        assertEquals(2, result.get("verified"));
        assertEquals(1, result.get("fake"));
    }

    @Test
    void getReviewCounts_WithoutProductId_ShouldReturnAllCounts() {
        // Given
        List<Review> allReviews = Arrays.asList(
            createReviewForProduct(1L, true, true, true, false),
            createReviewForProduct(2L, false, true, false, false),
            createReviewForProduct(3L, true, false, true, true)
        );

        when(reviewRepository.findAll()).thenReturn(allReviews);

        // When
        Map<String, Integer> result = adminReviewService.getReviewCounts(null);

        // Then
        assertNotNull(result);
        assertEquals(3, result.get("total"));
        assertEquals(2, result.get("approved"));
        assertEquals(1, result.get("pending"));
        assertEquals(2, result.get("visible"));
        assertEquals(1, result.get("hidden"));
        assertEquals(2, result.get("verified"));
        assertEquals(1, result.get("fake"));
    }

    @Test
    void updateReviewSettings_ShouldUpdateAllSettings() {
        // Given
        ReviewSettingsRequest request = new ReviewSettingsRequest();
        request.setAutoApprove(true);
        request.setRequireVerifiedPurchase(false);
        request.setMinimumReviewLength(5);
        request.setModerationEmails(Arrays.asList("admin@test.com"));

        // When
        ReviewSettingsDTO result = adminReviewService.updateReviewSettings(request);

        // Then
        assertNotNull(result);
        assertTrue(result.getAutoApprove());
        assertFalse(result.getRequireVerifiedPurchase());
        assertEquals(5, result.getMinimumReviewLength());
        assertEquals(1, result.getModerationEmails().size());
        assertEquals("admin@test.com", result.getModerationEmails().get(0));
    }

    @Test
    void getReviewSettings_ShouldReturnCurrentSettings() {
        // When
        ReviewSettingsDTO result = adminReviewService.getReviewSettings();

        // Then
        assertNotNull(result);
        assertFalse(result.getAutoApprove());
        assertTrue(result.getRequireVerifiedPurchase());
        assertEquals(10, result.getMinimumReviewLength());
        assertEquals(1, result.getModerationEmails().size());
        assertEquals("admin@suppkart.com", result.getModerationEmails().get(0));
    }

    @Test
    void getPendingReviews_ShouldReturnPendingReviewsPage() {
        // Given
        Page<Review> pendingReviews = new PageImpl<>(Arrays.asList(testReview));
        Pageable pageable = PageRequest.of(0, 20);
        
        when(reviewRepository.findPendingReviewsOrderByCreatedAtAsc(pageable)).thenReturn(pendingReviews);

        // When
        Page<ReviewDTO> result = adminReviewService.getPendingReviews(pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(reviewRepository).findPendingReviewsOrderByCreatedAtAsc(pageable);
    }

    // Helper methods
    private Review createReviewWithRating(int rating, boolean approved, boolean verified) {
        Review review = new Review();
        review.setRating(rating);
        review.setApproved(approved);
        review.setIsVisible(true);
        review.setVerified(verified);
        review.setCreatedAt(LocalDateTime.now());
        review.setProduct(testProduct);
        return review;
    }

    private Review createReviewForProduct(Long productId, boolean approved, boolean visible, 
                                        boolean verified, boolean fake) {
        Review review = new Review();
        Product product = new Product();
        product.setProductId(productId);
        review.setProduct(product);
        review.setApproved(approved);
        review.setIsVisible(visible);
        review.setVerified(verified);
        review.setCreatedByAdmin(fake);
        review.setRating(5);
        return review;
    }
}