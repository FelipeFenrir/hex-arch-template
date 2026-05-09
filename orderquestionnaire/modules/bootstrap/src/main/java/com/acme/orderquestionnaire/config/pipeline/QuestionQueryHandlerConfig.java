package com.acme.orderquestionnaire.config.pipeline;

import com.acme.orderquestionnaire.application.common.QueryHandler;
import com.acme.orderquestionnaire.application.question.dto.queries.GetQuestionById;
import com.acme.orderquestionnaire.application.question.dto.queries.SearchQuestionByFilter;
import com.acme.orderquestionnaire.application.question.dto.view.QuestionView;
import com.acme.orderquestionnaire.application.question.port.out.repository.QuestionQueryOutPort;
import com.acme.orderquestionnaire.application.question.service.GetQuestionByIdHandler;
import com.acme.orderquestionnaire.application.question.service.SearchQuestionByFilterHandler;
import com.acme.shared.engine.pagination.PageResult;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Optional;

@Configuration
public class QuestionQueryHandlerConfig {

    @Bean(name = "getQuestionByIdQueryHandler")
    public QueryHandler<GetQuestionById, Optional<QuestionView>> getQuestionByIdQueryHandler(
            QuestionQueryOutPort questionQueryOutPort) {
        return new GetQuestionByIdHandler(questionQueryOutPort);
    }

    @Bean(name = "searchQuestionByFilterQueryHandler")
    public QueryHandler<SearchQuestionByFilter, PageResult<QuestionView>> searchQuestionByFilterQueryHandler(
            QuestionQueryOutPort questionQueryOutPort) {
        return new SearchQuestionByFilterHandler(questionQueryOutPort);
    }
}

