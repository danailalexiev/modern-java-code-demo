package com.example.accounts.persistence;

import com.example.accounts.domain.Account;
import com.example.accounts.domain.UserId;
import java.util.Optional;

public interface AccountRepository {

    Optional<Account> find(UserId id);

    Account save(Account account);
}
