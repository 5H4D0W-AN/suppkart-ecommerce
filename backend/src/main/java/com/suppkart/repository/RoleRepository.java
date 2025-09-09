package com.suppkart.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.suppkart.model.entity.Role;
import com.suppkart.model.enums.RoleType;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    
    /**
     * Find role by name
     */
    Optional<Role> findByName(RoleType name);
    
    /**
     * Check if role exists by name
     */
    boolean existsByName(RoleType name);
}
