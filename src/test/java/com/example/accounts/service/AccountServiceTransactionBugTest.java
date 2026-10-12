package com.example.accounts.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.accounts.domain.Plan;
import com.example.accounts.domain.UserId;
import com.example.accounts.persistence.AuditEntryRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Demonstrates the bug: {@code audit.record} happens before the checks inside
 * a {@code @Transactional} method, so a rejected upgrade still commits the
 * audit row. {@code @Transactional} only rolls back on unchecked exceptions;
 * returning an {@code Err} value looks like a normal return to Spring.
 */
@SpringBootTest
class AccountServiceTransactionBugTest {

    @Autowired
    private AccountService service;

    @Autowired
    private AuditEntryRepository auditEntries;

    @Test
    void aRejectedUpgradeStillCommitsTheAuditRow() {
        long before = auditEntries.count();

        Result<UpgradeError, ?> result = service.upgrade(new UserId(3), Plan.PRO); // user 3 is suspended

        assertThat(result).isInstanceOf(Result.Err.class);
        assertThat(auditEntries.count()).isEqualTo(before + 1);
    }
}
