package com.suppkart.controller;

import com.suppkart.dto.content.BannerDTO;
import com.suppkart.dto.content.BannerCreateRequest;

import com.suppkart.dto.content.BannerFilterRequest;
import com.suppkart.dto.content.BannerOrderRequest;
import com.suppkart.dto.response.ApiResponse;
import com.suppkart.service.BannerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Admin Banner Controller for managing banners
 * Requires ADMIN or CONTENT_MANAGER role for all operations
 */
@RestController
@RequestMapping("/api/admin/banners")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN') or hasRole('CONTENT_MANAGER')")
public class AdminBannerController {

    private final BannerService bannerService;

    /**
     * Create a new banner
     */
    @PostMapping
    public ResponseEntity<ApiResponse<BannerDTO>> createBanner(
            @Valid @RequestBody BannerCreateRequest request) {
        log.info("Creating new banner with title: {}", request.getTitle());
        
        BannerDTO banner = bannerService.createBanner(request);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Banner created successfully", banner));
    }

    /**
     * Update an existing banner
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BannerDTO>> updateBanner(
            @PathVariable Long id,
            @Valid @RequestBody BannerCreateRequest request) {
        log.info("Updating banner with ID: {}", id);
        
        BannerDTO banner = bannerService.updateBanner(id, request);
        
        return ResponseEntity.ok(ApiResponse.success("Banner updated successfully", banner));
    }

    /**
     * Get banner by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BannerDTO>> getBannerById(@PathVariable Long id) {
        log.info("Fetching banner with ID: {}", id);
        
        BannerDTO banner = bannerService.getBannerById(id);
        
        return ResponseEntity.ok(ApiResponse.success("Banner retrieved successfully", banner));
    }

    /**
     * Get all banners with filtering
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<BannerDTO>>> getAllBanners(
            @ModelAttribute BannerFilterRequest filter,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("Fetching all banners with filters");
        
        Pageable pageable = PageRequest.of(page, size);
        Page<BannerDTO> bannerPage = bannerService.getAllBanners(filter, pageable);
        
        return ResponseEntity.ok(ApiResponse.success("Banners retrieved successfully", bannerPage.getContent()));
    }

    /**
     * Delete a banner
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBanner(@PathVariable Long id) {
        log.info("Deleting banner with ID: {}", id);
        
        bannerService.deleteBanner(id);
        
        return ResponseEntity.ok(ApiResponse.success("Banner deleted successfully"));
    }

    /**
     * Activate a banner
     */
    @PatchMapping("/{id}/activate")
    public ResponseEntity<ApiResponse<Void>> activateBanner(@PathVariable Long id) {
        log.info("Activating banner with ID: {}", id);
        
        bannerService.activateBanner(id);
        
        return ResponseEntity.ok(ApiResponse.success("Banner activated successfully"));
    }

    /**
     * Deactivate a banner
     */
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<ApiResponse<Void>> deactivateBanner(@PathVariable Long id) {
        log.info("Deactivating banner with ID: {}", id);
        
        bannerService.deactivateBanner(id);
        
        return ResponseEntity.ok(ApiResponse.success("Banner deactivated successfully"));
    }

    /**
     * Upload banner image
     */
    @PostMapping("/images")
    public ResponseEntity<ApiResponse<String>> uploadBannerImage(
            @RequestParam("file") MultipartFile file) {
        log.info("Uploading banner image: {}", file.getOriginalFilename());
        
        String imageUrl = bannerService.uploadBannerImage(file);
        
        return ResponseEntity.ok(ApiResponse.success("Banner image uploaded successfully", imageUrl));
    }

    /**
     * Reorder banners
     */
    @PutMapping("/reorder")
    public ResponseEntity<ApiResponse<Void>> reorderBanners(
            @Valid @RequestBody List<BannerOrderRequest> orderRequests) {
        log.info("Reordering {} banners", orderRequests.size());
        
        bannerService.reorderBanners(orderRequests);
        
        return ResponseEntity.ok(ApiResponse.success("Banners reordered successfully"));
    }
}
