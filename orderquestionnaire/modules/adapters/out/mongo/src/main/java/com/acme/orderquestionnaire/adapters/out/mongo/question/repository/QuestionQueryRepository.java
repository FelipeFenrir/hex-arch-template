package com.acme.orderquestionnaire.adapters.out.mongo.question.repository;

import com.acme.orderquestionnaire.adapters.out.mongo.question.entity.QuestionEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuestionQueryRepository extends MongoRepository<QuestionEntity, String> {
}

