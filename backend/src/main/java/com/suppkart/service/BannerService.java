package com.suppkart.service;

import com.suppkart.dto.content.*;
import com.suppkart.exception.ResourceNotFoundException;
import com.suppkart.model.entity.Banner;
import com.suppkart.model.enums.TargetDevice;
import com.suppkart.repository.BannerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing banners
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BannerService {

    private final BannerRepository bannerRepository;
    private final StorageService storageService;

    /**
     * Create a new banner
     */
    public BannerDTO createBanner(BannerCreateRequest request) {
        log.info("Creating new banner with title: {}", request.getTitle());

        Banner banner = Banner.builder()
                .title(request.getTitle())
                .imageUrl(request.getImageUrl())
                .linkUrl(request.getLinkUrl())
                .altText(request.getAltText())
                .displayOrder(request.getDisplayOrder())
                .active(request.getActive() != null ? request.getActive() : true)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .targetDevice(request.getTargetDevice() != null ? request.getTargetDevice() : TargetDevice.ALL)
                .location(request.getLocation())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Banner savedBanner = bannerRepository.save(banner);
        log.info("Banner created successfully with ID: {}", savedBanner.getId());

        return convertToDTO(savedBanner);
    }

    /**
     * Update an existing banner
     */
    public BannerDTO updateBanner(Long id, BannerUpdateRequest request) {
        log.info("Updating banner with ID: {}", id);

        Banner banner = bannerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Banner not found with ID: " + id));

        // Update fields if provided
        if (request.getTitle() != null) {
            banner.setTitle(request.getTitle());
        }
        if (request.getImageUrl() != null) {
            banner.setImageUrl(request.getImageUrl());
        }
        if (request.getLinkUrl() != null) {
            banner.setLinkUrl(request.getLinkUrl());
        }
        if (request.getAltText() != null) {
            banner.setAltText(request.getAltText());
        }
        if (request.getDisplayOrder() != null) {
            banner.setDisplayOrder(request.getDisplayOrder());
        }
        if (request.getActive() != null) {
            banner.setActive(request.getActive());
        }
        if (request.getStartDate() != null) {
            banner.setStartDate(request.getStartDate());
        }
        if (request.getEndDate() != null) {
            banner.setEndDate(request.getEndDate());
        }
        if (request.getTargetDevice() != null) {
            banner.setTargetDevice(request.getTargetDevice());
        }
        if (request.getLocation() != null) {
            banner.setLocation(request.getLocation());
        }

        banner.setUpdatedAt(LocalDateTime.now());
        Banner updatedBanner = bannerRepository.save(banner);

        log.info("Banner updated successfully with ID: {}", updatedBanner.getId());
        return convertToDTO(updatedBanner);
    }

    /**
     * Get banner by ID
     */
    @Transactional(readOnly = true)
    public BannerDTO getBannerById(Long id) {
        log.info("Fetching banner with ID: {}", id);

        Banner banner = bannerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Banner not found with ID: " + id));

        return convertToDTO(banner);
    }

    /**
     * Get all banners with filtering
     */
    @Transactional(readOnly = true)
    public Page<BannerDTO> getAllBanners(BannerFilterRequest filter, Pageable pageable) {
        log.info("Fetching banners with filter: {}", filter);

        // Create sort
        Sort sort = createSort(filter.getSortBy(), filter.getSortDirection());
        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);

        // For now, return all banners with sorting - filtering can be enhanced later
        Page<Banner> banners = bannerRepository.findAll(sortedPageable);

        // Apply basic filtering if title is provided
        if (filter.getTitle() != null && !filter.getTitle().trim().isEmpty()) {
            List<Banner> filteredBanners = bannerRepository.findByTitleContainingIgnoreCase(filter.getTitle());
            // Convert to Page manually for simplicity
            int start = (int) pageable.getOffset();
            int end = Math.min((start + pageable.getPageSize()), filteredBanners.size());
            List<Banner> pageContent = filteredBanners.subList(start, end);
            banners = new org.springframework.data.domain.PageImpl<>(pageContent, pageable, filteredBanners.size());
        }

        return banners.map(this::convertToDTO);
    }

    /**
     * Get active banners for location and device
     */
    @Transactional(readOnly = true)
    public List<BannerDTO> getActiveBanners(String location, String device) {
        log.info("Fetching active banners for location: {} and device: {}", location, device);

        LocalDateTime now = LocalDateTime.now();
        List<Banner> banners;

        if (location != null && device != null) {
            TargetDevice targetDevice = TargetDevice.valueOf(device.toUpperCase());
            banners = bannerRepository.findByLocationAndTargetDeviceAndActiveTrue(location, targetDevice);
        } else if (location != null) {
            banners = bannerRepository.findByLocationAndActiveTrue(location);
        } else if (device != null) {
            TargetDevice targetDevice = TargetDevice.valueOf(device.toUpperCase());
            banners = bannerRepository.findByTargetDeviceAndActiveTrue(targetDevice);
        } else {
            banners = bannerRepository.findByActiveTrueOrderByDisplayOrder();
        }

        // Filter by date range
        banners = banners.stream()
                .filter(banner -> {
                    boolean startDateValid = banner.getStartDate() == null || banner.getStartDate().isBefore(now) || banner.getStartDate().isEqual(now);
                    boolean endDateValid = banner.getEndDate() == null || banner.getEndDate().isAfter(now) || banner.getEndDate().isEqual(now);
                    return startDateValid && endDateValid;
                })
                .collect(Collectors.toList());

        return banners.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Delete banner
     */
    public void deleteBanner(Long id) {
        log.info("Deleting banner with ID: {}", id);

        Banner banner = bannerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Banner not found with ID: " + id));

        // Delete associated image if exists
        if (banner.getImageUrl() != null && !banner.getImageUrl().isEmpty()) {
            try {
                storageService.deleteFile(banner.getImageUrl());
            } catch (Exception e) {
                log.warn("Failed to delete banner image: {}", banner.getImageUrl(), e);
            }
        }

        bannerRepository.delete(banner);
        log.info("Banner deleted successfully with ID: {}", id);
    }

    /**
     * Activate banner
     */
    public void activateBanner(Long id) {
        log.info("Activating banner with ID: {}", id);

        Banner banner = bannerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Banner not found with ID: " + id));

        banner.setActive(true);
        banner.setUpdatedAt(LocalDateTime.now());
        bannerRepository.save(banner);

        log.info("Banner activated successfully with ID: {}", id);
    }

    /**
     * Deactivate banner
     */
    public void deactivateBanner(Long id) {
        log.info("Deactivating banner with ID: {}", id);

        Banner banner = bannerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Banner not found with ID: " + id));

        banner.setActive(false);
        banner.setUpdatedAt(LocalDateTime.now());
        bannerRepository.save(banner);

        log.info("Banner deactivated successfully with ID: {}", id);
    }

    /**
     * Upload banner image
     */
    public String uploadBannerImage(MultipartFile file) {
        log.info("Uploading banner image: {}", file.getOriginalFilename());

        try {
            String imageUrl = storageService.uploadImage(file, "banners");
            log.info("Banner image uploaded successfully: {}", imageUrl);
            return imageUrl;
        } catch (IOException e) {
            log.error("Failed to upload banner image: {}", file.getOriginalFilename(), e);
            throw new RuntimeException("Failed to upload banner image: " + e.getMessage(), e);
        }
    }

    /**
     * Reorder banners
     */
    public void reorderBanners(List<BannerOrderRequest> orderRequests) {
        log.info("Reordering {} banners", orderRequests.size());

        for (BannerOrderRequest orderRequest : orderRequests) {
            Banner banner = bannerRepository.findById(orderRequest.getBannerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Banner not found with ID: " + orderRequest.getBannerId()));

            banner.setDisplayOrder(orderRequest.getDisplayOrder());
            banner.setUpdatedAt(LocalDateTime.now());
            bannerRepository.save(banner);
        }

        log.info("Banners reordered successfully");
    }

    /**
     * Convert Banner entity to DTO
     */
    private BannerDTO convertToDTO(Banner banner) {
        return BannerDTO.builder()
                .id(banner.getId())
                .title(banner.getTitle())
                .imageUrl(banner.getImageUrl())
                .linkUrl(banner.getLinkUrl())
                .altText(banner.getAltText())
                .displayOrder(banner.getDisplayOrder())
                .active(banner.getActive())
                .startDate(banner.getStartDate())
                .endDate(banner.getEndDate())
                .targetDevice(banner.getTargetDevice())
                .location(banner.getLocation())
                .createdAt(banner.getCreatedAt())
                .updatedAt(banner.getUpdatedAt())
                .build();
    }

    /**
     * Create sort object
     */
    private Sort createSort(String sortBy, String sortDirection) {
        String field = sortBy != null ? sortBy : "displayOrder";
        Sort.Direction direction = "DESC".equalsIgnoreCase(sortDirection) ? Sort.Direction.DESC : Sort.Direction.ASC;
        return Sort.by(direction, field);
    }

    /**
     * Check if filter has any criteria
     */
    private boolean hasFilters(BannerFilterRequest filter) {
        return filter.getActive() != null ||
                (filter.getLocation() != null && !filter.getLocation().trim().isEmpty()) ||
                filter.getTargetDevice() != null ||
                (filter.getTitle() != null && !filter.getTitle().trim().isEmpty()) ||
                filter.getStartDate() != null ||
                filter.getEndDate() != null;
    }
}
