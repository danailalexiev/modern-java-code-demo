package com.example.accounts.service;

import com.example.accounts.persistence.UserEntity;
import org.springframework.stereotype.Component;

/**
 * Stub for an external billing system. Not wired into {@link UserService} yet.
 */
@Component
public class BillingClient {

    public boolean hasValidPaymentMethod(UserEntity user) {
        return user.isPaymentMethodOnFile();
    }
}
