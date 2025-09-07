package com.suppkart.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.suppkart.model.entity.RefreshToken;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    /**
     * Find refresh token by token string
     */
    Optional<RefreshToken> findByToken(String token);

    /**
     * Find all refresh tokens for a user
     */
    List<RefreshToken> findByUser_UserId(Long userId);

    /**
     * Find valid (non-revoked and non-expired) tokens for a user
     */
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.user.userId = :userId AND rt.revoked = false AND rt.expiresAt > :now")
    List<RefreshToken> findValidTokensByUserId(@Param("userId") Long userId, @Param("now") LocalDateTime now);

    /**
     * Find expired tokens
     */
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.expiresAt < :now")
    List<RefreshToken> findExpiredTokens(@Param("now") LocalDateTime now);

    /**
     * Check if token exists and is valid
     */
    @Query("SELECT COUNT(rt) > 0 FROM RefreshToken rt WHERE rt.token = :token AND rt.revoked = false AND rt.expiresAt > :now")
    boolean existsByTokenAndValidityCheck(@Param("token") String token, @Param("now") LocalDateTime now);

    /**
     * Revoke all tokens for a user
     */
    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.revoked = true WHERE rt.user.userId = :userId")
    void revokeAllTokensForUser(@Param("userId") Long userId);

    /**
     * Revoke specific token
     */
    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.revoked = true WHERE rt.token = :token")
    void revokeToken(@Param("token") String token);

    /**
     * Delete expired tokens
     */
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiresAt < :now")
    void deleteExpiredTokens(@Param("now") LocalDateTime now);

    /**
     * Delete all tokens for a user
     */
    void deleteByUser_UserId(Long userId);

    /**
     * Count active tokens for a user
     */
    @Query("SELECT COUNT(rt) FROM RefreshToken rt WHERE rt.user.userId = :userId AND rt.revoked = false AND rt.expiresAt > :now")
    long countActiveTokensByUserId(@Param("userId") Long userId, @Param("now") LocalDateTime now);

    /**
     * Find tokens by device info and user
     */
    List<RefreshToken> findByUser_UserIdAndDeviceInfo(Long userId, String deviceInfo);

    /**
     * Find tokens by IP address
     */
    List<RefreshToken> findByIpAddress(String ipAddress);

    /**
     * Delete all tokens for a user (by User entity)
     */
    void deleteByUser(com.suppkart.model.entity.User user);

    /**
     * Delete tokens expired before given date
     */
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiresAt < :expiryDate")
    void deleteByExpiryDateBefore(@Param("expiryDate") LocalDateTime expiryDate);
    
    /**
     * Find refresh tokens that have expired before the given date
     */
    List<RefreshToken> findByExpiryDateBefore(LocalDateTime expiryDate);
    
    /**
     * Count expired refresh tokens
     */
    @Query("SELECT COUNT(rt) FROM RefreshToken rt WHERE rt.expiresAt < :now")
    long countExpiredTokens(@Param("now") LocalDateTime now);
}
