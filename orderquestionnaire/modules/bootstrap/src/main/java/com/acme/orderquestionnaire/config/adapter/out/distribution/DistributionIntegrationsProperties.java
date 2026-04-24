package com.acme.orderquestionnaire.config.adapter.out.distribution;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DistributionIntegrationsProperties {
    private ApiIntegrationProperties api = new ApiIntegrationProperties();
    //private GrpcIntegrationProperties grpc;
    //private SqsIntegrationProperties sqs;
    //private SnsIntegrationProperties sns;
    //private KafkaIntegrationProperties kafka;
    //private RabbitMqIntegrationProperties rabbitmq;
}

