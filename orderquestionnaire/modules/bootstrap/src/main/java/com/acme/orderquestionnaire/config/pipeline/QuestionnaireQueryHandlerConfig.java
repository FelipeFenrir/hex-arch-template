package com.acme.orderquestionnaire.config.pipeline;

import com.acme.orderquestionnaire.application.common.QueryHandler;
import com.acme.orderquestionnaire.application.questionnaire.dto.queries.GetQuestionnaireById;
import com.acme.orderquestionnaire.application.questionnaire.dto.queries.SearchQuestionnaireByFilter;
import com.acme.orderquestionnaire.application.questionnaire.dto.view.QuestionnaireView;
import com.acme.orderquestionnaire.application.questionnaire.port.out.repository.QuestionnaireQueryOutPort;
import com.acme.orderquestionnaire.application.questionnaire.service.GetQuestionnaireByIdHandler;
import com.acme.orderquestionnaire.application.questionnaire.service.SearchQuestionnaireByFilterHandler;
import com.acme.shared.engine.pagination.PageResult;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Optional;

@Configuration
public class QuestionnaireQueryHandlerConfig {

    @Bean(name = "getQuestionnaireByIdQueryHandler")
    public QueryHandler<GetQuestionnaireById, Optional<QuestionnaireView>> getQuestionnaireByIdQueryHandler(
            QuestionnaireQueryOutPort questionnaireQueryOutPort) {
        return new GetQuestionnaireByIdHandler(questionnaireQueryOutPort);
    }

    @Bean(name = "searchQuestionnaireByFilterQueryHandler")
    public QueryHandler<SearchQuestionnaireByFilter, PageResult<QuestionnaireView>> searchQuestionnaireByFilterQueryHandler(
            QuestionnaireQueryOutPort questionnaireQueryOutPort) {
        return new SearchQuestionnaireByFilterHandler(questionnaireQueryOutPort);
    }
}

