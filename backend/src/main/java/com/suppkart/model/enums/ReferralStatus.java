package com.suppkart.model.enums;

public enum ReferralStatus {
    UNUSED,    // Referral code generated but not used yet
    USED,      // Someone registered using the code
    REWARDED   // Both referrer and referee have been rewarded
}
