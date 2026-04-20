package com.acme.security.tenant.port.out.repository;

import com.acme.security.tenant.Tenant;
import com.acme.shared.stereotypes.core.OutputPort;

import java.util.Optional;

@OutputPort
public interface TenantCommandOutPort {
    Optional<Tenant> findBySlug(String slug);
}
