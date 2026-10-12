package com.example.accounts.api;

import com.example.accounts.domain.Plan;
import com.example.accounts.persistence.UserEntity;

public record UserDto(long id, String email, Plan plan, boolean suspended) {

    public static UserDto from(UserEntity user) {
        return new UserDto(user.getId(), user.getEmail(), user.getPlan(), user.isSuspended());
    }
}
