package com.acme.orderquestionnaire.config.pipeline;

import com.acme.orderquestionnaire.application.question.port.in.usecase.UpdateQuestionUseCase;
import com.acme.orderquestionnaire.application.question.port.out.repository.QuestionCommandOutPort;
import com.acme.orderquestionnaire.application.question.service.UpdateQuestionService;
import com.acme.orderquestionnaire.application.question.service.context.UpdateQuestionPipelineContext;
import com.acme.orderquestionnaire.application.question.service.step.BuildUpdateQuestionAuditStep;
import com.acme.orderquestionnaire.application.question.service.step.BuildUpdatedQuestionStep;
import com.acme.orderquestionnaire.application.question.service.step.FetchExistingQuestionStep;
import com.acme.orderquestionnaire.application.question.service.step.PersistUpdatedQuestionStep;
import com.acme.orderquestionnaire.application.question.service.step.ResolveQuestionTransitionStep;
import com.acme.orderquestionnaire.application.question.service.step.ValidateUpdateQuestionCommandStep;
import com.acme.orderquestionnaire.config.pipeline.transactional.TransactionalUpdateQuestionUseCaseFacade;
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
public class UpdateQuestionUseCaseConfig {

    @Bean
    public UpdateQuestionService updateQuestionPipelineService(
            QuestionPipelineProperties properties,
            QuestionCommandOutPort questionCommandOutPort) {

        List<Step<UpdateQuestionPipelineContext>> allSteps = List.of(
                new ValidateUpdateQuestionCommandStep(),
                new BuildUpdateQuestionAuditStep(),
                new FetchExistingQuestionStep(questionCommandOutPort),
                new ResolveQuestionTransitionStep(),
                new BuildUpdatedQuestionStep(),
                new PersistUpdatedQuestionStep(questionCommandOutPort)
        );

        Map<String, Step<UpdateQuestionPipelineContext>> stepsById = allSteps.stream()
                .collect(Collectors.toMap(Step::id, Function.identity()));

        List<Step<UpdateQuestionPipelineContext>> orderedSteps = properties.getUpdatequestion().entrySet().stream()
                .filter(entry -> entry.getValue().isEnabled())
                .map(entry -> Objects.requireNonNull(
                        stepsById.get(entry.getKey()),
                        "Unknown step id configured for updatequestion pipeline: [" + entry.getKey() + "]"
                ))
                .toList();

        return new UpdateQuestionService(orderedSteps);
    }

    @Bean
    @Primary
    public UpdateQuestionUseCase updateQuestionUseCase(
            UpdateQuestionService updateQuestionPipelineService,
            ObjectProvider<PlatformTransactionManager> transactionManagerProvider) {
        return new TransactionalUpdateQuestionUseCaseFacade(
                transactionManagerProvider.getIfAvailable(),
                updateQuestionPipelineService
        );
    }
}

