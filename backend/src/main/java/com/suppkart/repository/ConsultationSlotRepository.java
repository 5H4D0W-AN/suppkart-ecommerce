package com.suppkart.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.suppkart.model.entity.ConsultationSlot;

/**
 * Repository interface for ConsultationSlot entity
 */
@Repository
public interface ConsultationSlotRepository extends JpaRepository<ConsultationSlot, Long> {

    /**
     * Find available slots for a specific day of week
     */
    List<ConsultationSlot> findByDayOfWeekAndIsAvailableTrue(Integer dayOfWeek);

    /**
     * Find a specific slot by day, start time and end time
     */
    Optional<ConsultationSlot> findByDayOfWeekAndStartTimeAndEndTime(
            Integer dayOfWeek, LocalTime startTime, LocalTime endTime);

    /**
     * Find all available slots ordered by day and start time
     */
    List<ConsultationSlot> findByIsAvailableTrueOrderByDayOfWeekAscStartTimeAsc();

    /**
     * Find slots for multiple days
     */
    List<ConsultationSlot> findByDayOfWeekInAndIsAvailableTrue(List<Integer> daysOfWeek);

    /**
     * Check if a slot exists for given day and time range
     */
    @Query("SELECT COUNT(cs) > 0 FROM ConsultationSlot cs WHERE cs.dayOfWeek = :dayOfWeek " +
           "AND cs.startTime <= :time AND cs.endTime > :time AND cs.isAvailable = true")
    boolean existsSlotForDayAndTime(@Param("dayOfWeek") Integer dayOfWeek, @Param("time") LocalTime time);

    /**
     * Find slots by day of week ordered by start time
     */
    List<ConsultationSlot> findByDayOfWeekOrderByStartTimeAsc(Integer dayOfWeek);

    /**
     * Find slots by day of week and time range
     */
    List<ConsultationSlot> findByDayOfWeekAndStartTimeLessThanEqualAndEndTimeGreaterThanEqual(
            Integer dayOfWeek, LocalTime startTime, LocalTime endTime);
}
