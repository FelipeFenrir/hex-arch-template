package com.acme.security.user.port.out.repository;

import com.acme.security.user.User;
import com.acme.shared.engine.pagination.HybridPageRequest;
import com.acme.shared.engine.pagination.PageResult;
import com.acme.shared.stereotypes.core.OutputPort;
import com.acme.shared.vo.TenantId;

import java.util.Optional;

@OutputPort
public interface UserCommandOutPort {
    User save(User user);
    Optional<User> findByUsernameAndTenant(String username, TenantId tenantId);
    Optional<User> findByIdAndTenant(String id, TenantId tenantId);
    PageResult<User> findPageByTenant(TenantId tenantId, HybridPageRequest pageRequest);
    boolean existsByIdAndTenant(String id, TenantId tenantId);
    void deleteByIdAndTenant(String id, TenantId tenantId);
}
