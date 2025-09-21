package com.suppkart.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.suppkart.model.entity.ContactMessage;
import com.suppkart.model.enums.ContactStatus;

/**
 * Repository interface for ContactMessage entity
 */
@Repository
public interface ContactMessageRepository extends JpaRepository<ContactMessage, Long> {

    /**
     * Find contact messages by status ordered by creation date descending
     */
    List<ContactMessage> findByStatusOrderByCreatedAtDesc(ContactStatus status);

    /**
     * Find contact messages by status with pagination
     */
    Page<ContactMessage> findByStatusOrderByCreatedAtDesc(ContactStatus status, Pageable pageable);

    /**
     * Find contact messages by email containing (case insensitive)
     */
    List<ContactMessage> findByEmailContainingIgnoreCase(String email);

    /**
     * Find contact messages by email containing (case insensitive) with pagination
     */
    Page<ContactMessage> findByEmailContainingIgnoreCase(String email, Pageable pageable);

    /**
     * Count contact messages by status
     */
    Long countByStatus(ContactStatus status);

    /**
     * Find all contact messages with pagination ordered by creation date descending
     */
    Page<ContactMessage> findAllByOrderByCreatedAtDesc(Pageable pageable);

    /**
     * Find contact messages by name containing (case insensitive)
     */
    List<ContactMessage> findByNameContainingIgnoreCase(String name);

    /**
     * Find contact messages by subject containing (case insensitive)
     */
    List<ContactMessage> findBySubjectContainingIgnoreCase(String subject);
}
