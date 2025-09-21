package com.suppkart.exception;

/**
 * Exception thrown for contact message-related operations
 */
public class ContactException extends BusinessException {

    public ContactException(String message) {
        super("CONTACT_ERROR", message);
    }

    public ContactException(String message, Throwable cause) {
        super("CONTACT_ERROR", message, cause);
    }

    public static ContactException messageNotFound(Long id) {
        return new ContactException(
            String.format("Contact message not found with id: %d", id)
        );
    }

    public static ContactException invalidStatus(String status) {
        return new ContactException(
            String.format("Invalid contact message status: %s", status)
        );
    }

    public static ContactException cannotUpdateMessage(String reason) {
        return new ContactException(
            String.format("Cannot update contact message: %s", reason)
        );
    }

    public static ContactException cannotDeleteMessage(String reason) {
        return new ContactException(
            String.format("Cannot delete contact message: %s", reason)
        );
    }

    public static ContactException duplicateMessage() {
        return new ContactException("Duplicate contact message detected");
    }

    public static ContactException invalidEmailFormat(String email) {
        return new ContactException(
            String.format("Invalid email format: %s", email)
        );
    }
}
