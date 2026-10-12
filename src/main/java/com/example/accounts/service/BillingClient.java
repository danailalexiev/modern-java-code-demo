package com.example.accounts.service;

import com.example.accounts.domain.UserId;
import com.example.accounts.persistence.UserEntity;
import com.example.accounts.persistence.UserRepository;
import org.springframework.stereotype.Component;

/**
 * Stub for an external billing system: real implementations would call out
 * over the network by customer id, not by passing a domain object.
 */
@Component
public class BillingClient {

    private final UserRepository users;

    public BillingClient(UserRepository users) {
        this.users = users;
    }

    public boolean hasValidPaymentMethod(UserId id) {
        return users.findById(id.value())
            .map(UserEntity::isPaymentMethodOnFile)
            .orElse(false);
    }
}
