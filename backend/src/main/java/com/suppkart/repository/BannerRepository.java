package com.suppkart.repository;

import com.suppkart.model.entity.Banner;
import com.suppkart.model.enums.TargetDevice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BannerRepository extends JpaRepository<Banner, Long> {

    /**
     * Find all active banners
     */
    List<Banner> findByActiveTrue();

    /**
     * Find active banners within date range
     */
    @Query("SELECT b FROM Banner b WHERE b.active = true AND " +
           "(b.startDate IS NULL OR b.startDate <= :now) AND " +
           "(b.endDate IS NULL OR b.endDate >= :now)")
    List<Banner> findActiveBannersInDateRange(@Param("now") LocalDateTime now);

    /**
     * Find active banners by location
     */
    List<Banner> findByLocationAndActiveTrue(String location);

    /**
     * Find active banners by target device
     */
    List<Banner> findByTargetDeviceAndActiveTrue(TargetDevice targetDevice);

    /**
     * Find active banners by location and target device
     */
    List<Banner> findByLocationAndTargetDeviceAndActiveTrue(String location, TargetDevice targetDevice);

    /**
     * Find all active banners ordered by display order
     */
    List<Banner> findByActiveTrueOrderByDisplayOrder();

    /**
     * Find active banners by location ordered by display order
     */
    List<Banner> findByLocationAndActiveTrueOrderByDisplayOrder(String location);

    /**
     * Find active banners by target device ordered by display order
     */
    List<Banner> findByTargetDeviceAndActiveTrueOrderByDisplayOrder(TargetDevice targetDevice);

    /**
     * Find active banners by location and target device ordered by display order
     */
    List<Banner> findByLocationAndTargetDeviceAndActiveTrueOrderByDisplayOrder(String location, TargetDevice targetDevice);

    /**
     * Find active banners within date range by location
     */
    @Query("SELECT b FROM Banner b WHERE b.active = true AND b.location = :location AND " +
           "(b.startDate IS NULL OR b.startDate <= :now) AND " +
           "(b.endDate IS NULL OR b.endDate >= :now) " +
           "ORDER BY b.displayOrder")
    List<Banner> findActiveBannersInDateRangeByLocation(@Param("location") String location, @Param("now") LocalDateTime now);

    /**
     * Find active banners within date range by target device
     */
    @Query("SELECT b FROM Banner b WHERE b.active = true AND b.targetDevice = :targetDevice AND " +
           "(b.startDate IS NULL OR b.startDate <= :now) AND " +
           "(b.endDate IS NULL OR b.endDate >= :now) " +
           "ORDER BY b.displayOrder")
    List<Banner> findActiveBannersInDateRangeByTargetDevice(@Param("targetDevice") TargetDevice targetDevice, @Param("now") LocalDateTime now);

    /**
     * Find active banners within date range by location and target device
     */
    @Query("SELECT b FROM Banner b WHERE b.active = true AND b.location = :location AND b.targetDevice = :targetDevice AND " +
           "(b.startDate IS NULL OR b.startDate <= :now) AND " +
           "(b.endDate IS NULL OR b.endDate >= :now) " +
           "ORDER BY b.displayOrder")
    List<Banner> findActiveBannersInDateRangeByLocationAndTargetDevice(
            @Param("location") String location, 
            @Param("targetDevice") TargetDevice targetDevice, 
            @Param("now") LocalDateTime now);

    /**
     * Find banners by title containing (case insensitive)
     */
    List<Banner> findByTitleContainingIgnoreCase(String title);

    /**
     * Find expired banners
     */
    @Query("SELECT b FROM Banner b WHERE b.endDate IS NOT NULL AND b.endDate < :now")
    List<Banner> findExpiredBanners(@Param("now") LocalDateTime now);

    /**
     * Find banners scheduled for future
     */
    @Query("SELECT b FROM Banner b WHERE b.startDate IS NOT NULL AND b.startDate > :now")
    List<Banner> findScheduledBanners(@Param("now") LocalDateTime now);

    /**
     * Get maximum display order for a location
     */
    @Query("SELECT COALESCE(MAX(b.displayOrder), 0) FROM Banner b WHERE b.location = :location")
    Integer getMaxDisplayOrderByLocation(@Param("location") String location);

    /**
     * Get maximum display order overall
     */
    @Query("SELECT COALESCE(MAX(b.displayOrder), 0) FROM Banner b")
    Integer getMaxDisplayOrder();

    /**
     * Count active banners by location
     */
    long countByLocationAndActiveTrue(String location);

    /**
     * Count active banners by target device
     */
    long countByTargetDeviceAndActiveTrue(TargetDevice targetDevice);
}
