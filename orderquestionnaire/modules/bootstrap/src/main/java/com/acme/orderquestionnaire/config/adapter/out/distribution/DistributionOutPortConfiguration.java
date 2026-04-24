package com.acme.orderquestionnaire.config.adapter.out.distribution;

import com.acme.orderquestionnaire.application.channel.port.out.ChannelDistributionOutPort;
import com.acme.orderquestionnaire.application.journey.port.out.JourneyDistributionOutPort;
import com.acme.orderquestionnaire.config.adapter.out.distribution.channel.ChannelDistributionOutPortDelegate;
import com.acme.orderquestionnaire.config.adapter.out.distribution.journey.JourneyDistributionOutPortDelegate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.List;

@Configuration
@EnableConfigurationProperties(OrderQuestionnaireOutAdaptersProperties.class)
public class DistributionOutPortConfiguration {

    @Bean
    public DistributionAdapterStrategy<ChannelDistributionOutPort> channelDistributionMongoStrategy(
            @Qualifier("channelDistributionMongoAdapter") ChannelDistributionOutPort adapter
    ) {
        return new DistributionAdapterStrategy<>(DistributionProvider.MONGO, adapter);
    }

    @Bean
    public DistributionAdapterStrategy<ChannelDistributionOutPort> channelDistributionApiStrategy(
            @Qualifier("channelDistributionApiAdapter") ChannelDistributionOutPort adapter
    ) {
        return new DistributionAdapterStrategy<>(DistributionProvider.API, adapter);
    }

    @Bean
    @Primary
    public ChannelDistributionOutPort channelDistributionOutPort(
            OrderQuestionnaireOutAdaptersProperties properties,
            List<DistributionAdapterStrategy<ChannelDistributionOutPort>> strategies
    ) {
        return new ChannelDistributionOutPortDelegate(properties.getChannelDistribution(), strategies);
    }

    @Bean
    public DistributionAdapterStrategy<JourneyDistributionOutPort> journeyDistributionMongoStrategy(
            @Qualifier("journeyDistributionMongoAdapter") JourneyDistributionOutPort adapter
    ) {
        return new DistributionAdapterStrategy<>(DistributionProvider.MONGO, adapter);
    }

    @Bean
    public DistributionAdapterStrategy<JourneyDistributionOutPort> journeyDistributionApiStrategy(
            @Qualifier("journeyDistributionApiAdapter") JourneyDistributionOutPort adapter
    ) {
        return new DistributionAdapterStrategy<>(DistributionProvider.API, adapter);
    }

    @Bean
    @Primary
    public JourneyDistributionOutPort journeyDistributionOutPort(
            OrderQuestionnaireOutAdaptersProperties properties,
            List<DistributionAdapterStrategy<JourneyDistributionOutPort>> strategies
    ) {
        return new JourneyDistributionOutPortDelegate(properties.getJourneyDistribution(), strategies);
    }

    @Bean
    public DistributionAdaptersConfigValidator distributionAdaptersConfigValidator(
            OrderQuestionnaireOutAdaptersProperties properties
    ) {
        return new DistributionAdaptersConfigValidator(properties);
    }
}

