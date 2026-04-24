package com.acme.orderquestionnaire.adapters.out.api.distribution.channel.error;

import feign.FeignException;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class ChannelDistributionApiErrorTranslator {

    public boolean translateExistsByIdFailure(Throwable cause, String channelDistributionId) {
        Objects.requireNonNull(channelDistributionId, "channelDistributionId must not be null");

        if (cause instanceof FeignException.NotFound) {
            return false;
        }

        // Non-404 failures are currently treated as "not found" until explicit domain error mapping is introduced.
        return false;
    }
}


