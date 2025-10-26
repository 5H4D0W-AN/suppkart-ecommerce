package com.suppkart.model.enums;

/**
 * Enum representing different content types for SEO metadata
 */
public enum ContentType {
    TEXT("Text"),
    MEDIA("Media"),
    HTML("HTML"),
    JSON("JSON");

    private final String displayName;

    ContentType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}