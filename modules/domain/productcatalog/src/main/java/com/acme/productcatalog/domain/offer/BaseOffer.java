package com.acme.productcatalog.domain.offer;

import com.acme.productcatalog.domain.common.parametrizationflow.ParametrizationStatus;
import com.acme.shared.vo.Id;

public class BaseOffer {
    private Id id;
    private String sourceId;
    private String name;
    private String surname;
    private String description;
    private ParametrizationStatus status;
}
