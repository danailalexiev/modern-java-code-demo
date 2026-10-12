package com.example.accounts.persistence;

import com.example.accounts.domain.Plan;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

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
}
