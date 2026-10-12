package com.example.accounts.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.accounts.domain.Account;
import com.example.accounts.domain.Plan;
import com.example.accounts.domain.UserId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import(JpaAccountRepositoryAdapter.class)
class JpaAccountRepositoryAdapterTest {

    @Autowired
    private AccountRepository accounts;

    @Test
    void mapsAnActiveUserRow() {
        Account account = accounts.find(new UserId(1)).orElseThrow();

        assertThat(account).isInstanceOf(Account.Active.class);
        assertThat(account.email().value()).isEqualTo("alice@example.com");
    }

    @Test
    void mapsASuspendedUserRowWithoutLosingTheSuspensionData() {
        Account account = accounts.find(new UserId(3)).orElseThrow();

        assertThat(account).isInstanceOf(Account.Suspended.class);
    }

    @Test
    void savingAnActiveAccountPersistsItsPlan() {
        Account.Active active = (Account.Active) accounts.find(new UserId(1)).orElseThrow();

        accounts.save(active.withPlan(Plan.PRO));

        Account reloaded = accounts.find(new UserId(1)).orElseThrow();
        assertThat(((Account.Active) reloaded).plan()).isEqualTo(Plan.PRO);
    }
}
