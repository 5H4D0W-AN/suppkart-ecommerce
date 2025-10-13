package com.suppkart.service;

import com.suppkart.config.FileUploadConfig;
import com.suppkart.exception.BusinessException;
import com.suppkart.integration.storage.FileStorageProvider;
import com.suppkart.integration.storage.StorageProviderType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class FileUploadServiceTest {

    @Mock
    private FileUploadConfig fileUploadConfig;

    @Mock
    private FileStorageProvider storageProvider;

    private FileUploadService fileUploadService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() throws IOException {
        // Mock file upload config - using lenient() to avoid unnecessary stubbing errors
        lenient().when(fileUploadConfig.getUploadDir()).thenReturn(tempDir.toString());
        lenient().when(fileUploadConfig.getBaseUrl()).thenReturn("http://localhost:8080");
        lenient().when(fileUploadConfig.getMaxFileSize()).thenReturn(5242880L); // 5MB
        lenient().when(fileUploadConfig.getAllowedImageTypes()).thenReturn(new String[]{"image/jpeg", "image/png", "image/gif", "image/webp"});
        lenient().when(fileUploadConfig.getAllowedVideoTypes()).thenReturn(new String[]{"video/mp4", "video/avi", "video/mov"});
        
        // Mock storage provider - using lenient() to avoid unnecessary stubbing errors
        lenient().when(storageProvider.getProviderType()).thenReturn(StorageProviderType.LOCAL);
        // Return dynamic URLs based on the parameters passed to uploadFile
        lenient().when(storageProvider.uploadFile(any(), any(), any())).thenAnswer(invocation -> {
            String subDirectory = invocation.getArgument(1);
            String filename = invocation.getArgument(2);
            return "http://localhost:8080/uploads/" + (subDirectory != null ? subDirectory + "/" : "") + filename;
        });
        lenient().when(storageProvider.deleteFile(any())).thenReturn(true);
        lenient().when(storageProvider.fileExists(any())).thenReturn(true);
        
        // Create the service manually after mocks are set up
        fileUploadService = new FileUploadService(fileUploadConfig, storageProvider);
    }

    // ========== SINGLE FILE UPLOAD TESTS ==========
    
    @Test
    void uploadFile_Success_JPEG() throws IOException {
        // Given
        MultipartFile file = mock(MultipartFile.class);
        lenient().when(file.isEmpty()).thenReturn(false);
        lenient().when(file.getSize()).thenReturn(1024L);
        lenient().when(file.getContentType()).thenReturn("image/jpeg");
        lenient().when(file.getOriginalFilename()).thenReturn("test.jpg");
        lenient().when(file.getInputStream()).thenReturn(new ByteArrayInputStream("test".getBytes()));

        // When
        String result = fileUploadService.uploadFile(file, "test");

        // Then
        assertNotNull(result);
        assertTrue(result.startsWith("http://localhost:8080/uploads/test/"));
        assertTrue(result.endsWith(".jpg"));
        
        // Verify storage provider was called with correct parameters
        verify(storageProvider).uploadFile(eq(file), eq("test"), any(String.class));
    }
    
    @Test
    void uploadFile_EmptyFile_ThrowsException() {
        // Given
        MultipartFile file = mock(MultipartFile.class);
        lenient().when(file.isEmpty()).thenReturn(true);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
            () -> fileUploadService.uploadFile(file, "test"));
        assertEquals("EMPTY_FILE", exception.getErrorCode());
        assertEquals("File is empty", exception.getMessage());
    }
    
    @Test
    void uploadFile_NullFile_ThrowsException() {
        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
            () -> fileUploadService.uploadFile(null, "test"));
        assertEquals("EMPTY_FILE", exception.getErrorCode());
    }

    @Test
    void uploadFile_FileTooLarge_ThrowsException() {
        // Given
        MultipartFile file = mock(MultipartFile.class);
        lenient().when(file.isEmpty()).thenReturn(false);
        lenient().when(file.getSize()).thenReturn(10485760L); // 10MB - exceeds 5MB limit

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
            () -> fileUploadService.uploadFile(file, "test"));
        assertEquals("FILE_TOO_LARGE", exception.getErrorCode());
        assertEquals("File size exceeds maximum allowed size of 5242880 bytes", exception.getMessage());
    }

    @Test
    void uploadFile_InvalidFileType_ThrowsException() {
        // Given
        MultipartFile file = mock(MultipartFile.class);
        lenient().when(file.isEmpty()).thenReturn(false);
        lenient().when(file.getSize()).thenReturn(1024L);
        lenient().when(file.getContentType()).thenReturn("application/pdf");

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
            () -> fileUploadService.uploadFile(file, "test"));
        assertEquals("INVALID_FILE_TYPE", exception.getErrorCode());
        assertEquals("File type application/pdf is not allowed", exception.getMessage());
    }

    @Test
    void uploadFiles_EmptyList_ReturnsEmptyList() {
        // Given
        List<MultipartFile> files = Arrays.asList();

        // When
        List<String> results = fileUploadService.uploadFiles(files, "test");

        // Then
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    void getMediaType_JPEG_ReturnsImage() {
        assertEquals("IMAGE", fileUploadService.getMediaType("image/jpeg"));
    }

    @Test
    void getMediaType_MP4_ReturnsVideo() {
        assertEquals("VIDEO", fileUploadService.getMediaType("video/mp4"));
    }

    @Test
    void getMediaType_Unknown_ReturnsImage() {
        // Default fallback for unknown types
        assertEquals("IMAGE", fileUploadService.getMediaType("application/pdf"));
    }
}