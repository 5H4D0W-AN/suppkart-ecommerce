package com.suppkart.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.suppkart.dto.response.ApiResponse;
import com.suppkart.dto.response.ReferralDto;
import com.suppkart.dto.response.ReferralRewardDto;
import com.suppkart.model.enums.ReferralStatus;
import com.suppkart.model.enums.RewardStatus;
import com.suppkart.service.ReferralRewardService;
import com.suppkart.service.ReferralService;

@RestController
@RequestMapping("/api/admin/referrals")
@PreAuthorize("hasRole('ADMIN')")
public class AdminReferralController {

    @Autowired
    private ReferralService referralService;

    @Autowired
    private ReferralRewardService referralRewardService;

    /**
     * List all referrals with filters and pagination
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<ReferralDto>>> getAllReferrals(
            @RequestParam(required = false) ReferralStatus status,
            @RequestParam(required = false) String referrerName,
            @RequestParam(required = false) String referralCode,
            Pageable pageable) {

        Page<ReferralDto> referrals = referralService.getAllReferralsAdmin(status, referrerName, referralCode, pageable);
        return ResponseEntity.ok(new ApiResponse<>(true, "Referrals retrieved successfully", referrals));
    }

    /**
     * Get platform-wide referral statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getPlatformReferralStats() {

        Map<String, Object> stats = new HashMap<>();

        // Get overall statistics
        stats.put("totalReferrals", referralService.getTotalReferralsCount());
        stats.put("successfulReferrals", referralService.getSuccessfulReferralsCount());
        stats.put("pendingReferrals", referralService.getPendingReferralsCount());
        stats.put("totalRewards", referralRewardService.getTotalRewardsCount());
        stats.put("totalRewardValue", referralRewardService.getTotalRewardValue());
        stats.put("activeRewards", referralRewardService.getActiveRewardsCount());
        stats.put("appliedRewards", referralRewardService.getAppliedRewardsCount());

        // Get monthly statistics
        stats.put("monthlyStats", referralService.getMonthlyReferralStats());

        return ResponseEntity.ok(new ApiResponse<>(true, "Platform referral statistics retrieved successfully", stats));
    }

    /**
     * List all rewards with filters
     */
    @GetMapping("/rewards")
    public ResponseEntity<ApiResponse<Page<ReferralRewardDto>>> getAllRewards(
            @RequestParam(required = false) RewardStatus status,
            @RequestParam(required = false) String userName,
            @RequestParam(required = false) Boolean isReferrerReward,
            Pageable pageable) {

        Page<ReferralRewardDto> rewards = referralRewardService.getAllRewardsWithFilters(status, userName, isReferrerReward, pageable);
        return ResponseEntity.ok(new ApiResponse<>(true, "Rewards retrieved successfully", rewards));
    }

    /**
     * Get referral analytics
     */
    @GetMapping("/analytics")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getReferralAnalytics(
            @RequestParam(required = false) String period) {

        Map<String, Object> analytics = referralService.getReferralAnalytics(period);
        return ResponseEntity.ok(new ApiResponse<>(true, "Referral analytics retrieved successfully", analytics));
    }

}
