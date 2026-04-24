package com.acme.orderquestionnaire.config.adapter.out.distribution;

public enum DistributionProvider {
    MONGO(true),
    API(true),
    GRPC(false),
    SQS(false),
    SNS(false),
    KAFKA(false),
    RABBITMQ(false);

    private final boolean enabled;

    DistributionProvider(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }
}

