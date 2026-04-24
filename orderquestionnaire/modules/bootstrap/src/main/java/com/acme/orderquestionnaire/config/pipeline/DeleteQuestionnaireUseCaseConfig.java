package com.acme.orderquestionnaire.config.pipeline;

import com.acme.orderquestionnaire.application.questionnaire.port.in.usecase.DeleteQuestionnaireUseCase;
import com.acme.orderquestionnaire.application.questionnaire.port.out.repository.QuestionnaireCommandOutPort;
import com.acme.orderquestionnaire.application.questionnaire.service.DeleteQuestionnaireService;
import com.acme.orderquestionnaire.application.questionnaire.service.context.DeleteQuestionnairePipelineContext;
import com.acme.orderquestionnaire.application.questionnaire.service.step.DeleteQuestionnaireStep;
import com.acme.orderquestionnaire.application.questionnaire.service.step.FetchQuestionnaireForDeleteStep;
import com.acme.orderquestionnaire.application.questionnaire.service.step.ValidateDeleteQuestionnaireCommandStep;
import com.acme.orderquestionnaire.application.questionnaire.service.step.ValidateQuestionnaireDeleteEligibilityStep;
import com.acme.orderquestionnaire.config.pipeline.transactional.TransactionalDeleteQuestionnaireUseCaseFacade;
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
public class DeleteQuestionnaireUseCaseConfig {

    @Bean
    public DeleteQuestionnaireService deleteQuestionnairePipelineService(
            QuestionnairePipelineProperties properties,
            QuestionnaireCommandOutPort questionnaireCommandOutPort) {

        List<Step<DeleteQuestionnairePipelineContext>> allSteps = List.of(
                new ValidateDeleteQuestionnaireCommandStep(),
                new FetchQuestionnaireForDeleteStep(questionnaireCommandOutPort),
                new ValidateQuestionnaireDeleteEligibilityStep(),
                new DeleteQuestionnaireStep(questionnaireCommandOutPort)
        );

        Map<String, Step<DeleteQuestionnairePipelineContext>> stepsById = allSteps.stream()
                .collect(Collectors.toMap(Step::id, Function.identity()));

        List<Step<DeleteQuestionnairePipelineContext>> orderedSteps =
                properties.getDeletequestionnaire().entrySet().stream()
                        .filter(entry -> entry.getValue().isEnabled())
                        .map(entry -> Objects.requireNonNull(
                                stepsById.get(entry.getKey()),
                                "Unknown step id configured for deletequestionnaire pipeline: [" + entry.getKey() + "]"
                        ))
                        .toList();

        return new DeleteQuestionnaireService(orderedSteps);
    }

    @Bean
    @Primary
    public DeleteQuestionnaireUseCase deleteQuestionnaireUseCase(
            DeleteQuestionnaireService deleteQuestionnairePipelineService,
            ObjectProvider<PlatformTransactionManager> transactionManagerProvider) {
        return new TransactionalDeleteQuestionnaireUseCaseFacade(
                transactionManagerProvider.getIfAvailable(),
                deleteQuestionnairePipelineService
        );
    }
}

