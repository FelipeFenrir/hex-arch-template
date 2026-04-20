package com.acme.shared.testutils;

import com.acme.shared.stereotypes.test.MockClass;

@MockClass
public record MockDomainClass(
        String id,
        String name,
        String socialName
) { }