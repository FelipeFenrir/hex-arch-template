package com.acme.security.client.port.in.usecase;

import com.acme.security.client.dto.view.ClientView;
import com.acme.shared.engine.pagination.HybridPageRequest;
import com.acme.shared.engine.pagination.PageResult;
import com.acme.shared.stereotypes.core.UseCase;

@UseCase
public interface ListClientsUseCase {
    PageResult<ClientView> listByTenant(String tenantId, HybridPageRequest pageRequest);
}

