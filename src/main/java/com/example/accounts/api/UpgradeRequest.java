package com.example.accounts.api;

import com.example.accounts.domain.Plan;
import jakarta.validation.constraints.NotNull;

public record UpgradeRequest(@NotNull Plan plan) {
}
