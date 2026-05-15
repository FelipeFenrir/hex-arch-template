package com.acme.security.user.port.in.usecase;

import com.acme.security.user.dto.view.UserView;
import com.acme.shared.engine.pagination.HybridPageRequest;
import com.acme.shared.engine.pagination.PageResult;
import com.acme.shared.stereotypes.core.UseCase;

@UseCase
public interface ListUsersUseCase {
    PageResult<UserView> listByTenant(String tenantId, HybridPageRequest pageRequest);
}

