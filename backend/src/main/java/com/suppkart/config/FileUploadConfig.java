package com.suppkart.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;

import jakarta.servlet.MultipartConfigElement;

@Configuration
@ConfigurationProperties(prefix = "app.file-upload")
public class FileUploadConfig {

    // General file upload settings
    private String uploadDir = "uploads/products";
    private String baseUrl = "http://localhost:8080";
    private long maxFileSize = 5 * 1024 * 1024; // 5MB
    private long maxRequestSize = 50 * 1024 * 1024; // 50MB
    private String[] allowedImageTypes = {"image/jpeg", "image/jpg", "image/png", "image/webp"};
    private String[] allowedVideoTypes = {"video/mp4", "video/webm"};

    // Storage provider configuration
    private Storage storage = new Storage();

    public static class Storage {

        private String provider = "local"; // local, s3, gcs, azure
        private S3Config s3 = new S3Config();
        private GcsConfig gcs = new GcsConfig();
        private AzureConfig azure = new AzureConfig();

        // Getters and setters
        public String getProvider() {
            return provider;
        }

        public void setProvider(String provider) {
            this.provider = provider;
        }

        public S3Config getS3() {
            return s3;
        }

        public void setS3(S3Config s3) {
            this.s3 = s3;
        }

        public GcsConfig getGcs() {
            return gcs;
        }

        public void setGcs(GcsConfig gcs) {
            this.gcs = gcs;
        }

        public AzureConfig getAzure() {
            return azure;
        }

        public void setAzure(AzureConfig azure) {
            this.azure = azure;
        }
    }

    public static class S3Config {

        private String bucketName;
        private String region = "us-east-1";
        private String accessKey;
        private String secretKey;
        private String cdnDomain; // Optional CloudFront domain

        // Getters and setters
        public String getBucketName() {
            return bucketName;
        }

        public void setBucketName(String bucketName) {
            this.bucketName = bucketName;
        }

        public String getRegion() {
            return region;
        }

        public void setRegion(String region) {
            this.region = region;
        }

        public String getAccessKey() {
            return accessKey;
        }

        public void setAccessKey(String accessKey) {
            this.accessKey = accessKey;
        }

        public String getSecretKey() {
            return secretKey;
        }

        public void setSecretKey(String secretKey) {
            this.secretKey = secretKey;
        }

        public String getCdnDomain() {
            return cdnDomain;
        }

        public void setCdnDomain(String cdnDomain) {
            this.cdnDomain = cdnDomain;
        }
    }

    public static class GcsConfig {

        private String bucketName;
        private String projectId;
        private String credentialsPath;

        // Getters and setters
        public String getBucketName() {
            return bucketName;
        }

        public void setBucketName(String bucketName) {
            this.bucketName = bucketName;
        }

        public String getProjectId() {
            return projectId;
        }

        public void setProjectId(String projectId) {
            this.projectId = projectId;
        }

        public String getCredentialsPath() {
            return credentialsPath;
        }

        public void setCredentialsPath(String credentialsPath) {
            this.credentialsPath = credentialsPath;
        }
    }

    public static class AzureConfig {

        private String containerName;
        private String accountName;
        private String accountKey;
        private String connectionString;

        // Getters and setters
        public String getContainerName() {
            return containerName;
        }

        public void setContainerName(String containerName) {
            this.containerName = containerName;
        }

        public String getAccountName() {
            return accountName;
        }

        public void setAccountName(String accountName) {
            this.accountName = accountName;
        }

        public String getAccountKey() {
            return accountKey;
        }

        public void setAccountKey(String accountKey) {
            this.accountKey = accountKey;
        }

        public String getConnectionString() {
            return connectionString;
        }

        public void setConnectionString(String connectionString) {
            this.connectionString = connectionString;
        }
    }

    @Bean
    public MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        factory.setMaxFileSize(DataSize.ofBytes(maxFileSize));
        factory.setMaxRequestSize(DataSize.ofBytes(maxRequestSize));
        return factory.createMultipartConfig();
    }

    @Bean
    public MultipartResolver multipartResolver() {
        return new StandardServletMultipartResolver();
    }

    // Getters and setters
    public String getUploadDir() {
        return uploadDir;
    }

    public void setUploadDir(String uploadDir) {
        this.uploadDir = uploadDir;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public long getMaxFileSize() {
        return maxFileSize;
    }

    public void setMaxFileSize(long maxFileSize) {
        this.maxFileSize = maxFileSize;
    }

    public long getMaxRequestSize() {
        return maxRequestSize;
    }

    public void setMaxRequestSize(long maxRequestSize) {
        this.maxRequestSize = maxRequestSize;
    }

    public String[] getAllowedImageTypes() {
        return allowedImageTypes;
    }

    public void setAllowedImageTypes(String[] allowedImageTypes) {
        this.allowedImageTypes = allowedImageTypes;
    }

    public String[] getAllowedVideoTypes() {
        return allowedVideoTypes;
    }

    public void setAllowedVideoTypes(String[] allowedVideoTypes) {
        this.allowedVideoTypes = allowedVideoTypes;
    }

    public Storage getStorage() {
        return storage;
    }

    public void setStorage(Storage storage) {
        this.storage = storage;
    }
}
