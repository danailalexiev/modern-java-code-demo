package com.example.accounts.service;

import com.example.accounts.domain.Plan;
import com.example.accounts.exception.UserNotFoundException;
import com.example.accounts.persistence.UserEntity;
import com.example.accounts.persistence.UserRepository;
import com.example.accounts.service.UpgradeResult.NotFound;
import com.example.accounts.service.UpgradeResult.PaymentRequired;
import com.example.accounts.service.UpgradeResult.Suspended;
import com.example.accounts.service.UpgradeResult.Upgraded;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository repo;
    private final BillingClient billing;

    public UserService(UserRepository repo, BillingClient billing) {
        this.repo = repo;
        this.billing = billing;
    }

    public UserEntity findById(long id) {
        return repo.findById(id).orElseThrow(() -> new UserNotFoundException(id));
    }

    public UpgradeResult upgrade(long id, Plan plan) {
        return repo.findById(id)
            .<UpgradeResult>map(user -> {
                if (user.isSuspended()) {
                    return new Suspended(id);
                }
                if (!billing.hasValidPaymentMethod(user)) {
                    return new PaymentRequired(id);
                }
                user.setPlan(plan);
                return new Upgraded(repo.save(user));
            })
            .orElseGet(() -> new NotFound(id));
    }
}
