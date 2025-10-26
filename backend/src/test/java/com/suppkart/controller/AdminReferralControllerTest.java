package com.suppkart.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.suppkart.dto.response.ApiResponse;
import com.suppkart.dto.response.ReferralDto;
import com.suppkart.dto.response.ReferralRewardDto;
import com.suppkart.model.enums.ReferralStatus;
import com.suppkart.model.enums.RewardStatus;
import com.suppkart.model.enums.RewardType;
import com.suppkart.service.ReferralRewardService;
import com.suppkart.service.ReferralService;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminReferralController Tests")
class AdminReferralControllerTest {

    @Mock
    private ReferralService referralService;

    @Mock
    private ReferralRewardService referralRewardService;

    @InjectMocks
    private AdminReferralController adminReferralController;

    private ReferralDto referralDto;
    private ReferralRewardDto referralRewardDto;
    private Page<ReferralDto> referralPage;
    private Page<ReferralRewardDto> rewardPage;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        // Setup pageable
        pageable = PageRequest.of(0, 10);

        // Setup referral DTO
        referralDto = new ReferralDto();
        referralDto.setReferralId(1L);
        referralDto.setReferralCode("REF123ABC");
        referralDto.setReferrerUserId(1L);
        referralDto.setReferrerUserName("John Doe");
        referralDto.setReferredUserId(2L);
        referralDto.setReferredUserName("Jane Smith");
        referralDto.setStatus(ReferralStatus.USED);
        referralDto.setCreatedAt(LocalDateTime.now().minusDays(5));
        referralDto.setUsageDate(LocalDateTime.now().minusDays(3));
        referralDto.setFirstOrderId(100L);
        referralDto.setFirstOrderCompletionDate(LocalDateTime.now().minusDays(1));

        // Setup referral reward DTO
        referralRewardDto = new ReferralRewardDto();
        referralRewardDto.setRewardId(1L);
        referralRewardDto.setUserId(1L);
        referralRewardDto.setUserName("John Doe");
        referralRewardDto.setReferralId(1L);
        referralRewardDto.setReferralCode("REF123ABC");
        referralRewardDto.setRewardType(RewardType.CREDIT);
        referralRewardDto.setRewardAmount(BigDecimal.valueOf(50.00));
        referralRewardDto.setStatus(RewardStatus.ACTIVE);
        referralRewardDto.setExpirationDate(LocalDateTime.now().plusDays(10));
        referralRewardDto.setIsReferrerReward(true);
        referralRewardDto.setCreatedAt(LocalDateTime.now().minusDays(2));

        // Setup pages
        List<ReferralDto> referralList = Arrays.asList(referralDto);
        referralPage = new PageImpl<>(referralList, pageable, 1);

