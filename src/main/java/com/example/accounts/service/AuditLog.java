package com.example.accounts.service;

import com.example.accounts.domain.Plan;
import com.example.accounts.persistence.AuditEntryEntity;
import com.example.accounts.persistence.AuditEntryRepository;
import java.time.Instant;
import org.springframework.stereotype.Component;

@Component
public class AuditLog {

    private final AuditEntryRepository repo;

    public AuditLog(AuditEntryRepository repo) {
        this.repo = repo;
    }

    public void record(long userId, String action, Plan plan) {
        repo.save(new AuditEntryEntity(userId, action, String.valueOf(plan), Instant.now()));
    }
}
