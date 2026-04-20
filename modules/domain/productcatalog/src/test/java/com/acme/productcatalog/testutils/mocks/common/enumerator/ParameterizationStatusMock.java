package com.acme.productcatalog.testutils.mocks.common.enumerator;

import com.acme.shared.stereotypes.test.MockClass;

@MockClass
public class ParameterizationStatusMock {
    public static String ACTIVE_STRING = "ACTIVE";
    public static String INACTIVE_STRING = "INACTIVE";
    public static String DRAFT_STRING = "DRAFT";

    private ParameterizationStatusMock() {}
}
