package com.example.accounts.persistence;

import com.example.accounts.domain.Account;
import com.example.accounts.domain.Email;
import com.example.accounts.domain.UserId;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * Maps the {@link UserEntity} row -- which still allows nonsensical combinations
 * of suspended/verification fields -- onto the {@link Account} sealed hierarchy,
 * which doesn't. Records can't be JPA entities, so this adapter is the boundary.
 */
@Component
public class JpaAccountRepositoryAdapter implements AccountRepository {

    private final UserRepository users;

    public JpaAccountRepositoryAdapter(UserRepository users) {
        this.users = users;
    }

    @Override
    public Optional<Account> find(UserId id) {
        return users.findById(id.value()).map(JpaAccountRepositoryAdapter::toDomain);
    }

    @Override
    public Account save(Account account) {
        UserEntity entity = users.findById(account.id().value())
            .orElseThrow(() -> new IllegalStateException("No row for " + account.id()));
        if (account instanceof Account.Active active) {
            entity.setPlan(active.plan());
        }
        return toDomain(users.save(entity));
    }

    private static Account toDomain(UserEntity entity) {
        UserId id = new UserId(entity.getId());
        Email email = new Email(entity.getEmail());
        if (entity.isSuspended()) {
            return new Account.Suspended(id, email, entity.getSuspendedSince(), entity.getSuspendedReason());
        }
        if (entity.getVerificationToken() != null) {
            return new Account.PendingVerification(
                id, email, entity.getVerificationToken(), entity.getVerificationExpiresAt());
        }
        return new Account.Active(id, email, entity.getPlan());
    }
}
