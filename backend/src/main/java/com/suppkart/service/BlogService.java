package com.suppkart.service;

import com.suppkart.dto.content.*;
import com.suppkart.exception.ResourceNotFoundException;
import com.suppkart.model.entity.BlogCategory;
import com.suppkart.model.entity.BlogPost;
import com.suppkart.model.entity.User;
import com.suppkart.model.enums.BlogPostStatus;
import com.suppkart.repository.BlogCategoryRepository;
import com.suppkart.repository.BlogPostRepository;
import com.suppkart.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class BlogService {

    private static final Logger logger = LoggerFactory.getLogger(BlogService.class);

    @Autowired
    private BlogPostRepository blogPostRepository;

    @Autowired
    private BlogCategoryRepository blogCategoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SlugGenerator slugGenerator;

    @Autowired
    private StorageService storageService;

    /**
     * Create a new blog post
     */
    public BlogPostDTO createBlogPost(BlogPostCreateRequest request) {
        logger.info("Creating new blog post with title: {}", request.getTitle());

        // Get author
        User author = userRepository.findById(request.getAuthorId())
                .orElseThrow(() -> new ResourceNotFoundException("Author not found with id: " + request.getAuthorId()));

        // Generate unique slug
        String slug = slugGenerator.generateUniqueSlug(
                request.getTitle(),
                blogPostRepository::existsBySlug
        );

        // Get categories
        Set<BlogCategory> categories = new HashSet<>();
        if (request.getCategoryIds() != null && !request.getCategoryIds().isEmpty()) {
            categories = request.getCategoryIds().stream()
                    .map(id -> blogCategoryRepository.findById(id)
                            .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id)))
                    .collect(Collectors.toSet());
        }

        // Create blog post
        BlogPost blogPost = new BlogPost();
        blogPost.setTitle(request.getTitle());
        blogPost.setSlug(slug);
        blogPost.setContent(request.getContent());
        blogPost.setFeaturedImage(request.getFeaturedImage());
        blogPost.setAuthor(author);
        blogPost.setStatus(request.getStatus() != null ? request.getStatus() : BlogPostStatus.DRAFT);
        blogPost.setPublishDate(request.getPublishDate());
        blogPost.setCategories(categories);
        blogPost.setTags(request.getTags() != null ? request.getTags() : new HashSet<>());
        blogPost.setMetaTitle(request.getMetaTitle());
        blogPost.setMetaDescription(request.getMetaDescription());
        blogPost.setMetaKeywords(request.getMetaKeywords());
        blogPost.setViews(0);

        BlogPost savedPost = blogPostRepository.save(blogPost);
        logger.info("Blog post created successfully with id: {}", savedPost.getId());

        return convertToDTO(savedPost);
    }

    /**
     * Update an existing blog post
     */
    public BlogPostDTO updateBlogPost(Long id, BlogPostUpdateRequest request) {
        logger.info("Updating blog post with id: {}", id);

        BlogPost blogPost = blogPostRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Blog post not found with id: " + id));

        // Update slug if title changed
        if (request.getTitle() != null && !request.getTitle().equals(blogPost.getTitle())) {
            String newSlug = slugGenerator.updateSlugIfNeeded(
                    blogPost.getSlug(),
                    request.getTitle(),
                    slug -> blogPostRepository.existsBySlug(slug) && !slug.equals(blogPost.getSlug())
            );
            blogPost.setSlug(newSlug);
            blogPost.setTitle(request.getTitle());
        }

        // Update other fields
        if (request.getContent() != null) {
            blogPost.setContent(request.getContent());
        }
        if (request.getFeaturedImage() != null) {
            blogPost.setFeaturedImage(request.getFeaturedImage());
        }
        if (request.getStatus() != null) {
            blogPost.setStatus(request.getStatus());
        }
        if (request.getPublishDate() != null) {
            blogPost.setPublishDate(request.getPublishDate());
        }
        if (request.getMetaTitle() != null) {
            blogPost.setMetaTitle(request.getMetaTitle());
        }
        if (request.getMetaDescription() != null) {
            blogPost.setMetaDescription(request.getMetaDescription());
        }
        if (request.getMetaKeywords() != null) {
            blogPost.setMetaKeywords(request.getMetaKeywords());
        }
        if (request.getTags() != null) {
            blogPost.setTags(request.getTags());
        }

        // Update categories
        if (request.getCategoryIds() != null) {
            Set<BlogCategory> categories = request.getCategoryIds().stream()
                    .map(categoryId -> blogCategoryRepository.findById(categoryId)
                            .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + categoryId)))
                    .collect(Collectors.toSet());
            blogPost.setCategories(categories);
        }

        BlogPost updatedPost = blogPostRepository.save(blogPost);
        logger.info("Blog post updated successfully with id: {}", updatedPost.getId());

        return convertToDTO(updatedPost);
    }

    /**
     * Get published blog post by slug (for public access)
     */
    @Transactional(readOnly = true)
    public BlogPostDTO getPublishedBlogPostBySlug(String slug) {
        BlogPost blogPost = blogPostRepository.findBySlugAndStatus(slug, BlogPostStatus.PUBLISHED)
                .orElseThrow(() -> new ResourceNotFoundException("Published blog post not found with slug: " + slug));

        // Increment view count
        blogPost.setViews(blogPost.getViews() + 1);
        blogPostRepository.save(blogPost);

        return convertToDTO(blogPost);
    }

    /**
     * Get blog post by ID (for admin access)
     */
    @Transactional(readOnly = true)
    public BlogPostDTO getBlogPostById(Long id) {
        BlogPost blogPost = blogPostRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Blog post not found with id: " + id));

        return convertToDTO(blogPost);
    }

    /**
     * Get all blog posts with filtering and pagination
     */
    @Transactional(readOnly = true)
    public Page<BlogPostListItemDTO> getAllBlogPosts(BlogPostFilterRequest filter, Pageable pageable) {
        Page<BlogPost> blogPosts;

        if (filter.getStatus() != null) {
            blogPosts = blogPostRepository.findByStatusOrderByCreatedAtDesc(filter.getStatus(), pageable);
        } else if (filter.getTitle() != null && !filter.getTitle().trim().isEmpty()) {
            blogPosts = blogPostRepository.findByTitleContainingIgnoreCase(filter.getTitle(), pageable);
        } else if (filter.getCategoryId() != null) {
            BlogCategory category = blogCategoryRepository.findById(filter.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + filter.getCategoryId()));
            blogPosts = blogPostRepository.findByCategoriesContaining(category, pageable);
        } else {
            blogPosts = blogPostRepository.findAllByOrderByCreatedAtDesc(pageable);
        }

        return blogPosts.map(this::convertToListItemDTO);
    }

    /**
     * Get published blog posts (for public access)
     */
    @Transactional(readOnly = true)
    public Page<BlogPostListItemDTO> getPublishedBlogPosts(Pageable pageable) {
        LocalDateTime now = LocalDateTime.now();
        Page<BlogPost> blogPosts = blogPostRepository.findByStatusAndPublishDateLessThanEqualOrderByPublishDateDesc(
                BlogPostStatus.PUBLISHED, now, pageable);

        return blogPosts.map(this::convertToListItemDTO);
    }

    /**
     * Delete blog post
     */
    public void deleteBlogPost(Long id) {
        logger.info("Deleting blog post with id: {}", id);

        BlogPost blogPost = blogPostRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Blog post not found with id: " + id));

        // Delete featured image if exists
        if (blogPost.getFeaturedImage() != null && !blogPost.getFeaturedImage().isEmpty()) {
            storageService.deleteFile(blogPost.getFeaturedImage());
        }

        blogPostRepository.delete(blogPost);
        logger.info("Blog post deleted successfully with id: {}", id);
    }

    /**
     * Publish blog post
     */
    public BlogPostDTO publishBlogPost(Long id) {
        logger.info("Publishing blog post with id: {}", id);

        BlogPost blogPost = blogPostRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Blog post not found with id: " + id));

        blogPost.setStatus(BlogPostStatus.PUBLISHED);
        if (blogPost.getPublishDate() == null) {
            blogPost.setPublishDate(LocalDateTime.now());
        }

        BlogPost publishedPost = blogPostRepository.save(blogPost);
        logger.info("Blog post published successfully with id: {}", publishedPost.getId());

        return convertToDTO(publishedPost);
    }

    /**
     * Unpublish blog post (change to draft)
     */
    public BlogPostDTO unpublishBlogPost(Long id) {
        logger.info("Unpublishing blog post with id: {}", id);

        BlogPost blogPost = blogPostRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Blog post not found with id: " + id));

        blogPost.setStatus(BlogPostStatus.DRAFT);

        BlogPost unpublishedPost = blogPostRepository.save(blogPost);
        logger.info("Blog post unpublished successfully with id: {}", unpublishedPost.getId());

        return convertToDTO(unpublishedPost);
    }

    /**
     * Get all blog categories
     */
    @Transactional(readOnly = true)
    public List<BlogCategoryDTO> getAllBlogCategories() {
        List<BlogCategory> categories = blogCategoryRepository.findAllByOrderByNameAsc();
        return categories.stream()
                .map(this::convertCategoryToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Create blog category
     */
    public BlogCategoryDTO createBlogCategory(BlogCategoryRequest request) {
        logger.info("Creating new blog category with name: {}", request.getName());

        // Check if category name already exists
        if (blogCategoryRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException("Category with name '" + request.getName() + "' already exists");
        }

        // Generate unique slug
        String slug = slugGenerator.generateUniqueSlug(
                request.getName(),
                blogCategoryRepository::existsBySlug
        );

        BlogCategory category = new BlogCategory();
        category.setName(request.getName());
        category.setSlug(slug);
        category.setDescription(request.getDescription());

        BlogCategory savedCategory = blogCategoryRepository.save(category);
        logger.info("Blog category created successfully with id: {}", savedCategory.getId());

        return convertCategoryToDTO(savedCategory);
    }

    /**
     * Update blog category
     */
    public BlogCategoryDTO updateBlogCategory(Long id, BlogCategoryRequest request) {
        logger.info("Updating blog category with id: {}", id);

        BlogCategory category = blogCategoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Blog category not found with id: " + id));

        // Check if new name conflicts with existing categories
        if (!category.getName().equals(request.getName()) && 
            blogCategoryRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException("Category with name '" + request.getName() + "' already exists");
        }

        // Update slug if name changed
        if (!category.getName().equals(request.getName())) {
            String newSlug = slugGenerator.updateSlugIfNeeded(
                    category.getSlug(),
                    request.getName(),
                    slug -> blogCategoryRepository.existsBySlug(slug) && !slug.equals(category.getSlug())
            );
            category.setSlug(newSlug);
        }

        category.setName(request.getName());
        category.setDescription(request.getDescription());

        BlogCategory updatedCategory = blogCategoryRepository.save(category);
        logger.info("Blog category updated successfully with id: {}", updatedCategory.getId());

        return convertCategoryToDTO(updatedCategory);
    }

    /**
     * Delete blog category
     */
    public void deleteBlogCategory(Long id) {
        logger.info("Deleting blog category with id: {}", id);

        BlogCategory category = blogCategoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Blog category not found with id: " + id));

        // Check if category is used by any blog posts
        List<BlogPost> postsUsingCategory = blogPostRepository.findByCategoriesContaining(category);
        if (!postsUsingCategory.isEmpty()) {
            throw new IllegalArgumentException("Cannot delete category that is used by " + postsUsingCategory.size() + " blog posts");
        }

        blogCategoryRepository.delete(category);
        logger.info("Blog category deleted successfully with id: {}", id);
    }

    /**
     * Upload blog image
     */
    public String uploadBlogImage(MultipartFile file) throws IOException {
        logger.info("Uploading blog image: {}", file.getOriginalFilename());

        String imageUrl = storageService.uploadImage(file, "blog/images");
        logger.info("Blog image uploaded successfully: {}", imageUrl);

        return imageUrl;
    }

    /**
     * Convert BlogPost entity to DTO
     */
    private BlogPostDTO convertToDTO(BlogPost blogPost) {
        BlogPostDTO dto = new BlogPostDTO();
        dto.setId(blogPost.getId());
        dto.setTitle(blogPost.getTitle());
        dto.setSlug(blogPost.getSlug());
        dto.setContent(blogPost.getContent());
        dto.setFeaturedImage(blogPost.getFeaturedImage());
        dto.setStatus(blogPost.getStatus());
        dto.setPublishDate(blogPost.getPublishDate());
        dto.setCreatedAt(blogPost.getCreatedAt());
        dto.setUpdatedAt(blogPost.getUpdatedAt());
        dto.setTags(blogPost.getTags());
        dto.setMetaTitle(blogPost.getMetaTitle());
        dto.setMetaDescription(blogPost.getMetaDescription());
        dto.setMetaKeywords(blogPost.getMetaKeywords());
        dto.setViews(blogPost.getViews());

        // Set author info
        if (blogPost.getAuthor() != null) {
            AuthorDTO authorInfo = new AuthorDTO();
            authorInfo.setId(blogPost.getAuthor().getUserId());
            authorInfo.setName(blogPost.getAuthor().getFirstName() + " " + blogPost.getAuthor().getLastName());
            authorInfo.setEmail(blogPost.getAuthor().getEmail());
            dto.setAuthor(authorInfo);
        }

        // Set categories
        if (blogPost.getCategories() != null) {
            dto.setCategories(blogPost.getCategories().stream()
                    .map(this::convertCategoryToDTO)
                    .collect(Collectors.toSet()));
        }

        return dto;
    }

    /**
     * Convert BlogPost entity to list item DTO
     */
    private BlogPostListItemDTO convertToListItemDTO(BlogPost blogPost) {
        BlogPostListItemDTO dto = new BlogPostListItemDTO();
        dto.setId(blogPost.getId());
        dto.setTitle(blogPost.getTitle());
        dto.setSlug(blogPost.getSlug());
        dto.setFeaturedImage(blogPost.getFeaturedImage());
        dto.setStatus(blogPost.getStatus());
        dto.setPublishDate(blogPost.getPublishDate());
        dto.setCreatedAt(blogPost.getCreatedAt());
        dto.setViews(blogPost.getViews());

        // Set author name
        if (blogPost.getAuthor() != null) {
            dto.setAuthorName(blogPost.getAuthor().getFirstName() + " " + blogPost.getAuthor().getLastName());
        }

        // Set category names
        if (blogPost.getCategories() != null) {
            dto.setCategoryNames(blogPost.getCategories().stream()
                    .map(BlogCategory::getName)
                    .collect(Collectors.toSet()));
        }

        // Set excerpt (first 200 characters of content without HTML)
        if (blogPost.getContent() != null) {
            String plainText = blogPost.getContent().replaceAll("<[^>]*>", "");
            dto.setExcerpt(plainText.length() > 200 ? plainText.substring(0, 200) + "..." : plainText);
        }

        return dto;
    }

    /**
     * Convert BlogCategory entity to DTO
     */
    private BlogCategoryDTO convertCategoryToDTO(BlogCategory category) {
        BlogCategoryDTO dto = new BlogCategoryDTO();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setSlug(category.getSlug());
        dto.setDescription(category.getDescription());
        dto.setCreatedAt(category.getCreatedAt());
        dto.setUpdatedAt(category.getUpdatedAt());
        return dto;
    }
}
