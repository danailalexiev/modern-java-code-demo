package com.example.accounts.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "audit_log")
public class AuditEntryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private long userId;

    private String action;

    private String detail;

    private Instant recordedAt;

    protected AuditEntryEntity() {
    }

    public AuditEntryEntity(long userId, String action, String detail, Instant recordedAt) {
        this.userId = userId;
        this.action = action;
        this.detail = detail;
        this.recordedAt = recordedAt;
    }

    public Long getId() {
        return id;
    }

    public long getUserId() {
        return userId;
    }

    public String getAction() {
        return action;
    }

    public String getDetail() {
        return detail;
    }

    public Instant getRecordedAt() {
        return recordedAt;
    }
}
