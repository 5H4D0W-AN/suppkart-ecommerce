package com.suppkart.controller;

import com.suppkart.dto.content.SeoMetadataDTO;
import com.suppkart.dto.response.ApiResponse;
import com.suppkart.model.enums.PageType;
import com.suppkart.service.SeoMetadataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Public controller for fetching page content for frontend rendering
 */
@RestController
@RequestMapping("/api/public/pages")
@RequiredArgsConstructor
@Slf4j
public class PageContentController {

    private final SeoMetadataService seoMetadataService;

    /**
     * Get active content elements for a specific page type (for frontend
     * rendering)
     */
    @GetMapping("/{pageType}/content")
    public ResponseEntity<ApiResponse<List<SeoMetadataDTO>>> getPageContent(@PathVariable PageType pageType) {
        log.info("Fetching active content for page type: {}", pageType);

        List<SeoMetadataDTO> content = seoMetadataService.getActivePageContent(pageType);

        return ResponseEntity.ok(ApiResponse.success("Page content retrieved successfully", content));
    }
}
