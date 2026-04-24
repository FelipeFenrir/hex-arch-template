package com.acme.orderquestionnaire.adapters.out.api.distribution.journey.adapter;

import com.acme.orderquestionnaire.adapters.out.api.distribution.journey.client.JourneyDistributionApiClient;
import com.acme.orderquestionnaire.adapters.out.api.distribution.journey.dto.JourneyDistributionApiDataDto;
import com.acme.orderquestionnaire.adapters.out.api.distribution.journey.dto.JourneyDistributionApiResponseDto;
import com.acme.orderquestionnaire.adapters.out.api.distribution.journey.error.JourneyDistributionApiErrorTranslator;
import com.acme.orderquestionnaire.application.journey.port.out.JourneyDistributionOutPort;
import com.acme.shared.stereotypes.adapter.OutputAdapter;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component("journeyDistributionApiAdapter")
@OutputAdapter
public class JourneyDistributionApiAdapter implements JourneyDistributionOutPort {

    private final JourneyDistributionApiClient journeyDistributionApiClient;
    private final JourneyDistributionApiErrorTranslator errorTranslator;

    public JourneyDistributionApiAdapter(
            JourneyDistributionApiClient journeyDistributionApiClient,
            JourneyDistributionApiErrorTranslator errorTranslator
    ) {
        this.journeyDistributionApiClient = Objects.requireNonNull(journeyDistributionApiClient,
                "journeyDistributionApiClient must not be null");
        this.errorTranslator = Objects.requireNonNull(errorTranslator, "errorTranslator must not be null");
    }

    @Override
    public boolean existsById(String journeyDistributionId) {
        try {
            JourneyDistributionApiResponseDto response = journeyDistributionApiClient.getById(journeyDistributionId);
            JourneyDistributionApiDataDto data = response == null ? null : response.data();
            return data != null && journeyDistributionId.equals(data.id());
        } catch (Exception ex) {
            return errorTranslator.translateExistsByIdFailure(ex, journeyDistributionId);
        }
    }
}

