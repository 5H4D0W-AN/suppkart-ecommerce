package com.suppkart.service;

import com.suppkart.dto.content.*;
import com.suppkart.exception.ResourceNotFoundException;
import com.suppkart.model.entity.Banner;
import com.suppkart.model.enums.TargetDevice;
import com.suppkart.repository.BannerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BannerService Tests")
class BannerServiceTest {

    @Mock
    private BannerRepository bannerRepository;

    @Mock
    private FileUploadService fileUploadService;

    @InjectMocks
    private BannerService bannerService;

    private Banner testBanner;
    private BannerCreateRequest createRequest;
    private BannerDTO expectedDTO;

    @BeforeEach
    void setUp() {
        // Create test banner entity
        testBanner = createTestBanner();
        
        // Create test request
        createRequest = createTestBannerRequest();
        
        // Create expected DTO
        expectedDTO = createTestBannerDTO();
    }

    @Nested
    @DisplayName("Create Banner Tests")
    class CreateBannerTests {

        @Test
        @DisplayName("Should create banner successfully with all fields")
        void shouldCreateBannerSuccessfully() {
            // Given
            when(bannerRepository.save(any(Banner.class))).thenReturn(testBanner);

            // When
            BannerDTO result = bannerService.createBanner(createRequest);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getTitle()).isEqualTo(createRequest.getTitle());
            assertThat(result.getImageUrl()).isEqualTo(createRequest.getImageUrl());
            assertThat(result.getLinkUrl()).isEqualTo(createRequest.getLinkUrl());
            assertThat(result.getAltText()).isEqualTo(createRequest.getAltText());
            assertThat(result.getDisplayOrder()).isEqualTo(createRequest.getDisplayOrder());
            assertThat(result.getActive()).isEqualTo(createRequest.getActive());
            assertThat(result.getTargetDevice()).isEqualTo(createRequest.getTargetDevice());
            assertThat(result.getLocation()).isEqualTo(createRequest.getLocation());

            verify(bannerRepository).save(any(Banner.class));
        }

        @Test
        @DisplayName("Should create banner with default values when optional fields are null")
        void shouldCreateBannerWithDefaults() {
            // Given
            BannerCreateRequest requestWithNulls = BannerCreateRequest.builder()
                    .title("Test Banner")
                    .imageUrl("http://example.com/image.jpg")
                    .location("homepage")
                    .active(null) // Should default to true
                    .targetDevice(null) // Should default to ALL
                    .build();

            Banner savedBanner = Banner.builder()
                    .id(1L)
                    .title("Test Banner")
                    .imageUrl("http://example.com/image.jpg")
                    .location("homepage")
                    .active(true)
                    .targetDevice(TargetDevice.ALL)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            when(bannerRepository.save(any(Banner.class))).thenReturn(savedBanner);

            // When
            BannerDTO result = bannerService.createBanner(requestWithNulls);

            // Then
            assertThat(result.getActive()).isTrue();
            assertThat(result.getTargetDevice()).isEqualTo(TargetDevice.ALL);
        }

