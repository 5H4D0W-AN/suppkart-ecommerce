package com.suppkart.service;

import com.suppkart.config.FileUploadConfig;
import com.suppkart.exception.BusinessException;
import com.suppkart.integration.storage.FileStorageProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class FileUploadService {

    private static final Logger logger = LoggerFactory.getLogger(FileUploadService.class);

    private final FileUploadConfig fileUploadConfig;
    private final FileStorageProvider storageProvider;

    public FileUploadService(FileUploadConfig fileUploadConfig, FileStorageProvider storageProvider) {
        this.fileUploadConfig = fileUploadConfig;
        this.storageProvider = storageProvider;
        logger.info("FileUploadService initialized with storage provider: {}", 
                   storageProvider.getProviderType().getDisplayName());
    }

    /**
     * Upload a single file using the configured storage provider
     */
    public String uploadFile(MultipartFile file, String subDirectory) {
        validateFile(file);

        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String fileExtension = getFileExtension(originalFilename);
        
        // If no extension from filename, derive from content type
        if (fileExtension.isEmpty()) {
            fileExtension = getExtensionFromContentType(file.getContentType());
        }
        
        String uniqueFilename = generateUniqueFilename(fileExtension);

        // Use the configured storage provider to upload
        String fileUrl = storageProvider.uploadFile(file, subDirectory, uniqueFilename);
        
        logger.info("File uploaded successfully using {}: {}", 
                   storageProvider.getProviderType().getDisplayName(), fileUrl);
        return fileUrl;
    }

    /**
     * Upload multiple files
     */
    public List<String> uploadFiles(List<MultipartFile> files, String subDirectory) {
        if (files == null || files.isEmpty()) {
            return List.of();
        }
        return files.stream()
                .map(file -> uploadFile(file, subDirectory))
                .toList();
    }

    /**
     * Delete a file by URL using the configured storage provider
     */
    public void deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.trim().isEmpty()) {
            return;
        }
        
        boolean deleted = storageProvider.deleteFile(fileUrl);
        if (deleted) {
            logger.info("File deleted successfully using {}: {}", 
                       storageProvider.getProviderType().getDisplayName(), fileUrl);
        } else {
            logger.warn("Failed to delete file using {}: {}", 
                       storageProvider.getProviderType().getDisplayName(), fileUrl);
        }
    }

    /**
     * Validate uploaded file
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("EMPTY_FILE", "File is empty");
        }

        if (file.getSize() > fileUploadConfig.getMaxFileSize()) {
            throw new BusinessException("FILE_TOO_LARGE",
                    "File size exceeds maximum allowed size of " + fileUploadConfig.getMaxFileSize() + " bytes");
        }

        String contentType = file.getContentType();
        if (contentType == null) {
            throw new BusinessException("INVALID_FILE_TYPE", "File type cannot be determined");
        }

        boolean isValidType = Arrays.asList(fileUploadConfig.getAllowedImageTypes()).contains(contentType)
                || Arrays.asList(fileUploadConfig.getAllowedVideoTypes()).contains(contentType);

        if (!isValidType) {
            throw new BusinessException("INVALID_FILE_TYPE",
                    "File type " + contentType + " is not allowed");
        }
    }

    /**
     * Get file extension from filename or content type
     */
    private String getFileExtension(String filename) {
        if (filename != null && filename.contains(".")) {
            return filename.substring(filename.lastIndexOf("."));
        }
        return "";
    }

    /**
     * Generate unique filename with timestamp and UUID
     */
    private String generateUniqueFilename(String extension) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        return timestamp + "_" + uuid + extension;
    }

    /**
     * Get media type from content type
     */
    public String getMediaType(String contentType) {
        if (contentType == null || contentType.trim().isEmpty()) {
            return "IMAGE";
        }

        String lowerContentType = contentType.toLowerCase();
        if (lowerContentType.startsWith("video/")) {
            return "VIDEO";
        } else if (lowerContentType.startsWith("image/")) {
            return "IMAGE";
        }

        return "IMAGE"; // Default
    }
    
    /**
     * Get file extension from content type
     */
    private String getExtensionFromContentType(String contentType) {
        if (contentType == null) {
            return "";
        }
        
        return switch (contentType.toLowerCase()) {
            case "image/jpeg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/gif" -> ".gif";
            case "image/webp" -> ".webp";
            case "video/mp4" -> ".mp4";
            case "video/avi" -> ".avi";
            case "video/mov" -> ".mov";
            default -> "";
        };
    }
}
