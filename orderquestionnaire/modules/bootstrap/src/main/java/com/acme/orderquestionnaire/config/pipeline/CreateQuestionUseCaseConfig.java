package com.acme.orderquestionnaire.config.pipeline;

import com.acme.orderquestionnaire.application.question.port.in.usecase.CreateQuestionUseCase;
import com.acme.orderquestionnaire.application.question.port.out.repository.QuestionCommandOutPort;
import com.acme.orderquestionnaire.application.question.service.CreateQuestionService;
import com.acme.orderquestionnaire.application.question.service.context.CreateQuestionPipelineContext;
import com.acme.orderquestionnaire.application.question.service.step.BuildAndPersistQuestionStep;
import com.acme.orderquestionnaire.application.question.service.step.BuildCreateQuestionAuditStep;
import com.acme.orderquestionnaire.application.question.service.step.CheckNoDuplicateStep;
import com.acme.orderquestionnaire.application.question.service.step.ValidateCreateQuestionCommandStep;
import com.acme.orderquestionnaire.config.pipeline.transactional.TransactionalCreateQuestionUseCaseFacade;
import com.acme.shared.pattern.pipeline.RollbackStyle;
import com.acme.shared.pattern.pipeline.Step;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Configuration
@EnableConfigurationProperties(QuestionPipelineProperties.class)
public class CreateQuestionUseCaseConfig {

    @Bean
    public CreateQuestionService createQuestionPipelineService(
            QuestionPipelineProperties properties,
            QuestionCommandOutPort questionCommandOutPort) {

        List<Step<CreateQuestionPipelineContext>> allSteps = List.of(
                new ValidateCreateQuestionCommandStep(),
                new CheckNoDuplicateStep(questionCommandOutPort),
                new BuildCreateQuestionAuditStep(),
                new BuildAndPersistQuestionStep(questionCommandOutPort, RollbackStyle.FRAMEWORK_TRANSACTION)
        );

        Map<String, Step<CreateQuestionPipelineContext>> stepsById = allSteps.stream()
                .collect(Collectors.toMap(Step::id, Function.identity()));

        List<Step<CreateQuestionPipelineContext>> orderedSteps =
                properties.getCreatequestion().entrySet().stream()
                        .filter(entry -> entry.getValue().isEnabled())
                        .map(entry -> Objects.requireNonNull(
                                stepsById.get(entry.getKey()),
                                "Unknown step id configured for createquestion pipeline: [" + entry.getKey() + "]"
                        ))
                        .toList();

        return new CreateQuestionService(orderedSteps);
    }

    @Bean
    @Primary
    public CreateQuestionUseCase createQuestionUseCase(
            CreateQuestionService createQuestionPipelineService,
            ObjectProvider<org.springframework.transaction.PlatformTransactionManager> transactionManagerProvider) {
        return new TransactionalCreateQuestionUseCaseFacade(
                transactionManagerProvider.getIfAvailable(),
                createQuestionPipelineService
        );
    }
}


