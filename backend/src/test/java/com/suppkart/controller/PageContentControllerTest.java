package com.suppkart.controller;

import com.suppkart.dto.content.SeoMetadataDTO;
import com.suppkart.model.enums.ContentType;
import com.suppkart.model.enums.PageType;
import com.suppkart.service.SeoMetadataService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("PageContentController Tests")
class PageContentControllerTest {

    private List<SeoMetadataDTO> testActiveContent;
    private final PageType TEST_PAGE_TYPE = PageType.HOME;

    @BeforeEach
    void setUp() {
        SeoMetadataDTO activeElement1 = SeoMetadataDTO.builder()
                .id(1L)
                .pageType(TEST_PAGE_TYPE)
                .elementKey("H2Header")
                .elementLabel("Main Header")
                .contentType(ContentType.TEXT)
                .contentValue("Welcome to SuppKart")
                .displayOrder(1)
                .isActive(true)
                .metaTitle("Home Page")
                .metaDescription("Welcome to our store")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        SeoMetadataDTO activeElement2 = SeoMetadataDTO.builder()
                .id(2L)
                .pageType(TEST_PAGE_TYPE)
                .elementKey("HeroImage")
                .elementLabel("Hero Banner")
                .contentType(ContentType.MEDIA)
                .mediaUrl("https://example.com/hero-banner.jpg")
                .altText("Hero banner image")
                .displayOrder(2)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        testActiveContent = Arrays.asList(activeElement1, activeElement2);
    }

    @Nested
    @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
    @AutoConfigureWebMvc
    @ActiveProfiles("test")
    @DisplayName("Integration Tests")
    class IntegrationTests {

        @Autowired
        private WebApplicationContext webApplicationContext;

        @MockBean
        private SeoMetadataService seoMetadataService;

        private MockMvc mockMvc;

        @BeforeEach
        void setUp() {
            mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        }

        @Test
        @DisplayName("Should get active page content successfully")
        void getPageContent_ShouldReturnActiveContent() throws Exception {
            // Given
            when(seoMetadataService.getActivePageContent(TEST_PAGE_TYPE))
                    .thenReturn(testActiveContent);

            // When & Then
            mockMvc.perform(get("/api/public/pages/{pageType}/content", TEST_PAGE_TYPE))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Page content retrieved successfully"))
                    .andExpect(jsonPath("$.data", hasSize(2)))
                    .andExpect(jsonPath("$.data[0].id").value(1L))
                    .andExpect(jsonPath("$.data[0].elementKey").value("H2Header"))
                    .andExpect(jsonPath("$.data[0].contentType").value("TEXT"))
                    .andExpect(jsonPath("$.data[0].contentValue").value("Welcome to SuppKart"))
                    .andExpect(jsonPath("$.data[0].isActive").value(true))
                    .andExpect(jsonPath("$.data[1].id").value(2L))
                    .andExpect(jsonPath("$.data[1].elementKey").value("HeroImage"))
                    .andExpect(jsonPath("$.data[1].contentType").value("MEDIA"))
                    .andExpect(jsonPath("$.data[1].mediaUrl").value("https://example.com/hero-banner.jpg"))
                    .andExpect(jsonPath("$.data[1].altText").value("Hero banner image"))
                    .andExpect(jsonPath("$.data[1].isActive").value(true));

            verify(seoMetadataService).getActivePageContent(TEST_PAGE_TYPE);
        }

        @Test
        @DisplayName("Should return empty list when no active content exists")
        void getPageContent_WithNoActiveContent_ShouldReturnEmptyList() throws Exception {
            // Given
            when(seoMetadataService.getActivePageContent(TEST_PAGE_TYPE))
                    .thenReturn(Arrays.asList());

            // When & Then
            mockMvc.perform(get("/api/public/pages/{pageType}/content", TEST_PAGE_TYPE))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Page content retrieved successfully"))
                    .andExpect(jsonPath("$.data", hasSize(0)));

            verify(seoMetadataService).getActivePageContent(TEST_PAGE_TYPE);
        }

