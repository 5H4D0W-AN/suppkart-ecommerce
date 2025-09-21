package com.suppkart.controller;

import com.suppkart.dto.response.ApiResponse;
import com.suppkart.service.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

/**
 * Image Upload Controller for Rich Text Editor
 * Handles image uploads for content management rich text editors
 */
@RestController
@RequestMapping("/api/admin/uploads")
@PreAuthorize("hasRole('ADMIN') or hasRole('CONTENT_MANAGER')")
public class ImageUploadController {

    private static final Logger log = LoggerFactory.getLogger(ImageUploadController.class);
    
    @Autowired
    private StorageService storageService;

    /**
     * Upload image for rich text editor
     */
    @PostMapping("/images")
    public ResponseEntity<ApiResponse<Map<String, String>>> uploadImage(
            @RequestParam("file") MultipartFile file) {
        log.info("Uploading image for rich text editor: {}", file.getOriginalFilename());
        
        try {
            // Validate file
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("File is empty"));
            }
            
            // Validate file type
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("File must be an image"));
            }
            
            // Validate file size (max 5MB)
            if (file.getSize() > 5 * 1024 * 1024) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("File size must be less than 5MB"));
            }
            
            // Upload file to storage
            String imageUrl = storageService.uploadFile(file, "content/images");
            
            // Return response in format expected by rich text editors
            Map<String, String> response = new HashMap<>();
            response.put("url", imageUrl);
            response.put("filename", file.getOriginalFilename());
            response.put("size", String.valueOf(file.getSize()));
            
            log.info("Image uploaded successfully: {}", imageUrl);
            return ResponseEntity.ok(ApiResponse.success("Image uploaded successfully", response));
            
        } catch (Exception e) {
            log.error("Error uploading image: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Failed to upload image: " + e.getMessage()));
        }
    }

    /**
     * Delete uploaded image
     */
    @DeleteMapping("/images")
    public ResponseEntity<ApiResponse<Void>> deleteImage(@RequestParam("url") String imageUrl) {
        log.info("Deleting image: {}", imageUrl);
        
        try {
            storageService.deleteFile(imageUrl);
            log.info("Image deleted successfully: {}", imageUrl);
            return ResponseEntity.ok(ApiResponse.success("Image deleted successfully"));
            
        } catch (Exception e) {
            log.error("Error deleting image: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Failed to delete image: " + e.getMessage()));
        }
    }

    /**
     * Upload multiple images at once
     */
    @PostMapping("/images/batch")
    public ResponseEntity<ApiResponse<Map<String, Object>>> uploadMultipleImages(
            @RequestParam("files") MultipartFile[] files) {
        log.info("Uploading {} images for rich text editor", files.length);
        
        try {
            if (files.length == 0) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("No files provided"));
            }
            
            if (files.length > 10) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Maximum 10 files allowed per batch"));
            }
            
            Map<String, Object> response = new HashMap<>();
            Map<String, String> successfulUploads = new HashMap<>();
            Map<String, String> failedUploads = new HashMap<>();
            
            for (MultipartFile file : files) {
                try {
                    // Validate each file
                    if (file.isEmpty()) {
                        failedUploads.put(file.getOriginalFilename(), "File is empty");
                        continue;
                    }
                    
                    String contentType = file.getContentType();
                    if (contentType == null || !contentType.startsWith("image/")) {
                        failedUploads.put(file.getOriginalFilename(), "File must be an image");
                        continue;
                    }
                    
                    if (file.getSize() > 5 * 1024 * 1024) {
                        failedUploads.put(file.getOriginalFilename(), "File size must be less than 5MB");
                        continue;
                    }
                    
                    // Upload file
                    String imageUrl = storageService.uploadFile(file, "content/images");
                    successfulUploads.put(file.getOriginalFilename(), imageUrl);
                    
                } catch (Exception e) {
                    failedUploads.put(file.getOriginalFilename(), e.getMessage());
                }
            }
            
            response.put("successful", successfulUploads);
            response.put("failed", failedUploads);
            response.put("totalFiles", files.length);
            response.put("successCount", successfulUploads.size());
            response.put("failureCount", failedUploads.size());
            
            log.info("Batch upload completed: {} successful, {} failed", 
                successfulUploads.size(), failedUploads.size());
            
            return ResponseEntity.ok(ApiResponse.success("Batch upload completed", response));
            
        } catch (Exception e) {
            log.error("Error in batch upload: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Failed to upload images: " + e.getMessage()));
        }
    }
}
