package com.acme.orderquestionnaire.adapters.out.api.distribution.journey.client;

import com.acme.orderquestionnaire.adapters.out.api.distribution.journey.config.JourneyDistributionFeignClientConfig;
import com.acme.orderquestionnaire.adapters.out.api.distribution.journey.dto.JourneyDistributionApiResponseDto;
import com.acme.orderquestionnaire.adapters.out.api.distribution.journey.fallback.JourneyDistributionApiFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "journeyDistributionApiClient",
        url = "${orderquestionnaire.adapters.out.journey-distribution.integrations.api.base-url}",
        configuration = JourneyDistributionFeignClientConfig.class,
        fallbackFactory = JourneyDistributionApiFallbackFactory.class
)
public interface JourneyDistributionApiClient {

    @GetMapping("/distribution/journey/{journeyId}")
    JourneyDistributionApiResponseDto getById(@PathVariable("journeyId") String journeyId);
}



