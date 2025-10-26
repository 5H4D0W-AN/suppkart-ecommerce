package com.suppkart.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.suppkart.dto.content.PageContentResponse;
import com.suppkart.dto.content.SeoMetadataDTO;
import com.suppkart.dto.content.SeoMetadataRequest;
import com.suppkart.exception.ResourceNotFoundException;
import com.suppkart.model.enums.ContentType;
import com.suppkart.config.UnifiedTestConfig;
import com.suppkart.model.enums.PageType;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import com.suppkart.service.FileUploadService;
import com.suppkart.service.SeoMetadataService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = AdminPageController.class, useDefaultFilters = false)
@ContextConfiguration(classes = {AdminPageController.class, UnifiedTestConfig.class, com.suppkart.exception.GlobalExceptionHandler.class})
@DisplayName("AdminPageController Tests")
class AdminPageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SeoMetadataService seoMetadataService;

    @MockBean
    private FileUploadService fileUploadService;

    @Autowired
    private ObjectMapper objectMapper;

    private SeoMetadataDTO testSeoMetadataDTO;
    private SeoMetadataRequest testRequest;
    private PageContentResponse testPageContentResponse;
    private final Long TEST_ID = 1L;
    private final PageType TEST_PAGE_TYPE = PageType.HOME;
    private final String TEST_ELEMENT_KEY = "H2Header";

    @BeforeEach
    void setUp() {
        testSeoMetadataDTO = SeoMetadataDTO.builder()
                .id(TEST_ID)
                .pageType(TEST_PAGE_TYPE)
                .elementKey(TEST_ELEMENT_KEY)
                .elementLabel("Main Header")
                .contentType(ContentType.TEXT)
                .contentValue("Welcome to SuppKart")
                .mediaUrl(null)
                .altText(null)
                .displayOrder(1)
                .isActive(true)
                .metaTitle("Home Page")
                .metaDescription("Welcome to our store")
                .metaKeywords("supplements, health, fitness")
                .ogTitle("SuppKart Home")
                .ogDescription("Your health supplement store")
                .ogImage("https://example.com/og-image.jpg")
                .canonicalUrl("https://suppkart.com/")
                .noIndex(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        testRequest = SeoMetadataRequest.builder()
                .pageType(TEST_PAGE_TYPE)
                .elementKey(TEST_ELEMENT_KEY)
                .elementLabel("Main Header")
                .contentType(ContentType.TEXT)
                .contentValue("Welcome to SuppKart")
                .mediaUrl(null)
                .altText(null)
                .displayOrder(1)
                .isActive(true)
                .metaTitle("Home Page")
                .metaDescription("Welcome to our store")
                .metaKeywords("supplements, health, fitness")
                .ogTitle("SuppKart Home")
                .ogDescription("Your health supplement store")
                .ogImage("https://example.com/og-image.jpg")
                .canonicalUrl("https://suppkart.com/")
                .noIndex(false)
                .build();

        testPageContentResponse = PageContentResponse.builder()
                .pageType(TEST_PAGE_TYPE)
                .pageDisplayName(TEST_PAGE_TYPE.getDisplayName())
                .elements(Arrays.asList(testSeoMetadataDTO))
                .totalElements(1)
                .build();
    }

    @Test
    @DisplayName("Should get all page types successfully")
    @WithMockUser(roles = {"ADMIN"})
    void getAllPageTypes_ShouldReturnPageTypes() throws Exception {
        // Given
        List<PageType> pageTypes = Arrays.asList(PageType.values());
        when(seoMetadataService.getAllPageTypes()).thenReturn(pageTypes);

        // When & Then
        mockMvc.perform(get("/api/admin/pages/types")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Page types retrieved successfully"))
                .andExpect(jsonPath("$.data", hasSize(PageType.values().length)))
                .andExpect(jsonPath("$.data", containsInAnyOrder(
                        Arrays.stream(PageType.values())
                                .map(Enum::name)
                                .toArray(String[]::new))));

        verify(seoMetadataService).getAllPageTypes();
    }

    @Test
    @DisplayName("Should require authentication for page types endpoint")
    void getAllPageTypes_WithoutAuth_ShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/admin/pages/types"))
                .andExpect(status().isUnauthorized());

        verify(seoMetadataService, never()).getAllPageTypes();
    }

    @Test
    @DisplayName("Should get page content successfully")
    @WithMockUser(roles = {"ADMIN"})
    void getPageContent_ShouldReturnPageContent() throws Exception {
        // Given
        when(seoMetadataService.getPageContent(TEST_PAGE_TYPE)).thenReturn(testPageContentResponse);

        // When & Then
        mockMvc.perform(get("/api/admin/pages/{pageType}", TEST_PAGE_TYPE)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Page content retrieved successfully"))
                .andExpect(jsonPath("$.data.pageType").value(TEST_PAGE_TYPE.name()))
                .andExpect(jsonPath("$.data.pageDisplayName").value(TEST_PAGE_TYPE.getDisplayName()))
                .andExpect(jsonPath("$.data.elements", hasSize(1)))
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.elements[0].id").value(TEST_ID))
                .andExpect(jsonPath("$.data.elements[0].elementKey").value(TEST_ELEMENT_KEY));

        verify(seoMetadataService).getPageContent(TEST_PAGE_TYPE);
    }

    @Test
    @DisplayName("Should create element successfully")
    @WithMockUser(roles = {"ADMIN"})
    void createElement_ShouldCreateAndReturnElement() throws Exception {
        // Given
        when(seoMetadataService.createElement(any(SeoMetadataRequest.class)))
                .thenReturn(testSeoMetadataDTO);

        // When & Then
        mockMvc.perform(post("/api/admin/pages/{pageType}/elements", TEST_PAGE_TYPE)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Element created successfully"))
                .andExpect(jsonPath("$.data.id").value(TEST_ID))
                .andExpect(jsonPath("$.data.elementKey").value(TEST_ELEMENT_KEY))
                .andExpect(jsonPath("$.data.pageType").value(TEST_PAGE_TYPE.name()));

        verify(seoMetadataService).createElement(argThat(request -> 
            request.getPageType().equals(TEST_PAGE_TYPE) &&
            request.getElementKey().equals(TEST_ELEMENT_KEY)));
    }

    @Test
    @DisplayName("Should validate request when creating element")
    @WithMockUser(roles = {"ADMIN"})
    void createElement_WithInvalidRequest_ShouldReturn400() throws Exception {
        // Given - invalid request with missing required fields
        SeoMetadataRequest invalidRequest = SeoMetadataRequest.builder()
                .elementKey("") // Invalid - blank
                .contentType(null) // Invalid - null
                .build();

        // When & Then
        mockMvc.perform(post("/api/admin/pages/{pageType}/elements", TEST_PAGE_TYPE)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(seoMetadataService, never()).createElement(any());
    }

    @Test
    @DisplayName("Should handle duplicate element key error")
    @WithMockUser(roles = {"ADMIN"})
    void createElement_WithDuplicateKey_ShouldReturn400() throws Exception {
        // Given
        when(seoMetadataService.createElement(any(SeoMetadataRequest.class)))
                .thenThrow(new IllegalArgumentException("Element with key 'H2Header' already exists"));

        // When & Then
        mockMvc.perform(post("/api/admin/pages/{pageType}/elements", TEST_PAGE_TYPE)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testRequest)))
                .andExpect(status().isBadRequest());

        verify(seoMetadataService).createElement(any(SeoMetadataRequest.class));
    }

    @Test
    @DisplayName("Should update element successfully")
    @WithMockUser(roles = {"ADMIN"})
    void updateElement_ShouldUpdateAndReturnElement() throws Exception {
        // Given
        when(seoMetadataService.updateElement(eq(TEST_ID), any(SeoMetadataRequest.class)))
                .thenReturn(testSeoMetadataDTO);

        // When & Then
        mockMvc.perform(put("/api/admin/pages/elements/{id}", TEST_ID)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Element updated successfully"))
                .andExpect(jsonPath("$.data.id").value(TEST_ID))
                .andExpect(jsonPath("$.data.elementKey").value(TEST_ELEMENT_KEY));

        verify(seoMetadataService).updateElement(eq(TEST_ID), any(SeoMetadataRequest.class));
    }

    @Test
    @DisplayName("Should handle element not found error on update")
    @WithMockUser(roles = {"ADMIN"})
    void updateElement_WithNonExistentId_ShouldReturn404() throws Exception {
        // Given
        when(seoMetadataService.updateElement(eq(TEST_ID), any(SeoMetadataRequest.class)))
                .thenThrow(new ResourceNotFoundException("Element not found with ID: " + TEST_ID));

        // When & Then
        mockMvc.perform(put("/api/admin/pages/elements/{id}", TEST_ID)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testRequest)))
                .andExpect(status().isNotFound());

        verify(seoMetadataService).updateElement(eq(TEST_ID), any(SeoMetadataRequest.class));
    }

    @Test
    @DisplayName("Should get element by ID successfully")
    @WithMockUser(roles = {"ADMIN"})
    void getElementById_ShouldReturnElement() throws Exception {
        // Given
        when(seoMetadataService.getElementById(TEST_ID)).thenReturn(testSeoMetadataDTO);

        // When & Then
        mockMvc.perform(get("/api/admin/pages/elements/{id}", TEST_ID)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Element retrieved successfully"))
                .andExpect(jsonPath("$.data.id").value(TEST_ID))
                .andExpect(jsonPath("$.data.elementKey").value(TEST_ELEMENT_KEY));

        verify(seoMetadataService).getElementById(TEST_ID);
    }

    @Test
    @DisplayName("Should handle element not found error on get")
    @WithMockUser(roles = {"ADMIN"})
    void getElementById_WithNonExistentId_ShouldReturn404() throws Exception {
        // Given
        when(seoMetadataService.getElementById(TEST_ID))
                .thenThrow(new ResourceNotFoundException("Element not found with ID: " + TEST_ID));

        // When & Then
        mockMvc.perform(get("/api/admin/pages/elements/{id}", TEST_ID)
                        .with(csrf()))
                .andExpect(status().isNotFound());

        verify(seoMetadataService).getElementById(TEST_ID);
    }

    @Test
    @DisplayName("Should delete element successfully")
    @WithMockUser(roles = {"ADMIN"})
    void deleteElement_ShouldDeleteElement() throws Exception {
        // Given
        doNothing().when(seoMetadataService).deleteElement(TEST_ID);

        // When & Then
        mockMvc.perform(delete("/api/admin/pages/elements/{id}", TEST_ID)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Element deleted successfully"));

        verify(seoMetadataService).deleteElement(TEST_ID);
    }

    @Test
    @DisplayName("Should handle element not found error on delete")
    @WithMockUser(roles = {"ADMIN"})
    void deleteElement_WithNonExistentId_ShouldReturn404() throws Exception {
        // Given
        doThrow(new ResourceNotFoundException("Element not found with ID: " + TEST_ID))
                .when(seoMetadataService).deleteElement(TEST_ID);

        // When & Then
        mockMvc.perform(delete("/api/admin/pages/elements/{id}", TEST_ID)
                        .with(csrf()))
                .andExpect(status().isNotFound());

        verify(seoMetadataService).deleteElement(TEST_ID);
    }

    @Test
    @DisplayName("Should upload media file successfully")
    @WithMockUser(roles = {"ADMIN"})
    void uploadMedia_ShouldUploadAndReturnUrl() throws Exception {
        // Given
        MockMultipartFile file = new MockMultipartFile(
                "file", 
                "test-image.jpg", 
                MediaType.IMAGE_JPEG_VALUE, 
                "test image content".getBytes());
        
        String expectedUrl = "https://example.com/uploads/home/test-image.jpg";
        when(fileUploadService.uploadFile(any(), eq("home"))).thenReturn(expectedUrl);

        // When & Then
        mockMvc.perform(multipart("/api/admin/pages/{pageType}/upload", TEST_PAGE_TYPE)
                        .file(file)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("File uploaded successfully"))
                .andExpect(jsonPath("$.data").value(expectedUrl));

        verify(fileUploadService).uploadFile(any(), eq("home"));
    }

    @Test
    @DisplayName("Should handle file upload error")
    @WithMockUser(roles = {"ADMIN"})
    void uploadMedia_WithUploadError_ShouldReturn500() throws Exception {
        // Given
        MockMultipartFile file = new MockMultipartFile(
                "file", 
                "test-image.jpg", 
                MediaType.IMAGE_JPEG_VALUE, 
                "test image content".getBytes());
        
        when(fileUploadService.uploadFile(any(), eq("home")))
                .thenThrow(new RuntimeException("Upload failed"));

        // When & Then
        mockMvc.perform(multipart("/api/admin/pages/{pageType}/upload", TEST_PAGE_TYPE)
                        .file(file)
                        .with(csrf()))
                .andExpect(status().isInternalServerError());

        verify(fileUploadService).uploadFile(any(), eq("home"));
    }

    @Test
    @DisplayName("Should require ADMIN or CONTENT_MANAGER role")
    @WithMockUser(roles = {"USER"})
    void adminEndpoints_WithUserRole_ShouldReturn403() throws Exception {
        mockMvc.perform(get("/api/admin/pages/types")
                        .with(csrf()))
                .andExpect(status().isForbidden());

        verify(seoMetadataService, never()).getAllPageTypes();
    }

    @Test
    @DisplayName("Should allow CONTENT_MANAGER role")
    @WithMockUser(roles = {"CONTENT_MANAGER"})
    void adminEndpoints_WithContentManagerRole_ShouldAllow() throws Exception {
        // Given
        List<PageType> pageTypes = Arrays.asList(PageType.values());
        when(seoMetadataService.getAllPageTypes()).thenReturn(pageTypes);

        // When & Then
        mockMvc.perform(get("/api/admin/pages/types")
                        .with(csrf()))
                .andExpect(status().isOk());

        verify(seoMetadataService).getAllPageTypes();
    }

    @Test
    @DisplayName("Should handle invalid page type parameter")
    @WithMockUser(roles = {"ADMIN"})
    void getPageContent_WithInvalidPageType_ShouldReturn400() throws Exception {
        mockMvc.perform(get("/api/admin/pages/{pageType}", "INVALID_PAGE_TYPE")
                        .with(csrf()))
                .andExpect(status().isBadRequest());

        verify(seoMetadataService, never()).getPageContent(any());
    }

    @Test
    @DisplayName("Should create subdirectory name correctly for upload")
    @WithMockUser(roles = {"ADMIN"})
    void uploadMedia_ShouldCreateCorrectSubdirectory() throws Exception {
        // Given
        MockMultipartFile file = new MockMultipartFile(
                "file", 
                "test-image.jpg", 
                MediaType.IMAGE_JPEG_VALUE, 
                "test image content".getBytes());
        
        String expectedUrl = "https://example.com/uploads/aboutus/test-image.jpg";
        when(fileUploadService.uploadFile(any(), eq("aboutus"))).thenReturn(expectedUrl);

        // When & Then
        mockMvc.perform(multipart("/api/admin/pages/{pageType}/upload", PageType.ABOUT_US)
                        .file(file)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(expectedUrl));

        // Verify subdirectory is created correctly (ABOUT_US -> aboutus)
        verify(fileUploadService).uploadFile(any(), eq("aboutus"));
    }
}