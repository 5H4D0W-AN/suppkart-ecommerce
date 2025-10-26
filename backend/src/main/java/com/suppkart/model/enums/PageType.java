package com.suppkart.model.enums;

/**
 * Enum representing different page types in the application
 */
public enum PageType {
    HOME("Home"),
    PRODUCT_DETAILS("Product Details"),
    SEARCH("Search"),
    CATEGORY("Category"),
    TERMS_AND_CONDITIONS("Terms and Conditions"),
    THANK_YOU("Thank You"),
    SIGN_IN("Sign In"),
    SIGN_UP("Sign Up"),
    BLOGS_SECTION("Blogs Section"),
    BLOG_DETAILS("Blog Details"),
    ABOUT_US("About Us"),
    PROFILE("Profile"),
    REFERRAL_PROGRAM("Referral Program"),
    CONSULTATION("Consultation");

    private final String displayName;

    PageType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}