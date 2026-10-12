package com.example.accounts.service;

import com.example.accounts.persistence.UserEntity;

public sealed interface UpgradeResult {
    record Upgraded(UserEntity user) implements UpgradeResult {}
    record NotFound(long id) implements UpgradeResult {}
    record Suspended(long id) implements UpgradeResult {}
    record PaymentRequired(long id) implements UpgradeResult {}
}
