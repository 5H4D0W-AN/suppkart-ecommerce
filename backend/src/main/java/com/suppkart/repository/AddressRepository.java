package com.suppkart.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.suppkart.model.entity.Address;
import com.suppkart.model.entity.User;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {

    /**
     * Find all addresses for a user
     */
    List<Address> findByUser_UserIdOrderByIsDefaultDescCreatedAtDesc(Long userId);

    /**
     * Find default address for a user
     */
    Optional<Address> findByUser_UserIdAndIsDefaultTrue(Long userId);

    /**
     * Find address by user and address ID
     */
    Optional<Address> findByAddressIdAndUser_UserId(Long addressId, Long userId);

    /**
     * Count addresses for a user
     */
    long countByUser_UserId(Long userId);

    /**
     * Check if user has default address
     */
    boolean existsByUser_UserIdAndIsDefaultTrue(Long userId);

    /**
     * Remove default status from all user addresses
     */
    @Modifying
    @Query("UPDATE Address a SET a.isDefault = false WHERE a.user.userId = :userId")
    void removeDefaultStatusForUser(@Param("userId") Long userId);

    /**
     * Delete all addresses for a user
     */
    void deleteByUser_UserId(Long userId);

    /**
     * Find addresses by postal code
     */
    List<Address> findByPostalCode(String postalCode);

    /**
     * Find addresses by city and state
     */
    List<Address> findByCityAndState(String city, String state);
    
    // Additional methods needed by AddressService
    
    /**
     * Find all addresses for a user ordered by default status and creation date
     */
    List<Address> findByUserOrderByIsDefaultDescCreatedAtDesc(User user);
    
    /**
     * Find address by ID and user
     */
    Optional<Address> findByAddressIdAndUser(Long addressId, User user);
    
    /**
     * Find default address by user
     */
    Optional<Address> findByUserAndIsDefaultTrue(User user);
    
    /**
     * Count addresses by user
     */
    long countByUser(User user);
    
    /**
     * Check if user has any addresses
     */
    boolean existsByUser(User user);
    
    /**
     * Find first address by user ordered by creation date
     */
    Optional<Address> findFirstByUserOrderByCreatedAtAsc(User user);
}
