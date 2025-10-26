package com.suppkart.service;

import com.suppkart.dto.content.PageContentResponse;
import com.suppkart.dto.content.SeoMetadataDTO;
import com.suppkart.dto.content.SeoMetadataRequest;
import com.suppkart.exception.ResourceNotFoundException;
import com.suppkart.model.entity.SeoMetadata;
import com.suppkart.model.enums.ContentType;
import com.suppkart.model.enums.PageType;
import com.suppkart.repository.SeoMetadataRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SeoMetadataService Tests")
class SeoMetadataServiceTest {

    @Mock
    private SeoMetadataRepository seoMetadataRepository;

    @InjectMocks
    private SeoMetadataService seoMetadataService;

    private SeoMetadata testSeoMetadata;
    private SeoMetadataRequest testRequest;
    private final Long TEST_ID = 1L;
    private final PageType TEST_PAGE_TYPE = PageType.HOME;
    private final String TEST_ELEMENT_KEY = "H2Header";

    @BeforeEach
    void setUp() {
        testSeoMetadata = SeoMetadata.builder()
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
    }

    @Test
    @DisplayName("Should return all page types")
    void getAllPageTypes_ShouldReturnAllPageTypes() {
        // When
        List<PageType> result = seoMetadataService.getAllPageTypes();

        // Then
        assertThat(result).containsExactlyInAnyOrder(PageType.values());
        assertThat(result).hasSize(PageType.values().length);
    }

    @Test
    @DisplayName("Should get page content successfully")
    void getPageContent_ShouldReturnPageContentResponse() {
        // Given
        List<SeoMetadata> mockElements = Arrays.asList(testSeoMetadata);
        when(seoMetadataRepository.findByPageTypeOrderByDisplayOrderAscCreatedAtAsc(TEST_PAGE_TYPE))
                .thenReturn(mockElements);

        // When
        PageContentResponse result = seoMetadataService.getPageContent(TEST_PAGE_TYPE);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getPageType()).isEqualTo(TEST_PAGE_TYPE);
        assertThat(result.getPageDisplayName()).isEqualTo(TEST_PAGE_TYPE.getDisplayName());
        assertThat(result.getElements()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
        
        SeoMetadataDTO dto = result.getElements().get(0);
        assertThat(dto.getId()).isEqualTo(TEST_ID);
        assertThat(dto.getElementKey()).isEqualTo(TEST_ELEMENT_KEY);
        
        verify(seoMetadataRepository).findByPageTypeOrderByDisplayOrderAscCreatedAtAsc(TEST_PAGE_TYPE);
    }

    @Test
    @DisplayName("Should get active page content successfully")
    void getActivePageContent_ShouldReturnActiveElements() {
        // Given
        List<SeoMetadata> mockElements = Arrays.asList(testSeoMetadata);
        when(seoMetadataRepository.findByPageTypeAndIsActiveTrueOrderByDisplayOrderAscCreatedAtAsc(TEST_PAGE_TYPE))
                .thenReturn(mockElements);

        // When
        List<SeoMetadataDTO> result = seoMetadataService.getActivePageContent(TEST_PAGE_TYPE);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(TEST_ID);
        assertThat(result.get(0).getIsActive()).isTrue();
        
        verify(seoMetadataRepository).findByPageTypeAndIsActiveTrueOrderByDisplayOrderAscCreatedAtAsc(TEST_PAGE_TYPE);
    }

