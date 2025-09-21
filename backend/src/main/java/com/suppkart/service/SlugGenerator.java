package com.suppkart.service;

import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.function.Function;
import java.util.regex.Pattern;

@Service
public class SlugGenerator {

    private static final Pattern NON_LATIN = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]");
    private static final Pattern MULTIPLE_HYPHENS = Pattern.compile("-{2,}");
    private static final Pattern LEADING_TRAILING_HYPHENS = Pattern.compile("^-|-$");

    /**
     * Generate a URL-friendly slug from the given input text
     * 
     * @param input The input text to convert to slug
     * @return A URL-friendly slug
     */
    public String generateSlug(String input) {
        if (input == null || input.trim().isEmpty()) {
            return "";
        }

        // Convert to lowercase and normalize unicode characters
        String slug = input.toLowerCase().trim();
        
        // Normalize unicode characters (remove accents, etc.)
        slug = Normalizer.normalize(slug, Normalizer.Form.NFD);
        
        // Replace whitespace with hyphens
        slug = WHITESPACE.matcher(slug).replaceAll("-");
        
        // Remove non-latin characters (keep only letters, numbers, and hyphens)
        slug = NON_LATIN.matcher(slug).replaceAll("");
        
        // Replace multiple consecutive hyphens with single hyphen
        slug = MULTIPLE_HYPHENS.matcher(slug).replaceAll("-");
        
        // Remove leading and trailing hyphens
        slug = LEADING_TRAILING_HYPHENS.matcher(slug).replaceAll("");
        
        // Ensure slug is not empty after processing
        if (slug.isEmpty()) {
            slug = "untitled";
        }
        
        // Limit slug length to 100 characters
        if (slug.length() > 100) {
            slug = slug.substring(0, 100);
            // Remove trailing hyphen if present after truncation
            slug = LEADING_TRAILING_HYPHENS.matcher(slug).replaceAll("");
        }
        
        return slug;
    }

    /**
     * Generate a unique slug by appending a number if the base slug already exists
     * 
     * @param baseSlug The base slug to make unique
     * @param existsFunction Function to check if a slug already exists
     * @return A unique slug
     */
    public String ensureUniqueSlug(String baseSlug, Function<String, Boolean> existsFunction) {
        if (baseSlug == null || baseSlug.trim().isEmpty()) {
            baseSlug = "untitled";
        }

        String candidateSlug = baseSlug;
        int counter = 1;

        // Keep trying with incremented numbers until we find a unique slug
        while (existsFunction.apply(candidateSlug)) {
            candidateSlug = baseSlug + "-" + counter;
            counter++;
            
            // Prevent infinite loop by limiting attempts
            if (counter > 1000) {
                candidateSlug = baseSlug + "-" + System.currentTimeMillis();
                break;
            }
        }

        return candidateSlug;
    }

    /**
     * Generate a unique slug from input text
     * 
     * @param input The input text to convert to slug
     * @param existsFunction Function to check if a slug already exists
     * @return A unique URL-friendly slug
     */
    public String generateUniqueSlug(String input, Function<String, Boolean> existsFunction) {
        String baseSlug = generateSlug(input);
        return ensureUniqueSlug(baseSlug, existsFunction);
    }

    /**
     * Validate if a string is a valid slug format
     * 
     * @param slug The slug to validate
     * @return true if the slug is valid, false otherwise
     */
    public boolean isValidSlug(String slug) {
        if (slug == null || slug.trim().isEmpty()) {
            return false;
        }

        // Check if slug contains only valid characters (letters, numbers, hyphens)
        if (!slug.matches("^[a-z0-9-]+$")) {
            return false;
        }

        // Check if slug starts or ends with hyphen
        if (slug.startsWith("-") || slug.endsWith("-")) {
            return false;
        }

        // Check if slug contains consecutive hyphens
        if (slug.contains("--")) {
            return false;
        }

        // Check length constraints
        if (slug.length() > 100) {
            return false;
        }

        return true;
    }

    /**
     * Clean and validate an existing slug
     * 
     * @param slug The slug to clean
     * @return A cleaned and valid slug
     */
    public String cleanSlug(String slug) {
        if (slug == null || slug.trim().isEmpty()) {
            return "untitled";
        }

        // Apply the same cleaning process as generateSlug
        return generateSlug(slug);
    }

    /**
     * Generate slug from title with fallback options
     * 
     * @param title Primary title to generate slug from
     * @param fallbackTitle Fallback title if primary is empty
     * @param defaultSlug Default slug if both titles are empty
     * @return Generated slug
     */
    public String generateSlugWithFallback(String title, String fallbackTitle, String defaultSlug) {
        if (title != null && !title.trim().isEmpty()) {
            return generateSlug(title);
        }
        
        if (fallbackTitle != null && !fallbackTitle.trim().isEmpty()) {
            return generateSlug(fallbackTitle);
        }
        
        return defaultSlug != null ? defaultSlug : "untitled";
    }

    /**
     * Update slug if title has changed significantly
     * 
     * @param currentSlug Current slug
     * @param newTitle New title
     * @param existsFunction Function to check if slug exists
     * @return Updated slug if needed, otherwise current slug
     */
    public String updateSlugIfNeeded(String currentSlug, String newTitle, Function<String, Boolean> existsFunction) {
        if (currentSlug == null || currentSlug.trim().isEmpty()) {
            return generateUniqueSlug(newTitle, existsFunction);
        }

        String newSlug = generateSlug(newTitle);
        
        // If the new slug would be the same as current, keep current
        if (currentSlug.equals(newSlug)) {
            return currentSlug;
        }

        // If current slug is still valid and not too different, keep it
        // This prevents breaking existing URLs unnecessarily
        if (isValidSlug(currentSlug)) {
            return currentSlug;
        }

        // Generate new unique slug
        return generateUniqueSlug(newTitle, existsFunction);
    }
}
