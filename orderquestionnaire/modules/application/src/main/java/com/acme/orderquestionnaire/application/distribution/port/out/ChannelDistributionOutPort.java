package com.acme.orderquestionnaire.application.distribution.port.out;

import com.acme.shared.stereotypes.core.OutputPort;

@OutputPort
public interface ChannelDistributionOutPort {

    boolean existsById(String channelDistributionId);
}

