package com.acme.orderquestionnaire.application.common;

public interface QueryHandler<Q, R> {
    R execute(Q query);
}
