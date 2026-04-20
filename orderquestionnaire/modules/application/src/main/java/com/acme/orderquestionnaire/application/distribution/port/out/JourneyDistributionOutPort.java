package com.acme.orderquestionnaire.application.distribution.port.out;

import com.acme.shared.stereotypes.core.OutputPort;

@OutputPort
public interface JourneyDistributionOutPort {

    boolean existsById(String journeyDistributionId);
}

