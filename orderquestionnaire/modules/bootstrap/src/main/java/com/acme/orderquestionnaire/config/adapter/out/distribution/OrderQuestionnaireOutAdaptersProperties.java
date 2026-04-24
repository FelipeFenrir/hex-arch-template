package com.acme.orderquestionnaire.config.adapter.out.distribution;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@NoArgsConstructor
@ConfigurationProperties(prefix = "orderquestionnaire.adapters.out")
public class OrderQuestionnaireOutAdaptersProperties {

    private DistributionAdapterProperties channelDistribution = new DistributionAdapterProperties();
    private DistributionAdapterProperties journeyDistribution = new DistributionAdapterProperties();
}

