package com.suppkart.controller;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.suppkart.dto.request.ReviewSubmitRequest;
import com.suppkart.dto.response.ApiResponse;
import com.suppkart.dto.response.ReviewDto;
import com.suppkart.security.JwtTokenProvider;
import com.suppkart.service.ReviewService;

@RestController
@RequestMapping("/api/products/{productId}/reviews")
@Validated
public class ReviewController {
    
    private final ReviewService reviewService;
    private final JwtTokenProvider jwtTokenProvider;
    
    public ReviewController(ReviewService reviewService, JwtTokenProvider jwtTokenProvider) {
        this.reviewService = reviewService;
        this.jwtTokenProvider = jwtTokenProvider;
    }
    
    @PostMapping
    public ResponseEntity<ApiResponse<ReviewDto>> submitReview(
            @PathVariable Long productId,
            @RequestBody ReviewSubmitRequest request,
            @RequestHeader("Authorization") String authHeader) {
        
        Long userId = jwtTokenProvider.getUserIdFromToken(authHeader.replace("Bearer ", ""));
        ReviewDto reviewDto = reviewService.submitReview(productId, userId, request);
        
        ApiResponse<ReviewDto> response = new ApiResponse<>(
            true, 
            "Review submitted successfully", 
            reviewDto
        );
        
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    
    @GetMapping
    public ResponseEntity<ApiResponse<Page<ReviewDto>>> getProductReviews(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        
        Page<ReviewDto> reviews = reviewService.getProductReviews(productId, page, size);
        
        ApiResponse<Page<ReviewDto>> response = new ApiResponse<>(
            true, 
            "Reviews retrieved successfully", 
            reviews
        );
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/by-rating/{rating}")
    public ResponseEntity<ApiResponse<Page<ReviewDto>>> getProductReviewsByRating(
            @PathVariable Long productId,
            @PathVariable Integer rating,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        
        Page<ReviewDto> reviews = reviewService.getProductReviewsByRating(productId, rating, page, size);
        
        ApiResponse<Page<ReviewDto>> response = new ApiResponse<>(
            true, 
            "Reviews retrieved successfully", 
            reviews
        );
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/verified")
    public ResponseEntity<ApiResponse<Page<ReviewDto>>> getVerifiedProductReviews(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        
        Page<ReviewDto> reviews = reviewService.getVerifiedProductReviews(productId, page, size);
        
        ApiResponse<Page<ReviewDto>> response = new ApiResponse<>(
            true, 
            "Verified reviews retrieved successfully", 
            reviews
        );
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/most-helpful")
    public ResponseEntity<ApiResponse<Page<ReviewDto>>> getMostHelpfulReviews(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        
        Page<ReviewDto> reviews = reviewService.getMostHelpfulReviews(productId, page, size);
        
        ApiResponse<Page<ReviewDto>> response = new ApiResponse<>(
            true, 
            "Most helpful reviews retrieved successfully", 
            reviews
        );
        
        return ResponseEntity.ok(response);
    }
}
