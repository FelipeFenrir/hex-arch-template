package com.acme.orderquestionnaire.config.pipeline;

import com.acme.orderquestionnaire.application.questionnaire.port.in.usecase.ValidateQuestionnaireAnswersUseCase;
import com.acme.orderquestionnaire.application.questionnaire.port.out.repository.QuestionnaireCommandOutPort;
import com.acme.orderquestionnaire.application.questionnaire.service.ValidateQuestionnaireAnswersService;
import com.acme.orderquestionnaire.application.questionnaire.service.context.ValidateQuestionnaireAnswersPipelineContext;
import com.acme.orderquestionnaire.application.questionnaire.service.step.FetchQuestionnaireForAnswersValidationStep;
import com.acme.orderquestionnaire.application.questionnaire.service.step.ValidateAnswersAgainstQuestionnaireStep;
import com.acme.orderquestionnaire.application.questionnaire.service.step.ValidateQuestionnaireAnswersCommandStep;
import com.acme.shared.pattern.pipeline.Step;
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
@EnableConfigurationProperties(QuestionnairePipelineProperties.class)
public class ValidateQuestionnaireAnswersUseCaseConfig {

    @Bean
    public ValidateQuestionnaireAnswersService validateQuestionnaireAnswersPipelineService(
            QuestionnairePipelineProperties properties,
            QuestionnaireCommandOutPort questionnaireCommandOutPort) {

        List<Step<ValidateQuestionnaireAnswersPipelineContext>> allSteps = List.of(
                new ValidateQuestionnaireAnswersCommandStep(),
                new FetchQuestionnaireForAnswersValidationStep(questionnaireCommandOutPort),
                new ValidateAnswersAgainstQuestionnaireStep()
        );

        Map<String, Step<ValidateQuestionnaireAnswersPipelineContext>> stepsById = allSteps.stream()
                .collect(Collectors.toMap(Step::id, Function.identity()));

        List<Step<ValidateQuestionnaireAnswersPipelineContext>> orderedSteps =
                properties.getValidatequestionnaireanswers().entrySet().stream()
                        .filter(entry -> entry.getValue().isEnabled())
                        .map(entry -> Objects.requireNonNull(
                                stepsById.get(entry.getKey()),
                                "Unknown step id configured for validatequestionnaireanswers pipeline: [" + entry.getKey() + "]"
                        ))
                        .toList();

        return new ValidateQuestionnaireAnswersService(orderedSteps);
    }

    @Bean
    @Primary
    public ValidateQuestionnaireAnswersUseCase validateQuestionnaireAnswersUseCase(
            ValidateQuestionnaireAnswersService validateQuestionnaireAnswersPipelineService) {
        return validateQuestionnaireAnswersPipelineService;
    }
}

