package com.example.accounts.api;

import com.example.accounts.domain.Account;
import com.example.accounts.domain.Plan;

public record AccountDto(long id, String email, Plan plan) {

    public static AccountDto from(Account account) {
        return switch (account) {
            case Account.Active a              -> new AccountDto(a.id().value(), a.email().value(), a.plan());
            case Account.Suspended s           -> new AccountDto(s.id().value(), s.email().value(), null);
            case Account.PendingVerification p -> new AccountDto(p.id().value(), p.email().value(), null);
        };
    }
}
