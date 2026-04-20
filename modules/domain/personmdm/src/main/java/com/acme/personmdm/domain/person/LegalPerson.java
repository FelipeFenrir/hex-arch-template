package com.acme.personmdm.domain.person;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

@Getter
@Setter
@RequiredArgsConstructor
@ToString
@EqualsAndHashCode(callSuper = false)
@SuperBuilder(setterPrefix = "with")
public class LegalPerson extends Person implements Serializable {
    private String taxIdentificationNumber;
}
