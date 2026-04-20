package com.acme.productcatalog.domain.common.vo;

import com.acme.productcatalog.domain.common.parametrizationflow.NecessaryAction;
import com.acme.productcatalog.domain.common.parametrizationflow.ParametrizationStatus;

import java.util.List;

public record TransitionStatus(
        ParametrizationStatus origin,
        ParametrizationStatus destination,
        List<NecessaryAction> actions
) { }
