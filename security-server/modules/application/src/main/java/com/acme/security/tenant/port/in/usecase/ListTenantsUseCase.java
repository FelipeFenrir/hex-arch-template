package com.acme.security.tenant.port.in.usecase;

import com.acme.security.tenant.dto.view.TenantView;
import com.acme.shared.engine.pagination.HybridPageRequest;
import com.acme.shared.engine.pagination.PageResult;
import com.acme.shared.stereotypes.core.UseCase;

@UseCase
public interface ListTenantsUseCase {
    PageResult<TenantView> list(HybridPageRequest pageRequest);
}

