package com.acme.productcatalog.domain.question;

import com.acme.productcatalog.domain.common.parametrizationflow.ParametrizationStatus;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@Builder(setterPrefix = "with")
public class Question {
    private String id;
    private String label;
    private ParametrizationStatus status;
}
