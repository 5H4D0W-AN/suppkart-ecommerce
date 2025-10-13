package com.suppkart.service;

import com.suppkart.dto.content.*;
import com.suppkart.exception.ResourceNotFoundException;
import com.suppkart.model.entity.*;
import com.suppkart.model.enums.BlogPostStatus;
import com.suppkart.model.enums.ContentType;
import com.suppkart.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
@DisplayName("BlogService Tests")
class BlogServiceTest {

    @Mock
    private BlogPostRepository blogPostRepository;

    @Mock
    private BlogCategoryRepository blogCategoryRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private SlugGenerator slugGenerator;

    @Mock
    private FileUploadService fileUploadService;

    @InjectMocks
    private BlogService blogService;

    private User testAuthor;
    private BlogPost testBlogPost;
    private BlogCategory testCategory;
    private Product testProduct;
    private BlogPostCreateRequest createRequest;

    @BeforeEach
    void setUp() {
        testAuthor = createTestUser();
        testCategory = createTestCategory();
        testProduct = createTestProduct();
        testBlogPost = createTestBlogPost();
        createRequest = createTestBlogPostRequest();
    }

    @Nested
    @DisplayName("Create Blog Post Tests")
    class CreateBlogPostTests {

        @Test
        @DisplayName("Should create blog post successfully with all fields")
        void shouldCreateBlogPostSuccessfully() {
            // Given
            when(userRepository.findById(1L)).thenReturn(Optional.of(testAuthor));
            when(slugGenerator.generateUniqueSlug(eq("Test Blog Post"), any())).thenReturn("test-blog-post");
            when(blogCategoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
            when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
            when(blogPostRepository.save(any(BlogPost.class))).thenReturn(testBlogPost);

            // When
            BlogPostDTO result = blogService.createBlogPost(createRequest);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getTitle()).isEqualTo("Test Blog Post");
            assertThat(result.getSlug()).isEqualTo("test-blog-post");
            assertThat(result.getContent()).isEqualTo("Test content");
            assertThat(result.getStatus()).isEqualTo(BlogPostStatus.DRAFT);

            verify(userRepository).findById(1L);
            verify(slugGenerator).generateUniqueSlug(eq("Test Blog Post"), any());
            verify(blogCategoryRepository).findById(1L);
            verify(productRepository).findById(1L);
            verify(blogPostRepository).save(any(BlogPost.class));
        }

