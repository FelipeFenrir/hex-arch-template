package com.acme.orderquestionnaire.config.adapter.out.distribution;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DistributionAdapterProperties {

    private DistributionProvider provider = DistributionProvider.MONGO;
    private boolean useAppDatabase = true;
    private DistributionIntegrationsProperties integrations = new DistributionIntegrationsProperties();
}

