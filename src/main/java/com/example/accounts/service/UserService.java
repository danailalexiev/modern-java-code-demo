package com.example.accounts.service;

import com.example.accounts.domain.Plan;
import com.example.accounts.exception.UserNotFoundException;
import com.example.accounts.exception.UserSuspendedException;
import com.example.accounts.persistence.UserEntity;
import com.example.accounts.persistence.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository repo;
    private final AuditLog audit;

    public UserService(UserRepository repo, AuditLog audit) {
        this.repo = repo;
        this.audit = audit;
    }

    public UserEntity findById(long id) {
        return repo.findById(id).orElseThrow(() -> new UserNotFoundException(id));
    }

    @Transactional
    public UserEntity upgrade(long id, Plan plan) {
        UserEntity user = findById(id);
        audit.record(id, "upgrade-attempt", plan);
        if (user.isSuspended()) {
            throw new UserSuspendedException(id);
        }
        user.setPlan(plan);
        return repo.save(user);
    }
}
