package com.acme.orderquestionnaire.application.questionnaire.service;

import com.acme.orderquestionnaire.application.common.QueryHandler;
import com.acme.orderquestionnaire.application.questionnaire.port.out.repository.QuestionnaireQueryOutPort;
import com.acme.orderquestionnaire.application.questionnaire.dto.queries.GetQuestionnaireById;
import com.acme.orderquestionnaire.application.questionnaire.dto.view.QuestionnaireView;

import java.util.Objects;
import java.util.Optional;

public class GetQuestionnaireByIdHandler implements
        QueryHandler<GetQuestionnaireById, Optional<QuestionnaireView>> {

    private final QuestionnaireQueryOutPort questionnaireRepository;

    public GetQuestionnaireByIdHandler(QuestionnaireQueryOutPort questionnaireRepository) {
        this.questionnaireRepository = Objects.requireNonNull(questionnaireRepository,
                "questionnaireRepository must not be null");
    }

    @Override
    public Optional<QuestionnaireView> execute(GetQuestionnaireById query) {
        return questionnaireRepository.findById(query);
    }
}
