package com.example.accounts.domain;

import java.time.Instant;

public sealed interface Account {
    UserId id();

    Email email();

    record Active(UserId id, Email email, Plan plan) implements Account {
        public Active withPlan(Plan newPlan) {
            return new Active(id, email, newPlan);
        }
    }

    record Suspended(UserId id, Email email, Instant since, String reason) implements Account {}

    record PendingVerification(UserId id, Email email, String token, Instant expiresAt) implements Account {}
}
