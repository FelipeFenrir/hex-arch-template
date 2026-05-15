package com.acme.security.user.service;

import com.acme.security.common.pagination.PageResultSupport;
import com.acme.security.user.dto.view.UserView;
import com.acme.security.user.port.in.usecase.ListUsersUseCase;
import com.acme.security.user.port.out.repository.UserCommandOutPort;
import com.acme.shared.engine.pagination.HybridPageRequest;
import com.acme.shared.engine.pagination.PageResult;
import com.acme.shared.vo.TenantId;

import java.util.Objects;

public class ListUsersService implements ListUsersUseCase {

    private final UserCommandOutPort userCommandOutPort;

    public ListUsersService(UserCommandOutPort userCommandOutPort) {
        this.userCommandOutPort = Objects.requireNonNull(userCommandOutPort,
                "userCommandOutPort must not be null");
    }

    @Override
    public PageResult<UserView> listByTenant(String tenantId, HybridPageRequest pageRequest) {
        PageResult<com.acme.security.user.User> page = userCommandOutPort.findPageByTenant(
                TenantId.fromString(tenantId),
                pageRequest
        );
        return PageResultSupport.map(page, UserView::from);
    }
}

