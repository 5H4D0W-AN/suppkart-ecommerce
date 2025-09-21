package com.suppkart.service;

import com.suppkart.dto.content.SeoMetadataDTO;
import com.suppkart.dto.content.SeoMetadataRequest;
import com.suppkart.exception.ResourceNotFoundException;
import com.suppkart.model.entity.SeoMetadata;
import com.suppkart.repository.SeoMetadataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SeoService {

    private final SeoMetadataRepository seoMetadataRepository;

    /**
     * Create or update SEO metadata for a specific page type and entity
     */
    public SeoMetadataDTO createOrUpdateSeoMetadata(String pageType, Long entityId, SeoMetadataRequest request) {
        log.info("Creating or updating SEO metadata for pageType: {}, entityId: {}", pageType, entityId);
        
        Optional<SeoMetadata> existingMetadata = seoMetadataRepository.findByPageTypeAndEntityId(pageType, entityId);
        
        SeoMetadata seoMetadata;
        if (existingMetadata.isPresent()) {
            seoMetadata = existingMetadata.get();
            updateSeoMetadataFromRequest(seoMetadata, request);
            seoMetadata.setUpdatedAt(LocalDateTime.now());
            log.info("Updated existing SEO metadata with id: {}", seoMetadata.getId());
        } else {
            seoMetadata = createSeoMetadataFromRequest(pageType, entityId, request);
            log.info("Created new SEO metadata for pageType: {}, entityId: {}", pageType, entityId);
        }
        
        SeoMetadata savedMetadata = seoMetadataRepository.save(seoMetadata);
        return convertToDTO(savedMetadata);
    }

    /**
     * Get SEO metadata by page type and entity ID
     */
    @Transactional(readOnly = true)
    public SeoMetadataDTO getSeoMetadataByPageTypeAndEntityId(String pageType, Long entityId) {
        log.info("Fetching SEO metadata for pageType: {}, entityId: {}", pageType, entityId);
        
        SeoMetadata seoMetadata = seoMetadataRepository.findByPageTypeAndEntityId(pageType, entityId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "SEO metadata not found for pageType: " + pageType + " and entityId: " + entityId));
        
        return convertToDTO(seoMetadata);
    }

    /**
     * Get all SEO metadata for a specific page type
     */
    @Transactional(readOnly = true)
    public List<SeoMetadataDTO> getSeoMetadataByPageType(String pageType) {
        log.info("Fetching all SEO metadata for pageType: {}", pageType);
        
        List<SeoMetadata> metadataList = seoMetadataRepository.findByPageType(pageType);
        return metadataList.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get SEO metadata by ID
     */
    @Transactional(readOnly = true)
    public SeoMetadataDTO getSeoMetadataById(Long id) {
        log.info("Fetching SEO metadata by id: {}", id);
        
        SeoMetadata seoMetadata = seoMetadataRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SEO metadata not found with id: " + id));
        
        return convertToDTO(seoMetadata);
    }

    /**
     * Delete SEO metadata by ID
     */
    public void deleteSeoMetadata(Long id) {
        log.info("Deleting SEO metadata with id: {}", id);
        
        if (!seoMetadataRepository.existsById(id)) {
            throw new ResourceNotFoundException("SEO metadata not found with id: " + id);
        }
        
        seoMetadataRepository.deleteById(id);
        log.info("Successfully deleted SEO metadata with id: {}", id);
    }

    /**
     * Delete SEO metadata by page type and entity ID
     */
    public void deleteSeoMetadataByPageTypeAndEntityId(String pageType, Long entityId) {
        log.info("Deleting SEO metadata for pageType: {}, entityId: {}", pageType, entityId);
        
        Optional<SeoMetadata> seoMetadata = seoMetadataRepository.findByPageTypeAndEntityId(pageType, entityId);
        if (seoMetadata.isPresent()) {
            seoMetadataRepository.delete(seoMetadata.get());
            log.info("Successfully deleted SEO metadata for pageType: {}, entityId: {}", pageType, entityId);
        } else {
            log.warn("No SEO metadata found for pageType: {}, entityId: {}", pageType, entityId);
        }
    }

    /**
     * Check if SEO metadata exists for page type and entity ID
     */
    @Transactional(readOnly = true)
    public boolean existsByPageTypeAndEntityId(String pageType, Long entityId) {
        return seoMetadataRepository.findByPageTypeAndEntityId(pageType, entityId).isPresent();
    }

    /**
     * Get all SEO metadata (for admin purposes)
     */
    @Transactional(readOnly = true)
    public List<SeoMetadataDTO> getAllSeoMetadata() {
        log.info("Fetching all SEO metadata");
        
        List<SeoMetadata> metadataList = seoMetadataRepository.findAll();
        return metadataList.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Create SeoMetadata entity from request
     */
    private SeoMetadata createSeoMetadataFromRequest(String pageType, Long entityId, SeoMetadataRequest request) {
        return SeoMetadata.builder()
                .pageType(pageType)
                .entityId(entityId)
                .title(request.getTitle())
                .description(request.getDescription())
                .keywords(request.getKeywords())
                .ogTitle(request.getOgTitle())
                .ogDescription(request.getOgDescription())
                .ogImage(request.getOgImage())
                .canonicalUrl(request.getCanonicalUrl())
                .noIndex(request.getNoIndex() != null ? request.getNoIndex() : false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Update existing SeoMetadata entity from request
     */
    private void updateSeoMetadataFromRequest(SeoMetadata seoMetadata, SeoMetadataRequest request) {
        seoMetadata.setTitle(request.getTitle());
        seoMetadata.setDescription(request.getDescription());
        seoMetadata.setKeywords(request.getKeywords());
        seoMetadata.setOgTitle(request.getOgTitle());
        seoMetadata.setOgDescription(request.getOgDescription());
        seoMetadata.setOgImage(request.getOgImage());
        seoMetadata.setCanonicalUrl(request.getCanonicalUrl());
        seoMetadata.setNoIndex(request.getNoIndex() != null ? request.getNoIndex() : false);
    }

    /**
     * Convert SeoMetadata entity to DTO
     */
    private SeoMetadataDTO convertToDTO(SeoMetadata seoMetadata) {
        return SeoMetadataDTO.builder()
                .id(seoMetadata.getId())
                .pageType(seoMetadata.getPageType())
                .entityId(seoMetadata.getEntityId())
                .title(seoMetadata.getTitle())
                .description(seoMetadata.getDescription())
                .keywords(seoMetadata.getKeywords())
                .ogTitle(seoMetadata.getOgTitle())
                .ogDescription(seoMetadata.getOgDescription())
                .ogImage(seoMetadata.getOgImage())
                .canonicalUrl(seoMetadata.getCanonicalUrl())
                .noIndex(seoMetadata.getNoIndex())
                .createdAt(seoMetadata.getCreatedAt())
                .updatedAt(seoMetadata.getUpdatedAt())
                .build();
    }
}
