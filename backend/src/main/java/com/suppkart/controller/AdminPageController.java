package com.suppkart.controller;

import com.suppkart.dto.content.PageDTO;
import com.suppkart.dto.content.PageCreateRequest;
import com.suppkart.dto.content.PageUpdateRequest;
import com.suppkart.dto.response.ApiResponse;
import com.suppkart.service.PageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Admin Page Controller for managing static pages
 * Requires ADMIN or CONTENT_MANAGER role for all operations
 */
@RestController
@RequestMapping("/api/admin/pages")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN') or hasRole('CONTENT_MANAGER')")
public class AdminPageController {

    private final PageService pageService;

    /**
     * Create a new page
     */
    @PostMapping
    public ResponseEntity<ApiResponse<PageDTO>> createPage(
            @Valid @RequestBody PageCreateRequest request) {
        log.info("Creating new page with title: {}", request.getTitle());
        
        PageDTO page = pageService.createPage(request);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Page created successfully", page));
    }

    /**
     * Update an existing page
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PageDTO>> updatePage(
            @PathVariable Long id,
            @Valid @RequestBody PageUpdateRequest request) {
        log.info("Updating page with ID: {}", id);
        
        PageDTO page = pageService.updatePage(id, request);
        
        return ResponseEntity.ok(ApiResponse.success("Page updated successfully", page));
    }

    /**
     * Get page by ID (for admin use)
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PageDTO>> getPageById(@PathVariable Long id) {
        log.info("Fetching page with ID: {}", id);
        
        PageDTO page = pageService.getPageById(id);
        
        return ResponseEntity.ok(ApiResponse.success("Page retrieved successfully", page));
    }

    /**
     * Get all pages (for admin use)
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<PageDTO>>> getAllPages() {
        log.info("Fetching all pages");
        
        List<PageDTO> pages = pageService.getAllPages();
        
        return ResponseEntity.ok(ApiResponse.success("Pages retrieved successfully", pages));
    }

    /**
     * Delete a page
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePage(@PathVariable Long id) {
        log.info("Deleting page with ID: {}", id);
        
        pageService.deletePage(id);
        
        return ResponseEntity.ok(ApiResponse.success("Page deleted successfully"));
    }

    /**
     * Publish a page
     */
    @PatchMapping("/{id}/publish")
    public ResponseEntity<ApiResponse<PageDTO>> publishPage(@PathVariable Long id) {
        log.info("Publishing page with ID: {}", id);
        
        PageDTO page = pageService.publishPage(id);
        
        return ResponseEntity.ok(ApiResponse.success("Page published successfully", page));
    }

    /**
     * Unpublish a page (change status to draft)
     */
    @PatchMapping("/{id}/unpublish")
    public ResponseEntity<ApiResponse<PageDTO>> unpublishPage(@PathVariable Long id) {
        log.info("Unpublishing page with ID: {}", id);
        
        PageDTO page = pageService.unpublishPage(id);
        
        return ResponseEntity.ok(ApiResponse.success("Page unpublished successfully", page));
    }

    /**
     * Get page by slug (for admin preview)
     */
    @GetMapping("/by-slug/{slug}")
    public ResponseEntity<ApiResponse<PageDTO>> getPageBySlug(@PathVariable String slug) {
        log.info("Fetching page with slug: {}", slug);
        
        PageDTO page = pageService.getPublishedPageBySlug(slug);
        
        return ResponseEntity.ok(ApiResponse.success("Page retrieved successfully", page));
    }
}
