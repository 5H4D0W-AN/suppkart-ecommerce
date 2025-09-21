package com.suppkart.model.entity;

import java.time.LocalTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

/**
 * Entity representing consultation available time slots
 */
@Entity
@Table(name = "consultation_slots")
public class ConsultationSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "day_of_week", nullable = false)
    private Integer dayOfWeek; // 1-7 for Monday-Sunday

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "max_bookings", nullable = false)
    private Integer maxBookings = 2;

    @Column(name = "is_available", nullable = false)
    private Boolean isAvailable = true;

    // Constructors
    public ConsultationSlot() {}

    public ConsultationSlot(Integer dayOfWeek, LocalTime startTime, LocalTime endTime, Integer maxBookings, Boolean isAvailable) {
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
        this.maxBookings = maxBookings;
        this.isAvailable = isAvailable;
    }

    @PrePersist
    protected void onCreate() {
        if (maxBookings == null) {
            maxBookings = 2;
        }
        if (isAvailable == null) {
            isAvailable = true;
        }
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Integer getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(Integer dayOfWeek) { this.dayOfWeek = dayOfWeek; }

    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }

    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }

    public Integer getMaxBookings() { return maxBookings; }
    public void setMaxBookings(Integer maxBookings) { this.maxBookings = maxBookings; }

    public Boolean getIsAvailable() { return isAvailable; }
    public void setIsAvailable(Boolean isAvailable) { this.isAvailable = isAvailable; }
}
