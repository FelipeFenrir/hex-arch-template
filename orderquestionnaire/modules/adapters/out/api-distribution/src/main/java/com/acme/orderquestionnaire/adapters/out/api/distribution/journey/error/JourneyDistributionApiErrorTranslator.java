package com.acme.orderquestionnaire.adapters.out.api.distribution.journey.error;

import feign.FeignException;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class JourneyDistributionApiErrorTranslator {

    public boolean translateExistsByIdFailure(Throwable cause, String journeyDistributionId) {
        Objects.requireNonNull(journeyDistributionId, "journeyDistributionId must not be null");

        if (cause instanceof FeignException.NotFound) {
            return false;
        }

        // Non-404 failures are currently treated as "not found" until explicit domain error mapping is introduced.
        return false;
    }
}


