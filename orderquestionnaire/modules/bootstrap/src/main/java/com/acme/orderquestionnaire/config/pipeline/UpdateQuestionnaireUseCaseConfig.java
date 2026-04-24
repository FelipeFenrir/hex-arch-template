package com.acme.orderquestionnaire.config.pipeline;

import com.acme.orderquestionnaire.application.question.port.out.repository.QuestionCommandOutPort;
import com.acme.orderquestionnaire.application.questionnaire.port.in.usecase.UpdateQuestionnaireUseCase;
import com.acme.orderquestionnaire.application.questionnaire.port.out.repository.QuestionnaireCommandOutPort;
import com.acme.orderquestionnaire.application.questionnaire.service.UpdateQuestionnaireService;
import com.acme.orderquestionnaire.application.questionnaire.service.context.UpdateQuestionnairePipelineContext;
import com.acme.orderquestionnaire.application.questionnaire.service.step.ApplyQuestionnaireUpdateTransitionStep;
import com.acme.orderquestionnaire.application.questionnaire.service.step.BuildUpdateQuestionnaireAuditStep;
import com.acme.orderquestionnaire.application.questionnaire.service.step.FetchExistingQuestionnaireStep;
import com.acme.orderquestionnaire.application.questionnaire.service.step.PersistUpdatedQuestionnaireStep;
import com.acme.orderquestionnaire.application.questionnaire.service.step.ResolveConfiguredQuestionsStep;
import com.acme.orderquestionnaire.application.questionnaire.service.step.ValidateActiveQuestionnaireUpdateRestrictionsStep;
import com.acme.orderquestionnaire.application.questionnaire.service.step.ValidateUpdateQuestionnaireCommandStep;
import com.acme.orderquestionnaire.config.pipeline.transactional.TransactionalUpdateQuestionnaireUseCaseFacade;
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
@EnableConfigurationProperties(QuestionnairePipelineProperties.class)
public class UpdateQuestionnaireUseCaseConfig {

    @Bean
    public UpdateQuestionnaireService updateQuestionnairePipelineService(
            QuestionnairePipelineProperties properties,
            QuestionnaireCommandOutPort questionnaireCommandOutPort,
            QuestionCommandOutPort questionCommandOutPort) {

        List<Step<UpdateQuestionnairePipelineContext>> allSteps = List.of(
                new ValidateUpdateQuestionnaireCommandStep(),
                new BuildUpdateQuestionnaireAuditStep(),
                new FetchExistingQuestionnaireStep(questionnaireCommandOutPort),
                new ValidateActiveQuestionnaireUpdateRestrictionsStep(),
                new ResolveConfiguredQuestionsStep(questionCommandOutPort),
                new ApplyQuestionnaireUpdateTransitionStep(),
                new PersistUpdatedQuestionnaireStep(questionnaireCommandOutPort)
        );

        Map<String, Step<UpdateQuestionnairePipelineContext>> stepsById = allSteps.stream()
                .collect(Collectors.toMap(Step::id, Function.identity()));

        List<Step<UpdateQuestionnairePipelineContext>> orderedSteps =
                properties.getUpdatequestionnaire().entrySet().stream()
                        .filter(entry -> entry.getValue().isEnabled())
                        .map(entry -> Objects.requireNonNull(
                                stepsById.get(entry.getKey()),
                                "Unknown step id configured for updatequestionnaire pipeline: [" + entry.getKey() + "]"
                        ))
                        .toList();

        return new UpdateQuestionnaireService(orderedSteps);
    }

    @Bean
    @Primary
    public UpdateQuestionnaireUseCase updateQuestionnaireUseCase(
            UpdateQuestionnaireService updateQuestionnairePipelineService,
            ObjectProvider<PlatformTransactionManager> transactionManagerProvider) {
        return new TransactionalUpdateQuestionnaireUseCaseFacade(
                transactionManagerProvider.getIfAvailable(),
                updateQuestionnairePipelineService
        );
    }
}

