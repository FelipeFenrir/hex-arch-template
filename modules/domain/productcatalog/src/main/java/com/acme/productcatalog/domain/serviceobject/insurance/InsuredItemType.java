package com.acme.productcatalog.domain.serviceobject.insurance;

import com.acme.productcatalog.domain.common.parametrizationflow.ParametrizationStatus;
import com.acme.productcatalog.domain.question.Question;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@Builder(setterPrefix = "with")
public class InsuredItemType {
    private String code;
    private String label;
    private ParametrizationStatus status;
    private List<Question> questions;
}
