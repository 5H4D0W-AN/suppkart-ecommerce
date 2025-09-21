package com.suppkart.exception;

/**
 * Exception thrown for consultation-related operations
 */
public class ConsultationException extends BusinessException {

    public ConsultationException(String message) {
        super("CONSULTATION_ERROR", message);
    }

    public ConsultationException(String message, Throwable cause) {
        super("CONSULTATION_ERROR", message, cause);
    }

    public static ConsultationException slotNotAvailable(String date, String time) {
        return new ConsultationException(
            String.format("No available consultation slot for date: %s at time: %s", date, time)
        );
    }

    public static ConsultationException maxBookingsReached(String date, String time) {
        return new ConsultationException(
            String.format("Maximum bookings reached for date: %s at time: %s", date, time)
        );
    }

    public static ConsultationException consultationNotFound(Long id) {
        return new ConsultationException(
            String.format("Consultation not found with id: %d", id)
        );
    }

    public static ConsultationException invalidDateRange() {
        return new ConsultationException("Invalid date range provided");
    }

    public static ConsultationException pastDateBooking() {
        return new ConsultationException("Cannot book consultation for past dates");
    }

    public static ConsultationException invalidTimeSlot() {
        return new ConsultationException("Invalid time slot selected");
    }

    public static ConsultationException consultationAlreadyExists(String date, String time) {
        return new ConsultationException(
            String.format("Consultation already exists for date: %s at time: %s", date, time)
        );
    }

    public static ConsultationException cannotCancelConsultation(String reason) {
        return new ConsultationException(
            String.format("Cannot cancel consultation: %s", reason)
        );
    }

    public static ConsultationException cannotUpdateConsultation(String reason) {
        return new ConsultationException(
            String.format("Cannot update consultation: %s", reason)
        );
    }
}
