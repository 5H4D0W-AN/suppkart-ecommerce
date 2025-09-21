package com.suppkart.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class StorageService {

    private static final Logger logger = LoggerFactory.getLogger(StorageService.class);

    @Value("${app.file.upload-dir:uploads}")
    private String uploadDir;

    @Value("${app.file.max-size:10485760}") // 10MB default
    private long maxFileSize;

    @Value("${app.file.allowed-types:jpg,jpeg,png,gif,webp,pdf,doc,docx}")
    private String allowedFileTypes;

    private static final List<String> IMAGE_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "gif", "webp");
    private static final List<String> DOCUMENT_EXTENSIONS = Arrays.asList("pdf", "doc", "docx", "txt");

    /**
     * Upload a file to the storage directory
     * 
     * @param file The file to upload
     * @param directory The subdirectory to store the file in
     * @return The URL/path of the uploaded file
     * @throws IOException If file upload fails
     */
    public String uploadFile(MultipartFile file, String directory) throws IOException {
        validateFile(file);

        // Create directory if it doesn't exist
        Path uploadPath = Paths.get(uploadDir, directory);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String fileExtension = getFileExtension(originalFilename);
        String uniqueFilename = generateUniqueFilename(originalFilename, fileExtension);

        // Save file
        Path filePath = uploadPath.resolve(uniqueFilename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // Return relative path/URL
        return "/" + directory + "/" + uniqueFilename;
    }

    /**
     * Upload an image file with additional validation
     * 
     * @param file The image file to upload
     * @param directory The subdirectory to store the file in
     * @return The URL/path of the uploaded image
     * @throws IOException If file upload fails
     */
    public String uploadImage(MultipartFile file, String directory) throws IOException {
        validateImageFile(file);
        return uploadFile(file, directory);
    }

    /**
     * Upload a document file with additional validation
     * 
     * @param file The document file to upload
     * @param directory The subdirectory to store the file in
     * @return The URL/path of the uploaded document
     * @throws IOException If file upload fails
     */
    public String uploadDocument(MultipartFile file, String directory) throws IOException {
        validateDocumentFile(file);
        return uploadFile(file, directory);
    }

    /**
     * Delete a file from storage
     * 
     * @param fileUrl The URL/path of the file to delete
     * @return true if file was deleted successfully, false otherwise
     */
    public boolean deleteFile(String fileUrl) {
        try {
            if (fileUrl == null || fileUrl.isEmpty()) {
                return false;
            }

            // Remove leading slash if present
            String relativePath = fileUrl.startsWith("/") ? fileUrl.substring(1) : fileUrl;
            Path filePath = Paths.get(uploadDir, relativePath);

            if (Files.exists(filePath)) {
                Files.delete(filePath);
                logger.info("File deleted successfully: {}", fileUrl);
                return true;
            } else {
                logger.warn("File not found for deletion: {}", fileUrl);
                return false;
            }
        } catch (IOException e) {
            logger.error("Error deleting file: {}", fileUrl, e);
            return false;
        }
    }

    /**
     * Get the full file URL for a given file key
     * 
     * @param fileKey The file key/path
     * @return The full URL to access the file
     */
    public String getFileUrl(String fileKey) {
        if (fileKey == null || fileKey.isEmpty()) {
            return null;
        }

        // For local storage, return the relative path
        // In production with S3, this would return the full S3 URL
        return fileKey.startsWith("/") ? fileKey : "/" + fileKey;
    }

    /**
     * Check if a file exists in storage
     * 
     * @param fileUrl The URL/path of the file to check
     * @return true if file exists, false otherwise
     */
    public boolean fileExists(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return false;
        }

        String relativePath = fileUrl.startsWith("/") ? fileUrl.substring(1) : fileUrl;
        Path filePath = Paths.get(uploadDir, relativePath);
        return Files.exists(filePath);
    }

    /**
     * Get file size in bytes
     * 
     * @param fileUrl The URL/path of the file
     * @return File size in bytes, or -1 if file doesn't exist
     */
    public long getFileSize(String fileUrl) {
        try {
            if (fileUrl == null || fileUrl.isEmpty()) {
                return -1;
            }

            String relativePath = fileUrl.startsWith("/") ? fileUrl.substring(1) : fileUrl;
            Path filePath = Paths.get(uploadDir, relativePath);

            if (Files.exists(filePath)) {
                return Files.size(filePath);
            }
        } catch (IOException e) {
            logger.error("Error getting file size: {}", fileUrl, e);
        }
        return -1;
    }

    /**
     * Validate uploaded file
     * 
     * @param file The file to validate
     * @throws IllegalArgumentException If file is invalid
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }

        if (file.getSize() > maxFileSize) {
            throw new IllegalArgumentException("File size exceeds maximum allowed size of " + maxFileSize + " bytes");
        }

        String filename = file.getOriginalFilename();
        if (filename == null || filename.trim().isEmpty()) {
            throw new IllegalArgumentException("File must have a valid filename");
        }

        String fileExtension = getFileExtension(filename).toLowerCase();
        List<String> allowedTypes = Arrays.asList(allowedFileTypes.toLowerCase().split(","));
        
        if (!allowedTypes.contains(fileExtension)) {
            throw new IllegalArgumentException("File type not allowed. Allowed types: " + allowedFileTypes);
        }
    }

    /**
     * Validate uploaded image file
     * 
     * @param file The image file to validate
     * @throws IllegalArgumentException If file is invalid
     */
    private void validateImageFile(MultipartFile file) {
        validateFile(file);

        String fileExtension = getFileExtension(file.getOriginalFilename()).toLowerCase();
        if (!IMAGE_EXTENSIONS.contains(fileExtension)) {
            throw new IllegalArgumentException("File must be an image. Allowed types: " + String.join(", ", IMAGE_EXTENSIONS));
        }

        // Additional image-specific validations can be added here
        // e.g., image dimensions, aspect ratio, etc.
    }

    /**
     * Validate uploaded document file
     * 
     * @param file The document file to validate
     * @throws IllegalArgumentException If file is invalid
     */
    private void validateDocumentFile(MultipartFile file) {
        validateFile(file);

        String fileExtension = getFileExtension(file.getOriginalFilename()).toLowerCase();
        if (!DOCUMENT_EXTENSIONS.contains(fileExtension)) {
            throw new IllegalArgumentException("File must be a document. Allowed types: " + String.join(", ", DOCUMENT_EXTENSIONS));
        }
    }

    /**
     * Get file extension from filename
     * 
     * @param filename The filename
     * @return The file extension (without dot)
     */
    private String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "";
        }

        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == filename.length() - 1) {
            return "";
        }

        return filename.substring(lastDotIndex + 1);
    }

    /**
     * Generate a unique filename
     * 
     * @param originalFilename The original filename
     * @param fileExtension The file extension
     * @return A unique filename
     */
    private String generateUniqueFilename(String originalFilename, String fileExtension) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        
        // Clean original filename (remove extension and special characters)
        String cleanName = originalFilename;
        if (cleanName.contains(".")) {
            cleanName = cleanName.substring(0, cleanName.lastIndexOf('.'));
        }
        cleanName = cleanName.replaceAll("[^a-zA-Z0-9]", "_").toLowerCase();
        
        // Limit length
        if (cleanName.length() > 20) {
            cleanName = cleanName.substring(0, 20);
        }

        return cleanName + "_" + timestamp + "_" + uuid + "." + fileExtension;
    }

    /**
     * Get content type based on file extension
     * 
     * @param filename The filename
     * @return The content type
     */
    public String getContentType(String filename) {
        String extension = getFileExtension(filename).toLowerCase();
        
        switch (extension) {
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            case "png":
                return "image/png";
            case "gif":
                return "image/gif";
            case "webp":
                return "image/webp";
            case "pdf":
                return "application/pdf";
            case "doc":
                return "application/msword";
            case "docx":
                return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "txt":
                return "text/plain";
            default:
                return "application/octet-stream";
        }
    }

    /**
     * Check if file is an image
     * 
     * @param filename The filename
     * @return true if file is an image, false otherwise
     */
    public boolean isImage(String filename) {
        String extension = getFileExtension(filename).toLowerCase();
        return IMAGE_EXTENSIONS.contains(extension);
    }

    /**
     * Check if file is a document
     * 
     * @param filename The filename
     * @return true if file is a document, false otherwise
     */
    public boolean isDocument(String filename) {
        String extension = getFileExtension(filename).toLowerCase();
        return DOCUMENT_EXTENSIONS.contains(extension);
    }
}