        @Test
        @DisplayName("Should handle repository exception during creation")
        void shouldHandleRepositoryException() {
            // Given
            when(bannerRepository.save(any(Banner.class)))
                    .thenThrow(new RuntimeException("Database error"));

            // When & Then
            assertThatThrownBy(() -> bannerService.createBanner(createRequest))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Database error");
        }
    }

    @Nested
    @DisplayName("Update Banner Tests")
    class UpdateBannerTests {

        @Test
        @DisplayName("Should update banner successfully")
        void shouldUpdateBannerSuccessfully() {
            // Given
            Long bannerId = 1L;
            when(bannerRepository.findById(bannerId)).thenReturn(Optional.of(testBanner));
            when(bannerRepository.save(any(Banner.class))).thenReturn(testBanner);

            // When
            BannerDTO result = bannerService.updateBanner(bannerId, createRequest);

            // Then
            assertThat(result).isNotNull();
            verify(bannerRepository).findById(bannerId);
            verify(bannerRepository).save(testBanner);
        }

        @Test
        @DisplayName("Should throw exception when banner not found for update")
        void shouldThrowExceptionWhenBannerNotFound() {
            // Given
            Long bannerId = 999L;
            when(bannerRepository.findById(bannerId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> bannerService.updateBanner(bannerId, createRequest))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Banner not found with ID: " + bannerId);

            verify(bannerRepository).findById(bannerId);
            verify(bannerRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should update all fields from request")
        void shouldUpdateAllFields() {
            // Given
            Long bannerId = 1L;
            Banner existingBanner = createTestBanner();
            
            BannerCreateRequest updateRequest = BannerCreateRequest.builder()
                    .title("Updated Title")
                    .imageUrl("http://example.com/updated.jpg")
                    .linkUrl("http://example.com/updated")
                    .altText("Updated Alt Text")
                    .displayOrder(99)
                    .active(false)
                    .startDate(LocalDateTime.now().plusDays(1))
                    .endDate(LocalDateTime.now().plusDays(30))
                    .targetDevice(TargetDevice.MOBILE)
                    .location("footer")
                    .build();

            when(bannerRepository.findById(bannerId)).thenReturn(Optional.of(existingBanner));
            when(bannerRepository.save(any(Banner.class))).thenReturn(existingBanner);

            // When
            bannerService.updateBanner(bannerId, updateRequest);

            // Then
            verify(bannerRepository).save(argThat(banner -> 
                banner.getTitle().equals("Updated Title") &&
                banner.getImageUrl().equals("http://example.com/updated.jpg") &&
                banner.getLinkUrl().equals("http://example.com/updated") &&
                banner.getAltText().equals("Updated Alt Text") &&
                banner.getDisplayOrder().equals(99) &&
                banner.getActive().equals(false) &&
                banner.getTargetDevice().equals(TargetDevice.MOBILE) &&
                banner.getLocation().equals("footer")
            ));
        }
    }

    @Nested
    @DisplayName("Get Banner Tests")
    class GetBannerTests {

        @Test
        @DisplayName("Should get banner by ID successfully")
        void shouldGetBannerById() {
            // Given
            Long bannerId = 1L;
            when(bannerRepository.findById(bannerId)).thenReturn(Optional.of(testBanner));

            // When
            BannerDTO result = bannerService.getBannerById(bannerId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(testBanner.getId());
            assertThat(result.getTitle()).isEqualTo(testBanner.getTitle());
            verify(bannerRepository).findById(bannerId);
        }

        @Test
        @DisplayName("Should throw exception when banner not found by ID")
        void shouldThrowExceptionWhenBannerNotFoundById() {
            // Given
            Long bannerId = 999L;
            when(bannerRepository.findById(bannerId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> bannerService.getBannerById(bannerId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Banner not found with ID: " + bannerId);
        }
    }

    @Nested
    @DisplayName("Get All Banners Tests")
    class GetAllBannersTests {

        @Test
        @DisplayName("Should get all banners with default sorting")
        void shouldGetAllBannersWithDefaultSorting() {
            // Given
            BannerFilterRequest filter = new BannerFilterRequest();
            Pageable pageable = PageRequest.of(0, 10);
            List<Banner> banners = Arrays.asList(testBanner);
            Page<Banner> bannerPage = new PageImpl<>(banners, pageable, 1);

            when(bannerRepository.findAll(any(Pageable.class))).thenReturn(bannerPage);

            // When
            Page<BannerDTO> result = bannerService.getAllBanners(filter, pageable);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getId()).isEqualTo(testBanner.getId());
            
            verify(bannerRepository).findAll(argThat((Pageable p) -> 
                p.getSort().getOrderFor("displayOrder") != null &&
                p.getSort().getOrderFor("displayOrder").getDirection() == Sort.Direction.ASC
            ));
        }

        @Test
        @DisplayName("Should get banners with custom sorting")
        void shouldGetBannersWithCustomSorting() {
            // Given
            BannerFilterRequest filter = BannerFilterRequest.builder()
                    .sortBy("title")
                    .sortDirection("DESC")
                    .build();
            Pageable pageable = PageRequest.of(0, 10);
            List<Banner> banners = Arrays.asList(testBanner);
            Page<Banner> bannerPage = new PageImpl<>(banners, pageable, 1);

            when(bannerRepository.findAll(any(Pageable.class))).thenReturn(bannerPage);

            // When
            Page<BannerDTO> result = bannerService.getAllBanners(filter, pageable);

            // Then
            assertThat(result).isNotNull();
            verify(bannerRepository).findAll(argThat((Pageable p) -> 
                p.getSort().getOrderFor("title") != null &&
                p.getSort().getOrderFor("title").getDirection() == Sort.Direction.DESC
            ));
        }

        @Test
        @DisplayName("Should filter banners by title")
        void shouldFilterBannersByTitle() {
            // Given
            BannerFilterRequest filter = BannerFilterRequest.builder()
                    .title("Test")
                    .build();
            Pageable pageable = PageRequest.of(0, 10);
            List<Banner> filteredBanners = Arrays.asList(testBanner);

            when(bannerRepository.findByTitleContainingIgnoreCase("Test")).thenReturn(filteredBanners);

            // When
            Page<BannerDTO> result = bannerService.getAllBanners(filter, pageable);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            verify(bannerRepository).findByTitleContainingIgnoreCase("Test");
        }

        @Test
        @DisplayName("Should handle empty title filter")
        void shouldHandleEmptyTitleFilter() {
            // Given
            BannerFilterRequest filter = BannerFilterRequest.builder()
                    .title("   ") // Empty/whitespace title
                    .build();
            Pageable pageable = PageRequest.of(0, 10);
            List<Banner> banners = Arrays.asList(testBanner);
            Page<Banner> bannerPage = new PageImpl<>(banners, pageable, 1);

            when(bannerRepository.findAll(any(Pageable.class))).thenReturn(bannerPage);

            // When
            Page<BannerDTO> result = bannerService.getAllBanners(filter, pageable);

            // Then
            assertThat(result).isNotNull();
            verify(bannerRepository).findAll(any(Pageable.class));
            verify(bannerRepository, never()).findByTitleContainingIgnoreCase(anyString());
        }
    }

    @Nested
    @DisplayName("Get Active Banners Tests")
    class GetActiveBannersTests {

        @Test
        @DisplayName("Should get active banners for location and device")
        void shouldGetActiveBannersForLocationAndDevice() {
            // Given
            String location = "homepage";
            String device = "MOBILE";
            List<Banner> banners = Arrays.asList(testBanner);

            when(bannerRepository.findByLocationAndTargetDeviceAndActiveTrue(location, TargetDevice.MOBILE))
                    .thenReturn(banners);

            // When
            List<BannerDTO> result = bannerService.getActiveBanners(location, device);

            // Then
            assertThat(result).hasSize(1);
            verify(bannerRepository).findByLocationAndTargetDeviceAndActiveTrue(location, TargetDevice.MOBILE);
        }

        @Test
        @DisplayName("Should get active banners for location only")
        void shouldGetActiveBannersForLocationOnly() {
            // Given
            String location = "homepage";
            List<Banner> banners = Arrays.asList(testBanner);

            when(bannerRepository.findByLocationAndActiveTrue(location)).thenReturn(banners);

            // When
            List<BannerDTO> result = bannerService.getActiveBanners(location, null);

            // Then
            assertThat(result).hasSize(1);
            verify(bannerRepository).findByLocationAndActiveTrue(location);
        }

        @Test
        @DisplayName("Should get active banners for device only")
        void shouldGetActiveBannersForDeviceOnly() {
            // Given
            String device = "DESKTOP";
            List<Banner> banners = Arrays.asList(testBanner);

            when(bannerRepository.findByTargetDeviceAndActiveTrue(TargetDevice.DESKTOP)).thenReturn(banners);

            // When
            List<BannerDTO> result = bannerService.getActiveBanners(null, device);

            // Then
            assertThat(result).hasSize(1);
            verify(bannerRepository).findByTargetDeviceAndActiveTrue(TargetDevice.DESKTOP);
        }

        @Test
        @DisplayName("Should get all active banners when no filters")
        void shouldGetAllActiveBannersWhenNoFilters() {
            // Given
            List<Banner> banners = Arrays.asList(testBanner);

            when(bannerRepository.findByActiveTrueOrderByDisplayOrder()).thenReturn(banners);

            // When
            List<BannerDTO> result = bannerService.getActiveBanners(null, null);

            // Then
            assertThat(result).hasSize(1);
            verify(bannerRepository).findByActiveTrueOrderByDisplayOrder();
        }

        @Test
        @DisplayName("Should filter banners by date range")
        void shouldFilterBannersByDateRange() {
            // Given
            LocalDateTime now = LocalDateTime.now();
            Banner validBanner = Banner.builder()
                    .id(1L)
                    .title("Valid Banner")
                    .active(true)
                    .startDate(now.minusDays(1))
                    .endDate(now.plusDays(1))
                    .build();

            Banner expiredBanner = Banner.builder()
                    .id(2L)
                    .title("Expired Banner")
                    .active(true)
                    .startDate(now.minusDays(10))
                    .endDate(now.minusDays(1))
                    .build();

            List<Banner> banners = Arrays.asList(validBanner, expiredBanner);

            when(bannerRepository.findByActiveTrueOrderByDisplayOrder()).thenReturn(banners);

            // When
            List<BannerDTO> result = bannerService.getActiveBanners(null, null);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getTitle()).isEqualTo("Valid Banner");
        }

        @Test
        @DisplayName("Should handle invalid device enum")
        void shouldHandleInvalidDeviceEnum() {
            // Given
            String invalidDevice = "INVALID_DEVICE";

            // When & Then
            assertThatThrownBy(() -> bannerService.getActiveBanners(null, invalidDevice))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("Delete Banner Tests")
    class DeleteBannerTests {

        @Test
        @DisplayName("Should delete banner successfully")
        void shouldDeleteBannerSuccessfully() {
            // Given
            Long bannerId = 1L;
            when(bannerRepository.findById(bannerId)).thenReturn(Optional.of(testBanner));

            // When
            bannerService.deleteBanner(bannerId);

            // Then
            verify(bannerRepository).findById(bannerId);
            verify(bannerRepository).delete(testBanner);
        }

        @Test
        @DisplayName("Should delete banner and associated image")
        void shouldDeleteBannerAndImage() {
            // Given
            Long bannerId = 1L;
            Banner bannerWithImage = Banner.builder()
                    .id(bannerId)
                    .title("Test Banner")
                    .imageUrl("http://example.com/image.jpg")
                    .build();

            when(bannerRepository.findById(bannerId)).thenReturn(Optional.of(bannerWithImage));

            // When
            bannerService.deleteBanner(bannerId);

            // Then
            verify(fileUploadService).deleteFile("http://example.com/image.jpg");
            verify(bannerRepository).delete(bannerWithImage);
        }

        @Test
        @DisplayName("Should handle image deletion failure gracefully")
        void shouldHandleImageDeletionFailure() {
            // Given
            Long bannerId = 1L;
            Banner bannerWithImage = Banner.builder()
                    .id(bannerId)
                    .title("Test Banner")
                    .imageUrl("http://example.com/image.jpg")
                    .build();

            when(bannerRepository.findById(bannerId)).thenReturn(Optional.of(bannerWithImage));
            doThrow(new RuntimeException("File deletion failed")).when(fileUploadService)
                    .deleteFile("http://example.com/image.jpg");

            // When
            bannerService.deleteBanner(bannerId);

            // Then
            verify(fileUploadService).deleteFile("http://example.com/image.jpg");
            verify(bannerRepository).delete(bannerWithImage); // Should still delete banner
        }

        @Test
        @DisplayName("Should throw exception when banner not found for deletion")
        void shouldThrowExceptionWhenBannerNotFoundForDeletion() {
            // Given
            Long bannerId = 999L;
            when(bannerRepository.findById(bannerId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> bannerService.deleteBanner(bannerId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Banner not found with ID: " + bannerId);

            verify(bannerRepository, never()).delete(any());
        }

        @Test
        @DisplayName("Should skip image deletion when imageUrl is null or empty")
        void shouldSkipImageDeletionWhenImageUrlIsNullOrEmpty() {
            // Given
            Long bannerId = 1L;
            Banner bannerWithoutImage = Banner.builder()
                    .id(bannerId)
                    .title("Test Banner")
                    .imageUrl(null)
                    .build();

            when(bannerRepository.findById(bannerId)).thenReturn(Optional.of(bannerWithoutImage));

            // When
            bannerService.deleteBanner(bannerId);

            // Then
            verify(fileUploadService, never()).deleteFile(any());
            verify(bannerRepository).delete(bannerWithoutImage);
        }
    }

    @Nested
    @DisplayName("Activate/Deactivate Banner Tests")
    class ActivateDeactivateBannerTests {

        @Test
        @DisplayName("Should activate banner successfully")
        void shouldActivateBannerSuccessfully() {
            // Given
            Long bannerId = 1L;
            Banner inactiveBanner = Banner.builder()
                    .id(bannerId)
                    .title("Test Banner")
                    .active(false)
                    .build();

            when(bannerRepository.findById(bannerId)).thenReturn(Optional.of(inactiveBanner));
            when(bannerRepository.save(any(Banner.class))).thenReturn(inactiveBanner);

            // When
            bannerService.activateBanner(bannerId);

            // Then
            verify(bannerRepository).save(argThat(banner -> 
                banner.getActive().equals(true)
            ));
        }

        @Test
        @DisplayName("Should deactivate banner successfully")
        void shouldDeactivateBannerSuccessfully() {
            // Given
            Long bannerId = 1L;
            Banner activeBanner = Banner.builder()
                    .id(bannerId)
                    .title("Test Banner")
                    .active(true)
                    .build();

            when(bannerRepository.findById(bannerId)).thenReturn(Optional.of(activeBanner));
            when(bannerRepository.save(any(Banner.class))).thenReturn(activeBanner);

            // When
            bannerService.deactivateBanner(bannerId);

            // Then
            verify(bannerRepository).save(argThat(banner -> 
                banner.getActive().equals(false)
            ));
        }

        @Test
        @DisplayName("Should throw exception when banner not found for activation")
        void shouldThrowExceptionWhenBannerNotFoundForActivation() {
            // Given
            Long bannerId = 999L;
            when(bannerRepository.findById(bannerId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> bannerService.activateBanner(bannerId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Banner not found with ID: " + bannerId);
        }

        @Test
        @DisplayName("Should throw exception when banner not found for deactivation")
        void shouldThrowExceptionWhenBannerNotFoundForDeactivation() {
            // Given
            Long bannerId = 999L;
            when(bannerRepository.findById(bannerId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> bannerService.deactivateBanner(bannerId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Banner not found with ID: " + bannerId);
        }
    }

    @Nested
    @DisplayName("Upload Banner Image Tests")
    class UploadBannerImageTests {

        @Test
        @DisplayName("Should upload banner image successfully")
        void shouldUploadBannerImageSuccessfully() {
            // Given
            MultipartFile file = new MockMultipartFile(
                    "file", 
                    "test-image.jpg", 
                    "image/jpeg", 
                    "test image content".getBytes()
            );
            String expectedUrl = "http://example.com/uploads/banners/test-image.jpg";

            when(fileUploadService.uploadFile(file, "banners")).thenReturn(expectedUrl);

            // When
            String result = bannerService.uploadBannerImage(file);

            // Then
            assertThat(result).isEqualTo(expectedUrl);
            verify(fileUploadService).uploadFile(file, "banners");
        }

        @Test
        @DisplayName("Should handle upload failure")
        void shouldHandleUploadFailure() {
            // Given
            MultipartFile file = new MockMultipartFile(
                    "file", 
                    "test-image.jpg", 
                    "image/jpeg", 
                    "test image content".getBytes()
            );

            when(fileUploadService.uploadFile(file, "banners"))
                    .thenThrow(new RuntimeException("Upload failed"));

            // When & Then
            assertThatThrownBy(() -> bannerService.uploadBannerImage(file))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Upload failed");
        }
    }

    @Nested
    @DisplayName("Reorder Banners Tests")
    class ReorderBannersTests {

        @Test
        @DisplayName("Should reorder banners successfully")
        void shouldReorderBannersSuccessfully() {
            // Given
            List<BannerOrderRequest> orderRequests = Arrays.asList(
                    BannerOrderRequest.builder().bannerId(1L).displayOrder(1).build(),
                    BannerOrderRequest.builder().bannerId(2L).displayOrder(2).build()
            );

            Banner banner1 = Banner.builder().id(1L).title("Banner 1").displayOrder(10).build();
            Banner banner2 = Banner.builder().id(2L).title("Banner 2").displayOrder(20).build();

            when(bannerRepository.findById(1L)).thenReturn(Optional.of(banner1));
            when(bannerRepository.findById(2L)).thenReturn(Optional.of(banner2));
            when(bannerRepository.save(any(Banner.class))).thenReturn(banner1, banner2);

            // When
            bannerService.reorderBanners(orderRequests);

            // Then
            verify(bannerRepository).save(argThat(banner -> 
                banner.getId().equals(1L) && banner.getDisplayOrder().equals(1)
            ));
            verify(bannerRepository).save(argThat(banner -> 
                banner.getId().equals(2L) && banner.getDisplayOrder().equals(2)
            ));
        }

        @Test
        @DisplayName("Should throw exception when banner not found during reorder")
        void shouldThrowExceptionWhenBannerNotFoundDuringReorder() {
            // Given
            List<BannerOrderRequest> orderRequests = Arrays.asList(
                    BannerOrderRequest.builder().bannerId(999L).displayOrder(1).build()
            );

            when(bannerRepository.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> bannerService.reorderBanners(orderRequests))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Banner not found with ID: 999");
        }

        @Test
        @DisplayName("Should handle empty reorder list")
        void shouldHandleEmptyReorderList() {
            // Given
            List<BannerOrderRequest> emptyList = Arrays.asList();

            // When
            bannerService.reorderBanners(emptyList);

            // Then
            verify(bannerRepository, never()).findById(any());
            verify(bannerRepository, never()).save(any());
        }
    }

    // Helper methods for creating test data
    private Banner createTestBanner() {
        return Banner.builder()
                .id(1L)
                .title("Test Banner")
                .imageUrl("http://example.com/image.jpg")
                .linkUrl("http://example.com/link")
                .altText("Test Alt Text")
                .displayOrder(1)
                .active(true)
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().plusDays(30))
                .targetDevice(TargetDevice.ALL)
                .location("homepage")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private BannerCreateRequest createTestBannerRequest() {
        return BannerCreateRequest.builder()
                .title("Test Banner")
                .imageUrl("http://example.com/image.jpg")
                .linkUrl("http://example.com/link")
                .altText("Test Alt Text")
                .displayOrder(1)
                .active(true)
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().plusDays(30))
                .targetDevice(TargetDevice.ALL)
                .location("homepage")
                .build();
    }

    private BannerDTO createTestBannerDTO() {
        return BannerDTO.builder()
                .id(1L)
                .title("Test Banner")
                .imageUrl("http://example.com/image.jpg")
                .linkUrl("http://example.com/link")
                .altText("Test Alt Text")
                .displayOrder(1)
                .active(true)
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().plusDays(30))
                .targetDevice(TargetDevice.ALL)
                .location("homepage")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}