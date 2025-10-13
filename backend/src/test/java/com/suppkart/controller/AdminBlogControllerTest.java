package com.suppkart.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.suppkart.dto.content.*;
import com.suppkart.exception.ResourceNotFoundException;
import com.suppkart.model.enums.BlogPostStatus;
import com.suppkart.model.enums.ContentType;
import com.suppkart.service.BlogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("AdminBlogController Tests")
class AdminBlogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BlogService blogService;

    @Autowired
    private ObjectMapper objectMapper;

    private BlogPostCreateRequest createRequest;
    private BlogPostDTO blogPostDTO;
    private BlogPostListItemDTO blogPostListItemDTO;
    private BlogCategoryDTO categoryDTO;
    private BlogCategoryRequest categoryRequest;
    private SuggestedProductDTO suggestedProductDTO;

    @BeforeEach
    void setUp() {
        // Setup blog post create request
        createRequest = new BlogPostCreateRequest();
        createRequest.setTitle("Test Blog Post");
        createRequest.setContent("Test content for the blog post");
        createRequest.setContentType(ContentType.HTML);
        createRequest.setExcerpt("Test excerpt");
        createRequest.setFeaturedImage("http://example.com/image.jpg");
        createRequest.setAuthorId(1L);
        createRequest.setStatus(BlogPostStatus.DRAFT);
        createRequest.setPublishDate(LocalDateTime.now());
        createRequest.setCategoryIds(Set.of(1L));
        createRequest.setSuggestedProductIds(Set.of(1L));
        createRequest.setTags(Set.of("tag1", "tag2"));
        createRequest.setMetaTitle("Test Meta Title");
        createRequest.setMetaDescription("Test Meta Description");
        createRequest.setMetaKeywords("test, keywords");

        // Setup blog post DTO
        blogPostDTO = BlogPostDTO.builder()
                .id(1L)
                .title("Test Blog Post")
                .slug("test-blog-post")
                .content("Test content for the blog post")
                .contentType(ContentType.HTML)
                .excerpt("Test excerpt")
                .featuredImage("http://example.com/image.jpg")
                .status(BlogPostStatus.DRAFT)
                .publishDate(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .tags(Set.of("tag1", "tag2"))
                .metaTitle("Test Meta Title")
                .metaDescription("Test Meta Description")
                .metaKeywords("test, keywords")
                .views(0)
                .build();

        // Setup blog post list item DTO
        blogPostListItemDTO = BlogPostListItemDTO.builder()
                .id(1L)
                .title("Test Blog Post")
                .slug("test-blog-post")
                .excerpt("Test excerpt")
                .featuredImage("http://example.com/image.jpg")
                .status(BlogPostStatus.DRAFT)
                .publishDate(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .views(0)
                .authorName("John Doe")
                .categoryNames(Set.of("Test Category"))
                .build();

        // Setup category DTO
        categoryDTO = BlogCategoryDTO.builder()
                .id(1L)
                .name("Test Category")
                .slug("test-category")
                .description("Test Description")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // Setup category request
        categoryRequest = new BlogCategoryRequest();
        categoryRequest.setName("Test Category");
        categoryRequest.setDescription("Test Description");

        // Setup suggested product DTO
        suggestedProductDTO = SuggestedProductDTO.builder()
                .productId(1L)
                .name("Test Product")
                .shortDescription("Test Description")
                .price(new BigDecimal("99.99"))
                .imageUrl("http://example.com/product.jpg")
                .slug("test-product")
                .inStock(true)
                .rating(4.5)
                .reviewCount(10)
                .build();
    }

    @Nested
    @DisplayName("Blog Post CRUD Tests")
    class BlogPostCrudTests {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should create blog post successfully")
        void shouldCreateBlogPostSuccessfully() throws Exception {
            // Given
            when(blogService.createBlogPost(any(BlogPostCreateRequest.class))).thenReturn(blogPostDTO);

            // When & Then
            mockMvc.perform(post("/api/admin/blog/posts")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.message", is("Blog post created successfully")))
                    .andExpect(jsonPath("$.data.id", is(1)))
                    .andExpect(jsonPath("$.data.title", is("Test Blog Post")))
                    .andExpect(jsonPath("$.data.slug", is("test-blog-post")));

            verify(blogService).createBlogPost(any(BlogPostCreateRequest.class));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should return validation error for invalid blog post")
        void shouldReturnValidationErrorForInvalidBlogPost() throws Exception {
            // Given - Invalid request (missing title)
            createRequest.setTitle(null);

            // When & Then
            mockMvc.perform(post("/api/admin/blog/posts")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createRequest)))
                    .andExpect(status().isBadRequest());

            verify(blogService, never()).createBlogPost(any());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should update blog post successfully")
        void shouldUpdateBlogPostSuccessfully() throws Exception {
            // Given
            Long postId = 1L;
            when(blogService.updateBlogPost(eq(postId), any(BlogPostCreateRequest.class)))
                    .thenReturn(blogPostDTO);

            // When & Then
            mockMvc.perform(put("/api/admin/blog/posts/{id}", postId)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.message", is("Blog post updated successfully")))
                    .andExpect(jsonPath("$.data.id", is(1)));

            verify(blogService).updateBlogPost(eq(postId), any(BlogPostCreateRequest.class));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should get blog post by ID successfully")
        void shouldGetBlogPostByIdSuccessfully() throws Exception {
            // Given
            Long postId = 1L;
            when(blogService.getBlogPostById(postId)).thenReturn(blogPostDTO);

            // When & Then
            mockMvc.perform(get("/api/admin/blog/posts/{id}", postId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.message", is("Blog post retrieved successfully")))
                    .andExpect(jsonPath("$.data.id", is(1)))
                    .andExpect(jsonPath("$.data.title", is("Test Blog Post")));

            verify(blogService).getBlogPostById(postId);
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should return not found when blog post doesn't exist")
        void shouldReturnNotFoundWhenBlogPostDoesntExist() throws Exception {
            // Given
            Long postId = 999L;
            when(blogService.getBlogPostById(postId))
                    .thenThrow(new ResourceNotFoundException("Blog post not found with id: " + postId));

            // When & Then
            mockMvc.perform(get("/api/admin/blog/posts/{id}", postId))
                    .andExpect(status().isNotFound());

            verify(blogService).getBlogPostById(postId);
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should get all blog posts with pagination")
        void shouldGetAllBlogPostsWithPagination() throws Exception {
            // Given
            List<BlogPostListItemDTO> posts = Arrays.asList(blogPostListItemDTO);
            Page<BlogPostListItemDTO> postPage = new PageImpl<>(posts, PageRequest.of(0, 20), 1);

            when(blogService.getAllBlogPosts(any(BlogPostFilterRequest.class), any()))
                    .thenReturn(postPage);

            // When & Then
            mockMvc.perform(get("/api/admin/blog/posts")
                    .param("page", "0")
                    .param("size", "20"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.message", is("Blog posts retrieved successfully")))
                    .andExpect(jsonPath("$.data.content", hasSize(1)))
                    .andExpect(jsonPath("$.data.content[0].title", is("Test Blog Post")))
                    .andExpect(jsonPath("$.data.totalElements", is(1)));

            verify(blogService).getAllBlogPosts(any(BlogPostFilterRequest.class), any());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should delete blog post successfully")
        void shouldDeleteBlogPostSuccessfully() throws Exception {
            // Given
            Long postId = 1L;
            doNothing().when(blogService).deleteBlogPost(postId);

            // When & Then
            mockMvc.perform(delete("/api/admin/blog/posts/{id}", postId)
                    .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.message", is("Blog post deleted successfully")));

            verify(blogService).deleteBlogPost(postId);
        }
    }   
 @Nested
    @DisplayName("Blog Post Status Management Tests")
    class BlogPostStatusManagementTests {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should publish blog post successfully")
        void shouldPublishBlogPostSuccessfully() throws Exception {
            // Given
            Long postId = 1L;
            BlogPostDTO publishedPost = BlogPostDTO.builder()
                    .id(postId)
                    .title("Test Blog Post")
                    .status(BlogPostStatus.PUBLISHED)
                    .publishDate(LocalDateTime.now())
                    .build();

            when(blogService.publishBlogPost(postId)).thenReturn(publishedPost);

            // When & Then
            mockMvc.perform(patch("/api/admin/blog/posts/{id}/publish", postId)
                    .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.message", is("Blog post published successfully")))
                    .andExpect(jsonPath("$.data.id", is(1)))
                    .andExpect(jsonPath("$.data.status", is("PUBLISHED")));

            verify(blogService).publishBlogPost(postId);
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should unpublish blog post successfully")
        void shouldUnpublishBlogPostSuccessfully() throws Exception {
            // Given
            Long postId = 1L;
            BlogPostDTO unpublishedPost = BlogPostDTO.builder()
                    .id(postId)
                    .title("Test Blog Post")
                    .status(BlogPostStatus.DRAFT)
                    .build();

            when(blogService.unpublishBlogPost(postId)).thenReturn(unpublishedPost);

            // When & Then
            mockMvc.perform(patch("/api/admin/blog/posts/{id}/unpublish", postId)
                    .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.message", is("Blog post unpublished successfully")))
                    .andExpect(jsonPath("$.data.id", is(1)))
                    .andExpect(jsonPath("$.data.status", is("DRAFT")));

            verify(blogService).unpublishBlogPost(postId);
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should return not found when publishing non-existent blog post")
        void shouldReturnNotFoundWhenPublishingNonExistentBlogPost() throws Exception {
            // Given
            Long postId = 999L;
            when(blogService.publishBlogPost(postId))
                    .thenThrow(new ResourceNotFoundException("Blog post not found with id: " + postId));

            // When & Then
            mockMvc.perform(patch("/api/admin/blog/posts/{id}/publish", postId)
                    .with(csrf()))
                    .andExpect(status().isNotFound());

            verify(blogService).publishBlogPost(postId);
        }
    }

    @Nested
    @DisplayName("Image Upload Tests")
    class ImageUploadTests {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should upload blog image successfully")
        void shouldUploadBlogImageSuccessfully() throws Exception {
            // Given
            MockMultipartFile file = new MockMultipartFile(
                    "file", 
                    "test-image.jpg", 
                    "image/jpeg", 
                    "test image content".getBytes()
            );
            String expectedUrl = "http://example.com/uploads/blog/images/test-image.jpg";

            when(blogService.uploadBlogImage(any())).thenReturn(expectedUrl);

            // When & Then
            mockMvc.perform(multipart("/api/admin/blog/images")
                    .file(file)
                    .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.message", is("Image uploaded successfully")))
                    .andExpect(jsonPath("$.data", is(expectedUrl)));

            verify(blogService).uploadBlogImage(any());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should handle image upload failure")
        void shouldHandleImageUploadFailure() throws Exception {
            // Given
            MockMultipartFile file = new MockMultipartFile(
                    "file", 
                    "test-image.jpg", 
                    "image/jpeg", 
                    "test image content".getBytes()
            );

            when(blogService.uploadBlogImage(any()))
                    .thenThrow(new RuntimeException("Upload failed"));

            // When & Then
            mockMvc.perform(multipart("/api/admin/blog/images")
                    .file(file)
                    .with(csrf()))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.success", is(false)))
                    .andExpect(jsonPath("$.error.message", containsString("Failed to upload image")));

            verify(blogService).uploadBlogImage(any());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should return bad request for missing file")
        void shouldReturnBadRequestForMissingFile() throws Exception {
            // When & Then
            mockMvc.perform(multipart("/api/admin/blog/images")
                    .with(csrf()))
                    .andExpect(status().isBadRequest());

            verify(blogService, never()).uploadBlogImage(any());
        }
    }

    @Nested
    @DisplayName("Available Products Tests")
    class AvailableProductsTests {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should get available products for suggestions")
        void shouldGetAvailableProductsForSuggestions() throws Exception {
            // Given
            List<SuggestedProductDTO> products = Arrays.asList(suggestedProductDTO);
            Page<SuggestedProductDTO> productPage = new PageImpl<>(products, PageRequest.of(0, 20), 1);

            when(blogService.getAvailableProductsForSuggestion(eq("protein"), any()))
                    .thenReturn(productPage);

            // When & Then
            mockMvc.perform(get("/api/admin/blog/products/available")
                    .param("search", "protein")
                    .param("page", "0")
                    .param("size", "20"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.message", is("Available products retrieved successfully")))
                    .andExpect(jsonPath("$.data.content", hasSize(1)))
                    .andExpect(jsonPath("$.data.content[0].name", is("Test Product")))
                    .andExpect(jsonPath("$.data.totalElements", is(1)));

            verify(blogService).getAvailableProductsForSuggestion(eq("protein"), any());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should get available products without search")
        void shouldGetAvailableProductsWithoutSearch() throws Exception {
            // Given
            List<SuggestedProductDTO> products = Arrays.asList(suggestedProductDTO);
            Page<SuggestedProductDTO> productPage = new PageImpl<>(products, PageRequest.of(0, 20), 1);

            when(blogService.getAvailableProductsForSuggestion(eq(""), any()))
                    .thenReturn(productPage);

            // When & Then
            mockMvc.perform(get("/api/admin/blog/products/available")
                    .param("page", "0")
                    .param("size", "20"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.data.content", hasSize(1)));

            verify(blogService).getAvailableProductsForSuggestion(eq(""), any());
        }
    }

    @Nested
    @DisplayName("Blog Category Tests")
    class BlogCategoryTests {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should get all blog categories")
        void shouldGetAllBlogCategories() throws Exception {
            // Given
            List<BlogCategoryDTO> categories = Arrays.asList(categoryDTO);
            when(blogService.getAllBlogCategories()).thenReturn(categories);

            // When & Then
            mockMvc.perform(get("/api/admin/blog/categories"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.message", is("Blog categories retrieved successfully")))
                    .andExpect(jsonPath("$.data", hasSize(1)))
                    .andExpect(jsonPath("$.data[0].name", is("Test Category")));

            verify(blogService).getAllBlogCategories();
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should create blog category successfully")
        void shouldCreateBlogCategorySuccessfully() throws Exception {
            // Given
            when(blogService.createBlogCategory(any(BlogCategoryRequest.class))).thenReturn(categoryDTO);

            // When & Then
            mockMvc.perform(post("/api/admin/blog/categories")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(categoryRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.message", is("Blog category created successfully")))
                    .andExpect(jsonPath("$.data.id", is(1)))
                    .andExpect(jsonPath("$.data.name", is("Test Category")));

            verify(blogService).createBlogCategory(any(BlogCategoryRequest.class));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should return validation error for invalid category")
        void shouldReturnValidationErrorForInvalidCategory() throws Exception {
            // Given - Invalid request (missing name)
            categoryRequest.setName(null);

            // When & Then
            mockMvc.perform(post("/api/admin/blog/categories")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(categoryRequest)))
                    .andExpect(status().isBadRequest());

            verify(blogService, never()).createBlogCategory(any());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should update blog category successfully")
        void shouldUpdateBlogCategorySuccessfully() throws Exception {
            // Given
            Long categoryId = 1L;
            when(blogService.updateBlogCategory(eq(categoryId), any(BlogCategoryRequest.class)))
                    .thenReturn(categoryDTO);

            // When & Then
            mockMvc.perform(put("/api/admin/blog/categories/{id}", categoryId)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(categoryRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.message", is("Blog category updated successfully")))
                    .andExpect(jsonPath("$.data.id", is(1)));

            verify(blogService).updateBlogCategory(eq(categoryId), any(BlogCategoryRequest.class));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should delete blog category successfully")
        void shouldDeleteBlogCategorySuccessfully() throws Exception {
            // Given
            Long categoryId = 1L;
            doNothing().when(blogService).deleteBlogCategory(categoryId);

            // When & Then
            mockMvc.perform(delete("/api/admin/blog/categories/{id}", categoryId)
                    .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.message", is("Blog category deleted successfully")));

            verify(blogService).deleteBlogCategory(categoryId);
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should return bad request when deleting category in use")
        void shouldReturnBadRequestWhenDeletingCategoryInUse() throws Exception {
            // Given
            Long categoryId = 1L;
            doThrow(new IllegalArgumentException("Cannot delete category that is used by 2 blog posts"))
                    .when(blogService).deleteBlogCategory(categoryId);

            // When & Then
            mockMvc.perform(delete("/api/admin/blog/categories/{id}", categoryId)
                    .with(csrf()))
                    .andExpect(status().isBadRequest());

            verify(blogService).deleteBlogCategory(categoryId);
        }
    }

    @Nested
    @DisplayName("Security Tests")
    class SecurityTests {

        @Test
        @DisplayName("Should return unauthorized when not authenticated")
        void shouldReturnUnauthorizedWhenNotAuthenticated() throws Exception {
            // When & Then
            mockMvc.perform(post("/api/admin/blog/posts")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createRequest)))
                    .andExpect(status().isUnauthorized());

            verify(blogService, never()).createBlogPost(any());
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should return forbidden when user doesn't have admin role")
        void shouldReturnForbiddenWhenUserDoesntHaveAdminRole() throws Exception {
            // When & Then
            mockMvc.perform(post("/api/admin/blog/posts")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createRequest)))
                    .andExpect(status().isForbidden());

            verify(blogService, never()).createBlogPost(any());
        }

        @Test
        @WithMockUser(roles = "CONTENT_MANAGER")
        @DisplayName("Should allow access with CONTENT_MANAGER role")
        void shouldAllowAccessWithContentManagerRole() throws Exception {
            // Given
            when(blogService.createBlogPost(any(BlogPostCreateRequest.class))).thenReturn(blogPostDTO);

            // When & Then
            mockMvc.perform(post("/api/admin/blog/posts")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success", is(true)));

            verify(blogService).createBlogPost(any(BlogPostCreateRequest.class));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should work without CSRF token")
        void shouldWorkWithoutCsrfToken() throws Exception {
            // Given - CSRF is typically disabled for API endpoints
            when(blogService.createBlogPost(any(BlogPostCreateRequest.class))).thenReturn(blogPostDTO);

            // When & Then
            mockMvc.perform(post("/api/admin/blog/posts")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success", is(true)));

            verify(blogService).createBlogPost(any(BlogPostCreateRequest.class));
        }
    }

    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should return bad request for empty title")
        void shouldReturnBadRequestForEmptyTitle() throws Exception {
            // Given
            createRequest.setTitle("");

            // When & Then
            mockMvc.perform(post("/api/admin/blog/posts")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createRequest)))
                    .andExpect(status().isBadRequest());

            verify(blogService, never()).createBlogPost(any());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should return bad request for empty content")
        void shouldReturnBadRequestForEmptyContent() throws Exception {
            // Given
            createRequest.setContent("");

            // When & Then
            mockMvc.perform(post("/api/admin/blog/posts")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createRequest)))
                    .andExpect(status().isBadRequest());

            verify(blogService, never()).createBlogPost(any());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should return bad request for title exceeding max length")
        void shouldReturnBadRequestForTitleExceedingMaxLength() throws Exception {
            // Given
            createRequest.setTitle("a".repeat(256)); // Exceeds 255 character limit

            // When & Then
            mockMvc.perform(post("/api/admin/blog/posts")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createRequest)))
                    .andExpect(status().isBadRequest());

            verify(blogService, never()).createBlogPost(any());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should return bad request for excerpt exceeding max length")
        void shouldReturnBadRequestForExcerptExceedingMaxLength() throws Exception {
            // Given
            createRequest.setExcerpt("a".repeat(501)); // Exceeds 500 character limit

            // When & Then
            mockMvc.perform(post("/api/admin/blog/posts")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createRequest)))
                    .andExpect(status().isBadRequest());

            verify(blogService, never()).createBlogPost(any());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should return bad request for meta title exceeding max length")
        void shouldReturnBadRequestForMetaTitleExceedingMaxLength() throws Exception {
            // Given
            createRequest.setMetaTitle("a".repeat(256)); // Exceeds 255 character limit

            // When & Then
            mockMvc.perform(post("/api/admin/blog/posts")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createRequest)))
                    .andExpect(status().isBadRequest());

            verify(blogService, never()).createBlogPost(any());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should return bad request for category name exceeding max length")
        void shouldReturnBadRequestForCategoryNameExceedingMaxLength() throws Exception {
            // Given
            categoryRequest.setName("a".repeat(101)); // Exceeds 100 character limit

            // When & Then
            mockMvc.perform(post("/api/admin/blog/categories")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(categoryRequest)))
                    .andExpect(status().isBadRequest());

            verify(blogService, never()).createBlogCategory(any());
        }
    }

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should handle service exceptions gracefully")
        void shouldHandleServiceExceptionsGracefully() throws Exception {
            // Given
            when(blogService.createBlogPost(any(BlogPostCreateRequest.class)))
                    .thenThrow(new RuntimeException("Database connection failed"));

            // When & Then
            mockMvc.perform(post("/api/admin/blog/posts")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createRequest)))
                    .andExpect(status().isInternalServerError());

            verify(blogService).createBlogPost(any(BlogPostCreateRequest.class));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should handle malformed JSON gracefully")
        void shouldHandleMalformedJsonGracefully() throws Exception {
            // When & Then
            mockMvc.perform(post("/api/admin/blog/posts")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{invalid json"))
                    .andExpect(status().isBadRequest());

            verify(blogService, never()).createBlogPost(any());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should handle missing content type gracefully")
        void shouldHandleMissingContentTypeGracefully() throws Exception {
            // When & Then
            mockMvc.perform(post("/api/admin/blog/posts")
                    .with(csrf())
                    .content(objectMapper.writeValueAsString(createRequest)))
                    .andExpect(status().isUnsupportedMediaType());

            verify(blogService, never()).createBlogPost(any());
        }
    }
}