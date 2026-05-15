package com.acme.security.user.in.dto.response;

import com.acme.security.user.dto.view.UserView;

import java.util.Set;

public record UserResponse(
        String id,
        String tenantId,
        String username,
        Set<String> roles,
        boolean active
) {
    public static UserResponse from(UserView view) {
        return new UserResponse(
                view.id(),
                view.tenantId(),
                view.username(),
                view.roles(),
                view.active()
        );
    }
}

