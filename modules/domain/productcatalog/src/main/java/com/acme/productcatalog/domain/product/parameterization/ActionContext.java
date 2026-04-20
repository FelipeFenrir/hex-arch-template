package com.acme.productcatalog.domain.product.parameterization;

import com.acme.productcatalog.domain.product.parameterization.actions.BusinessApprovalAction;
import com.acme.productcatalog.domain.product.parameterization.actions.EmailNotificationAction;
import com.acme.productcatalog.domain.product.parameterization.actions.EventNotificationAction;
import com.acme.productcatalog.domain.product.parameterization.actions.PrecificationAction;
import com.acme.productcatalog.domain.product.parameterization.actions.ValidateFieldsAction;

public class ActionContext {
    private final ValidateFieldsAction validator;
    private final BusinessApprovalAction approver;
    private final EmailNotificationAction emailNotification;
    private final EventNotificationAction eventNotification;
    private final PrecificationAction precification;

    public ActionContext(
            ValidateFieldsAction validator,
            BusinessApprovalAction approver,
            EmailNotificationAction emailNotification,
            EventNotificationAction eventNotification,
            PrecificationAction precification) {
        this.validator = validator;
        this.approver = approver;
        this.emailNotification = emailNotification;
        this.eventNotification = eventNotification;
        this.precification = precification;
    }

    public ValidateFieldsAction validator() { return validator; }
    public BusinessApprovalAction approver() { return approver; }
    public EmailNotificationAction emailNotification() { return emailNotification; }
    public EventNotificationAction eventNotification() { return eventNotification; }
    public PrecificationAction precification() { return precification; }
}
