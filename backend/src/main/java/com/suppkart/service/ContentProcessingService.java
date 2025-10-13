package com.suppkart.service;

import com.suppkart.model.enums.ContentType;
import org.springframework.stereotype.Service;

/**
 * Service for processing different types of content (HTML, Markdown, Plain Text)
 */
@Service
public class ContentProcessingService {

    /**
     * Convert content to HTML based on content type
     */
    public String convertToHtml(String content, ContentType contentType) {
        if (content == null || content.trim().isEmpty()) {
            return "";
        }

        switch (contentType) {
            case HTML:
                return sanitizeHtml(content);
            case MARKDOWN:
                return convertMarkdownToHtml(content);
            case PLAIN_TEXT:
                return convertPlainTextToHtml(content);
            default:
                return sanitizeHtml(content);
        }
    }

    /**
     * Extract plain text from content for excerpts and search
     */
    public String extractPlainText(String content, ContentType contentType) {
        if (content == null || content.trim().isEmpty()) {
            return "";
        }

        switch (contentType) {
            case HTML:
                return stripHtmlTags(content);
            case MARKDOWN:
                return stripMarkdownSyntax(content);
            case PLAIN_TEXT:
                return content;
            default:
                return stripHtmlTags(content);
        }
    }

    /**
     * Generate excerpt from content
     */
    public String generateExcerpt(String content, ContentType contentType, int maxLength) {
        String plainText = extractPlainText(content, contentType);
        if (plainText.length() <= maxLength) {
            return plainText;
        }
        
        // Find the last complete word within the limit
        String truncated = plainText.substring(0, maxLength);
        int lastSpace = truncated.lastIndexOf(' ');
        if (lastSpace > 0) {
            truncated = truncated.substring(0, lastSpace);
        }
        
        return truncated + "...";
    }

    /**
     * Basic HTML sanitization (you might want to use a library like OWASP Java HTML Sanitizer)
     */
    private String sanitizeHtml(String html) {
        // Basic sanitization - in production, use a proper HTML sanitizer
        return html
            .replaceAll("<script[^>]*>.*?</script>", "")
            .replaceAll("<iframe[^>]*>.*?</iframe>", "")
            .replaceAll("javascript:", "")
            .replaceAll("on\\w+\\s*=", ""); // Remove event handlers
    }

    /**
     * Convert Markdown to HTML (basic implementation)
     * In production, use a library like CommonMark or flexmark-java
     */
    private String convertMarkdownToHtml(String markdown) {
        return markdown
            // Headers
            .replaceAll("^### (.*$)", "<h3>$1</h3>")
            .replaceAll("^## (.*$)", "<h2>$1</h2>")
            .replaceAll("^# (.*$)", "<h1>$1</h1>")
            // Bold and italic
            .replaceAll("\\*\\*(.*?)\\*\\*", "<strong>$1</strong>")
            .replaceAll("\\*(.*?)\\*", "<em>$1</em>")
            // Links
            .replaceAll("\\[([^\\]]+)\\]\\(([^\\)]+)\\)", "<a href=\"$2\">$1</a>")
            // Line breaks
            .replaceAll("\\n\\n", "</p><p>")
            .replaceAll("\\n", "<br>")
            // Wrap in paragraphs
            .replaceAll("^(.+)", "<p>$1</p>");
    }

    /**
     * Convert plain text to HTML with proper line breaks
     */
    private String convertPlainTextToHtml(String plainText) {
        return "<p>" + plainText
            .replaceAll("&", "&amp;")
            .replaceAll("<", "&lt;")
            .replaceAll(">", "&gt;")
            .replaceAll("\"", "&quot;")
            .replaceAll("'", "&#x27;")
            .replaceAll("\\n\\n", "</p><p>")
            .replaceAll("\\n", "<br>") + "</p>";
    }

    /**
     * Strip HTML tags from content
     */
    private String stripHtmlTags(String html) {
        return html.replaceAll("<[^>]*>", "").trim();
    }

    /**
     * Strip Markdown syntax from content
     */
    private String stripMarkdownSyntax(String markdown) {
        return markdown
            .replaceAll("^#{1,6}\\s+", "") // Headers
            .replaceAll("\\*\\*(.*?)\\*\\*", "$1") // Bold
            .replaceAll("\\*(.*?)\\*", "$1") // Italic
            .replaceAll("\\[([^\\]]+)\\]\\([^\\)]+\\)", "$1") // Links
            .replaceAll("`([^`]+)`", "$1") // Inline code
            .trim();
    }
}