package com.suppkart.integration.storage;

/**
 * Enum for different storage provider types
 */
public enum StorageProviderType {
    LOCAL("Local File System"),
    S3("Amazon S3"),
    GCS("Google Cloud Storage"),
    AZURE("Azure Blob Storage");
    
    private final String displayName;
    
    StorageProviderType(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}