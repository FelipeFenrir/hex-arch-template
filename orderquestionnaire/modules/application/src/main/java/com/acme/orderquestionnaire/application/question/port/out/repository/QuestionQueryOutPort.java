package com.acme.orderquestionnaire.application.question.port.out.repository;

import com.acme.orderquestionnaire.application.question.dto.queries.GetQuestionById;
import com.acme.orderquestionnaire.application.question.dto.queries.SearchQuestionByFilter;
import com.acme.orderquestionnaire.application.question.dto.view.QuestionView;
import com.acme.shared.engine.pagination.PageResult;
import com.acme.shared.stereotypes.core.OutputPort;

import java.util.Optional;

@OutputPort
public interface QuestionQueryOutPort {

    Optional<QuestionView> findById(GetQuestionById id);

    PageResult<QuestionView> findAll(SearchQuestionByFilter criteria);
}

