package com.suppkart.dto.response;

import com.suppkart.model.enums.ContactStatus;

import java.time.LocalDateTime;

/**
 * Response DTO for contact messages
 */
public class ContactMessageResponse {
    
    private Long id;
    private String name;
    private String email;
    private String phone;
    private String subject;
    private String message;
    private ContactStatus status;
    private String response;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime respondedAt;
    private boolean isResponded;

    // Default constructor
    public ContactMessageResponse() {
    }

    // All-args constructor
    public ContactMessageResponse(Long id, String name, String email, String phone, String subject, 
                                String message, ContactStatus status, String response, 
                                LocalDateTime createdAt, LocalDateTime updatedAt, 
                                LocalDateTime respondedAt, boolean isResponded) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.subject = subject;
        this.message = message;
        this.status = status;
        this.response = response;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.respondedAt = respondedAt;
        this.isResponded = isResponded;
    }

    // Getter and Setter methods
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public ContactStatus getStatus() {
        return status;
    }

    public void setStatus(ContactStatus status) {
        this.status = status;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getRespondedAt() {
        return respondedAt;
    }

    public void setRespondedAt(LocalDateTime respondedAt) {
        this.respondedAt = respondedAt;
    }

    public boolean isResponded() {
        return isResponded;
    }

    public void setResponded(boolean responded) {
        isResponded = responded;
    }

    // Static method to create a builder
    public static Builder builder() {
        return new Builder();
    }

    // Builder class
    public static class Builder {
        private Long id;
        private String name;
        private String email;
        private String phone;
        private String subject;
        private String message;
        private ContactStatus status;
        private String response;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private LocalDateTime respondedAt;
        private boolean isResponded;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder phone(String phone) {
            this.phone = phone;
            return this;
        }

        public Builder subject(String subject) {
            this.subject = subject;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder status(ContactStatus status) {
            this.status = status;
            return this;
        }

        public Builder response(String response) {
            this.response = response;
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public Builder respondedAt(LocalDateTime respondedAt) {
            this.respondedAt = respondedAt;
            return this;
        }

        public Builder isResponded(boolean isResponded) {
            this.isResponded = isResponded;
            return this;
        }

        public ContactMessageResponse build() {
            return new ContactMessageResponse(id, name, email, phone, subject, message, 
                                            status, response, createdAt, updatedAt, 
                                            respondedAt, isResponded);
        }
    }
}
