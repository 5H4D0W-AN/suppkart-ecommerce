package com.suppkart.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.suppkart.dto.response.ApiResponse;
import com.suppkart.dto.response.ReferralDto;
import com.suppkart.dto.response.ReferralRewardDto;
import com.suppkart.dto.response.ReferralStatsDto;
import com.suppkart.model.entity.User;
import com.suppkart.service.ReferralRewardService;
import com.suppkart.service.ReferralService;
import com.suppkart.service.UserService;

@RestController
@RequestMapping("/api/referrals")
@PreAuthorize("hasRole('USER')")
public class ReferralController {
    
    @Autowired
    private ReferralService referralService;
    
    @Autowired
    private ReferralRewardService referralRewardService;
    
    @Autowired
    private UserService userService;
    
    /**
     * Get user's referral code
     */
    @GetMapping("/my-code")
    public ResponseEntity<ApiResponse<Map<String, String>>> getMyReferralCode(Authentication authentication) {
        
        User currentUser = userService.findByEmail(authentication.getName()).orElseThrow();
        String referralCode = referralService.getUserReferralCode(currentUser);
        
        Map<String, String> response = new HashMap<>();
        response.put("referralCode", referralCode);
        response.put("shareUrl", "https://suppkart.com/register?ref=" + referralCode);
        
        return ResponseEntity.ok(new ApiResponse<>(true, "Referral code retrieved successfully", response));
    }
    
    /**
     * Get user's referral statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<ReferralStatsDto>> getReferralStats(Authentication authentication) {
        
        User currentUser = userService.findByEmail(authentication.getName()).orElseThrow();
        ReferralStatsDto stats = referralService.getUserReferralStats(currentUser);
        return ResponseEntity.ok(new ApiResponse<>(true, "Referral statistics retrieved successfully", stats));
    }
    
    /**
     * Get user's referrals with pagination
     */
    @GetMapping("/my-referrals")
    public ResponseEntity<ApiResponse<Page<ReferralDto>>> getMyReferrals(
            Authentication authentication,
            Pageable pageable) {
        
        User currentUser = userService.findByEmail(authentication.getName()).orElseThrow();
        Page<ReferralDto> referrals = referralService.getUserReferrals(currentUser, pageable);
        return ResponseEntity.ok(new ApiResponse<>(true, "User referrals retrieved successfully", referrals));
    }
    
    /**
     * Generate sharing links for various platforms
     */
    @PostMapping("/share")
    public ResponseEntity<ApiResponse<Map<String, String>>> generateSharingLinks(
            Authentication authentication,
            @RequestParam(required = false, defaultValue = "all") String platform) {
        
        User currentUser = userService.findByEmail(authentication.getName()).orElseThrow();
        String referralCode = referralService.getUserReferralCode(currentUser);
        Map<String, String> sharingLinks = new HashMap<>();
        
        if ("all".equals(platform)) {
            sharingLinks.put("whatsapp", referralService.generateSharingUrl(referralCode, "whatsapp"));
            sharingLinks.put("facebook", referralService.generateSharingUrl(referralCode, "facebook"));
            sharingLinks.put("twitter", referralService.generateSharingUrl(referralCode, "twitter"));
            sharingLinks.put("email", referralService.generateSharingUrl(referralCode, "email"));
            sharingLinks.put("direct", referralService.generateSharingUrl(referralCode, "direct"));
        } else {
            sharingLinks.put(platform, referralService.generateSharingUrl(referralCode, platform));
        }
        
        return ResponseEntity.ok(new ApiResponse<>(true, "Sharing links generated successfully", sharingLinks));
    }
    
    /**
     * Get available rewards for current user
     */
    @GetMapping("/rewards")
    public ResponseEntity<ApiResponse<List<ReferralRewardDto>>> getAvailableRewards(Authentication authentication) {
        
        User currentUser = userService.findByEmail(authentication.getName()).orElseThrow();
        List<ReferralRewardDto> rewards = referralRewardService.getActiveRewardsByUser(currentUser);
        return ResponseEntity.ok(new ApiResponse<>(true, "Available rewards retrieved successfully", rewards));
    }
    
    /**
     * Get user's reward history with pagination
     */
    @GetMapping("/rewards/history")
    public ResponseEntity<ApiResponse<Page<ReferralRewardDto>>> getRewardHistory(
            Authentication authentication,
            Pageable pageable) {
        
        User currentUser = userService.findByEmail(authentication.getName()).orElseThrow();
        Page<ReferralRewardDto> rewards = referralRewardService.getUserRewards(currentUser, pageable);
        return ResponseEntity.ok(new ApiResponse<>(true, "Reward history retrieved successfully", rewards));
    }
    
    /**
     * Check if referral code is valid
     */
    @GetMapping("/validate")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> validateReferralCode(@RequestParam String code) {
        
        boolean isValid = referralService.isValidReferralCode(code);
        
        Map<String, Boolean> response = new HashMap<>();
        response.put("valid", isValid);
        
        return ResponseEntity.ok(new ApiResponse<>(true, "Referral code validation completed", response));
    }
    
    /**
     * Get available credit amount
     */
    @GetMapping("/credit")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAvailableCredit(Authentication authentication) {
        
        User currentUser = userService.findByEmail(authentication.getName()).orElseThrow();
        var availableCredit = referralRewardService.getAvailableCreditForUser(currentUser);
        boolean hasRewards = referralRewardService.hasAvailableRewards(currentUser);
        
        Map<String, Object> response = new HashMap<>();
        response.put("availableCredit", availableCredit);
        response.put("hasAvailableRewards", hasRewards);
        
        return ResponseEntity.ok(new ApiResponse<>(true, "Available credit retrieved successfully", response));
    }
}
