package com.example.accounts.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.accounts.domain.Plan;
import com.example.accounts.domain.UserId;
import com.example.accounts.persistence.AuditEntryRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * The fix for step-4-tx-bug: a rejected upgrade must not leave an audit row
 * behind. {@code upgrade} orders every check before its first write.
 * {@code upgradeWithRollbackOnError} writes audit first but rolls the whole
 * transaction back -- audit write included -- whenever the result is an
 * {@code Err}. Both must behave the same from the caller's point of view.
 */
@SpringBootTest
class AccountServiceTransactionTest {

    @Autowired
    private AccountService service;

    @Autowired
    private AuditEntryRepository auditEntries;

    @Test
    void checksFirstDoesNotAuditARejectedUpgrade() {
        long before = auditEntries.count();

        Result<UpgradeError, ?> result = service.upgrade(new UserId(3), Plan.PRO); // suspended

        assertThat(result).isInstanceOf(Result.Err.class);
        assertThat(auditEntries.count()).isEqualTo(before);
    }

    @Test
    void checksFirstStillAuditsASuccessfulUpgrade() {
        long before = auditEntries.count();

        Result<UpgradeError, ?> result = service.upgrade(new UserId(1), Plan.PRO); // active + payment on file

        assertThat(result).isInstanceOf(Result.Ok.class);
        assertThat(auditEntries.count()).isEqualTo(before + 1);
    }

    @Test
    void rollbackOnErrorDoesNotAuditARejectedUpgrade() {
        long before = auditEntries.count();

        Result<UpgradeError, ?> result = service.upgradeWithRollbackOnError(new UserId(2), Plan.PRO); // no payment

        assertThat(result).isInstanceOf(Result.Err.class);
        assertThat(auditEntries.count()).isEqualTo(before);
    }

    @Test
    void rollbackOnErrorStillAuditsASuccessfulUpgrade() {
        long before = auditEntries.count();

        Result<UpgradeError, ?> result = service.upgradeWithRollbackOnError(new UserId(1), Plan.ENTERPRISE);

        assertThat(result).isInstanceOf(Result.Ok.class);
        assertThat(auditEntries.count()).isEqualTo(before + 1);
    }
}
