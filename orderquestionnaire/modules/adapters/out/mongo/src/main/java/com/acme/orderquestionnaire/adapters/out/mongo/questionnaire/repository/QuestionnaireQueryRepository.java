package com.acme.orderquestionnaire.adapters.out.mongo.questionnaire.repository;

import com.acme.orderquestionnaire.adapters.out.mongo.questionnaire.entity.QuestionnaireEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuestionnaireQueryRepository extends MongoRepository<QuestionnaireEntity, String> {
}

