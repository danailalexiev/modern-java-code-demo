package com.example.accounts.service;

import com.example.accounts.domain.Account;
import com.example.accounts.domain.Account.Active;
import com.example.accounts.domain.Account.PendingVerification;
import com.example.accounts.domain.Account.Suspended;
import com.example.accounts.domain.Plan;
import com.example.accounts.domain.UserId;
import com.example.accounts.exception.UserNotFoundException;
import com.example.accounts.persistence.AccountRepository;
import com.example.accounts.service.UpgradeError.AccountSuspended;
import com.example.accounts.service.UpgradeError.NotFound;
import com.example.accounts.service.UpgradeError.NotVerified;
import com.example.accounts.service.UpgradeError.PaymentRequired;
import org.springframework.stereotype.Service;

@Service
public class AccountService {

    private final AccountRepository accounts;
    private final BillingClient billing;

    public AccountService(AccountRepository accounts, BillingClient billing) {
        this.accounts = accounts;
        this.billing = billing;
    }

    public Account findById(UserId id) {
        return accounts.find(id).orElseThrow(() -> new UserNotFoundException(id.value()));
    }

    public Result<UpgradeError, Active> upgrade(UserId id, Plan plan) {
        return Result.<UpgradeError, Account>fromOptional(accounts.find(id), () -> new NotFound(id))
            .flatMap(this::requireActive)
            .flatMap(this::requirePaymentMethod)
            .map(active -> accounts.save(active.withPlan(plan)));
    }

    private Result<UpgradeError, Active> requireActive(Account account) {
        return switch (account) {
            case Active a              -> Result.ok(a);
            case Suspended s           -> Result.err(new AccountSuspended(s.id(), s.since()));
            case PendingVerification p -> Result.err(new NotVerified(p.id()));
        };
    }

    private Result<UpgradeError, Active> requirePaymentMethod(Active active) {
        return billing.hasValidPaymentMethod(active.id())
            ? Result.ok(active)
            : Result.err(new PaymentRequired(active.id()));
    }
}
