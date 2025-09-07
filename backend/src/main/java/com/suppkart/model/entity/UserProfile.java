package com.suppkart.model.entity;

import java.time.LocalDateTime;
import java.util.Objects;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "user_profiles")
@EntityListeners(AuditingEntityListener.class)
public class UserProfile {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "profile_id")
    private Long profileId;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;
    
    @Column(name = "first_name", nullable = false)
    private String firstName;
    
    @Column(name = "last_name", nullable = false)
    private String lastName;
    
    @Column(name = "phone", length = 20)
    private String phone;
    
    @Column(name = "profile_image_url")
    private String profileImageUrl;
    
    @Column(name = "date_of_birth")
    private LocalDateTime dateOfBirth;
    
    @Column(name = "gender", length = 10)
    private String gender;
    
    @Column(name = "occupation")
    private String occupation;
    
    @Column(name = "bio", columnDefinition = "TEXT")
    private String bio;
    
    @Column(name = "preferred_language", length = 10)
    private String preferredLanguage;
    
    @Column(name = "timezone", length = 50)
    private String timezone;
    
    @Column(name = "notification_preferences", columnDefinition = "JSON")
    private String notificationPreferences;
    
    @Column(name = "privacy_settings", columnDefinition = "JSON")
    private String privacySettings;
    
    @Column(name = "is_newsletter_subscribed", nullable = false)
    private Boolean isNewsletterSubscribed = false;
    
    @Column(name = "is_sms_notifications_enabled", nullable = false)
    private Boolean isSmsNotificationsEnabled = true;
    
    @Column(name = "is_email_notifications_enabled", nullable = false)
    private Boolean isEmailNotificationsEnabled = true;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // Constructors
    public UserProfile() {}
    
    public UserProfile(User user) {
        this.user = user;
    }
    
    // Getters and Setters
    public Long getProfileId() {
        return profileId;
    }
    
    public void setProfileId(Long profileId) {
        this.profileId = profileId;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public String getFirstName() {
        return firstName;
    }
    
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    
    public String getLastName() {
        return lastName;
    }
    
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public String getProfileImageUrl() {
        return profileImageUrl;
    }
    
    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }
    
    public LocalDateTime getDateOfBirth() {
        return dateOfBirth;
    }
    
    public void setDateOfBirth(LocalDateTime dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }
    
    public String getGender() {
        return gender;
    }
    
    public void setGender(String gender) {
        this.gender = gender;
    }
    
    public String getOccupation() {
        return occupation;
    }
    
    public void setOccupation(String occupation) {
        this.occupation = occupation;
    }
    
    public String getBio() {
        return bio;
    }
    
    public void setBio(String bio) {
        this.bio = bio;
    }
    
    public String getPreferredLanguage() {
        return preferredLanguage;
    }
    
    public void setPreferredLanguage(String preferredLanguage) {
        this.preferredLanguage = preferredLanguage;
    }
    
    public String getTimezone() {
        return timezone;
    }
    
    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }
    
    public String getNotificationPreferences() {
        return notificationPreferences;
    }
    
    public void setNotificationPreferences(String notificationPreferences) {
        this.notificationPreferences = notificationPreferences;
    }
    
    public String getPrivacySettings() {
        return privacySettings;
    }
    
    public void setPrivacySettings(String privacySettings) {
        this.privacySettings = privacySettings;
    }
    
    public Boolean getIsNewsletterSubscribed() {
        return isNewsletterSubscribed;
    }
    
    public void setIsNewsletterSubscribed(Boolean isNewsletterSubscribed) {
        this.isNewsletterSubscribed = isNewsletterSubscribed;
    }
    
    public Boolean getIsSmsNotificationsEnabled() {
        return isSmsNotificationsEnabled;
    }
    
    public void setIsSmsNotificationsEnabled(Boolean isSmsNotificationsEnabled) {
        this.isSmsNotificationsEnabled = isSmsNotificationsEnabled;
    }
    
    public Boolean getIsEmailNotificationsEnabled() {
        return isEmailNotificationsEnabled;
    }
    
    public void setIsEmailNotificationsEnabled(Boolean isEmailNotificationsEnabled) {
        this.isEmailNotificationsEnabled = isEmailNotificationsEnabled;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserProfile that = (UserProfile) o;
        return Objects.equals(profileId, that.profileId) && Objects.equals(user, that.user);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(profileId, user);
    }
    
    @Override
    public String toString() {
        return "UserProfile{" +
                "profileId=" + profileId +
                ", profileImageUrl='" + profileImageUrl + '\'' +
                ", gender='" + gender + '\'' +
                ", occupation='" + occupation + '\'' +
                '}';
    }
}
