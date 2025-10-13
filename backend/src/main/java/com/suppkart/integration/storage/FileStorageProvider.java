package com.suppkart.integration.storage;

import org.springframework.web.multipart.MultipartFile;

/**
 * Interface for different file storage providers (Local, S3, GCS, etc.)
 */
public interface FileStorageProvider {
    
    /**
     * Upload a file to storage
     * @param file The file to upload
     * @param directory The directory/bucket path
     * @param filename The filename to use
     * @return The public URL of the uploaded file
     */
    String uploadFile(MultipartFile file, String directory, String filename);
    
    /**
     * Delete a file from storage
     * @param fileUrl The URL of the file to delete
     * @return true if deleted successfully, false otherwise
     */
    boolean deleteFile(String fileUrl);
    
    /**
     * Check if a file exists
     * @param fileUrl The URL of the file to check
     * @return true if file exists, false otherwise
     */
    boolean fileExists(String fileUrl);
    
    /**
     * Get the storage provider type
     * @return The provider type (LOCAL, S3, GCS, etc.)
     */
    StorageProviderType getProviderType();
}