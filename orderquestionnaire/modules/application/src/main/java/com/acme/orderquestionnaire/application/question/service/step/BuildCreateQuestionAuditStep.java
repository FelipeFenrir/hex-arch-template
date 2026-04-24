package com.acme.orderquestionnaire.application.question.service.step;

import com.acme.orderquestionnaire.application.question.service.context.CreateQuestionPipelineContext;
import com.acme.orderquestionnaire.domain.audit.OrderQuestionnaireAuditFactory;
import com.acme.shared.pattern.pipeline.Step;
import com.acme.shared.pattern.result.DomainError;
import com.acme.shared.pattern.result.Result;

import java.util.List;

public class BuildCreateQuestionAuditStep implements Step<CreateQuestionPipelineContext> {

    @Override
    public String id() {
        return "BUILD_AUDIT";
    }

    @Override
    public Result<Void, List<DomainError>> execute(CreateQuestionPipelineContext context) {
        var command = context.command();
        var createdBy = command.createdBy();

        return OrderQuestionnaireAuditFactory.createNewForQuestion(
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

