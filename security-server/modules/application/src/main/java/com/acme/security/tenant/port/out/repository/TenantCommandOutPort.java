package com.acme.security.tenant.port.out.repository;

import com.acme.security.tenant.Tenant;
import com.acme.shared.engine.pagination.HybridPageRequest;
import com.acme.shared.engine.pagination.PageResult;
import com.acme.shared.stereotypes.core.OutputPort;

import java.util.Optional;

@OutputPort
public interface TenantCommandOutPort {
    Tenant save(Tenant tenant);
    Optional<Tenant> findById(String id);
    Optional<Tenant> findBySlug(String slug);
    PageResult<Tenant> findPage(HybridPageRequest pageRequest);
    boolean existsBySlug(String slug);
    void deleteById(String id);
}
