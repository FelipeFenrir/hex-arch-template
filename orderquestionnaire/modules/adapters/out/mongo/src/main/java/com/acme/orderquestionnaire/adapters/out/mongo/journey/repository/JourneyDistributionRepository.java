package com.acme.orderquestionnaire.adapters.out.mongo.journey.repository;

import com.acme.orderquestionnaire.adapters.out.mongo.journey.entity.JourneyDistributionEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JourneyDistributionRepository extends MongoRepository<JourneyDistributionEntity, String> {
}

