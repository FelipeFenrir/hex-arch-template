package com.acme.security.client.out.mongo.document;

import com.acme.security.client.Client;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Set;

@Document(collection = "clients")
// Garante que o clientId seja único apenas dentro do mesmo tenant
@CompoundIndex(name = "client_tenant_idx", def = "{'clientId': 1, 'tenantId': 1}", unique = true)
public record ClientDocument(
        @Id
        String id,
        String tenantId,
        String clientId,
        String clientSecret,
        Set<String> redirectUris,
        Set<String> scopes,
        Set<String>grantTypes,
        boolean active
) {
    public static ClientDocument of(Client client) {
        return new ClientDocument(
                client.idValue(),
                client.tenantValue(),
                client.clientId(),
                client.clientSecret(),
                client.redirectUris(),
                client.scopes(),
                client.grantTypes(),
                client.isActive()
        );
    }

    public static Client map(ClientDocument clientDocument) {
        return Client.rehydrate(
                clientDocument.id(),
                clientDocument.tenantId(),
                clientDocument.clientId(),
                clientDocument.clientSecret(),
                clientDocument.redirectUris(),
                clientDocument.scopes(),
                clientDocument.grantTypes(),
                clientDocument.active()
        );
    }
}
