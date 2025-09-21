package com.suppkart.controller;

import com.suppkart.dto.content.BlogCategoryDTO;
import com.suppkart.dto.content.BlogPostDTO;
import com.suppkart.dto.content.BlogPostFilterRequest;
import com.suppkart.dto.content.BlogPostListItemDTO;
import com.suppkart.dto.response.ApiResponse;
import com.suppkart.model.enums.BlogPostStatus;
import com.suppkart.service.BlogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Public Blog Controller for frontend blog functionality
 * Provides public access to published blog content
 */
@RestController
@RequestMapping("/api/blog")
@RequiredArgsConstructor
@Slf4j
public class PublicBlogController {

    private final BlogService blogService;

    /**
     * Get published blog posts with pagination and filtering
     */
    @GetMapping("/posts")
    public ResponseEntity<ApiResponse<Page<BlogPostListItemDTO>>> getPublishedBlogPosts(
            BlogPostFilterRequest filter,
            @PageableDefault(size = 10) Pageable pageable) {
        log.info("Fetching published blog posts with filter: {}", filter);
        
        // Ensure we only get published posts for public access
        if (filter == null) {
            filter = new BlogPostFilterRequest();
        }
        filter.setStatus(BlogPostStatus.PUBLISHED);
        
        Page<BlogPostListItemDTO> blogPosts = blogService.getAllBlogPosts(filter, pageable);
        
        return ResponseEntity.ok(ApiResponse.success("Published blog posts retrieved successfully", blogPosts));
    }

    /**
     * Get published blog post by slug
     */
    @GetMapping("/posts/{slug}")
    public ResponseEntity<ApiResponse<BlogPostDTO>> getPublishedBlogPostBySlug(@PathVariable String slug) {
        log.info("Fetching published blog post by slug: {}", slug);
        
        BlogPostDTO blogPost = blogService.getPublishedBlogPostBySlug(slug);
        
        return ResponseEntity.ok(ApiResponse.success("Blog post retrieved successfully", blogPost));
    }

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
     * Get published blog posts by category slug
     */
    @GetMapping("/categories/{slug}/posts")
    public ResponseEntity<ApiResponse<Page<BlogPostListItemDTO>>> getPostsByCategory(
            @PathVariable String slug,
            @PageableDefault(size = 10) Pageable pageable) {
        log.info("Fetching published blog posts for category slug: {}", slug);
        
        BlogPostFilterRequest filter = new BlogPostFilterRequest();
        filter.setStatus(BlogPostStatus.PUBLISHED);
        filter.setCategorySlug(slug);
        
        Page<BlogPostListItemDTO> blogPosts = blogService.getAllBlogPosts(filter, pageable);
        
        return ResponseEntity.ok(ApiResponse.success("Blog posts for category retrieved successfully", blogPosts));
    }

    /**
     * Search published blog posts
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<BlogPostListItemDTO>>> searchBlogPosts(
            @RequestParam String query,
            @PageableDefault(size = 10) Pageable pageable) {
        log.info("Searching published blog posts with query: {}", query);
        
        BlogPostFilterRequest filter = new BlogPostFilterRequest();
        filter.setStatus(BlogPostStatus.PUBLISHED);
        filter.setSearchQuery(query);
        
        Page<BlogPostListItemDTO> blogPosts = blogService.getAllBlogPosts(filter, pageable);
        
        return ResponseEntity.ok(ApiResponse.success("Blog search results retrieved successfully", blogPosts));
    }

    /**
     * Get recent blog posts (for homepage, sidebar, etc.)
     */
    @GetMapping("/recent")
    public ResponseEntity<ApiResponse<List<BlogPostListItemDTO>>> getRecentBlogPosts(
            @RequestParam(defaultValue = "5") int limit) {
        log.info("Fetching {} recent blog posts", limit);
        
        BlogPostFilterRequest filter = new BlogPostFilterRequest();
        filter.setStatus(BlogPostStatus.PUBLISHED);
        
        Page<BlogPostListItemDTO> blogPosts = blogService.getAllBlogPosts(filter, 
                Pageable.ofSize(limit));
        
        return ResponseEntity.ok(ApiResponse.success("Recent blog posts retrieved successfully", 
                blogPosts.getContent()));
    }

    /**
     * Get popular blog posts (based on views)
     */
    @GetMapping("/popular")
    public ResponseEntity<ApiResponse<List<BlogPostListItemDTO>>> getPopularBlogPosts(
            @RequestParam(defaultValue = "5") int limit) {
        log.info("Fetching {} popular blog posts", limit);
        
        BlogPostFilterRequest filter = new BlogPostFilterRequest();
        filter.setStatus(BlogPostStatus.PUBLISHED);
        filter.setSortBy("views");
        filter.setSortDirection("desc");
        
        Page<BlogPostListItemDTO> blogPosts = blogService.getAllBlogPosts(filter, 
                Pageable.ofSize(limit));
        
        return ResponseEntity.ok(ApiResponse.success("Popular blog posts retrieved successfully", 
                blogPosts.getContent()));
    }

}