    @Test
    @DisplayName("Should create element successfully")
    void createElement_ShouldCreateAndReturnElement() {
        // Given
        when(seoMetadataRepository.existsByPageTypeAndElementKey(TEST_PAGE_TYPE, TEST_ELEMENT_KEY))
                .thenReturn(false);
        when(seoMetadataRepository.save(any(SeoMetadata.class)))
                .thenReturn(testSeoMetadata);

        // When
        SeoMetadataDTO result = seoMetadataService.createElement(testRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(TEST_ID);
        assertThat(result.getElementKey()).isEqualTo(TEST_ELEMENT_KEY);
        assertThat(result.getPageType()).isEqualTo(TEST_PAGE_TYPE);
        
        verify(seoMetadataRepository).existsByPageTypeAndElementKey(TEST_PAGE_TYPE, TEST_ELEMENT_KEY);
        verify(seoMetadataRepository).save(any(SeoMetadata.class));
    }

    @Test
    @DisplayName("Should throw exception when creating element with duplicate key")
    void createElement_WithDuplicateKey_ShouldThrowException() {
        // Given
        when(seoMetadataRepository.existsByPageTypeAndElementKey(TEST_PAGE_TYPE, TEST_ELEMENT_KEY))
                .thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> seoMetadataService.createElement(testRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Element with key '" + TEST_ELEMENT_KEY + "' already exists");
        
        verify(seoMetadataRepository).existsByPageTypeAndElementKey(TEST_PAGE_TYPE, TEST_ELEMENT_KEY);
        verify(seoMetadataRepository, never()).save(any(SeoMetadata.class));
    }

    @Test
    @DisplayName("Should create element with default values when null")
    void createElement_WithNullValues_ShouldUseDefaults() {
        // Given
        testRequest.setIsActive(null);
        testRequest.setNoIndex(null);
        
        when(seoMetadataRepository.existsByPageTypeAndElementKey(TEST_PAGE_TYPE, TEST_ELEMENT_KEY))
                .thenReturn(false);
        when(seoMetadataRepository.save(any(SeoMetadata.class)))
                .thenReturn(testSeoMetadata);

        // When
        SeoMetadataDTO result = seoMetadataService.createElement(testRequest);

        // Then
        assertThat(result).isNotNull();
        verify(seoMetadataRepository).save(argThat(element -> 
            element.getIsActive() == true && element.getNoIndex() == false));
    }

    @Test
    @DisplayName("Should update element successfully")
    void updateElement_ShouldUpdateAndReturnElement() {
        // Given
        SeoMetadata existingElement = SeoMetadata.builder()
                .id(TEST_ID)
                .pageType(TEST_PAGE_TYPE)
                .elementKey(TEST_ELEMENT_KEY)
                .build();
        
        when(seoMetadataRepository.findById(TEST_ID))
                .thenReturn(Optional.of(existingElement));
        when(seoMetadataRepository.save(any(SeoMetadata.class)))
                .thenReturn(testSeoMetadata);

        // When
        SeoMetadataDTO result = seoMetadataService.updateElement(TEST_ID, testRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(TEST_ID);
        
        verify(seoMetadataRepository).findById(TEST_ID);
        verify(seoMetadataRepository).save(any(SeoMetadata.class));
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent element")
    void updateElement_WithNonExistentId_ShouldThrowException() {
        // Given
        when(seoMetadataRepository.findById(TEST_ID))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> seoMetadataService.updateElement(TEST_ID, testRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Element not found with ID: " + TEST_ID);
        
        verify(seoMetadataRepository).findById(TEST_ID);
        verify(seoMetadataRepository, never()).save(any(SeoMetadata.class));
    }

    @Test
    @DisplayName("Should update element with default values when null")
    void updateElement_WithNullValues_ShouldUseDefaults() {
        // Given
        testRequest.setIsActive(null);
        testRequest.setNoIndex(null);
        
        SeoMetadata existingElement = SeoMetadata.builder()
                .id(TEST_ID)
                .pageType(TEST_PAGE_TYPE)
                .elementKey(TEST_ELEMENT_KEY)
                .build();
        
        when(seoMetadataRepository.findById(TEST_ID))
                .thenReturn(Optional.of(existingElement));
        when(seoMetadataRepository.save(any(SeoMetadata.class)))
                .thenReturn(testSeoMetadata);

        // When
        seoMetadataService.updateElement(TEST_ID, testRequest);

        // Then
        verify(seoMetadataRepository).save(argThat(element -> 
            element.getIsActive() == true && element.getNoIndex() == false));
    }

    @Test
    @DisplayName("Should delete element successfully")
    void deleteElement_ShouldDeleteElement() {
        // Given
        when(seoMetadataRepository.existsById(TEST_ID))
                .thenReturn(true);

        // When
        seoMetadataService.deleteElement(TEST_ID);

        // Then
        verify(seoMetadataRepository).existsById(TEST_ID);
        verify(seoMetadataRepository).deleteById(TEST_ID);
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent element")
    void deleteElement_WithNonExistentId_ShouldThrowException() {
        // Given
        when(seoMetadataRepository.existsById(TEST_ID))
                .thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> seoMetadataService.deleteElement(TEST_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Element not found with ID: " + TEST_ID);
        
        verify(seoMetadataRepository).existsById(TEST_ID);
        verify(seoMetadataRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("Should get element by ID successfully")
    void getElementById_ShouldReturnElement() {
        // Given
        when(seoMetadataRepository.findById(TEST_ID))
                .thenReturn(Optional.of(testSeoMetadata));

        // When
        SeoMetadataDTO result = seoMetadataService.getElementById(TEST_ID);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(TEST_ID);
        assertThat(result.getElementKey()).isEqualTo(TEST_ELEMENT_KEY);
        
        verify(seoMetadataRepository).findById(TEST_ID);
    }

    @Test
    @DisplayName("Should throw exception when getting non-existent element")
    void getElementById_WithNonExistentId_ShouldThrowException() {
        // Given
        when(seoMetadataRepository.findById(TEST_ID))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> seoMetadataService.getElementById(TEST_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Element not found with ID: " + TEST_ID);
        
        verify(seoMetadataRepository).findById(TEST_ID);
    }

    @Test
    @DisplayName("Should convert entity to DTO correctly")
    void convertToDTO_ShouldMapAllFields() {
        // Given - using the existing testSeoMetadata

        // When
        PageContentResponse result = seoMetadataService.getPageContent(TEST_PAGE_TYPE);
        when(seoMetadataRepository.findByPageTypeOrderByDisplayOrderAscCreatedAtAsc(TEST_PAGE_TYPE))
                .thenReturn(Arrays.asList(testSeoMetadata));

        // Re-call to get the actual conversion
        result = seoMetadataService.getPageContent(TEST_PAGE_TYPE);
        SeoMetadataDTO dto = result.getElements().get(0);

        // Then - verify all fields are mapped correctly
        assertThat(dto.getId()).isEqualTo(testSeoMetadata.getId());
        assertThat(dto.getPageType()).isEqualTo(testSeoMetadata.getPageType());
        assertThat(dto.getElementKey()).isEqualTo(testSeoMetadata.getElementKey());
        assertThat(dto.getElementLabel()).isEqualTo(testSeoMetadata.getElementLabel());
        assertThat(dto.getContentType()).isEqualTo(testSeoMetadata.getContentType());
        assertThat(dto.getContentValue()).isEqualTo(testSeoMetadata.getContentValue());
        assertThat(dto.getMediaUrl()).isEqualTo(testSeoMetadata.getMediaUrl());
        assertThat(dto.getAltText()).isEqualTo(testSeoMetadata.getAltText());
        assertThat(dto.getDisplayOrder()).isEqualTo(testSeoMetadata.getDisplayOrder());
        assertThat(dto.getIsActive()).isEqualTo(testSeoMetadata.getIsActive());
        assertThat(dto.getMetaTitle()).isEqualTo(testSeoMetadata.getMetaTitle());
        assertThat(dto.getMetaDescription()).isEqualTo(testSeoMetadata.getMetaDescription());
        assertThat(dto.getMetaKeywords()).isEqualTo(testSeoMetadata.getMetaKeywords());
        assertThat(dto.getOgTitle()).isEqualTo(testSeoMetadata.getOgTitle());
        assertThat(dto.getOgDescription()).isEqualTo(testSeoMetadata.getOgDescription());
        assertThat(dto.getOgImage()).isEqualTo(testSeoMetadata.getOgImage());
        assertThat(dto.getCanonicalUrl()).isEqualTo(testSeoMetadata.getCanonicalUrl());
        assertThat(dto.getNoIndex()).isEqualTo(testSeoMetadata.getNoIndex());
        assertThat(dto.getCreatedAt()).isEqualTo(testSeoMetadata.getCreatedAt());
        assertThat(dto.getUpdatedAt()).isEqualTo(testSeoMetadata.getUpdatedAt());
    }

    @Test
    @DisplayName("Should handle media content type correctly")
    void createElement_WithMediaContent_ShouldHandleCorrectly() {
        // Given
        testRequest.setContentType(ContentType.MEDIA);
        testRequest.setContentValue(null);
        testRequest.setMediaUrl("https://example.com/image.jpg");
        testRequest.setAltText("Product image");
        
        when(seoMetadataRepository.existsByPageTypeAndElementKey(TEST_PAGE_TYPE, TEST_ELEMENT_KEY))
                .thenReturn(false);
        when(seoMetadataRepository.save(any(SeoMetadata.class)))
                .thenReturn(testSeoMetadata);

        // When
        SeoMetadataDTO result = seoMetadataService.createElement(testRequest);

        // Then
        assertThat(result).isNotNull();
        verify(seoMetadataRepository).save(argThat(element -> 
            element.getContentType() == ContentType.MEDIA &&
            element.getMediaUrl().equals("https://example.com/image.jpg") &&
            element.getAltText().equals("Product image")));
    }
}