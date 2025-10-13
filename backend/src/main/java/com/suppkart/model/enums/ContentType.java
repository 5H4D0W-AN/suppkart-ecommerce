package com.suppkart.model.enums;

/**
 * Enum representing different content types for blog posts
 */
public enum ContentType {
    /**
     * HTML content - rich formatted content with HTML tags
     * Best for: Rich text editors, complex formatting
     */
    HTML,
    
    /**
     * Markdown content - lightweight markup language
     * Best for: Developer-friendly writing, simple formatting
     */
    MARKDOWN,
    
    /**
     * Plain text content - no formatting
     * Best for: Simple text-only content
     */
    PLAIN_TEXT
}