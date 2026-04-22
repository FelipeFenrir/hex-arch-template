package com.acme.orderquestionnaire.application.questionnaire.service;

import com.acme.orderquestionnaire.application.channel.port.out.ChannelDistributionOutPort;
import com.acme.orderquestionnaire.application.audit.dto.command.AuditUserParam;
import com.acme.orderquestionnaire.application.questionnaire.error.QuestionnaireErrors;
import com.acme.orderquestionnaire.application.questionnaire.dto.command.CreateQuestionnaireCommand;
import com.acme.orderquestionnaire.application.questionnaire.dto.view.QuestionnaireCreatedView;
import com.acme.orderquestionnaire.application.questionnaire.port.in.usecase.CreateQuestionnaireUseCase;
import com.acme.orderquestionnaire.application.journey.port.out.JourneyDistributionOutPort;
import com.acme.orderquestionnaire.application.questionnaire.port.out.repository.QuestionnaireCommandOutPort;
import com.acme.orderquestionnaire.domain.audit.OrderQuestionnaireAuditFactory;
import com.acme.orderquestionnaire.domain.questionnaire.QuestionnaireFactory;
import com.acme.orderquestionnaire.domain.questionnaire.vo.QuestionnaireId;
import com.acme.shared.pattern.result.DomainError;
import com.acme.shared.pattern.result.Guard;
import com.acme.shared.pattern.result.Result;
import com.acme.shared.vo.AuditInfo;

import java.util.List;
import java.util.Objects;

public class CreateQuestionnaireService implements CreateQuestionnaireUseCase {

    private final QuestionnaireCommandOutPort questionnaireRepository;
    private final ChannelDistributionOutPort channelDistributionOutPort;
    private final JourneyDistributionOutPort journeyDistributionOutPort;

    public CreateQuestionnaireService(QuestionnaireCommandOutPort questionnaireRepository,
                                      ChannelDistributionOutPort channelDistributionOutPort,
                                      JourneyDistributionOutPort journeyDistributionOutPort) {

        this.questionnaireRepository = Objects.requireNonNull(questionnaireRepository,
                "questionnaireRepository must not be null");
        this.channelDistributionOutPort = Objects.requireNonNull(channelDistributionOutPort,
                "channelDistributionQueryPort must not be null");
        this.journeyDistributionOutPort = Objects.requireNonNull(journeyDistributionOutPort,
                "journeyDistributionQueryPort must not be null");
    }

    @Override
    public Result<QuestionnaireCreatedView, List<DomainError>> execute(CreateQuestionnaireCommand command) {
        Result<Void, List<DomainError>> validations = validateCommand(command);
        if (validations.isFailure()) {
            return Result.failure(validations.errorOrElseThrow(() ->
                    new IllegalStateException("Expected failure validations result")));
        }

        return validations
                .flatMap(ignored -> buildCreatedAudit(command))
                .flatMap(auditInfo -> checkChannelDistributionExists(command.channelDistributionId())
                        .flatMap(__ -> checkJourneyDistributionExists(command.journeyDistributionId()))
                        .flatMap(__ -> checkNoDuplicate(command.id(), command.channelDistributionId(),
                                command.journeyDistributionId()))
                        .flatMap(__ -> QuestionnaireFactory.createNew(
                                command.id(),
                                command.channelDistributionId(),
                                command.journeyDistributionId(),
                                command.description(),
                                auditInfo)))
                .flatMap(builder -> builder.build())
                .flatMap(questionnaireRepository::create)
                .map(QuestionnaireCreatedView::from);
    }

    private Result<Void, List<DomainError>> validateCommand(CreateQuestionnaireCommand command) {
        if (command == null) {
            return QuestionnaireErrors.INVALID_COMMAND.asFailure();
        }

        return Guard.collect(List.of(
                QuestionnaireFactory.validateCreatePayload(
                        command.id(),
                        command.channelDistributionId(),
                        command.journeyDistributionId(),
                        command.description()
                )
        ));
    }

    private Result<AuditInfo, List<DomainError>> buildCreatedAudit(CreateQuestionnaireCommand command) {
        AuditUserParam createdBy = command.createdBy();
        return OrderQuestionnaireAuditFactory.createNewForQuestionnaire(
                createdBy == null ? null : createdBy.id(),
                createdBy == null ? null : createdBy.referenceCode(),
                createdBy == null ? null : createdBy.name(),
                createdBy == null ? null : createdBy.email(),
                command.createdAt());
    }

    private Result<Void, List<DomainError>> checkChannelDistributionExists(String channelDistributionId) {
        return channelDistributionOutPort.existsById(channelDistributionId)
                ? Result.success(null)
                : QuestionnaireErrors.CHANNEL_DISTRIBUTION_NOT_FOUND.asFailure(channelDistributionId);
    }

    private Result<Void, List<DomainError>> checkJourneyDistributionExists(String journeyDistributionId) {
        return journeyDistributionOutPort.existsById(journeyDistributionId)
                ? Result.success(null)
                : QuestionnaireErrors.JOURNEY_DISTRIBUTION_NOT_FOUND.asFailure(journeyDistributionId);
    }

    private Result<Void, List<DomainError>> checkNoDuplicate(String id,
                                                             String channelDistributionId,
                                                             String journeyDistributionId) {
        QuestionnaireId questionnaireId = QuestionnaireId.of(id, channelDistributionId, journeyDistributionId);
        return questionnaireRepository.existsById(questionnaireId)
                ? QuestionnaireErrors.QUESTIONNAIRE_ALREADY_EXISTS.asFailure(id)
                : Result.success(null);
    }
}
