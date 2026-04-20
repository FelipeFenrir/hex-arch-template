package com.acme.orderquestionnaire.application.questionnaire.service;

import com.acme.orderquestionnaire.application.common.QueryHandler;
import com.acme.orderquestionnaire.application.questionnaire.port.out.repository.QuestionnaireQueryOutPort;
import com.acme.orderquestionnaire.application.questionnaire.dto.queries.SearchQuestionnaireByFilter;
import com.acme.orderquestionnaire.application.questionnaire.dto.view.QuestionnaireView;
import com.acme.shared.engine.pagination.PageResult;

import java.util.Objects;

public class SearchQuestionnaireByFilterHandler implements
        QueryHandler<SearchQuestionnaireByFilter, PageResult<QuestionnaireView>> {

    private final QuestionnaireQueryOutPort questionnaireRepository;

    public SearchQuestionnaireByFilterHandler(QuestionnaireQueryOutPort questionnaireRepository) {
        this.questionnaireRepository = Objects.requireNonNull(questionnaireRepository,
                "questionnaireRepository must not be null");
    }

    @Override
    public PageResult<QuestionnaireView> execute(SearchQuestionnaireByFilter query) {
        return questionnaireRepository.findAll(query);
    }
}
