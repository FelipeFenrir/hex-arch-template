package com.acme.security.client.service;

import com.acme.security.client.Client;
import com.acme.security.client.dto.view.ClientView;
import com.acme.security.client.port.in.usecase.ListClientsUseCase;
import com.acme.security.client.port.out.repository.ClientCommandOutPort;
import com.acme.security.common.pagination.PageResultSupport;
import com.acme.shared.engine.pagination.HybridPageRequest;
import com.acme.shared.engine.pagination.PageResult;
import com.acme.shared.vo.TenantId;

import java.util.Objects;

public class ListClientsService implements ListClientsUseCase {

    private final ClientCommandOutPort clientCommandOutPort;

    public ListClientsService(ClientCommandOutPort clientCommandOutPort) {
        this.clientCommandOutPort = Objects.requireNonNull(clientCommandOutPort,
                "clientCommandOutPort must not be null");
    }

    @Override
    public PageResult<ClientView> listByTenant(String tenantId, HybridPageRequest pageRequest) {
        PageResult<Client> page = clientCommandOutPort.findPageByTenant(TenantId.fromString(tenantId), pageRequest);
        return PageResultSupport.map(page, ClientView::from);
    }
}

