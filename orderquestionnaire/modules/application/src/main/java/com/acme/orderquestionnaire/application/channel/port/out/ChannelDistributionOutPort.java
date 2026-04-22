package com.acme.orderquestionnaire.application.channel.port.out;

import com.acme.shared.stereotypes.core.OutputPort;

@OutputPort
public interface ChannelDistributionOutPort {

    boolean existsById(String channelDistributionId);
}

