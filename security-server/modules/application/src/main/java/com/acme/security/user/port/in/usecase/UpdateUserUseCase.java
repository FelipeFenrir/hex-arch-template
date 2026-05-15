package com.acme.security.user.port.in.usecase;

import com.acme.security.user.User;
import com.acme.security.user.dto.command.UpdateUserCommand;
import com.acme.shared.pattern.result.DomainError;
import com.acme.shared.pattern.result.Result;
import com.acme.shared.stereotypes.core.UseCase;

@UseCase
public interface UpdateUserUseCase {
    Result<User, DomainError> execute(UpdateUserCommand command);
}

