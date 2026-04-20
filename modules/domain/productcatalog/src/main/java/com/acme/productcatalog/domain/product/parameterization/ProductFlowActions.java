package com.acme.productcatalog.domain.product.parameterization;

import com.acme.productcatalog.domain.common.parametrizationflow.NecessaryAction;

public enum ProductFlowActions implements NecessaryAction {
    VALIDATE_FIELDS,
    BUSINESS_TEAM_MANUAL_APPROVAL,
    SEND_INTERNAL_COMMUNICATION,
    SET_PRICE,
    SEND_PRODUCER_COMMUNICATION,
    SEND_DISTRIBUTOR_COMMUNICATION
}
