package com.suppkart.controller;

import com.suppkart.dto.content.*;
import com.suppkart.dto.response.ApiResponse;
import com.suppkart.exception.ResourceNotFoundException;
import com.suppkart.model.enums.TargetDevice;
import com.suppkart.service.BannerService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminBannerController Unit Tests")
class AdminBannerControllerTest {

    @Mock
    private BannerService bannerService;

    @InjectMocks
    private AdminBannerController adminBannerController;

    private BannerCreateRequest createRequest;
    private BannerDTO bannerDTO;
    private List<BannerOrderRequest> orderRequests;

    @BeforeEach
    void setUp() {
        createRequest = createTestBannerRequest();
        bannerDTO = createTestBannerDTO();
        orderRequests = createTestOrderRequests();
    }

    @Nested
    @DisplayName("Create Banner Tests")
    class CreateBannerTests {

        @Test
        @DisplayName("Should create banner successfully")
        void shouldCreateBannerSuccessfully() {
            // Given
            when(bannerService.createBanner(any(BannerCreateRequest.class))).thenReturn(bannerDTO);

            // When
            ResponseEntity<ApiResponse<BannerDTO>> response = adminBannerController.createBanner(createRequest);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().isSuccess()).isTrue();
            assertThat(response.getBody().getMessage()).isEqualTo("Banner created successfully");
            assertThat(response.getBody().getData()).isEqualTo(bannerDTO);

            verify(bannerService).createBanner(createRequest);
        }

        @Test
        @DisplayName("Should handle service exception during creation")
        void shouldHandleServiceExceptionDuringCreation() {
            // Given
            when(bannerService.createBanner(any(BannerCreateRequest.class)))
                    .thenThrow(new RuntimeException("Database error"));

            // When & Then
            assertThatThrownBy(() -> adminBannerController.createBanner(createRequest))
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
            when(bannerService.updateBanner(eq(bannerId), any(BannerCreateRequest.class)))
                    .thenReturn(bannerDTO);

            // When
            ResponseEntity<ApiResponse<BannerDTO>> response = adminBannerController.updateBanner(bannerId, createRequest);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().isSuccess()).isTrue();
            assertThat(response.getBody().getMessage()).isEqualTo("Banner updated successfully");
            assertThat(response.getBody().getData()).isEqualTo(bannerDTO);

