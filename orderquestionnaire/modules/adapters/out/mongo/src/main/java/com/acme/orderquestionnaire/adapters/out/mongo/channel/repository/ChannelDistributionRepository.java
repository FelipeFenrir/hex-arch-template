package com.acme.orderquestionnaire.adapters.out.mongo.channel.repository;

import com.acme.orderquestionnaire.adapters.out.mongo.channel.entity.ChannelDistributionEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChannelDistributionRepository extends MongoRepository<ChannelDistributionEntity, String> {
}

