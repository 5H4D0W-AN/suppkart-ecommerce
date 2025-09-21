package com.suppkart.service;

import com.suppkart.dto.content.PageDTO;
import com.suppkart.dto.content.PageCreateRequest;
import com.suppkart.dto.content.PageUpdateRequest;
import com.suppkart.exception.ResourceNotFoundException;
import com.suppkart.model.entity.Page;
import com.suppkart.model.entity.User;
import com.suppkart.model.enums.PageStatus;
import com.suppkart.repository.PageRepository;
import com.suppkart.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PageService {

    private final PageRepository pageRepository;
    private final UserRepository userRepository;
    private final SlugGenerator slugGenerator;

    /**
     * Create a new page
     */
    public PageDTO createPage(PageCreateRequest request) {
        log.info("Creating new page with title: {}", request.getTitle());

        // Get current user
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(currentUsername)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + currentUsername));

        // Generate unique slug
        String baseSlug = slugGenerator.generateSlug(request.getTitle());
        String uniqueSlug = slugGenerator.ensureUniqueSlug(baseSlug, pageRepository::existsBySlug);

        // Create page entity
        Page page = Page.builder()
                .title(request.getTitle())
                .slug(uniqueSlug)
                .content(request.getContent())
                .status(request.getStatus() != null ? request.getStatus() : PageStatus.DRAFT)
                .lastUpdatedBy(currentUser)
                .metaTitle(request.getMetaTitle())
                .metaDescription(request.getMetaDescription())
                .metaKeywords(request.getMetaKeywords())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Page savedPage = pageRepository.save(page);
        log.info("Page created successfully with ID: {} and slug: {}", savedPage.getId(), savedPage.getSlug());

        return convertToDTO(savedPage);
    }

    /**
     * Update an existing page
     */
    public PageDTO updatePage(Long id, PageUpdateRequest request) {
        log.info("Updating page with ID: {}", id);

        Page page = pageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Page not found with ID: " + id));

        // Get current user
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(currentUsername)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + currentUsername));

        // Update slug if title changed
        if (request.getTitle() != null && !request.getTitle().equals(page.getTitle())) {
            String baseSlug = slugGenerator.generateSlug(request.getTitle());
            String uniqueSlug = slugGenerator.ensureUniqueSlug(baseSlug, slug -> 
                pageRepository.existsBySlug(slug) && !slug.equals(page.getSlug()));
            page.setSlug(uniqueSlug);
        }

        // Update fields
        if (request.getTitle() != null) {
            page.setTitle(request.getTitle());
        }
        if (request.getContent() != null) {
            page.setContent(request.getContent());
        }
        if (request.getStatus() != null) {
            page.setStatus(request.getStatus());
        }
        if (request.getMetaTitle() != null) {
            page.setMetaTitle(request.getMetaTitle());
        }
        if (request.getMetaDescription() != null) {
            page.setMetaDescription(request.getMetaDescription());
        }
        if (request.getMetaKeywords() != null) {
            page.setMetaKeywords(request.getMetaKeywords());
        }

        page.setLastUpdatedBy(currentUser);
        page.setUpdatedAt(LocalDateTime.now());

        Page savedPage = pageRepository.save(page);
        log.info("Page updated successfully with ID: {}", savedPage.getId());

        return convertToDTO(savedPage);
    }

    /**
     * Get published page by slug (for public access)
     */
    @Transactional(readOnly = true)
    public PageDTO getPublishedPageBySlug(String slug) {
        log.info("Fetching published page by slug: {}", slug);

        Page page = pageRepository.findBySlugAndStatus(slug, PageStatus.PUBLISHED)
                .orElseThrow(() -> new ResourceNotFoundException("Published page not found with slug: " + slug));

        return convertToDTO(page);
    }

    /**
     * Get page by ID (for admin use)
     */
    @Transactional(readOnly = true)
    public PageDTO getPageById(Long id) {
        log.info("Fetching page by ID: {}", id);

        Page page = pageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Page not found with ID: " + id));

        return convertToDTO(page);
    }

    /**
     * Get all pages (for admin use)
     */
    @Transactional(readOnly = true)
    public List<PageDTO> getAllPages() {
        log.info("Fetching all pages");

        List<Page> pages = pageRepository.findAllByOrderByUpdatedAtDesc();
        return pages.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get pages by status
     */
    @Transactional(readOnly = true)
    public List<PageDTO> getPagesByStatus(PageStatus status) {
        log.info("Fetching pages by status: {}", status);

        List<Page> pages = pageRepository.findByStatusOrderByUpdatedAtDesc(status);
        return pages.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Delete a page
     */
    public void deletePage(Long id) {
        log.info("Deleting page with ID: {}", id);

        Page page = pageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Page not found with ID: " + id));

        pageRepository.delete(page);
        log.info("Page deleted successfully with ID: {}", id);
    }

    /**
     * Publish a page
     */
    public PageDTO publishPage(Long id) {
        log.info("Publishing page with ID: {}", id);

        Page page = pageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Page not found with ID: " + id));

        // Get current user
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(currentUsername)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + currentUsername));

        page.setStatus(PageStatus.PUBLISHED);
        page.setLastUpdatedBy(currentUser);
        page.setUpdatedAt(LocalDateTime.now());

        Page savedPage = pageRepository.save(page);
        log.info("Page published successfully with ID: {}", savedPage.getId());

        return convertToDTO(savedPage);
    }

    /**
     * Unpublish a page (change status to draft)
     */
    public PageDTO unpublishPage(Long id) {
        log.info("Unpublishing page with ID: {}", id);

        Page page = pageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Page not found with ID: " + id));

        // Get current user
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(currentUsername)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + currentUsername));

        page.setStatus(PageStatus.DRAFT);
        page.setLastUpdatedBy(currentUser);
        page.setUpdatedAt(LocalDateTime.now());

        Page savedPage = pageRepository.save(page);
        log.info("Page unpublished successfully with ID: {}", savedPage.getId());

        return convertToDTO(savedPage);
    }

    /**
     * Check if slug exists
     */
    @Transactional(readOnly = true)
    public boolean slugExists(String slug) {
        return pageRepository.existsBySlug(slug);
    }

    /**
     * Check if slug exists excluding current page
     */
    @Transactional(readOnly = true)
    public boolean slugExistsExcludingId(String slug, Long excludeId) {
        return pageRepository.existsBySlugAndIdNot(slug, excludeId);
    }

    /**
     * Convert Page entity to DTO
     */
    private PageDTO convertToDTO(Page page) {
        return PageDTO.builder()
                .id(page.getId())
                .title(page.getTitle())
                .slug(page.getSlug())
                .content(page.getContent())
                .status(page.getStatus())
                .lastUpdatedBy(page.getLastUpdatedBy() != null ? 
                    PageDTO.UserDTO.builder()
                        .id(page.getLastUpdatedBy().getUserId())
                        .name(page.getLastUpdatedBy().getFullName())
                        .email(page.getLastUpdatedBy().getEmail())
                        .build() : null)
                .createdAt(page.getCreatedAt())
                .updatedAt(page.getUpdatedAt())
                .metaTitle(page.getMetaTitle())
                .metaDescription(page.getMetaDescription())
                .metaKeywords(page.getMetaKeywords())
                .build();
    }
}
