package com.acme.orderquestionnaire.adapters.out.mongo.config;

import com.acme.shared.TenantContextHolder;
import com.mongodb.ClientSessionOptions;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import org.bson.codecs.configuration.CodecRegistry;
import org.springframework.dao.support.PersistenceExceptionTranslator;
import org.springframework.data.mongodb.MongoDatabaseFactory;

public class MultiTenantMongoDbFactory implements MongoDatabaseFactory {
    private final MongoClient client;
    private final String dbPrefix;

    public MultiTenantMongoDbFactory(MongoClient client, String dbPrefix) {
        this.client = client;
        this.dbPrefix = dbPrefix;
    }

    @Override
    public MongoDatabase getMongoDatabase() {
        var dbName = dbPrefix + "_" + TenantContextHolder.currentTenant();
        return client.getDatabase(dbName);
    }

    @Override
    public MongoDatabase getMongoDatabase(String dbName) {
        return client.getDatabase(dbName);
    }

    @Override
    public PersistenceExceptionTranslator getExceptionTranslator() {
        return null;
    }

    @Override
    public CodecRegistry getCodecRegistry() {
        return MongoDatabaseFactory.super.getCodecRegistry();
    }

    @Override
    public ClientSession getSession(ClientSessionOptions options) {
        return null;
    }

    @Override
    public MongoDatabaseFactory withSession(ClientSession session) {
        return null;
    }
}

