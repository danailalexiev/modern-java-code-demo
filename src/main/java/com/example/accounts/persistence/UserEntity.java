package com.example.accounts.persistence;

import com.example.accounts.domain.Plan;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "users")
public class UserEntity {

    @Id
    private Long id;

    private String email;

    @Enumerated(EnumType.STRING)
    private Plan plan;

    private boolean suspended;

    private boolean paymentMethodOnFile;

    // Nullable, ad hoc state fields at the JPA boundary -- exactly what Account
    // (the domain model) replaces with illegal-states-unrepresentable records.
    private Instant suspendedSince;

    private String suspendedReason;

    private String verificationToken;

    private Instant verificationExpiresAt;

    protected UserEntity() {
    }

    public UserEntity(Long id, String email, Plan plan, boolean suspended, boolean paymentMethodOnFile) {
        this.id = id;
        this.email = email;
        this.plan = plan;
        this.suspended = suspended;
        this.paymentMethodOnFile = paymentMethodOnFile;
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public Plan getPlan() {
        return plan;
    }

    public void setPlan(Plan plan) {
        this.plan = plan;
    }

    public boolean isSuspended() {
        return suspended;
    }

    public boolean isPaymentMethodOnFile() {
        return paymentMethodOnFile;
    }

    public Instant getSuspendedSince() {
        return suspendedSince;
    }

    public String getSuspendedReason() {
        return suspendedReason;
    }

    public String getVerificationToken() {
        return verificationToken;
    }

    public Instant getVerificationExpiresAt() {
        return verificationExpiresAt;
    }
}
