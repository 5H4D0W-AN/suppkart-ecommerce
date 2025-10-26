package com.suppkart.controller;

import com.suppkart.dto.content.*;
import com.suppkart.dto.response.ApiResponse;
import com.suppkart.model.enums.PageType;
import com.suppkart.service.FileUploadService;
import com.suppkart.service.SeoMetadataService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Admin Page Controller for managing dynamic page content and SEO metadata
 * Requires ADMIN or CONTENT_MANAGER role for all operations
 */
@RestController
@RequestMapping("/api/admin/pages")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN') or hasRole('CONTENT_MANAGER')")
public class AdminPageController {

    private final SeoMetadataService seoMetadataService;
    private final FileUploadService fileUploadService;

    /**
     * Get all available page types for management
     */
    @GetMapping("/types")
    public ResponseEntity<ApiResponse<List<PageType>>> getAllPageTypes() {
        log.info("Fetching all available page types");

        List<PageType> pageTypes = seoMetadataService.getAllPageTypes();

        return ResponseEntity.ok(ApiResponse.success("Page types retrieved successfully", pageTypes));
    }

    /**
     * Get all content elements for a specific page type
     */
    @GetMapping("/{pageType}")
    public ResponseEntity<ApiResponse<PageContentResponse>> getPageContent(@PathVariable PageType pageType) {
        log.info("Fetching content for page type: {}", pageType);

        PageContentResponse pageContent = seoMetadataService.getPageContent(pageType);

        return ResponseEntity.ok(ApiResponse.success("Page content retrieved successfully", pageContent));
    }

    /**
     * Create a new content element for a page
     */
    @PostMapping("/{pageType}/elements")
    public ResponseEntity<ApiResponse<SeoMetadataDTO>> createElement(
            @PathVariable PageType pageType,
            @Valid @RequestBody SeoMetadataRequest request) {
        log.info("Creating new element for page type: {} with key: {}", pageType, request.getElementKey());

        // Ensure the page type matches the path parameter
        request.setPageType(pageType);

        SeoMetadataDTO element = seoMetadataService.createElement(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Element created successfully", element));
    }

    /**
     * Update an existing content element
     */
    @PutMapping("/elements/{id}")
    public ResponseEntity<ApiResponse<SeoMetadataDTO>> updateElement(
            @PathVariable Long id,
            @Valid @RequestBody SeoMetadataRequest request) {
        log.info("Updating element with ID: {}", id);

        SeoMetadataDTO element = seoMetadataService.updateElement(id, request);

        return ResponseEntity.ok(ApiResponse.success("Element updated successfully", element));
    }

    /**
     * Upload media file for page content
     */
    @PostMapping("/{pageType}/upload")
    public ResponseEntity<ApiResponse<String>> uploadMedia(
            @PathVariable PageType pageType,
            @RequestParam("file") MultipartFile file) {
        log.info("Uploading media file for page type: {}", pageType);

        // Create subdirectory based on page type (e.g., "default_path/AboutUs/image.jpg")
        String subDirectory = pageType.name().toLowerCase().replace("_", "");
        String fileUrl = fileUploadService.uploadFile(file, subDirectory);

        return ResponseEntity.ok(ApiResponse.success("File uploaded successfully", fileUrl));
    }

    /**
     * Get element by ID (for editing)
     */
    @GetMapping("/elements/{id}")
    public ResponseEntity<ApiResponse<SeoMetadataDTO>> getElementById(@PathVariable Long id) {
        log.info("Fetching element with ID: {}", id);

        SeoMetadataDTO element = seoMetadataService.getElementById(id);

        return ResponseEntity.ok(ApiResponse.success("Element retrieved successfully", element));
    }

    /**
     * Delete a content element
     */
    @DeleteMapping("/elements/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteElement(@PathVariable Long id) {
        log.info("Deleting element with ID: {}", id);

        seoMetadataService.deleteElement(id);

        return ResponseEntity.ok(ApiResponse.success("Element deleted successfully"));
    }
}
