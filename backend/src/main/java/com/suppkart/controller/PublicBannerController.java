package com.suppkart.controller;

import com.suppkart.dto.content.BannerDTO;
import com.suppkart.dto.response.ApiResponse;
import com.suppkart.model.enums.TargetDevice;
import com.suppkart.service.BannerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Public Banner Controller for frontend banner functionality Provides public
 * access to active banners for display
 */
@RestController
@RequestMapping("/api/banners")
@RequiredArgsConstructor
@Slf4j
public class PublicBannerController {

    private final BannerService bannerService;

    /**
     * Get active banners with optional location and device filtering
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<BannerDTO>>> getActiveBanners(
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String device) {
        log.info("Fetching active banners with location: {} and device: {}", location, device);

        List<BannerDTO> banners = bannerService.getActiveBanners(location, device);

        return ResponseEntity.ok(ApiResponse.success("Active banners retrieved successfully", banners));
    }

    /**
     * Get banners for homepage
     */
    @GetMapping("/homepage")
    public ResponseEntity<ApiResponse<List<BannerDTO>>> getHomepageBanners(
            @RequestParam(required = false) String device) {
        log.info("Fetching homepage banners for device: {}", device);

        List<BannerDTO> banners = bannerService.getActiveBanners("HOME_TOP", device);

        return ResponseEntity.ok(ApiResponse.success("Homepage banners retrieved successfully", banners));
    }

    /**
     * Get banners for a specific location
     */
    @GetMapping("/location/{location}")
    public ResponseEntity<ApiResponse<List<BannerDTO>>> getBannersByLocation(
            @PathVariable String location,
            @RequestParam(required = false) String device) {
        log.info("Fetching banners for location: {} and device: {}", location, device);

        List<BannerDTO> banners = bannerService.getActiveBanners(location, device);

        return ResponseEntity.ok(ApiResponse.success("Banners for location retrieved successfully", banners));
    }

    /**
     * Get banners for mobile devices
     */
    @GetMapping("/mobile")
    public ResponseEntity<ApiResponse<List<BannerDTO>>> getMobileBanners(
            @RequestParam(required = false) String location) {
        log.info("Fetching mobile banners for location: {}", location);

        List<BannerDTO> banners = bannerService.getActiveBanners(location, "MOBILE");

        return ResponseEntity.ok(ApiResponse.success("Mobile banners retrieved successfully", banners));
    }

    /**
     * Get banners for desktop devices
     */
    @GetMapping("/desktop")
    public ResponseEntity<ApiResponse<List<BannerDTO>>> getDesktopBanners(
            @RequestParam(required = false) String location) {
        log.info("Fetching desktop banners for location: {}", location);

        List<BannerDTO> banners = bannerService.getActiveBanners(location, "DESKTOP");

        return ResponseEntity.ok(ApiResponse.success("Desktop banners retrieved successfully", banners));
    }

    /**
     * Get banners for product pages
     */
    @GetMapping("/product-page")
    public ResponseEntity<ApiResponse<List<BannerDTO>>> getProductPageBanners(
            @RequestParam(required = false) String device) {
        log.info("Fetching product page banners for device: {}", device);

        List<BannerDTO> banners = bannerService.getActiveBanners("PRODUCT_PAGE", device);

        return ResponseEntity.ok(ApiResponse.success("Product page banners retrieved successfully", banners));
    }

    /**
     * Get banners for category pages
     */
    @GetMapping("/category-page")
    public ResponseEntity<ApiResponse<List<BannerDTO>>> getCategoryPageBanners(
            @RequestParam(required = false) String device) {
        log.info("Fetching category page banners for device: {}", device);

        List<BannerDTO> banners = bannerService.getActiveBanners("CATEGORY_PAGE", device);

        return ResponseEntity.ok(ApiResponse.success("Category page banners retrieved successfully", banners));
    }

    /**
     * Get banners for checkout page
     */
    @GetMapping("/checkout")
    public ResponseEntity<ApiResponse<List<BannerDTO>>> getCheckoutBanners(
            @RequestParam(required = false) String device) {
        log.info("Fetching checkout banners for device: {}", device);

        List<BannerDTO> banners = bannerService.getActiveBanners("CHECKOUT", device);

        return ResponseEntity.ok(ApiResponse.success("Checkout banners retrieved successfully", banners));
    }

    /**
     * Get promotional banners (for special offers, sales, etc.)
     */
    @GetMapping("/promotional")
    public ResponseEntity<ApiResponse<List<BannerDTO>>> getPromotionalBanners(
            @RequestParam(required = false) String device) {
        log.info("Fetching promotional banners for device: {}", device);

        List<BannerDTO> banners = bannerService.getActiveBanners("PROMOTIONAL", device);

        return ResponseEntity.ok(ApiResponse.success("Promotional banners retrieved successfully", banners));
    }
}
