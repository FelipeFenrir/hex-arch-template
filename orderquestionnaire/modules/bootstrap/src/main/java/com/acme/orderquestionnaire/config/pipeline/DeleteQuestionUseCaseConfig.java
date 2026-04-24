package com.acme.orderquestionnaire.config.pipeline;

import com.acme.orderquestionnaire.application.question.port.in.usecase.DeleteQuestionUseCase;
import com.acme.orderquestionnaire.application.question.port.out.repository.QuestionCommandOutPort;
import com.acme.orderquestionnaire.application.question.service.DeleteQuestionService;
import com.acme.orderquestionnaire.application.question.service.context.DeleteQuestionPipelineContext;
import com.acme.orderquestionnaire.application.question.service.step.CheckQuestionNotInUseStep;
import com.acme.orderquestionnaire.application.question.service.step.DeleteQuestionStep;
import com.acme.orderquestionnaire.application.question.service.step.FetchQuestionForDeleteStep;
import com.acme.orderquestionnaire.application.question.service.step.ValidateDeleteQuestionIdStep;
import com.acme.orderquestionnaire.application.questionnaire.port.out.repository.QuestionnaireCommandOutPort;
import com.acme.orderquestionnaire.config.pipeline.transactional.TransactionalDeleteQuestionUseCaseFacade;
import com.acme.shared.pattern.pipeline.Step;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Configuration
@EnableConfigurationProperties(QuestionPipelineProperties.class)
public class DeleteQuestionUseCaseConfig {

    @Bean
    public DeleteQuestionService deleteQuestionPipelineService(
            QuestionPipelineProperties properties,
            QuestionCommandOutPort questionCommandOutPort,
            QuestionnaireCommandOutPort questionnaireCommandOutPort) {

        List<Step<DeleteQuestionPipelineContext>> allSteps = List.of(
                new ValidateDeleteQuestionIdStep(),
                new FetchQuestionForDeleteStep(questionCommandOutPort),
                new CheckQuestionNotInUseStep(questionnaireCommandOutPort),
                new DeleteQuestionStep(questionCommandOutPort)
        );

        Map<String, Step<DeleteQuestionPipelineContext>> stepsById = allSteps.stream()
                .collect(Collectors.toMap(Step::id, Function.identity()));

        List<Step<DeleteQuestionPipelineContext>> orderedSteps =
                properties.getDeletequestion().entrySet().stream()
                        .filter(entry -> entry.getValue().isEnabled())
                        .map(entry -> Objects.requireNonNull(
                                stepsById.get(entry.getKey()),
                                "Unknown step id configured for deletequestion pipeline: [" + entry.getKey() + "]"
                        ))
                        .toList();

        return new DeleteQuestionService(orderedSteps);
    }

    @Bean
    @Primary
    public DeleteQuestionUseCase deleteQuestionUseCase(
            DeleteQuestionService deleteQuestionPipelineService,
            ObjectProvider<PlatformTransactionManager> transactionManagerProvider) {
        return new TransactionalDeleteQuestionUseCaseFacade(
                transactionManagerProvider.getIfAvailable(),
                deleteQuestionPipelineService
        );
    }
}

