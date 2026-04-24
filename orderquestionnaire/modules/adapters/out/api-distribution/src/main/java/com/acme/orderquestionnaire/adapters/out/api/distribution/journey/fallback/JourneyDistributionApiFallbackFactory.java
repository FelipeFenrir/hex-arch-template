package com.acme.orderquestionnaire.adapters.out.api.distribution.journey.fallback;

import com.acme.orderquestionnaire.adapters.out.api.distribution.journey.client.JourneyDistributionApiClient;
import com.acme.orderquestionnaire.adapters.out.api.distribution.journey.dto.JourneyDistributionApiResponseDto;
import org.springframework.stereotype.Component;
import org.springframework.cloud.openfeign.FallbackFactory;

@Component
public class JourneyDistributionApiFallbackFactory implements FallbackFactory<JourneyDistributionApiClient> {

    @Override
    public JourneyDistributionApiClient create(Throwable cause) {
        return journeyId -> new JourneyDistributionApiResponseDto(null);
    }
}


