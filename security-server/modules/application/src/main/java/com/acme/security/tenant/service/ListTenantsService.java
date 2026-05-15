package com.acme.security.tenant.service;

import com.acme.security.common.pagination.PageResultSupport;
import com.acme.security.tenant.dto.view.TenantView;
import com.acme.security.tenant.port.in.usecase.ListTenantsUseCase;
import com.acme.security.tenant.port.out.repository.TenantCommandOutPort;
import com.acme.shared.engine.pagination.HybridPageRequest;
import com.acme.shared.engine.pagination.PageResult;

import java.util.Objects;

public class ListTenantsService implements ListTenantsUseCase {

    private final TenantCommandOutPort tenantCommandOutPort;

    public ListTenantsService(TenantCommandOutPort tenantCommandOutPort) {
        this.tenantCommandOutPort = Objects.requireNonNull(tenantCommandOutPort,
                "tenantCommandOutPort must not be null");
    }

    @Override
    public PageResult<TenantView> list(HybridPageRequest pageRequest) {
        PageResult<com.acme.security.tenant.Tenant> page = tenantCommandOutPort.findPage(pageRequest);
        return PageResultSupport.map(page, TenantView::from);
    }
}