        @Test
        @DisplayName("Should throw exception when author not found")
        void shouldThrowExceptionWhenAuthorNotFound() {
            // Given
            when(userRepository.findById(999L)).thenReturn(Optional.empty());
            createRequest.setAuthorId(999L);

            // When & Then
            assertThatThrownBy(() -> blogService.createBlogPost(createRequest))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Author not found with id: 999");

            verify(userRepository).findById(999L);
            verify(blogPostRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when category not found")
        void shouldThrowExceptionWhenCategoryNotFound() {
            // Given
            when(userRepository.findById(1L)).thenReturn(Optional.of(testAuthor));
            when(blogCategoryRepository.findById(999L)).thenReturn(Optional.empty());
            createRequest.setCategoryIds(Set.of(999L));

            // When & Then
            assertThatThrownBy(() -> blogService.createBlogPost(createRequest))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Category not found with id: 999");

            verify(blogCategoryRepository).findById(999L);
            verify(blogPostRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when suggested product not found")
        void shouldThrowExceptionWhenSuggestedProductNotFound() {
            // Given
            when(userRepository.findById(1L)).thenReturn(Optional.of(testAuthor));
            when(slugGenerator.generateUniqueSlug(any(), any())).thenReturn("test-slug");
            when(blogCategoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
            when(productRepository.findById(999L)).thenReturn(Optional.empty());
            createRequest.setSuggestedProductIds(Set.of(999L));

            // When & Then
            assertThatThrownBy(() -> blogService.createBlogPost(createRequest))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Product not found with id: 999");

            verify(productRepository).findById(999L);
            verify(blogPostRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should create blog post with default status when status is null")
        void shouldCreateBlogPostWithDefaultStatus() {
            // Given
            createRequest.setStatus(null);
            when(userRepository.findById(1L)).thenReturn(Optional.of(testAuthor));
            when(slugGenerator.generateUniqueSlug(any(), any())).thenReturn("test-slug");
            when(blogCategoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
            when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
            when(blogPostRepository.save(any(BlogPost.class))).thenReturn(testBlogPost);

            // When
            BlogPostDTO result = blogService.createBlogPost(createRequest);

            // Then
            assertThat(result.getStatus()).isEqualTo(BlogPostStatus.DRAFT);
            verify(blogPostRepository).save(argThat(post -> 
                post.getStatus() == BlogPostStatus.DRAFT
            ));
        }
    }

    @Nested
    @DisplayName("Update Blog Post Tests")
    class UpdateBlogPostTests {

        @Test
        @DisplayName("Should update blog post successfully")
        void shouldUpdateBlogPostSuccessfully() {
            // Given
            Long postId = 1L;
            when(blogPostRepository.findById(postId)).thenReturn(Optional.of(testBlogPost));
            lenient().when(slugGenerator.updateSlugIfNeeded(any(), any(), any())).thenReturn("updated-slug");
            when(blogCategoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
            when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
            when(blogPostRepository.save(any(BlogPost.class))).thenReturn(testBlogPost);

            // When
            BlogPostDTO result = blogService.updateBlogPost(postId, createRequest);

            // Then
            assertThat(result).isNotNull();
            verify(blogPostRepository).findById(postId);
            verify(blogPostRepository).save(testBlogPost);
        }

        @Test
        @DisplayName("Should throw exception when blog post not found for update")
        void shouldThrowExceptionWhenBlogPostNotFound() {
            // Given
            Long postId = 999L;
            when(blogPostRepository.findById(postId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> blogService.updateBlogPost(postId, createRequest))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Blog post not found with id: 999");

            verify(blogPostRepository).findById(postId);
            verify(blogPostRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should update slug when title changes")
        void shouldUpdateSlugWhenTitleChanges() {
            // Given
            Long postId = 1L;
            BlogPost existingPost = createTestBlogPost();
            existingPost.setTitle("Old Title");
            existingPost.setSlug("old-title");
            
            createRequest.setTitle("New Title");
            
            when(blogPostRepository.findById(postId)).thenReturn(Optional.of(existingPost));
            when(slugGenerator.updateSlugIfNeeded(eq("old-title"), eq("New Title"), any()))
                    .thenReturn("new-title");
            when(blogCategoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
            when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
            when(blogPostRepository.save(any(BlogPost.class))).thenReturn(existingPost);

            // When
            blogService.updateBlogPost(postId, createRequest);

            // Then
            verify(slugGenerator).updateSlugIfNeeded(eq("old-title"), eq("New Title"), any());
            verify(blogPostRepository).save(argThat(post -> 
                post.getSlug().equals("new-title")
            ));
        }
    }    
@Nested
    @DisplayName("Get Blog Post Tests")
    class GetBlogPostTests {

        @Test
        @DisplayName("Should get blog post by ID successfully")
        void shouldGetBlogPostById() {
            // Given
            Long postId = 1L;
            when(blogPostRepository.findById(postId)).thenReturn(Optional.of(testBlogPost));

            // When
            BlogPostDTO result = blogService.getBlogPostById(postId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(testBlogPost.getId());
            assertThat(result.getTitle()).isEqualTo(testBlogPost.getTitle());
            verify(blogPostRepository).findById(postId);
        }

        @Test
        @DisplayName("Should throw exception when blog post not found by ID")
        void shouldThrowExceptionWhenBlogPostNotFoundById() {
            // Given
            Long postId = 999L;
            when(blogPostRepository.findById(postId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> blogService.getBlogPostById(postId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Blog post not found with id: 999");
        }

        @Test
        @DisplayName("Should get published blog post by slug and increment views")
        void shouldGetPublishedBlogPostBySlugAndIncrementViews() {
            // Given
            String slug = "test-slug";
            testBlogPost.setViews(5);
            when(blogPostRepository.findBySlugAndStatus(slug, BlogPostStatus.PUBLISHED))
                    .thenReturn(Optional.of(testBlogPost));
            when(blogPostRepository.save(any(BlogPost.class))).thenReturn(testBlogPost);

            // When
            BlogPostDTO result = blogService.getPublishedBlogPostBySlug(slug);

            // Then
            assertThat(result).isNotNull();
            verify(blogPostRepository).save(argThat(post -> 
                post.getViews().equals(6)
            ));
        }

        @Test
        @DisplayName("Should handle null views when incrementing")
        void shouldHandleNullViewsWhenIncrementing() {
            // Given
            String slug = "test-slug";
            testBlogPost.setViews(null);
            when(blogPostRepository.findBySlugAndStatus(slug, BlogPostStatus.PUBLISHED))
                    .thenReturn(Optional.of(testBlogPost));
            when(blogPostRepository.save(any(BlogPost.class))).thenReturn(testBlogPost);

            // When
            blogService.getPublishedBlogPostBySlug(slug);

            // Then
            verify(blogPostRepository).save(argThat(post -> 
                post.getViews().equals(1)
            ));
        }
    }

    @Nested
    @DisplayName("Get All Blog Posts Tests")
    class GetAllBlogPostsTests {

        @Test
        @DisplayName("Should get all blog posts with default sorting")
        void shouldGetAllBlogPostsWithDefaultSorting() {
            // Given
            BlogPostFilterRequest filter = new BlogPostFilterRequest();
            Pageable pageable = PageRequest.of(0, 10);
            List<BlogPost> posts = Arrays.asList(testBlogPost);
            Page<BlogPost> postPage = new PageImpl<>(posts, pageable, 1);

            when(blogPostRepository.findAllByOrderByCreatedAtDesc(pageable)).thenReturn(postPage);

            // When
            Page<BlogPostListItemDTO> result = blogService.getAllBlogPosts(filter, pageable);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            verify(blogPostRepository).findAllByOrderByCreatedAtDesc(pageable);
        }

        @Test
        @DisplayName("Should filter blog posts by status")
        void shouldFilterBlogPostsByStatus() {
            // Given
            BlogPostFilterRequest filter = new BlogPostFilterRequest();
            filter.setStatus(BlogPostStatus.PUBLISHED);
            Pageable pageable = PageRequest.of(0, 10);
            List<BlogPost> posts = Arrays.asList(testBlogPost);
            Page<BlogPost> postPage = new PageImpl<>(posts, pageable, 1);

            when(blogPostRepository.findByStatusOrderByCreatedAtDesc(BlogPostStatus.PUBLISHED, pageable))
                    .thenReturn(postPage);

            // When
            Page<BlogPostListItemDTO> result = blogService.getAllBlogPosts(filter, pageable);

            // Then
            assertThat(result).isNotNull();
            verify(blogPostRepository).findByStatusOrderByCreatedAtDesc(BlogPostStatus.PUBLISHED, pageable);
        }

        @Test
        @DisplayName("Should filter blog posts by title")
        void shouldFilterBlogPostsByTitle() {
            // Given
            BlogPostFilterRequest filter = new BlogPostFilterRequest();
            filter.setTitle("Test");
            Pageable pageable = PageRequest.of(0, 10);
            List<BlogPost> posts = Arrays.asList(testBlogPost);
            Page<BlogPost> postPage = new PageImpl<>(posts, pageable, 1);

            when(blogPostRepository.findByTitleContainingIgnoreCase("Test", pageable))
                    .thenReturn(postPage);

            // When
            Page<BlogPostListItemDTO> result = blogService.getAllBlogPosts(filter, pageable);

            // Then
            assertThat(result).isNotNull();
            verify(blogPostRepository).findByTitleContainingIgnoreCase("Test", pageable);
        }

        @Test
        @DisplayName("Should filter blog posts by category")
        void shouldFilterBlogPostsByCategory() {
            // Given
            BlogPostFilterRequest filter = new BlogPostFilterRequest();
            filter.setCategoryId(1L);
            Pageable pageable = PageRequest.of(0, 10);
            List<BlogPost> posts = Arrays.asList(testBlogPost);
            Page<BlogPost> postPage = new PageImpl<>(posts, pageable, 1);

            when(blogCategoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
            when(blogPostRepository.findByCategoriesContaining(testCategory, pageable))
                    .thenReturn(postPage);

            // When
            Page<BlogPostListItemDTO> result = blogService.getAllBlogPosts(filter, pageable);

            // Then
            assertThat(result).isNotNull();
            verify(blogCategoryRepository).findById(1L);
            verify(blogPostRepository).findByCategoriesContaining(testCategory, pageable);
        }

        @Test
        @DisplayName("Should get published blog posts for public access")
        void shouldGetPublishedBlogPostsForPublicAccess() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            List<BlogPost> posts = Arrays.asList(testBlogPost);
            Page<BlogPost> postPage = new PageImpl<>(posts, pageable, 1);

            when(blogPostRepository.findByStatusAndPublishDateLessThanEqualOrderByPublishDateDesc(
                    eq(BlogPostStatus.PUBLISHED), any(LocalDateTime.class), eq(pageable)))
                    .thenReturn(postPage);

            // When
            Page<BlogPostListItemDTO> result = blogService.getPublishedBlogPosts(pageable);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            verify(blogPostRepository).findByStatusAndPublishDateLessThanEqualOrderByPublishDateDesc(
                    eq(BlogPostStatus.PUBLISHED), any(LocalDateTime.class), eq(pageable));
        }
    }

    @Nested
    @DisplayName("Delete Blog Post Tests")
    class DeleteBlogPostTests {

        @Test
        @DisplayName("Should delete blog post successfully")
        void shouldDeleteBlogPostSuccessfully() {
            // Given
            Long postId = 1L;
            when(blogPostRepository.findById(postId)).thenReturn(Optional.of(testBlogPost));

            // When
            blogService.deleteBlogPost(postId);

            // Then
            verify(blogPostRepository).findById(postId);
            verify(blogPostRepository).delete(testBlogPost);
        }

        @Test
        @DisplayName("Should delete blog post and featured image")
        void shouldDeleteBlogPostAndFeaturedImage() {
            // Given
            Long postId = 1L;
            testBlogPost.setFeaturedImage("http://example.com/image.jpg");
            when(blogPostRepository.findById(postId)).thenReturn(Optional.of(testBlogPost));

            // When
            blogService.deleteBlogPost(postId);

            // Then
            verify(fileUploadService).deleteFile("http://example.com/image.jpg");
            verify(blogPostRepository).delete(testBlogPost);
        }

        @Test
        @DisplayName("Should skip image deletion when featured image is null")
        void shouldSkipImageDeletionWhenFeaturedImageIsNull() {
            // Given
            Long postId = 1L;
            testBlogPost.setFeaturedImage(null);
            when(blogPostRepository.findById(postId)).thenReturn(Optional.of(testBlogPost));

            // When
            blogService.deleteBlogPost(postId);

            // Then
            verify(fileUploadService, never()).deleteFile(any());
            verify(blogPostRepository).delete(testBlogPost);
        }

        @Test
        @DisplayName("Should throw exception when blog post not found for deletion")
        void shouldThrowExceptionWhenBlogPostNotFoundForDeletion() {
            // Given
            Long postId = 999L;
            when(blogPostRepository.findById(postId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> blogService.deleteBlogPost(postId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Blog post not found with id: 999");

            verify(blogPostRepository, never()).delete(any());
        }
    }

    @Nested
    @DisplayName("Publish/Unpublish Blog Post Tests")
    class PublishUnpublishBlogPostTests {

        @Test
        @DisplayName("Should publish blog post successfully")
        void shouldPublishBlogPostSuccessfully() {
            // Given
            Long postId = 1L;
            testBlogPost.setStatus(BlogPostStatus.DRAFT);
            testBlogPost.setPublishDate(null);
            when(blogPostRepository.findById(postId)).thenReturn(Optional.of(testBlogPost));
            when(blogPostRepository.save(any(BlogPost.class))).thenReturn(testBlogPost);

            // When
            BlogPostDTO result = blogService.publishBlogPost(postId);

            // Then
            assertThat(result).isNotNull();
            verify(blogPostRepository).save(argThat(post -> 
                post.getStatus() == BlogPostStatus.PUBLISHED &&
                post.getPublishDate() != null
            ));
        }

        @Test
        @DisplayName("Should publish blog post without changing existing publish date")
        void shouldPublishBlogPostWithoutChangingExistingPublishDate() {
            // Given
            Long postId = 1L;
            LocalDateTime existingPublishDate = LocalDateTime.now().minusDays(1);
            testBlogPost.setStatus(BlogPostStatus.DRAFT);
            testBlogPost.setPublishDate(existingPublishDate);
            when(blogPostRepository.findById(postId)).thenReturn(Optional.of(testBlogPost));
            when(blogPostRepository.save(any(BlogPost.class))).thenReturn(testBlogPost);

            // When
            blogService.publishBlogPost(postId);

            // Then
            verify(blogPostRepository).save(argThat(post -> 
                post.getStatus() == BlogPostStatus.PUBLISHED &&
                post.getPublishDate().equals(existingPublishDate)
            ));
        }

        @Test
        @DisplayName("Should unpublish blog post successfully")
        void shouldUnpublishBlogPostSuccessfully() {
            // Given
            Long postId = 1L;
            testBlogPost.setStatus(BlogPostStatus.PUBLISHED);
            when(blogPostRepository.findById(postId)).thenReturn(Optional.of(testBlogPost));
            when(blogPostRepository.save(any(BlogPost.class))).thenReturn(testBlogPost);

            // When
            BlogPostDTO result = blogService.unpublishBlogPost(postId);

            // Then
            assertThat(result).isNotNull();
            verify(blogPostRepository).save(argThat(post -> 
                post.getStatus() == BlogPostStatus.DRAFT
            ));
        }

        @Test
        @DisplayName("Should throw exception when blog post not found for publishing")
        void shouldThrowExceptionWhenBlogPostNotFoundForPublishing() {
            // Given
            Long postId = 999L;
            when(blogPostRepository.findById(postId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> blogService.publishBlogPost(postId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Blog post not found with id: 999");
        }
    }  
  @Nested
    @DisplayName("Blog Category Tests")
    class BlogCategoryTests {

        @Test
        @DisplayName("Should get all blog categories")
        void shouldGetAllBlogCategories() {
            // Given
            List<BlogCategory> categories = Arrays.asList(testCategory);
            when(blogCategoryRepository.findAllByOrderByNameAsc()).thenReturn(categories);

            // When
            List<BlogCategoryDTO> result = blogService.getAllBlogCategories();

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getName()).isEqualTo("Test Category");
            verify(blogCategoryRepository).findAllByOrderByNameAsc();
        }

        @Test
        @DisplayName("Should create blog category successfully")
        void shouldCreateBlogCategorySuccessfully() {
            // Given
            BlogCategoryRequest request = new BlogCategoryRequest();
            request.setName("New Category");
            request.setDescription("New Description");

            when(blogCategoryRepository.existsByName("New Category")).thenReturn(false);
            when(slugGenerator.generateUniqueSlug(eq("New Category"), any())).thenReturn("new-category");
            when(blogCategoryRepository.save(any(BlogCategory.class))).thenReturn(testCategory);

            // When
            BlogCategoryDTO result = blogService.createBlogCategory(request);

            // Then
            assertThat(result).isNotNull();
            verify(blogCategoryRepository).existsByName("New Category");
            verify(slugGenerator).generateUniqueSlug(eq("New Category"), any());
            verify(blogCategoryRepository).save(any(BlogCategory.class));
        }

        @Test
        @DisplayName("Should throw exception when category name already exists")
        void shouldThrowExceptionWhenCategoryNameAlreadyExists() {
            // Given
            BlogCategoryRequest request = new BlogCategoryRequest();
            request.setName("Existing Category");

            when(blogCategoryRepository.existsByName("Existing Category")).thenReturn(true);

            // When & Then
            assertThatThrownBy(() -> blogService.createBlogCategory(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Category with name 'Existing Category' already exists");

            verify(blogCategoryRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should update blog category successfully")
        void shouldUpdateBlogCategorySuccessfully() {
            // Given
            Long categoryId = 1L;
            BlogCategoryRequest request = new BlogCategoryRequest();
            request.setName("Updated Category");
            request.setDescription("Updated Description");

            when(blogCategoryRepository.findById(categoryId)).thenReturn(Optional.of(testCategory));
            when(blogCategoryRepository.existsByName("Updated Category")).thenReturn(false);
            when(slugGenerator.updateSlugIfNeeded(any(), any(), any())).thenReturn("updated-category");
            when(blogCategoryRepository.save(any(BlogCategory.class))).thenReturn(testCategory);

            // When
            BlogCategoryDTO result = blogService.updateBlogCategory(categoryId, request);

            // Then
            assertThat(result).isNotNull();
            verify(blogCategoryRepository).findById(categoryId);
            verify(blogCategoryRepository).save(testCategory);
        }

        @Test
        @DisplayName("Should delete blog category successfully")
        void shouldDeleteBlogCategorySuccessfully() {
            // Given
            Long categoryId = 1L;
            when(blogCategoryRepository.findById(categoryId)).thenReturn(Optional.of(testCategory));
            when(blogPostRepository.findByCategoriesContaining(testCategory)).thenReturn(Arrays.asList());

            // When
            blogService.deleteBlogCategory(categoryId);

            // Then
            verify(blogCategoryRepository).findById(categoryId);
            verify(blogPostRepository).findByCategoriesContaining(testCategory);
            verify(blogCategoryRepository).delete(testCategory);
        }

        @Test
        @DisplayName("Should throw exception when deleting category used by blog posts")
        void shouldThrowExceptionWhenDeletingCategoryUsedByBlogPosts() {
            // Given
            Long categoryId = 1L;
            when(blogCategoryRepository.findById(categoryId)).thenReturn(Optional.of(testCategory));
            when(blogPostRepository.findByCategoriesContaining(testCategory))
                    .thenReturn(Arrays.asList(testBlogPost, testBlogPost));

            // When & Then
            assertThatThrownBy(() -> blogService.deleteBlogCategory(categoryId))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Cannot delete category that is used by 2 blog posts");

            verify(blogCategoryRepository, never()).delete(any());
        }
    }

    @Nested
    @DisplayName("File Upload Tests")
    class FileUploadTests {

        @Test
        @DisplayName("Should upload blog image successfully")
        void shouldUploadBlogImageSuccessfully() {
            // Given
            MultipartFile file = new MockMultipartFile(
                    "file", 
                    "test-image.jpg", 
                    "image/jpeg", 
                    "test image content".getBytes()
            );
            String expectedUrl = "http://example.com/uploads/blog/images/test-image.jpg";

            when(fileUploadService.uploadFile(file, "blog/images")).thenReturn(expectedUrl);

            // When
            String result = blogService.uploadBlogImage(file);

            // Then
            assertThat(result).isEqualTo(expectedUrl);
            verify(fileUploadService).uploadFile(file, "blog/images");
        }

        @Test
        @DisplayName("Should handle upload failure")
        void shouldHandleUploadFailure() {
            // Given
            MultipartFile file = new MockMultipartFile(
                    "file", 
                    "test-image.jpg", 
                    "image/jpeg", 
                    "test image content".getBytes()
            );

            when(fileUploadService.uploadFile(file, "blog/images"))
                    .thenThrow(new RuntimeException("Upload failed"));

            // When & Then
            assertThatThrownBy(() -> blogService.uploadBlogImage(file))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Upload failed");
        }
    }

    @Nested
    @DisplayName("Get Available Products Tests")
    class GetAvailableProductsTests {

        @Test
        @DisplayName("Should get available products with search")
        void shouldGetAvailableProductsWithSearch() {
            // Given
            String search = "protein";
            Pageable pageable = PageRequest.of(0, 10);
            List<Product> products = Arrays.asList(testProduct);
            Page<Product> productPage = new PageImpl<>(products, pageable, 1);

            when(productRepository.searchProducts("protein", pageable)).thenReturn(productPage);

            // When
            Page<SuggestedProductDTO> result = blogService.getAvailableProductsForSuggestion(search, pageable);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            verify(productRepository).searchProducts("protein", pageable);
        }

        @Test
        @DisplayName("Should get all active products when search is empty")
        void shouldGetAllActiveProductsWhenSearchIsEmpty() {
            // Given
            String search = "";
            Pageable pageable = PageRequest.of(0, 10);
            List<Product> products = Arrays.asList(testProduct);
            Page<Product> productPage = new PageImpl<>(products, pageable, 1);

            when(productRepository.findByIsActiveTrue(pageable)).thenReturn(productPage);

            // When
            Page<SuggestedProductDTO> result = blogService.getAvailableProductsForSuggestion(search, pageable);

            // Then
            assertThat(result).isNotNull();
            verify(productRepository).findByIsActiveTrue(pageable);
        }

        @Test
        @DisplayName("Should get all active products when search is null")
        void shouldGetAllActiveProductsWhenSearchIsNull() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            List<Product> products = Arrays.asList(testProduct);
            Page<Product> productPage = new PageImpl<>(products, pageable, 1);

            when(productRepository.findByIsActiveTrue(pageable)).thenReturn(productPage);

            // When
            Page<SuggestedProductDTO> result = blogService.getAvailableProductsForSuggestion(null, pageable);

            // Then
            assertThat(result).isNotNull();
            verify(productRepository).findByIsActiveTrue(pageable);
        }
    }

    // Helper methods for creating test data
    private User createTestUser() {
        User user = new User();
        user.setUserId(1L);
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setEmail("john.doe@example.com");
        return user;
    }

    private BlogCategory createTestCategory() {
        BlogCategory category = new BlogCategory();
        category.setId(1L);
        category.setName("Test Category");
        category.setSlug("test-category");
        category.setDescription("Test Description");
        category.setCreatedAt(LocalDateTime.now());
        category.setUpdatedAt(LocalDateTime.now());
        return category;
    }

    private Product createTestProduct() {
        Product product = new Product();
        product.setProductId(1L);
        product.setName("Test Product");
        product.setShortDescription("Test Description");
        product.setSlug("test-product");
        product.setIsActive(true);
        return product;
    }

    private BlogPost createTestBlogPost() {
        BlogPost post = new BlogPost();
        post.setId(1L);
        post.setTitle("Test Blog Post");
        post.setSlug("test-blog-post");
        post.setContent("Test content");
        post.setContentType(ContentType.HTML);
        post.setExcerpt("Test excerpt");
        post.setFeaturedImage("http://example.com/image.jpg");
        post.setStatus(BlogPostStatus.DRAFT);
        post.setPublishDate(LocalDateTime.now());
        post.setCreatedAt(LocalDateTime.now());
        post.setUpdatedAt(LocalDateTime.now());
        post.setAuthor(testAuthor);
        post.setCategories(Set.of(testCategory));
        post.setSuggestedProducts(Set.of(testProduct));
        post.setTags(Set.of("tag1", "tag2"));
        post.setMetaTitle("Test Meta Title");
        post.setMetaDescription("Test Meta Description");
        post.setMetaKeywords("test, keywords");
        post.setViews(0);
        return post;
    }

    private BlogPostCreateRequest createTestBlogPostRequest() {
        BlogPostCreateRequest request = new BlogPostCreateRequest();
        request.setTitle("Test Blog Post");
        request.setContent("Test content");
        request.setContentType(ContentType.HTML);
        request.setExcerpt("Test excerpt");
        request.setFeaturedImage("http://example.com/image.jpg");
        request.setAuthorId(1L);
        request.setStatus(BlogPostStatus.DRAFT);
        request.setPublishDate(LocalDateTime.now());
        request.setCategoryIds(Set.of(1L));
        request.setSuggestedProductIds(Set.of(1L));
        request.setTags(Set.of("tag1", "tag2"));
        request.setMetaTitle("Test Meta Title");
        request.setMetaDescription("Test Meta Description");
        request.setMetaKeywords("test, keywords");
        return request;
    }
}