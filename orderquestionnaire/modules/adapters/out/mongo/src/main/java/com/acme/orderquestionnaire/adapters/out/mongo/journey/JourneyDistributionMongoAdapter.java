package com.acme.orderquestionnaire.adapters.out.mongo.journey;

import com.acme.orderquestionnaire.adapters.out.mongo.journey.repository.JourneyDistributionRepository;
import com.acme.orderquestionnaire.application.journey.port.out.JourneyDistributionOutPort;
import com.acme.shared.stereotypes.adapter.OutputAdapter;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component("journeyDistributionMongoAdapter")
@OutputAdapter
public class JourneyDistributionMongoAdapter implements JourneyDistributionOutPort {

    private final JourneyDistributionRepository journeyDistributionRepository;

    public JourneyDistributionMongoAdapter(JourneyDistributionRepository journeyDistributionRepository) {
        this.journeyDistributionRepository = Objects.requireNonNull(journeyDistributionRepository,
                "journeyDistributionRepository must not be null");
    }

    @Override
    public boolean existsById(String journeyDistributionId) {
        return journeyDistributionRepository.existsById(journeyDistributionId);
    }
}


