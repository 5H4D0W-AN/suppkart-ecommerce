package com.suppkart.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.suppkart.model.entity.Role;
import com.suppkart.model.enums.RoleType;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    
    /**
     * Find role by role type
     */
    Optional<Role> findByRoleType(RoleType roleType);
    
    /**
     * Check if role exists by role type
     */
    boolean existsByRoleType(RoleType roleType);
    
    /**
     * Find role by name (alias for findByRoleType)
     */
    default Optional<Role> findByName(RoleType roleType) {
        return findByRoleType(roleType);
    }
}
