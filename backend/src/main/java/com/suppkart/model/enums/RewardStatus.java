package com.suppkart.model.enums;

public enum RewardStatus {
    PENDING,   // Reward created but not yet active (waiting for first purchase)
    ACTIVE,    // Reward is active and available to use
    APPLIED,   // Reward has been applied to an order
    EXPIRED    // Reward has expired and is no longer usable
}
