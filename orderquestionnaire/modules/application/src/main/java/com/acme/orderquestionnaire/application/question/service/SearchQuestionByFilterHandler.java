package com.acme.orderquestionnaire.application.question.service;

import com.acme.orderquestionnaire.application.common.QueryHandler;
import com.acme.orderquestionnaire.application.question.dto.queries.SearchQuestionByFilter;
import com.acme.orderquestionnaire.application.question.port.out.repository.QuestionQueryOutPort;
import com.acme.orderquestionnaire.application.question.dto.view.QuestionView;
import com.acme.shared.engine.pagination.PageResult;

import java.util.Objects;

public class SearchQuestionByFilterHandler implements QueryHandler<SearchQuestionByFilter, PageResult<QuestionView>> {

    private final QuestionQueryOutPort questionRepository;

    public SearchQuestionByFilterHandler(QuestionQueryOutPort questionRepository) {
        this.questionRepository = Objects.requireNonNull(questionRepository,
                "questionRepository must not be null");
    }

    @Override
    public PageResult<QuestionView> execute(SearchQuestionByFilter query) {
        return questionRepository.findAll(query);
    }
}
