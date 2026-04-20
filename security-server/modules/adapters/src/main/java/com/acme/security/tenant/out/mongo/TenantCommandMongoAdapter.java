package com.acme.security.tenant.out.mongo;

import com.acme.security.tenant.out.mongo.repository.MongoTenantRepository;
import com.acme.security.tenant.port.out.repository.TenantCommandOutPort;
import com.acme.security.tenant.Tenant;
import com.acme.shared.stereotypes.adapter.OutputAdapter;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;

@Component
@OutputAdapter
public class TenantCommandMongoAdapter implements TenantCommandOutPort {
    private final MongoTenantRepository mongoTenantRepository;

    public TenantCommandMongoAdapter(MongoTenantRepository mongoTenantRepository) {
        this.mongoTenantRepository = Objects.requireNonNull(mongoTenantRepository,
                "mongoTenantRepository must not be null");
    }

    @Override
    public Optional<Tenant> findBySlug(String slug) {
        return mongoTenantRepository.findBySlug(slug)
                .map(doc -> Tenant.rehydrate(doc.id(), doc.name(), doc.slug(), doc.active()));
    }
}
