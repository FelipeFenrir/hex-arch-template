package com.acme.security.user.errors;

import com.acme.shared.pattern.result.DomainError;

public final class UserDomainErrors {
    private UserDomainErrors() {
        throw new IllegalStateException("Utility class");
    }

    public static DomainError invalidPassword() {
        return new DomainError("INVALID_PASSWORD", "Invalid password.");
    }

    public static DomainError samePassword() {
        return new DomainError("SAME_PASSWORD", "The new password cannot be the same as the current password.");
    }

    public static DomainError usernameAlreadyExists() {
        return new DomainError("USERNAME_ALREADY_EXISTS", "Username already exists.");
    }

    public static DomainError userNotFound() {
        return new DomainError("USER_NOT_FOUND", "User not found.");
    }
}
