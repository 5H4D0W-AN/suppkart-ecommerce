package com.suppkart.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.suppkart.model.entity.UserProfile;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {

    /**
     * Find user profile by user ID
     */
    Optional<UserProfile> findByUser_UserId(Long userId);

    /**
     * Find user profile by user email
     */
    @Query("SELECT up FROM UserProfile up WHERE up.user.email = :email")
    Optional<UserProfile> findByUserEmail(@Param("email") String email);

    /**
     * Check if profile exists for user
     */
    boolean existsByUser_UserId(Long userId);

    /**
     * Delete profile by user ID
     */
    void deleteByUser_UserId(Long userId);

    /**
     * Find profiles by phone number
     */
    Optional<UserProfile> findByPhone(String phone);

    /**
     * Check if phone number exists
     */
    boolean existsByPhone(String phone);
}
