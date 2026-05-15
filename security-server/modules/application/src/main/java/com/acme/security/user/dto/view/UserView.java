package com.acme.security.user.dto.view;

import com.acme.security.user.User;

import java.util.Set;

public record UserView(
        String id,
        String tenantId,
        String username,
        Set<String> roles,
        boolean active
) {
    public static UserView from(User user) {
        return new UserView(
                user.idValue(),
                user.tenantValue(),
                user.username(),
                user.roles(),
                user.isActive()
        );
    }
}

