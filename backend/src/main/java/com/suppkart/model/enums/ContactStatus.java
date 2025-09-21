package com.suppkart.model.enums;

/**
 * Enum representing the status of contact messages
 */
public enum ContactStatus {
    NEW("New message awaiting review"),
    IN_PROGRESS("Message is being processed"),
    RESOLVED("Message has been resolved"),
    SPAM("Message marked as spam");

    private final String description;

    ContactStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
