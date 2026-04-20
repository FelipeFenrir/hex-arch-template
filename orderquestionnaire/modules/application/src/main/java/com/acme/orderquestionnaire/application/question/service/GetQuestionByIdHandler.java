package com.acme.orderquestionnaire.application.question.service;

import com.acme.orderquestionnaire.application.common.QueryHandler;
import com.acme.orderquestionnaire.application.question.dto.queries.GetQuestionById;
import com.acme.orderquestionnaire.application.question.port.out.repository.QuestionQueryOutPort;
import com.acme.orderquestionnaire.application.question.dto.view.QuestionView;

import java.util.Objects;
import java.util.Optional;

public class GetQuestionByIdHandler implements QueryHandler<GetQuestionById, Optional<QuestionView>> {

    private final QuestionQueryOutPort questionRepository;

    public GetQuestionByIdHandler(QuestionQueryOutPort questionRepository) {
        this.questionRepository = Objects.requireNonNull(questionRepository,
                "questionRepository must not be null");
    }

    @Override
    public Optional<QuestionView> execute(GetQuestionById query) {
        if (query.id() == null || query.id().isBlank()) {
            return Optional.empty();
        }
        return questionRepository.findById(query);
    }
}
