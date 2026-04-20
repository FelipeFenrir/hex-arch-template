package com.acme.orderquestionnaire.application.question.port.out.repository;

import com.acme.orderquestionnaire.domain.question.Question;
import com.acme.shared.pattern.result.DomainError;
import com.acme.shared.pattern.result.Result;
import com.acme.shared.stereotypes.core.OutputPort;

import java.util.Optional;
import java.util.List;

@OutputPort
public interface QuestionCommandOutPort {

    Result<Question, List<DomainError>> create(Question entry);

    Result<Question, List<DomainError>> update(Question question);

    boolean existsById(String id);

    Optional<Question> findQuestionById(String id);

    Result<Void, List<DomainError>> deleteById(String id);

    Result<Integer, List<DomainError>> deleteAllByIds(List<String> ids);
}


