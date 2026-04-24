package com.acme.orderquestionnaire.config.pipeline;

import com.acme.orderquestionnaire.application.channel.port.out.ChannelDistributionOutPort;
import com.acme.orderquestionnaire.application.journey.port.out.JourneyDistributionOutPort;
import com.acme.orderquestionnaire.application.questionnaire.port.in.usecase.CreateQuestionnaireUseCase;
import com.acme.orderquestionnaire.application.questionnaire.port.out.repository.QuestionnaireCommandOutPort;
import com.acme.orderquestionnaire.application.questionnaire.service.CreateQuestionnaireService;
import com.acme.orderquestionnaire.application.questionnaire.service.context.CreateQuestionnairePipelineContext;
import com.acme.orderquestionnaire.application.questionnaire.service.step.BuildAndPersistQuestionnaireStep;
import com.acme.orderquestionnaire.application.questionnaire.service.step.BuildAuditStep;
import com.acme.orderquestionnaire.application.questionnaire.service.step.CheckChannelDistributionStep;
import com.acme.orderquestionnaire.application.questionnaire.service.step.CheckJourneyDistributionStep;
import com.acme.orderquestionnaire.application.questionnaire.service.step.CheckNoDuplicateStep;
import com.acme.orderquestionnaire.application.questionnaire.service.step.ValidateCommandStep;
import com.acme.orderquestionnaire.config.pipeline.transactional.TransactionalCreateQuestionnaireUseCaseFacade;
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

/**
 * Assembles the {@link CreateQuestionnaireService} pipeline from configuration.
 *
 * <p>Responsibilities:
 * <ol>
 *   <li>Registers all known steps with their injected ports.</li>
 *   <li>Reads the step names and {@code enabled} flags from
 *       {@link QuestionnairePipelineProperties#getCreatequestionnaire()}.</li>
 *   <li>Respects the YAML declaration order (backed by {@link java.util.LinkedHashMap}).</li>
 *   <li>Filters disabled steps and fails fast on unknown step IDs.</li>
 *   <li>Injects the ordered {@link List}&lt;{@link Step}&gt; into {@link CreateQuestionnaireService}.</li>
 * </ol>
 *
 * <p>To disable a step at runtime, set {@code enabled: false} in {@code application.yml}
 * without touching production code.
 */
@Configuration
@EnableConfigurationProperties(QuestionnairePipelineProperties.class)
public class CreateQuestionnaireUseCaseConfig {

    @Bean
    public CreateQuestionnaireService createQuestionnairePipelineService(
            QuestionnairePipelineProperties properties,
            QuestionnaireCommandOutPort questionnaireCommandOutPort,
            ChannelDistributionOutPort channelDistributionOutPort,
            JourneyDistributionOutPort journeyDistributionOutPort) {

        // ── Registry: all steps, wired with their port dependencies ──────────
        List<Step<CreateQuestionnairePipelineContext>> allSteps = List.of(
                new ValidateCommandStep(),
                new BuildAuditStep(),
                new CheckChannelDistributionStep(channelDistributionOutPort),
                new CheckJourneyDistributionStep(journeyDistributionOutPort),
                new CheckNoDuplicateStep(questionnaireCommandOutPort),
                new BuildAndPersistQuestionnaireStep(questionnaireCommandOutPort, RollbackStyle.FRAMEWORK_TRANSACTION)
        );

        Map<String, Step<CreateQuestionnairePipelineContext>> stepsById = allSteps.stream()
                .collect(Collectors.toMap(Step::id, Function.identity()));

        // ── Ordered and filtered list from YAML (LinkedHashMap preserves order) ──
        List<Step<CreateQuestionnairePipelineContext>> orderedSteps =
                properties.getCreatequestionnaire().entrySet().stream()
                        .filter(entry -> entry.getValue().isEnabled())
                        .map(entry -> Objects.requireNonNull(
                                stepsById.get(entry.getKey()),
                                "Unknown step id configured for createquestionnaire pipeline: [" + entry.getKey() + "]"
                        ))
                        .toList();

        return new CreateQuestionnaireService(orderedSteps);
    }

    @Bean
    @Primary
    public CreateQuestionnaireUseCase createQuestionnaireUseCase(
            CreateQuestionnaireService createQuestionnairePipelineService,
            ObjectProvider<org.springframework.transaction.PlatformTransactionManager> transactionManagerProvider) {
        return new TransactionalCreateQuestionnaireUseCaseFacade(
                transactionManagerProvider.getIfAvailable(),
                createQuestionnairePipelineService
        );
    }
}