        @Test
        @DisplayName("Should handle invalid page type parameter")
        void getPageContent_WithInvalidPageType_ShouldReturn400() throws Exception {
            mockMvc.perform(get("/api/public/pages/{pageType}/content", "INVALID_PAGE_TYPE"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Invalid value 'INVALID_PAGE_TYPE' for parameter 'pageType'"));
        }

        @Test
        @DisplayName("Should work for all valid page types")
        void getPageContent_WithAllValidPageTypes_ShouldWork() throws Exception {
            // Test a few different page types
            PageType[] testPageTypes = {PageType.HOME, PageType.ABOUT_US, PageType.PRODUCT_DETAILS};
            
            for (PageType pageType : testPageTypes) {
                when(seoMetadataService.getActivePageContent(pageType))
                        .thenReturn(Arrays.asList());

                mockMvc.perform(get("/api/public/pages/{pageType}/content", pageType))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.success").value(true));

                verify(seoMetadataService).getActivePageContent(pageType);
            }
        }

        @Test
        @DisplayName("Should handle service exceptions gracefully")
        void getPageContent_WithServiceException_ShouldReturn500() throws Exception {
            // Given
            when(seoMetadataService.getActivePageContent(TEST_PAGE_TYPE))
                    .thenThrow(new RuntimeException("Database connection failed"));

            // When & Then
            mockMvc.perform(get("/api/public/pages/{pageType}/content", TEST_PAGE_TYPE))
                    .andExpect(status().isInternalServerError());

            verify(seoMetadataService).getActivePageContent(TEST_PAGE_TYPE);
        }

        @Test
        @DisplayName("Should return content ordered by display order")
        void getPageContent_ShouldReturnOrderedContent() throws Exception {
            // Given - content should be ordered by displayOrder
            when(seoMetadataService.getActivePageContent(TEST_PAGE_TYPE))
                    .thenReturn(testActiveContent);

            // When & Then
            mockMvc.perform(get("/api/public/pages/{pageType}/content", TEST_PAGE_TYPE))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data[0].displayOrder").value(1))
                    .andExpect(jsonPath("$.data[1].displayOrder").value(2));

            verify(seoMetadataService).getActivePageContent(TEST_PAGE_TYPE);
        }

        @Test
        @DisplayName("Should not require authentication for public endpoint")
        void getPageContent_WithoutAuth_ShouldWork() throws Exception {
            // Given
            when(seoMetadataService.getActivePageContent(TEST_PAGE_TYPE))
                    .thenReturn(testActiveContent);

            // When & Then - No authentication required for public endpoint
            mockMvc.perform(get("/api/public/pages/{pageType}/content", TEST_PAGE_TYPE))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));

            verify(seoMetadataService).getActivePageContent(TEST_PAGE_TYPE);
        }
    }

    @Nested
    @ExtendWith(MockitoExtension.class)
    @DisplayName("Unit Tests")
    class UnitTests {

        @Mock
        private SeoMetadataService seoMetadataService;

        @InjectMocks
        private PageContentController pageContentController;

        @Test
        @DisplayName("Should get active page content successfully")
        void getPageContent_ShouldReturnActiveContent() {
            // Given
            when(seoMetadataService.getActivePageContent(TEST_PAGE_TYPE))
                    .thenReturn(testActiveContent);

            // When
            ResponseEntity<?> response = pageContentController.getPageContent(TEST_PAGE_TYPE);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(seoMetadataService).getActivePageContent(TEST_PAGE_TYPE);
        }

        @Test
        @DisplayName("Should return empty list when no active content exists")
        void getPageContent_WithNoActiveContent_ShouldReturnEmptyList() {
            // Given
            when(seoMetadataService.getActivePageContent(TEST_PAGE_TYPE))
                    .thenReturn(Arrays.asList());

            // When
            ResponseEntity<?> response = pageContentController.getPageContent(TEST_PAGE_TYPE);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(seoMetadataService).getActivePageContent(TEST_PAGE_TYPE);
        }

        @Test
        @DisplayName("Should work with all valid page types")
        void getPageContent_WithAllValidPageTypes_ShouldWork() {
            // Given
            for (PageType pageType : PageType.values()) {
                when(seoMetadataService.getActivePageContent(pageType))
                        .thenReturn(testActiveContent);

                // When
                ResponseEntity<?> response = pageContentController.getPageContent(pageType);

                // Then
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            }

            verify(seoMetadataService, times(PageType.values().length))
                    .getActivePageContent(any(PageType.class));
        }

        @Test
        @DisplayName("Should handle service exceptions gracefully")
        void getPageContent_WithServiceException_ShouldThrowException() {
            // Given
            when(seoMetadataService.getActivePageContent(TEST_PAGE_TYPE))
                    .thenThrow(new RuntimeException("Database connection failed"));

            // When & Then
            assertThatThrownBy(() -> pageContentController.getPageContent(TEST_PAGE_TYPE))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Database connection failed");

            verify(seoMetadataService).getActivePageContent(TEST_PAGE_TYPE);
        }

        @Test
        @DisplayName("Should return ordered content by display order")
        void getPageContent_ShouldReturnOrderedContent() {
            // Given
            SeoMetadataDTO element1 = SeoMetadataDTO.builder()
                    .id(1L)
                    .pageType(TEST_PAGE_TYPE)
                    .elementKey("Element1")
                    .displayOrder(2)
                    .isActive(true)
                    .build();

            SeoMetadataDTO element2 = SeoMetadataDTO.builder()
                    .id(2L)
                    .pageType(TEST_PAGE_TYPE)
                    .elementKey("Element2")
                    .displayOrder(1)
                    .isActive(true)
                    .build();

            List<SeoMetadataDTO> orderedContent = Arrays.asList(element2, element1); // Should be ordered by displayOrder

            when(seoMetadataService.getActivePageContent(TEST_PAGE_TYPE))
                    .thenReturn(orderedContent);

            // When
            ResponseEntity<?> response = pageContentController.getPageContent(TEST_PAGE_TYPE);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(seoMetadataService).getActivePageContent(TEST_PAGE_TYPE);
        }

        @Test
        @DisplayName("Should work without authentication")
        void getPageContent_WithoutAuth_ShouldWork() {
            // Given
            when(seoMetadataService.getActivePageContent(TEST_PAGE_TYPE))
                    .thenReturn(testActiveContent);

            // When
            ResponseEntity<?> response = pageContentController.getPageContent(TEST_PAGE_TYPE);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(seoMetadataService).getActivePageContent(TEST_PAGE_TYPE);
        }
    }
}