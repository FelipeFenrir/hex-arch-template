package com.acme.orderquestionnaire.application.questionnaire.service.step;

import com.acme.orderquestionnaire.application.questionnaire.service.context.CreateQuestionnairePipelineContext;
import com.acme.orderquestionnaire.domain.audit.OrderQuestionnaireAuditFactory;
import com.acme.shared.pattern.pipeline.Step;
import com.acme.shared.pattern.result.DomainError;
import com.acme.shared.pattern.result.Result;

import java.util.List;

/**
 * Builds and validates the {@link com.acme.shared.vo.AuditInfo} for the new questionnaire.
 *
 * <p>On success, writes the {@code AuditInfo} into the pipeline context so that
 * downstream steps (namely {@code BuildAndPersistQuestionnaireStep}) can consume it.
 * No side-effects; rollback is a no-op (default {@code NONE}).
 */
public class BuildAuditStep implements Step<CreateQuestionnairePipelineContext> {

    @Override
    public String id() {
        return "BUILD_AUDIT";
    }

    @Override
    public Result<Void, List<DomainError>> execute(CreateQuestionnairePipelineContext context) {
        var command = context.command();
        var createdBy = command.createdBy();

        return OrderQuestionnaireAuditFactory.createNewForQuestionnaire(
                        createdBy == null ? null : createdBy.id(),
                        createdBy == null ? null : createdBy.referenceCode(),
                        createdBy == null ? null : createdBy.name(),
                        createdBy == null ? null : createdBy.email(),
                        command.createdAt()
                )
                .map(auditInfo -> {
                    context.auditInfo(auditInfo);
                    return (Void) null;
                });
    }
}

