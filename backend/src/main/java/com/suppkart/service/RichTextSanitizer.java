package com.suppkart.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Service
@Slf4j
public class RichTextSanitizer {

    // Patterns for dangerous content
    private static final Pattern SCRIPT_PATTERN = Pattern.compile("<script[^>]*>.*?</script>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern JAVASCRIPT_PATTERN = Pattern.compile("javascript:", Pattern.CASE_INSENSITIVE);
    private static final Pattern ONCLICK_PATTERN = Pattern.compile("on\\w+\\s*=", Pattern.CASE_INSENSITIVE);
    private static final Pattern STYLE_EXPRESSION_PATTERN = Pattern.compile("expression\\s*\\(", Pattern.CASE_INSENSITIVE);

    /**
     * Sanitize HTML content to prevent XSS attacks
     */
    public String sanitize(String richText) {
        if (richText == null || richText.trim().isEmpty()) {
            return "";
        }

        log.debug("Sanitizing HTML content of length: {}", richText.length());

        String sanitized = richText;

        // Remove script tags
        sanitized = SCRIPT_PATTERN.matcher(sanitized).replaceAll("");

        // Remove javascript: URLs
        sanitized = JAVASCRIPT_PATTERN.matcher(sanitized).replaceAll("");

        // Remove event handlers (onclick, onload, etc.)
        sanitized = ONCLICK_PATTERN.matcher(sanitized).replaceAll("");

        // Remove CSS expressions
        sanitized = STYLE_EXPRESSION_PATTERN.matcher(sanitized).replaceAll("");

        // Remove potentially dangerous tags
        sanitized = removeDangerousTags(sanitized);

        // Clean up attributes
        sanitized = cleanAttributes(sanitized);

        log.debug("Sanitized HTML content to length: {}", sanitized.length());
        return sanitized;
    }

    /**
     * Extract plain text from HTML content
     */
    public String extractPlainText(String richText) {
        if (richText == null || richText.trim().isEmpty()) {
            return "";
        }

        // Remove all HTML tags
        String plainText = richText.replaceAll("<[^>]+>", "");

        // Decode HTML entities
        plainText = decodeHtmlEntities(plainText);

        // Clean up whitespace
        plainText = plainText.replaceAll("\\s+", " ").trim();

        return plainText;
    }

    /**
     * Check if content contains potentially dangerous elements
     */
    public boolean containsDangerousContent(String richText) {
        if (richText == null || richText.trim().isEmpty()) {
            return false;
        }

        return SCRIPT_PATTERN.matcher(richText).find() ||
               JAVASCRIPT_PATTERN.matcher(richText).find() ||
               ONCLICK_PATTERN.matcher(richText).find() ||
               STYLE_EXPRESSION_PATTERN.matcher(richText).find();
    }

    /**
     * Get a safe excerpt from rich text content
     */
    public String getExcerpt(String richText, int maxLength) {
        String plainText = extractPlainText(richText);
        
        if (plainText.length() <= maxLength) {
            return plainText;
        }

        // Find the last space before maxLength to avoid cutting words
        int cutPoint = plainText.lastIndexOf(' ', maxLength);
        if (cutPoint == -1) {
            cutPoint = maxLength;
        }

        return plainText.substring(0, cutPoint) + "...";
    }

    /**
     * Remove dangerous HTML tags
     */
    private String removeDangerousTags(String content) {
        // List of dangerous tags to remove
        String[] dangerousTags = {
                "script", "object", "embed", "applet", "meta", "iframe",
                "frame", "frameset", "link", "style", "base", "form",
                "input", "button", "textarea", "select", "option"
        };

        String result = content;
        for (String tag : dangerousTags) {
            // Remove opening and closing tags
            result = result.replaceAll("(?i)<" + tag + "[^>]*>", "");
            result = result.replaceAll("(?i)</" + tag + ">", "");
        }

        return result;
    }

    /**
     * Clean HTML attributes to remove dangerous ones
     */
    private String cleanAttributes(String content) {
        // Remove style attributes that might contain expressions
        content = content.replaceAll("(?i)style\\s*=\\s*[\"'][^\"']*expression[^\"']*[\"']", "");
        
        // Remove data attributes that might be used for XSS
        content = content.replaceAll("(?i)data-[^=]*\\s*=\\s*[\"'][^\"']*[\"']", "");
        
        return content;
    }

    /**
     * Decode common HTML entities
     */
    private String decodeHtmlEntities(String text) {
        return text
                .replace("&amp;", "&")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&quot;", "\"")
                .replace("&#39;", "'")
                .replace("&nbsp;", " ");
    }

    /**
     * Validate if HTML content is safe for storage
     */
    public boolean isContentSafe(String richText) {
        if (richText == null) {
            return true;
        }

        // Check for dangerous content
        if (containsDangerousContent(richText)) {
            log.warn("Content contains dangerous elements");
            return false;
        }

        // Check content length (prevent extremely large content)
        if (richText.length() > 1_000_000) { // 1MB limit
            log.warn("Content exceeds maximum allowed length");
            return false;
        }

        return true;
    }

    /**
     * Sanitize and validate content in one step
     */
    public String sanitizeAndValidate(String richText) {
        if (!isContentSafe(richText)) {
            throw new IllegalArgumentException("Content contains unsafe elements or exceeds size limits");
        }

        return sanitize(richText);
    }
}
