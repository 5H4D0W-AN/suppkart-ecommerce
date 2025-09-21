package com.suppkart.dto.request;

import java.time.LocalDate;
import java.time.LocalTime;

import com.suppkart.model.enums.ConsultationType;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO for consultation booking request
 */
public class ConsultationBookingRequest {

    private Long userId; // Optional, for authenticated users

    @Size(max = 100, message = "Guest name cannot exceed 100 characters")
    private String guestName; // Required for guest bookings

    @Email(message = "Invalid email format")
    @Size(max = 150, message = "Email cannot exceed 150 characters")
    private String guestEmail; // Required for guest bookings

    @Size(max = 20, message = "Phone number cannot exceed 20 characters")
    private String guestPhone; // Optional

    @NotNull(message = "Consultation date is required")
    private LocalDate consultationDate;

    @NotNull(message = "Consultation time is required")
    private LocalTime consultationTime;

    @NotBlank(message = "Timezone is required")
    private String timezone;

    @NotBlank(message = "Topic is required")
    @Size(max = 200, message = "Topic cannot exceed 200 characters")
    private String topic;

    @NotNull(message = "Consultation type is required")
    private ConsultationType consultationType;

    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    private String notes;

    // Constructors
    public ConsultationBookingRequest() {}

    // Getters and Setters
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getGuestName() { return guestName; }
    public void setGuestName(String guestName) { this.guestName = guestName; }

    public String getGuestEmail() { return guestEmail; }
    public void setGuestEmail(String guestEmail) { this.guestEmail = guestEmail; }

    public String getGuestPhone() { return guestPhone; }
    public void setGuestPhone(String guestPhone) { this.guestPhone = guestPhone; }

    public LocalDate getConsultationDate() { return consultationDate; }
    public void setConsultationDate(LocalDate consultationDate) { this.consultationDate = consultationDate; }

    public LocalTime getConsultationTime() { return consultationTime; }
    public void setConsultationTime(LocalTime consultationTime) { this.consultationTime = consultationTime; }

    public String getTimezone() { return timezone; }
    public void setTimezone(String timezone) { this.timezone = timezone; }

    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }

    public ConsultationType getConsultationType() { return consultationType; }
    public void setConsultationType(ConsultationType consultationType) { this.consultationType = consultationType; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
