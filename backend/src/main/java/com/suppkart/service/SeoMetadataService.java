package com.suppkart.service;

import com.suppkart.dto.content.PageContentResponse;
import com.suppkart.dto.content.SeoMetadataRequest;
import com.suppkart.dto.content.SeoMetadataDTO;
import com.suppkart.exception.ResourceNotFoundException;
import com.suppkart.model.entity.SeoMetadata;
import com.suppkart.model.enums.PageType;
import com.suppkart.repository.SeoMetadataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SeoMetadataService {

    private final SeoMetadataRepository seoMetadataRepository;

    /**
     * Get all available page types
     */
    @Transactional(readOnly = true)
    public List<PageType> getAllPageTypes() {
        return Arrays.asList(PageType.values());
    }

    /**
     * Get all content elements for a specific page type
     */
    @Transactional(readOnly = true)
    public PageContentResponse getPageContent(PageType pageType) {
        log.info("Fetching content for page type: {}", pageType);
        
        List<SeoMetadata> elements = seoMetadataRepository.findByPageTypeOrderByDisplayOrderAscCreatedAtAsc(pageType);
        
        List<SeoMetadataDTO> elementDTOs = elements.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        return PageContentResponse.builder()
                .pageType(pageType)
                .pageDisplayName(pageType.getDisplayName())
                .elements(elementDTOs)
                .totalElements(elementDTOs.size())
                .build();
    }

    /**
     * Get active content elements for a specific page type (for frontend)
     */
    @Transactional(readOnly = true)
    public List<SeoMetadataDTO> getActivePageContent(PageType pageType) {
        log.info("Fetching active content for page type: {}", pageType);
        
        List<SeoMetadata> elements = seoMetadataRepository.findByPageTypeAndIsActiveTrueOrderByDisplayOrderAscCreatedAtAsc(pageType);
        
        return elements.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Create a new content element
     */
    public SeoMetadataDTO createElement(SeoMetadataRequest request) {
        log.info("Creating new element for page type: {} with key: {}", request.getPageType(), request.getElementKey());
        
        // Check if element key already exists for this page type
        if (seoMetadataRepository.existsByPageTypeAndElementKey(request.getPageType(), request.getElementKey())) {
            throw new IllegalArgumentException("Element with key '" + request.getElementKey() + "' already exists for page type: " + request.getPageType());
        }
        
        SeoMetadata element = SeoMetadata.builder()
                .pageType(request.getPageType())
                .elementKey(request.getElementKey())
                .elementLabel(request.getElementLabel())
                .contentType(request.getContentType())
                .contentValue(request.getContentValue())
                .mediaUrl(request.getMediaUrl())
                .altText(request.getAltText())
                .displayOrder(request.getDisplayOrder())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .metaTitle(request.getMetaTitle())
                .metaDescription(request.getMetaDescription())
                .metaKeywords(request.getMetaKeywords())
                .ogTitle(request.getOgTitle())
                .ogDescription(request.getOgDescription())
                .ogImage(request.getOgImage())
                .canonicalUrl(request.getCanonicalUrl())
                .noIndex(request.getNoIndex() != null ? request.getNoIndex() : false)
                .build();
        
        SeoMetadata savedElement = seoMetadataRepository.save(element);
        log.info("Element created successfully with ID: {}", savedElement.getId());
        
        return convertToDTO(savedElement);
    }

    /**
     * Update an existing content element
     */
    public SeoMetadataDTO updateElement(Long id, SeoMetadataRequest request) {
        log.info("Updating element with ID: {}", id);
        
        SeoMetadata element = seoMetadataRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Element not found with ID: " + id));
        
        // Update all fields directly (no null checks needed since frontend sends complete data)
        element.setElementLabel(request.getElementLabel());
        element.setContentType(request.getContentType());
        element.setContentValue(request.getContentValue());
        element.setMediaUrl(request.getMediaUrl());
        element.setAltText(request.getAltText());
        element.setDisplayOrder(request.getDisplayOrder());
        element.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);
        element.setMetaTitle(request.getMetaTitle());
        element.setMetaDescription(request.getMetaDescription());
        element.setMetaKeywords(request.getMetaKeywords());
        element.setOgTitle(request.getOgTitle());
        element.setOgDescription(request.getOgDescription());
        element.setOgImage(request.getOgImage());
        element.setCanonicalUrl(request.getCanonicalUrl());
        element.setNoIndex(request.getNoIndex() != null ? request.getNoIndex() : false);
        
        SeoMetadata savedElement = seoMetadataRepository.save(element);
        log.info("Element updated successfully with ID: {}", savedElement.getId());
        
        return convertToDTO(savedElement);
    }

    /**
     * Delete a content element
     */
    public void deleteElement(Long id) {
        log.info("Deleting element with ID: {}", id);
        
        if (!seoMetadataRepository.existsById(id)) {
            throw new ResourceNotFoundException("Element not found with ID: " + id);
        }
        
        seoMetadataRepository.deleteById(id);
        log.info("Element deleted successfully with ID: {}", id);
    }

    /**
     * Get element by ID
     */
    @Transactional(readOnly = true)
    public SeoMetadataDTO getElementById(Long id) {
        log.info("Fetching element with ID: {}", id);
        
        SeoMetadata element = seoMetadataRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Element not found with ID: " + id));
        
        return convertToDTO(element);
    }

    /**
     * Convert SeoMetadata entity to DTO
     */
    private SeoMetadataDTO convertToDTO(SeoMetadata element) {
        return SeoMetadataDTO.builder()
                .id(element.getId())
                .pageType(element.getPageType())
                .elementKey(element.getElementKey())
                .elementLabel(element.getElementLabel())
                .contentType(element.getContentType())
                .contentValue(element.getContentValue())
                .mediaUrl(element.getMediaUrl())
                .altText(element.getAltText())
                .displayOrder(element.getDisplayOrder())
                .isActive(element.getIsActive())
                .metaTitle(element.getMetaTitle())
                .metaDescription(element.getMetaDescription())
                .metaKeywords(element.getMetaKeywords())
                .ogTitle(element.getOgTitle())
                .ogDescription(element.getOgDescription())
                .ogImage(element.getOgImage())
                .canonicalUrl(element.getCanonicalUrl())
                .noIndex(element.getNoIndex())
                .createdAt(element.getCreatedAt())
                .updatedAt(element.getUpdatedAt())
                .build();
    }
}