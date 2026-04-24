package com.acme.orderquestionnaire.application.questionnaire.service.step;

import com.acme.orderquestionnaire.application.questionnaire.service.context.UpdateQuestionnairePipelineContext;
import com.acme.orderquestionnaire.domain.audit.OrderQuestionnaireAuditFactory;
import com.acme.shared.pattern.pipeline.Step;
import com.acme.shared.pattern.result.DomainError;
import com.acme.shared.pattern.result.Result;

import java.util.List;

public class BuildUpdateQuestionnaireAuditStep implements Step<UpdateQuestionnairePipelineContext> {

    @Override
    public String id() {
        return "BUILD_AUDIT";
    }

    @Override
    public Result<Void, List<DomainError>> execute(UpdateQuestionnairePipelineContext context) {
        var command = context.command();
        var updatedBy = command.updatedBy();

        return OrderQuestionnaireAuditFactory.updateUserForQuestionnaire(
                        updatedBy == null ? null : updatedBy.id(),
                        updatedBy == null ? null : updatedBy.referenceCode(),
                        updatedBy == null ? null : updatedBy.name(),
                        updatedBy == null ? null : updatedBy.email(),
                        command.updatedAt()
                )
                .map(auditUser -> {
                    context.updatedActor(auditUser);
                    return null;
                });
    }
}

