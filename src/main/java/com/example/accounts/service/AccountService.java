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
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

@Service
public class AccountService {

    private final AccountRepository accounts;
    private final BillingClient billing;
    private final AuditLog audit;
    private final TransactionTemplate tx;

    public AccountService(
            AccountRepository accounts,
            BillingClient billing,
            AuditLog audit,
            PlatformTransactionManager transactionManager) {
        this.accounts = accounts;
        this.billing = billing;
        this.audit = audit;
        this.tx = new TransactionTemplate(transactionManager);
    }

    public Account findById(UserId id) {
        return accounts.find(id).orElseThrow(() -> new UserNotFoundException(id.value()));
    }

    /** The fix: every check runs before the first write, so a rejected upgrade writes nothing. */
    @Transactional
    public Result<UpgradeError, Active> upgrade(UserId id, Plan plan) {
        return Result.<UpgradeError, Account>fromOptional(accounts.find(id), () -> new NotFound(id))
            .flatMap(this::requireActive)
            .flatMap(this::requirePaymentMethod)
            .map(active -> {
                audit.record(id.value(), "upgrade-attempt", plan);
                return accounts.save(active.withPlan(plan));
            });
    }

    /**
     * Alternative fix for when checks can't be ordered before every write: do the
     * write-first version anyway, but roll back the whole transaction -- audit
     * write included -- whenever the result is an {@code Err}.
     */
    public Result<UpgradeError, Active> upgradeWithRollbackOnError(UserId id, Plan plan) {
        return tx.execute(status -> {
            Result<UpgradeError, Active> result = doUpgradeAuditFirst(id, plan);
            if (result instanceof Result.Err<?, ?>) {
                status.setRollbackOnly();
            }
            return result;
        });
    }

    private Result<UpgradeError, Active> doUpgradeAuditFirst(UserId id, Plan plan) {
        audit.record(id.value(), "upgrade-attempt", plan); // write happens first
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
