package com.suppkart.integration.storage;

import com.suppkart.config.FileUploadConfig;
import com.suppkart.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * Local file system storage provider
 */
@Component
@ConditionalOnProperty(name = "app.file-upload.storage.provider", havingValue = "local", matchIfMissing = true)
public class LocalFileStorageProvider implements FileStorageProvider {
    
    private static final Logger logger = LoggerFactory.getLogger(LocalFileStorageProvider.class);
    
    private final FileUploadConfig config;
    
    public LocalFileStorageProvider(FileUploadConfig config) {
        this.config = config;
    }
    
    @Override
    public String uploadFile(MultipartFile file, String directory, String filename) {
        try {
            // Create upload directory if it doesn't exist
            String uploadPath = config.getUploadDir() + "/" + directory;
            Path uploadDir = Paths.get(uploadPath);
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }
            
            // Save file
            Path filePath = uploadDir.resolve(filename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            
            // Return URL
            String fileUrl = config.getBaseUrl() + "/uploads/" + directory + "/" + filename;
            logger.info("File uploaded to local storage: {}", fileUrl);
            return fileUrl;
            
        } catch (IOException e) {
            logger.error("Failed to upload file to local storage: {}", e.getMessage());
            throw new BusinessException("FILE_UPLOAD_ERROR", "Failed to upload file: " + e.getMessage());
        }
    }
    
    @Override
    public boolean deleteFile(String fileUrl) {
        try {
            if (fileUrl == null || fileUrl.trim().isEmpty()) {
                return false;
            }
            
            // Extract relative path from URL
            String baseUrl = config.getBaseUrl();
            if (fileUrl.startsWith(baseUrl)) {
                String relativePath = fileUrl.substring(baseUrl.length());
                if (relativePath.startsWith("/uploads/")) {
                    relativePath = relativePath.substring("/uploads/".length());
                }
                
                Path filePath = Paths.get(config.getUploadDir(), relativePath);
                if (Files.exists(filePath)) {
                    Files.delete(filePath);
                    logger.info("File deleted from local storage: {}", fileUrl);
                    return true;
                } else {
                    logger.warn("File not found for deletion: {}", fileUrl);
                    return false;
                }
            }
            return false;
        } catch (IOException e) {
            logger.error("Failed to delete file from local storage: {}", e.getMessage());
            return false;
        }
    }
    
    @Override
    public boolean fileExists(String fileUrl) {
        try {
            if (fileUrl == null || fileUrl.trim().isEmpty()) {
                return false;
            }
            
            String baseUrl = config.getBaseUrl();
            if (fileUrl.startsWith(baseUrl)) {
                String relativePath = fileUrl.substring(baseUrl.length());
                if (relativePath.startsWith("/uploads/")) {
                    relativePath = relativePath.substring("/uploads/".length());
                }
                
                Path filePath = Paths.get(config.getUploadDir(), relativePath);
                return Files.exists(filePath);
            }
            return false;
        } catch (Exception e) {
            logger.error("Error checking file existence: {}", e.getMessage());
            return false;
        }
    }
    
    @Override
    public StorageProviderType getProviderType() {
        return StorageProviderType.LOCAL;
    }
}