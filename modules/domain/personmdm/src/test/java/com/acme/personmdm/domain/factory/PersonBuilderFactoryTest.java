package com.acme.personmdm.domain.factory;

import com.acme.personmdm.domain.common.enumerator.BiologicalSex;
import com.acme.personmdm.domain.common.enumerator.PersonType;
import com.acme.personmdm.domain.person.NaturalPerson;
import com.acme.personmdm.domain.person.Person;
import com.acme.personmdm.domain.person.factory.PersonBuilderFactory;
import com.acme.shared.stereotypes.test.UnitTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@UnitTest
@DisplayName("PersonBuilderFactory")
class PersonBuilderFactoryTest {

    @Test
    @DisplayName("Then I create natural persons using the factory")
    void naturalPersonBuilder() {
        final NaturalPerson naturalPerson = PersonBuilderFactory
                .naturalPersonBuilder()
                .withId("3b149477-a060-43d6-bb89-8a1ee3b4f806")
                .withName("Fulano de Tal")
                .withSocialName("Fulaninho")
                .withBiologicalSex("M")
                .build();

        assertAll(
                () -> {
                    assertNotNull(naturalPerson);
                    assertInstanceOf(Person.class, naturalPerson);
                    assertEquals(PersonType.NP, naturalPerson.getPersonType());
                    assertEquals(BiologicalSex.MALE, naturalPerson.getBiologicalSex());
                }
        );
    }

    @Test
    @DisplayName("Then I create legal persons using the factory")
    void legalPersonBuilder() {
        final Person legalPerson = PersonBuilderFactory
                .legalPersonBuilder()
                .withId("3b149477-a060-43d6-bb89-8a1ee3b4f806")
                .withName("Fulano LTDA")
                .withSocialName("Fulano Burgers")
                .build();

        assertAll(
                () -> {
                    assertNotNull(legalPerson);
                    assertInstanceOf(Person.class, legalPerson);
                    assertEquals(PersonType.LP, legalPerson.getPersonType());
                }
        );
    }
}