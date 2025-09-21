package com.suppkart.controller;

import com.suppkart.dto.content.PageDTO;
import com.suppkart.dto.response.ApiResponse;
import com.suppkart.service.PageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Public Page Controller for frontend static page functionality
 * Provides public access to published static pages
 */
@RestController
@RequestMapping("/api/pages")
@RequiredArgsConstructor
@Slf4j
public class PublicPageController {

    private final PageService pageService;

    /**
     * Get published page by slug
     */
    @GetMapping("/{slug}")
    public ResponseEntity<ApiResponse<PageDTO>> getPublishedPageBySlug(@PathVariable String slug) {
        log.info("Fetching published page by slug: {}", slug);
        
        PageDTO page = pageService.getPublishedPageBySlug(slug);
        
        return ResponseEntity.ok(ApiResponse.success("Page retrieved successfully", page));
    }

    /**
     * Get page by slug with SEO metadata
     * This endpoint includes SEO metadata for better search engine optimization
     */
    @GetMapping("/{slug}/seo")
    public ResponseEntity<ApiResponse<PageDTO>> getPublishedPageWithSeo(@PathVariable String slug) {
        log.info("Fetching published page with SEO metadata by slug: {}", slug);
        
        PageDTO page = pageService.getPublishedPageBySlug(slug);
        
        // Note: SEO metadata would be included in the PageDTO if needed
        // This could be enhanced to include Open Graph tags, meta descriptions, etc.
        
        return ResponseEntity.ok(ApiResponse.success("Page with SEO metadata retrieved successfully", page));
    }

    /**
     * Check if a page exists by slug (useful for navigation menus)
     */
    @GetMapping("/{slug}/exists")
    public ResponseEntity<ApiResponse<Boolean>> checkPageExists(@PathVariable String slug) {
        log.info("Checking if page exists with slug: {}", slug);
        
        try {
            pageService.getPublishedPageBySlug(slug);
            return ResponseEntity.ok(ApiResponse.success("Page exists", true));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.success("Page does not exist", false));
        }
    }

    /**
     * Get page content only (without metadata) - useful for AJAX requests
     */
    @GetMapping("/{slug}/content")
    public ResponseEntity<ApiResponse<String>> getPageContent(@PathVariable String slug) {
        log.info("Fetching page content only for slug: {}", slug);
        
        PageDTO page = pageService.getPublishedPageBySlug(slug);
        
        return ResponseEntity.ok(ApiResponse.success("Page content retrieved successfully", page.getContent()));
    }
}