        List<ReferralRewardDto> rewardList = Arrays.asList(referralRewardDto);
        rewardPage = new PageImpl<>(rewardList, pageable, 1);
    }

    @Nested
    @DisplayName("Get All Referrals Tests")
    class GetAllReferralsTests {

        @Test
        @DisplayName("Should return all referrals without filters")
        void shouldReturnAllReferralsWithoutFilters() {
            // Arrange
            when(referralService.getAllReferralsAdmin(null, null, null, pageable))
                    .thenReturn(referralPage);

            // Act
            ResponseEntity<ApiResponse<Page<ReferralDto>>> response = 
                    adminReferralController.getAllReferrals(null, null, null, pageable);

            // Assert
            assertNotNull(response);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertTrue(response.getBody().isSuccess());
            assertEquals("Referrals retrieved successfully", response.getBody().getMessage());
            assertNotNull(response.getBody().getData());
            assertEquals(1, response.getBody().getData().getTotalElements());
            assertEquals(referralDto.getReferralId(), 
                    response.getBody().getData().getContent().get(0).getReferralId());

            verify(referralService).getAllReferralsAdmin(null, null, null, pageable);
        }

        @Test
        @DisplayName("Should return filtered referrals by status")
        void shouldReturnFilteredReferralsByStatus() {
            // Arrange
            ReferralStatus status = ReferralStatus.USED;
            when(referralService.getAllReferralsAdmin(status, null, null, pageable))
                    .thenReturn(referralPage);

            // Act
            ResponseEntity<ApiResponse<Page<ReferralDto>>> response = 
                    adminReferralController.getAllReferrals(status, null, null, pageable);

            // Assert
            assertNotNull(response);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertTrue(response.getBody().isSuccess());
            assertEquals(1, response.getBody().getData().getTotalElements());

            verify(referralService).getAllReferralsAdmin(status, null, null, pageable);
        }

        @Test
        @DisplayName("Should return filtered referrals by referrer name")
        void shouldReturnFilteredReferralsByReferrerName() {
            // Arrange
            String referrerName = "John";
            when(referralService.getAllReferralsAdmin(null, referrerName, null, pageable))
                    .thenReturn(referralPage);

            // Act
            ResponseEntity<ApiResponse<Page<ReferralDto>>> response = 
                    adminReferralController.getAllReferrals(null, referrerName, null, pageable);

            // Assert
            assertNotNull(response);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertTrue(response.getBody().isSuccess());
            assertEquals(1, response.getBody().getData().getTotalElements());

            verify(referralService).getAllReferralsAdmin(null, referrerName, null, pageable);
        }

        @Test
        @DisplayName("Should return filtered referrals by referral code")
        void shouldReturnFilteredReferralsByReferralCode() {
            // Arrange
            String referralCode = "REF123";
            when(referralService.getAllReferralsAdmin(null, null, referralCode, pageable))
                    .thenReturn(referralPage);

            // Act
            ResponseEntity<ApiResponse<Page<ReferralDto>>> response = 
                    adminReferralController.getAllReferrals(null, null, referralCode, pageable);

            // Assert
            assertNotNull(response);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertTrue(response.getBody().isSuccess());
            assertEquals(1, response.getBody().getData().getTotalElements());

            verify(referralService).getAllReferralsAdmin(null, null, referralCode, pageable);
        }

        @Test
        @DisplayName("Should return filtered referrals with all filters")
        void shouldReturnFilteredReferralsWithAllFilters() {
            // Arrange
            ReferralStatus status = ReferralStatus.USED;
            String referrerName = "John";
            String referralCode = "REF123";
            when(referralService.getAllReferralsAdmin(status, referrerName, referralCode, pageable))
                    .thenReturn(referralPage);

            // Act
            ResponseEntity<ApiResponse<Page<ReferralDto>>> response = 
                    adminReferralController.getAllReferrals(status, referrerName, referralCode, pageable);

            // Assert
            assertNotNull(response);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertTrue(response.getBody().isSuccess());
            assertEquals(1, response.getBody().getData().getTotalElements());

            verify(referralService).getAllReferralsAdmin(status, referrerName, referralCode, pageable);
        }

        @Test
        @DisplayName("Should return empty page when no referrals found")
        void shouldReturnEmptyPageWhenNoReferralsFound() {
            // Arrange
            Page<ReferralDto> emptyPage = new PageImpl<>(Arrays.asList(), pageable, 0);
            when(referralService.getAllReferralsAdmin(null, null, null, pageable))
                    .thenReturn(emptyPage);

            // Act
            ResponseEntity<ApiResponse<Page<ReferralDto>>> response = 
                    adminReferralController.getAllReferrals(null, null, null, pageable);

            // Assert
            assertNotNull(response);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertTrue(response.getBody().isSuccess());
            assertEquals(0, response.getBody().getData().getTotalElements());

            verify(referralService).getAllReferralsAdmin(null, null, null, pageable);
        }
    }

    @Nested
    @DisplayName("Get Platform Referral Stats Tests")
    class GetPlatformReferralStatsTests {

        @Test
        @DisplayName("Should return platform referral statistics")
        void shouldReturnPlatformReferralStatistics() {
            // Arrange
            Map<String, Object> monthlyStats = new HashMap<>();
            monthlyStats.put("monthlyReferrals", 25L);
            monthlyStats.put("monthlySuccessful", 20L);
            monthlyStats.put("period", "OCTOBER 2025");

            when(referralService.getTotalReferralsCount()).thenReturn(100L);
            when(referralService.getSuccessfulReferralsCount()).thenReturn(80L);
            when(referralService.getPendingReferralsCount()).thenReturn(15L);
            when(referralRewardService.getTotalRewardsCount()).thenReturn(150L);
            when(referralRewardService.getTotalRewardValue()).thenReturn(BigDecimal.valueOf(7500.00));
            when(referralRewardService.getActiveRewardsCount()).thenReturn(45L);
            when(referralRewardService.getAppliedRewardsCount()).thenReturn(90L);
            when(referralService.getMonthlyReferralStats()).thenReturn(monthlyStats);

            // Act
            ResponseEntity<ApiResponse<Map<String, Object>>> response = 
                    adminReferralController.getPlatformReferralStats();

            // Assert
            assertNotNull(response);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertTrue(response.getBody().isSuccess());
            assertEquals("Platform referral statistics retrieved successfully", response.getBody().getMessage());
            
            Map<String, Object> stats = response.getBody().getData();
            assertNotNull(stats);
            assertEquals(100L, stats.get("totalReferrals"));
            assertEquals(80L, stats.get("successfulReferrals"));
            assertEquals(15L, stats.get("pendingReferrals"));
            assertEquals(150L, stats.get("totalRewards"));
            assertEquals(BigDecimal.valueOf(7500.00), stats.get("totalRewardValue"));
            assertEquals(45L, stats.get("activeRewards"));
            assertEquals(90L, stats.get("appliedRewards"));
            assertEquals(monthlyStats, stats.get("monthlyStats"));

            // Verify all service calls
            verify(referralService).getTotalReferralsCount();
            verify(referralService).getSuccessfulReferralsCount();
            verify(referralService).getPendingReferralsCount();
            verify(referralRewardService).getTotalRewardsCount();
            verify(referralRewardService).getTotalRewardValue();
            verify(referralRewardService).getActiveRewardsCount();
            verify(referralRewardService).getAppliedRewardsCount();
            verify(referralService).getMonthlyReferralStats();
        }

        @Test
        @DisplayName("Should handle zero statistics gracefully")
        void shouldHandleZeroStatisticsGracefully() {
            // Arrange
            Map<String, Object> emptyMonthlyStats = new HashMap<>();
            emptyMonthlyStats.put("monthlyReferrals", 0L);
            emptyMonthlyStats.put("monthlySuccessful", 0L);

            when(referralService.getTotalReferralsCount()).thenReturn(0L);
            when(referralService.getSuccessfulReferralsCount()).thenReturn(0L);
            when(referralService.getPendingReferralsCount()).thenReturn(0L);
            when(referralRewardService.getTotalRewardsCount()).thenReturn(0L);
            when(referralRewardService.getTotalRewardValue()).thenReturn(BigDecimal.ZERO);
            when(referralRewardService.getActiveRewardsCount()).thenReturn(0L);
            when(referralRewardService.getAppliedRewardsCount()).thenReturn(0L);
            when(referralService.getMonthlyReferralStats()).thenReturn(emptyMonthlyStats);

            // Act
            ResponseEntity<ApiResponse<Map<String, Object>>> response = 
                    adminReferralController.getPlatformReferralStats();

            // Assert
            assertNotNull(response);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertTrue(response.getBody().isSuccess());
            
            Map<String, Object> stats = response.getBody().getData();
            assertEquals(0L, stats.get("totalReferrals"));
            assertEquals(BigDecimal.ZERO, stats.get("totalRewardValue"));
        }
    }

    @Nested
    @DisplayName("Get All Rewards Tests")
    class GetAllRewardsTests {

        @Test
        @DisplayName("Should return all rewards without filters")
        void shouldReturnAllRewardsWithoutFilters() {
            // Arrange
            when(referralRewardService.getAllRewardsWithFilters(null, null, null, pageable))
                    .thenReturn(rewardPage);

            // Act
            ResponseEntity<ApiResponse<Page<ReferralRewardDto>>> response = 
                    adminReferralController.getAllRewards(null, null, null, pageable);

            // Assert
            assertNotNull(response);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertTrue(response.getBody().isSuccess());
            assertEquals("Rewards retrieved successfully", response.getBody().getMessage());
            assertEquals(1, response.getBody().getData().getTotalElements());
            assertEquals(referralRewardDto.getRewardId(), 
                    response.getBody().getData().getContent().get(0).getRewardId());

            verify(referralRewardService).getAllRewardsWithFilters(null, null, null, pageable);
        }

        @Test
        @DisplayName("Should return filtered rewards by status")
        void shouldReturnFilteredRewardsByStatus() {
            // Arrange
            RewardStatus status = RewardStatus.ACTIVE;
            when(referralRewardService.getAllRewardsWithFilters(status, null, null, pageable))
                    .thenReturn(rewardPage);

            // Act
            ResponseEntity<ApiResponse<Page<ReferralRewardDto>>> response = 
                    adminReferralController.getAllRewards(status, null, null, pageable);

            // Assert
            assertNotNull(response);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertTrue(response.getBody().isSuccess());
            assertEquals(1, response.getBody().getData().getTotalElements());

            verify(referralRewardService).getAllRewardsWithFilters(status, null, null, pageable);
        }

        @Test
        @DisplayName("Should return filtered rewards by user name")
        void shouldReturnFilteredRewardsByUserName() {
            // Arrange
            String userName = "John";
            when(referralRewardService.getAllRewardsWithFilters(null, userName, null, pageable))
                    .thenReturn(rewardPage);

            // Act
            ResponseEntity<ApiResponse<Page<ReferralRewardDto>>> response = 
                    adminReferralController.getAllRewards(null, userName, null, pageable);

            // Assert
            assertNotNull(response);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertTrue(response.getBody().isSuccess());
            assertEquals(1, response.getBody().getData().getTotalElements());

            verify(referralRewardService).getAllRewardsWithFilters(null, userName, null, pageable);
        }

        @Test
        @DisplayName("Should return filtered rewards by referrer reward flag")
        void shouldReturnFilteredRewardsByReferrerRewardFlag() {
            // Arrange
            Boolean isReferrerReward = true;
            when(referralRewardService.getAllRewardsWithFilters(null, null, isReferrerReward, pageable))
                    .thenReturn(rewardPage);

            // Act
            ResponseEntity<ApiResponse<Page<ReferralRewardDto>>> response = 
                    adminReferralController.getAllRewards(null, null, isReferrerReward, pageable);

            // Assert
            assertNotNull(response);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertTrue(response.getBody().isSuccess());
            assertEquals(1, response.getBody().getData().getTotalElements());

            verify(referralRewardService).getAllRewardsWithFilters(null, null, isReferrerReward, pageable);
        }

        @Test
        @DisplayName("Should return filtered rewards with all filters")
        void shouldReturnFilteredRewardsWithAllFilters() {
            // Arrange
            RewardStatus status = RewardStatus.ACTIVE;
            String userName = "John";
            Boolean isReferrerReward = true;
            when(referralRewardService.getAllRewardsWithFilters(status, userName, isReferrerReward, pageable))
                    .thenReturn(rewardPage);

            // Act
            ResponseEntity<ApiResponse<Page<ReferralRewardDto>>> response = 
                    adminReferralController.getAllRewards(status, userName, isReferrerReward, pageable);

            // Assert
            assertNotNull(response);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertTrue(response.getBody().isSuccess());
            assertEquals(1, response.getBody().getData().getTotalElements());

            verify(referralRewardService).getAllRewardsWithFilters(status, userName, isReferrerReward, pageable);
        }

        @Test
        @DisplayName("Should return empty page when no rewards found")
        void shouldReturnEmptyPageWhenNoRewardsFound() {
            // Arrange
            Page<ReferralRewardDto> emptyPage = new PageImpl<>(Arrays.asList(), pageable, 0);
            when(referralRewardService.getAllRewardsWithFilters(null, null, null, pageable))
                    .thenReturn(emptyPage);

            // Act
            ResponseEntity<ApiResponse<Page<ReferralRewardDto>>> response = 
                    adminReferralController.getAllRewards(null, null, null, pageable);

            // Assert
            assertNotNull(response);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertTrue(response.getBody().isSuccess());
            assertEquals(0, response.getBody().getData().getTotalElements());

            verify(referralRewardService).getAllRewardsWithFilters(null, null, null, pageable);
        }
    }

    @Nested
    @DisplayName("Get Referral Analytics Tests")
    class GetReferralAnalyticsTests {

        @Test
        @DisplayName("Should return referral analytics without period")
        void shouldReturnReferralAnalyticsWithoutPeriod() {
            // Arrange
            Map<String, Object> analytics = new HashMap<>();
            analytics.put("period", "month");
            analytics.put("totalReferrals", 50L);
            analytics.put("successfulReferrals", 40L);
            analytics.put("conversionRate", 80.0);

            when(referralService.getReferralAnalytics(null)).thenReturn(analytics);

            // Act
            ResponseEntity<ApiResponse<Map<String, Object>>> response = 
                    adminReferralController.getReferralAnalytics(null);

            // Assert
            assertNotNull(response);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertTrue(response.getBody().isSuccess());
            assertEquals("Referral analytics retrieved successfully", response.getBody().getMessage());
            
            Map<String, Object> responseData = response.getBody().getData();
            assertEquals("month", responseData.get("period"));
            assertEquals(50L, responseData.get("totalReferrals"));
            assertEquals(40L, responseData.get("successfulReferrals"));
            assertEquals(80.0, responseData.get("conversionRate"));

            verify(referralService).getReferralAnalytics(null);
        }

        @Test
        @DisplayName("Should return referral analytics for week period")
        void shouldReturnReferralAnalyticsForWeekPeriod() {
            // Arrange
            String period = "week";
            Map<String, Object> analytics = new HashMap<>();
            analytics.put("period", period);
            analytics.put("totalReferrals", 10L);
            analytics.put("successfulReferrals", 8L);
            analytics.put("conversionRate", 80.0);

            when(referralService.getReferralAnalytics(period)).thenReturn(analytics);

            // Act
            ResponseEntity<ApiResponse<Map<String, Object>>> response = 
                    adminReferralController.getReferralAnalytics(period);

            // Assert
            assertNotNull(response);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertTrue(response.getBody().isSuccess());
            assertEquals(period, response.getBody().getData().get("period"));
            assertEquals(10L, response.getBody().getData().get("totalReferrals"));

            verify(referralService).getReferralAnalytics(period);
        }

        @Test
        @DisplayName("Should return referral analytics for month period")
        void shouldReturnReferralAnalyticsForMonthPeriod() {
            // Arrange
            String period = "month";
            Map<String, Object> analytics = new HashMap<>();
            analytics.put("period", period);
            analytics.put("totalReferrals", 100L);
            analytics.put("successfulReferrals", 75L);
            analytics.put("conversionRate", 75.0);

            when(referralService.getReferralAnalytics(period)).thenReturn(analytics);

            // Act
            ResponseEntity<ApiResponse<Map<String, Object>>> response = 
                    adminReferralController.getReferralAnalytics(period);

            // Assert
            assertNotNull(response);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertTrue(response.getBody().isSuccess());
            assertEquals(period, response.getBody().getData().get("period"));
            assertEquals(100L, response.getBody().getData().get("totalReferrals"));

            verify(referralService).getReferralAnalytics(period);
        }

        @Test
        @DisplayName("Should return referral analytics for year period")
        void shouldReturnReferralAnalyticsForYearPeriod() {
            // Arrange
            String period = "year";
            Map<String, Object> analytics = new HashMap<>();
            analytics.put("period", period);
            analytics.put("totalReferrals", 1200L);
            analytics.put("successfulReferrals", 900L);
            analytics.put("conversionRate", 75.0);

            when(referralService.getReferralAnalytics(period)).thenReturn(analytics);

            // Act
            ResponseEntity<ApiResponse<Map<String, Object>>> response = 
                    adminReferralController.getReferralAnalytics(period);

            // Assert
            assertNotNull(response);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertTrue(response.getBody().isSuccess());
            assertEquals(period, response.getBody().getData().get("period"));
            assertEquals(1200L, response.getBody().getData().get("totalReferrals"));

            verify(referralService).getReferralAnalytics(period);
        }

        @Test
        @DisplayName("Should handle empty analytics gracefully")
        void shouldHandleEmptyAnalyticsGracefully() {
            // Arrange
            Map<String, Object> emptyAnalytics = new HashMap<>();
            when(referralService.getReferralAnalytics(null)).thenReturn(emptyAnalytics);

            // Act
            ResponseEntity<ApiResponse<Map<String, Object>>> response = 
                    adminReferralController.getReferralAnalytics(null);

            // Assert
            assertNotNull(response);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertTrue(response.getBody().isSuccess());
            assertTrue(response.getBody().getData().isEmpty());

            verify(referralService).getReferralAnalytics(null);
        }
    }

    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests {

        @Test
        @DisplayName("Should handle multiple concurrent requests")
        void shouldHandleMultipleConcurrentRequests() {
            // Arrange
            when(referralService.getAllReferralsAdmin(any(), any(), any(), any()))
                    .thenReturn(referralPage);
            when(referralRewardService.getAllRewardsWithFilters(any(), any(), any(), any()))
                    .thenReturn(rewardPage);

            // Act & Assert - Simulate concurrent requests
            ResponseEntity<ApiResponse<Page<ReferralDto>>> referralResponse1 = 
                    adminReferralController.getAllReferrals(null, null, null, pageable);
            ResponseEntity<ApiResponse<Page<ReferralDto>>> referralResponse2 = 
                    adminReferralController.getAllReferrals(ReferralStatus.USED, null, null, pageable);
            ResponseEntity<ApiResponse<Page<ReferralRewardDto>>> rewardResponse = 
                    adminReferralController.getAllRewards(null, null, null, pageable);

            assertNotNull(referralResponse1);
            assertNotNull(referralResponse2);
            assertNotNull(rewardResponse);
            assertEquals(HttpStatus.OK, referralResponse1.getStatusCode());
            assertEquals(HttpStatus.OK, referralResponse2.getStatusCode());
            assertEquals(HttpStatus.OK, rewardResponse.getStatusCode());
        }

        @Test
        @DisplayName("Should maintain data consistency across endpoints")
        void shouldMaintainDataConsistencyAcrossEndpoints() {
            // Arrange
            when(referralService.getAllReferralsAdmin(any(), any(), any(), any()))
                    .thenReturn(referralPage);
            when(referralService.getTotalReferralsCount()).thenReturn(1L);

            // Act
            ResponseEntity<ApiResponse<Page<ReferralDto>>> referralResponse = 
                    adminReferralController.getAllReferrals(null, null, null, pageable);
            ResponseEntity<ApiResponse<Map<String, Object>>> statsResponse = 
                    adminReferralController.getPlatformReferralStats();

            // Assert
            assertEquals(1, referralResponse.getBody().getData().getTotalElements());
            assertEquals(1L, statsResponse.getBody().getData().get("totalReferrals"));
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle null pageable gracefully")
        void shouldHandleNullPageableGracefully() {
            // Arrange
            when(referralService.getAllReferralsAdmin(any(), any(), any(), isNull()))
                    .thenReturn(referralPage);

            // Act
            ResponseEntity<ApiResponse<Page<ReferralDto>>> response = 
                    adminReferralController.getAllReferrals(null, null, null, null);

            // Assert
            assertNotNull(response);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            verify(referralService).getAllReferralsAdmin(any(), any(), any(), isNull());
        }

        @Test
        @DisplayName("Should handle empty string filters")
        void shouldHandleEmptyStringFilters() {
            // Arrange
            when(referralService.getAllReferralsAdmin(any(), eq(""), eq(""), any()))
                    .thenReturn(referralPage);

            // Act
            ResponseEntity<ApiResponse<Page<ReferralDto>>> response = 
                    adminReferralController.getAllReferrals(null, "", "", pageable);

            // Assert
            assertNotNull(response);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            verify(referralService).getAllReferralsAdmin(any(), eq(""), eq(""), any());
        }

        @Test
        @DisplayName("Should handle whitespace-only filters")
        void shouldHandleWhitespaceOnlyFilters() {
            // Arrange
            when(referralService.getAllReferralsAdmin(any(), eq("   "), eq("   "), any()))
                    .thenReturn(referralPage);

            // Act
            ResponseEntity<ApiResponse<Page<ReferralDto>>> response = 
                    adminReferralController.getAllReferrals(null, "   ", "   ", pageable);

            // Assert
            assertNotNull(response);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            verify(referralService).getAllReferralsAdmin(any(), eq("   "), eq("   "), any());
        }
    }
}