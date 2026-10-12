package com.example.accounts.service;

import com.example.accounts.domain.UserId;
import java.time.Instant;

public sealed interface UpgradeError {
    record NotFound(UserId id) implements UpgradeError {}
    record AccountSuspended(UserId id, Instant since) implements UpgradeError {}
    record NotVerified(UserId id) implements UpgradeError {}
    record PaymentRequired(UserId id) implements UpgradeError {}
}
