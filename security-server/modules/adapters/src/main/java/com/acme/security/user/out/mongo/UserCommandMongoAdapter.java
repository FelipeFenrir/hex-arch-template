package com.acme.security.user.out.mongo;

import com.acme.security.user.out.mongo.document.UserDocument;
import com.acme.security.user.out.mongo.repository.MongoUserRepository;
import com.acme.security.security.port.out.engine.PasswordEncoderPort;
import com.acme.security.user.port.out.repository.UserCommandOutPort;
import com.acme.security.user.User;
import com.acme.shared.stereotypes.adapter.OutputAdapter;
import com.acme.shared.vo.TenantId;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;

@Component
@OutputAdapter
public class UserCommandMongoAdapter implements UserCommandOutPort {

    private final MongoUserRepository mongoUserRepository;

    public UserCommandMongoAdapter(MongoUserRepository mongoUserRepository) {
        this.mongoUserRepository = Objects.requireNonNull(mongoUserRepository,
                "mongoUserRepository must not be null");
    }

    @Override
    public User save(User user) {
        var userDocument = UserDocument.of(user);
        return UserDocument.map(mongoUserRepository.save(userDocument));
    }

    @Override
    public Optional<User> findByUsernameAndTenant(String username, TenantId tenantId) {
        return mongoUserRepository
                .findByUsernameAndTenantId(username, tenantId.stringValue())
                .map(UserDocument::map);
    }
}