            verify(bannerService).updateBanner(bannerId, createRequest);
        }

        @Test
        @DisplayName("Should handle banner not found during update")
        void shouldHandleBannerNotFoundDuringUpdate() {
            // Given
            Long bannerId = 999L;
            when(bannerService.updateBanner(eq(bannerId), any(BannerCreateRequest.class)))
                    .thenThrow(new ResourceNotFoundException("Banner not found with ID: " + bannerId));

            // When & Then
            assertThatThrownBy(() -> adminBannerController.updateBanner(bannerId, createRequest))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Banner not found with ID: " + bannerId);
        }
    }

    @Nested
    @DisplayName("Get Banner Tests")
    class GetBannerTests {

        @Test
        @DisplayName("Should get banner by ID successfully")
        void shouldGetBannerByIdSuccessfully() {
            // Given
            Long bannerId = 1L;
            when(bannerService.getBannerById(bannerId)).thenReturn(bannerDTO);

            // When
            ResponseEntity<ApiResponse<BannerDTO>> response = adminBannerController.getBannerById(bannerId);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().isSuccess()).isTrue();
            assertThat(response.getBody().getMessage()).isEqualTo("Banner retrieved successfully");
            assertThat(response.getBody().getData()).isEqualTo(bannerDTO);

            verify(bannerService).getBannerById(bannerId);
        }

        @Test
        @DisplayName("Should handle banner not found")
        void shouldHandleBannerNotFound() {
            // Given
            Long bannerId = 999L;
            when(bannerService.getBannerById(bannerId))
                    .thenThrow(new ResourceNotFoundException("Banner not found with ID: " + bannerId));

            // When & Then
            assertThatThrownBy(() -> adminBannerController.getBannerById(bannerId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Banner not found with ID: " + bannerId);
        }
    }

    @Nested
    @DisplayName("Get All Banners Tests")
    class GetAllBannersTests {

        @Test
        @DisplayName("Should get all banners with default pagination")
        void shouldGetAllBannersWithDefaultPagination() {
            // Given
            BannerFilterRequest filter = new BannerFilterRequest();
            List<BannerDTO> banners = Arrays.asList(bannerDTO);
            Page<BannerDTO> bannerPage = new PageImpl<>(banners, PageRequest.of(0, 20), 1);
            
            when(bannerService.getAllBanners(any(BannerFilterRequest.class), any()))
                    .thenReturn(bannerPage);

            // When
            ResponseEntity<ApiResponse<List<BannerDTO>>> response = 
                    adminBannerController.getAllBanners(filter, 0, 20);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().isSuccess()).isTrue();
            assertThat(response.getBody().getMessage()).isEqualTo("Banners retrieved successfully");
            assertThat(response.getBody().getData()).hasSize(1);
            assertThat(response.getBody().getData().get(0)).isEqualTo(bannerDTO);

            verify(bannerService).getAllBanners(eq(filter), any());
        }

        @Test
        @DisplayName("Should handle empty result")
        void shouldHandleEmptyResult() {
            // Given
            BannerFilterRequest filter = new BannerFilterRequest();
            Page<BannerDTO> emptyPage = new PageImpl<>(Arrays.asList(), PageRequest.of(0, 20), 0);
            
            when(bannerService.getAllBanners(any(BannerFilterRequest.class), any()))
                    .thenReturn(emptyPage);

            // When
            ResponseEntity<ApiResponse<List<BannerDTO>>> response = 
                    adminBannerController.getAllBanners(filter, 0, 20);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().isSuccess()).isTrue();
            assertThat(response.getBody().getData()).isEmpty();
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
            doNothing().when(bannerService).deleteBanner(bannerId);

            // When
            ResponseEntity<ApiResponse<Void>> response = adminBannerController.deleteBanner(bannerId);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().isSuccess()).isTrue();
            assertThat(response.getBody().getMessage()).isEqualTo("Banner deleted successfully");

            verify(bannerService).deleteBanner(bannerId);
        }

        @Test
        @DisplayName("Should handle banner not found during deletion")
        void shouldHandleBannerNotFoundDuringDeletion() {
            // Given
            Long bannerId = 999L;
            doThrow(new ResourceNotFoundException("Banner not found with ID: " + bannerId))
                    .when(bannerService).deleteBanner(bannerId);

            // When & Then
            assertThatThrownBy(() -> adminBannerController.deleteBanner(bannerId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Banner not found with ID: " + bannerId);
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
            doNothing().when(bannerService).activateBanner(bannerId);

            // When
            ResponseEntity<ApiResponse<Void>> response = adminBannerController.activateBanner(bannerId);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().isSuccess()).isTrue();
            assertThat(response.getBody().getMessage()).isEqualTo("Banner activated successfully");

            verify(bannerService).activateBanner(bannerId);
        }

        @Test
        @DisplayName("Should deactivate banner successfully")
        void shouldDeactivateBannerSuccessfully() {
            // Given
            Long bannerId = 1L;
            doNothing().when(bannerService).deactivateBanner(bannerId);

            // When
            ResponseEntity<ApiResponse<Void>> response = adminBannerController.deactivateBanner(bannerId);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().isSuccess()).isTrue();
            assertThat(response.getBody().getMessage()).isEqualTo("Banner deactivated successfully");

            verify(bannerService).deactivateBanner(bannerId);
        }
    }

    @Nested
    @DisplayName("Upload Banner Image Tests")
    class UploadBannerImageTests {

        @Test
        @DisplayName("Should upload banner image successfully")
        void shouldUploadBannerImageSuccessfully() {
            // Given
            MockMultipartFile file = new MockMultipartFile(
                    "file", 
                    "test-image.jpg", 
                    "image/jpeg", 
                    "test image content".getBytes()
            );
            String expectedUrl = "http://example.com/uploads/banners/test-image.jpg";

            when(bannerService.uploadBannerImage(any())).thenReturn(expectedUrl);

            // When
            ResponseEntity<ApiResponse<String>> response = adminBannerController.uploadBannerImage(file);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().isSuccess()).isTrue();
            assertThat(response.getBody().getMessage()).isEqualTo("Banner image uploaded successfully");
            assertThat(response.getBody().getData()).isEqualTo(expectedUrl);

            verify(bannerService).uploadBannerImage(file);
        }

        @Test
        @DisplayName("Should handle upload failure")
        void shouldHandleUploadFailure() {
            // Given
            MockMultipartFile file = new MockMultipartFile(
                    "file", 
                    "test-image.jpg", 
                    "image/jpeg", 
                    "test image content".getBytes()
            );

            when(bannerService.uploadBannerImage(any()))
                    .thenThrow(new RuntimeException("Upload failed"));

            // When & Then
            assertThatThrownBy(() -> adminBannerController.uploadBannerImage(file))
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
            doNothing().when(bannerService).reorderBanners(anyList());

            // When
            ResponseEntity<ApiResponse<Void>> response = adminBannerController.reorderBanners(orderRequests);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().isSuccess()).isTrue();
            assertThat(response.getBody().getMessage()).isEqualTo("Banners reordered successfully");

            verify(bannerService).reorderBanners(orderRequests);
        }

        @Test
        @DisplayName("Should handle reorder failure")
        void shouldHandleReorderFailure() {
            // Given
            doThrow(new ResourceNotFoundException("Banner not found"))
                    .when(bannerService).reorderBanners(anyList());

            // When & Then
            assertThatThrownBy(() -> adminBannerController.reorderBanners(orderRequests))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Banner not found");
        }
    }

    // Helper methods for creating test data
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

    private List<BannerOrderRequest> createTestOrderRequests() {
        return Arrays.asList(
                BannerOrderRequest.builder().bannerId(1L).displayOrder(1).build(),
                BannerOrderRequest.builder().bannerId(2L).displayOrder(2).build()
        );
    }
}