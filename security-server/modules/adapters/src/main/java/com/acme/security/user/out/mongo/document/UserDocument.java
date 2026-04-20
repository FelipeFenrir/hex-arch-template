package com.acme.security.user.out.mongo.document;

import com.acme.security.user.User;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Set;

@Document(collection = "users")
// Garante que o username seja único apenas dentro do mesmo tenant
@CompoundIndex(name = "user_tenant_idx", def = "{'username': 1, 'tenantId': 1}", unique = true)
public record UserDocument (
        @Id
        String id,
        String tenantId,
        String username,
        String password,
        Set<String> roles,
        boolean active
    )
{
    public static UserDocument of(User user) {
        return new UserDocument(
                user.idValue(),
                user.tenantValue(),
                user.username(),
                user.password(),
                user.roles(),
                user.isActive()
        );
    }

    public static User map(UserDocument userDocument) {
        return User.rehydrate(
                userDocument.id(),
                userDocument.tenantId(),
                userDocument.username(),
                userDocument.password(),
                userDocument.roles(),
                userDocument.active()
        );
    }
}
