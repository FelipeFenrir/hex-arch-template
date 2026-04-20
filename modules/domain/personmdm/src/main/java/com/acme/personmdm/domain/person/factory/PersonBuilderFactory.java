package com.acme.personmdm.domain.person.factory;

import com.acme.personmdm.domain.common.enumerator.PersonType;
import com.acme.personmdm.domain.person.LegalPerson;
import com.acme.personmdm.domain.person.NaturalPerson;

public class PersonBuilderFactory {

    public static NaturalPerson.NaturalPersonBuilderImpl naturalPersonBuilder() {
        return NaturalPerson.builder().withPersonType(PersonType.NP);
    }

    public static LegalPerson.LegalPersonBuilder<?,?> legalPersonBuilder() {
        return LegalPerson.builder().withPersonType(PersonType.LP);
    }

}