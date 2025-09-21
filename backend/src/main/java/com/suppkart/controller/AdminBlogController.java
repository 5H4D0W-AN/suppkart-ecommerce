package com.suppkart.controller;

import com.suppkart.dto.content.BlogPostDTO;
import com.suppkart.dto.content.BlogPostListItemDTO;
import com.suppkart.dto.content.BlogPostCreateRequest;
import com.suppkart.dto.content.BlogPostUpdateRequest;
import com.suppkart.dto.content.BlogPostFilterRequest;
import com.suppkart.dto.content.BlogCategoryDTO;
import com.suppkart.dto.content.BlogCategoryRequest;
import com.suppkart.dto.response.ApiResponse;
import com.suppkart.service.BlogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * Admin Blog Controller for managing blog posts and categories
 * Requires ADMIN or CONTENT_MANAGER role for all operations
 */
@RestController
@RequestMapping("/api/admin/blog")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN') or hasRole('CONTENT_MANAGER')")
public class AdminBlogController {

    private final BlogService blogService;

    // ==================== BLOG POST ENDPOINTS ====================

    /**
     * Create a new blog post
     */
    @PostMapping("/posts")
    public ResponseEntity<ApiResponse<BlogPostDTO>> createBlogPost(
            @Valid @RequestBody BlogPostCreateRequest request) {
        log.info("Creating new blog post with title: {}", request.getTitle());
        
        BlogPostDTO blogPost = blogService.createBlogPost(request);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Blog post created successfully", blogPost));
    }

    /**
     * Update an existing blog post
     */
    @PutMapping("/posts/{id}")
    public ResponseEntity<ApiResponse<BlogPostDTO>> updateBlogPost(
            @PathVariable Long id,
            @Valid @RequestBody BlogPostUpdateRequest request) {
        log.info("Updating blog post with ID: {}", id);
        
        BlogPostDTO blogPost = blogService.updateBlogPost(id, request);
        
        return ResponseEntity.ok(ApiResponse.success("Blog post updated successfully", blogPost));
    }

    /**
     * Get blog post by ID (for admin use)
     */
    @GetMapping("/posts/{id}")
    public ResponseEntity<ApiResponse<BlogPostDTO>> getBlogPostById(@PathVariable Long id) {
        log.info("Fetching blog post with ID: {}", id);
        
        BlogPostDTO blogPost = blogService.getBlogPostById(id);
        
        return ResponseEntity.ok(ApiResponse.success("Blog post retrieved successfully", blogPost));
    }

    /**
     * Get all blog posts with filtering and pagination
     */
    @GetMapping("/posts")
    public ResponseEntity<ApiResponse<Page<BlogPostListItemDTO>>> getAllBlogPosts(
            @ModelAttribute BlogPostFilterRequest filter,
            @PageableDefault(size = 20) Pageable pageable) {
        log.info("Fetching blog posts with filter: {}", filter);
        
        Page<BlogPostListItemDTO> blogPosts = blogService.getAllBlogPosts(filter, pageable);
        
        return ResponseEntity.ok(ApiResponse.success("Blog posts retrieved successfully", blogPosts));
    }

    /**
     * Delete a blog post
     */
    @DeleteMapping("/posts/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBlogPost(@PathVariable Long id) {
        log.info("Deleting blog post with ID: {}", id);
        
        blogService.deleteBlogPost(id);
        
        return ResponseEntity.ok(ApiResponse.success("Blog post deleted successfully"));
    }

    /**
     * Publish a blog post
     */
    @PatchMapping("/posts/{id}/publish")
    public ResponseEntity<ApiResponse<BlogPostDTO>> publishBlogPost(@PathVariable Long id) {
        log.info("Publishing blog post with ID: {}", id);
        
        BlogPostDTO blogPost = blogService.publishBlogPost(id);
        
        return ResponseEntity.ok(ApiResponse.success("Blog post published successfully", blogPost));
    }

    /**
     * Unpublish a blog post (change status to draft)
     */
    @PatchMapping("/posts/{id}/unpublish")
    public ResponseEntity<ApiResponse<BlogPostDTO>> unpublishBlogPost(@PathVariable Long id) {
        log.info("Unpublishing blog post with ID: {}", id);
        
        BlogPostDTO blogPost = blogService.unpublishBlogPost(id);
        
        return ResponseEntity.ok(ApiResponse.success("Blog post unpublished successfully", blogPost));
    }

    /**
     * Upload blog image
     */
    @PostMapping(value = "/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<String>> uploadBlogImage(
            @RequestParam("file") MultipartFile file) {
        log.info("Uploading blog image: {}", file.getOriginalFilename());
        
        try {
            String imageUrl = blogService.uploadBlogImage(file);
            return ResponseEntity.ok(ApiResponse.success("Image uploaded successfully", imageUrl));
        } catch (IOException e) {
            log.error("Failed to upload blog image: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to upload image: " + e.getMessage()));
        }
    }

    // ==================== BLOG CATEGORY ENDPOINTS ====================

    /**
     * Get all blog categories
     */
    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<BlogCategoryDTO>>> getAllBlogCategories() {
        log.info("Fetching all blog categories");
        
        List<BlogCategoryDTO> categories = blogService.getAllBlogCategories();
        
        return ResponseEntity.ok(ApiResponse.success("Blog categories retrieved successfully", categories));
    }

    /**
     * Create a new blog category
     */
    @PostMapping("/categories")
    public ResponseEntity<ApiResponse<BlogCategoryDTO>> createBlogCategory(
            @Valid @RequestBody BlogCategoryRequest request) {
        log.info("Creating new blog category with name: {}", request.getName());
        
        BlogCategoryDTO category = blogService.createBlogCategory(request);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Blog category created successfully", category));
    }

    /**
     * Update an existing blog category
     */
    @PutMapping("/categories/{id}")
    public ResponseEntity<ApiResponse<BlogCategoryDTO>> updateBlogCategory(
            @PathVariable Long id,
            @Valid @RequestBody BlogCategoryRequest request) {
        log.info("Updating blog category with ID: {}", id);
        
        BlogCategoryDTO category = blogService.updateBlogCategory(id, request);
        
        return ResponseEntity.ok(ApiResponse.success("Blog category updated successfully", category));
    }

    /**
     * Delete a blog category
     */
    @DeleteMapping("/categories/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBlogCategory(@PathVariable Long id) {
        log.info("Deleting blog category with ID: {}", id);
        
        blogService.deleteBlogCategory(id);
        
        return ResponseEntity.ok(ApiResponse.success("Blog category deleted successfully"));
    }
}
