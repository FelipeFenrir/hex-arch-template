package com.acme.orderquestionnaire.application.questionnaire.port.out.repository;

import com.acme.orderquestionnaire.application.questionnaire.dto.queries.GetQuestionnaireById;
import com.acme.orderquestionnaire.application.questionnaire.dto.queries.SearchQuestionnaireByFilter;
import com.acme.orderquestionnaire.application.questionnaire.dto.view.QuestionnaireView;
import com.acme.shared.engine.pagination.PageResult;
import com.acme.shared.stereotypes.core.OutputPort;

import java.util.Optional;

@OutputPort
public interface QuestionnaireQueryOutPort {
    Optional<QuestionnaireView> findById(GetQuestionnaireById id);

    PageResult<QuestionnaireView> findAll(SearchQuestionnaireByFilter criteria);
}
