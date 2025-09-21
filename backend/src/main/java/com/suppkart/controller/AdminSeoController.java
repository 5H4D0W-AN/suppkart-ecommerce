package com.suppkart.controller;

import com.suppkart.dto.content.SeoMetadataDTO;
import com.suppkart.dto.content.SeoMetadataRequest;
import com.suppkart.dto.response.ApiResponse;
import com.suppkart.service.SeoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Admin SEO Controller for managing SEO metadata
 * Requires ADMIN or CONTENT_MANAGER role for all operations
 */
@RestController
@RequestMapping("/api/admin/seo")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN') or hasRole('CONTENT_MANAGER')")
public class AdminSeoController {

    private final SeoService seoService;

    /**
     * Create or update SEO metadata for a specific page type and entity
     */
    @PostMapping("/{pageType}/{entityId}")
    public ResponseEntity<ApiResponse<SeoMetadataDTO>> createOrUpdateSeoMetadata(
            @PathVariable String pageType,
            @PathVariable Long entityId,
            @Valid @RequestBody SeoMetadataRequest request) {
        log.info("Creating/updating SEO metadata for pageType: {} and entityId: {}", pageType, entityId);
        
        SeoMetadataDTO seoMetadata = seoService.createOrUpdateSeoMetadata(pageType, entityId, request);
        
        return ResponseEntity.ok(ApiResponse.success("SEO metadata saved successfully", seoMetadata));
    }

    /**
     * Get SEO metadata by page type and entity ID
     */
    @GetMapping("/{pageType}/{entityId}")
    public ResponseEntity<ApiResponse<SeoMetadataDTO>> getSeoMetadata(
            @PathVariable String pageType,
            @PathVariable Long entityId) {
        log.info("Fetching SEO metadata for pageType: {} and entityId: {}", pageType, entityId);
        
        SeoMetadataDTO seoMetadata = seoService.getSeoMetadataByPageTypeAndEntityId(pageType, entityId);
        
        return ResponseEntity.ok(ApiResponse.success("SEO metadata retrieved successfully", seoMetadata));
    }

    /**
     * Get all SEO metadata for a specific page type
     */
    @GetMapping("/{pageType}")
    public ResponseEntity<ApiResponse<List<SeoMetadataDTO>>> getSeoMetadataByPageType(
            @PathVariable String pageType) {
        log.info("Fetching all SEO metadata for pageType: {}", pageType);
        
        List<SeoMetadataDTO> seoMetadataList = seoService.getSeoMetadataByPageType(pageType);
        
        return ResponseEntity.ok(ApiResponse.success("SEO metadata list retrieved successfully", seoMetadataList));
    }

    /**
     * Delete SEO metadata by ID
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteSeoMetadata(@PathVariable Long id) {
        log.info("Deleting SEO metadata with ID: {}", id);
        
        seoService.deleteSeoMetadata(id);
        
        return ResponseEntity.ok(ApiResponse.success("SEO metadata deleted successfully"));
    }
}
