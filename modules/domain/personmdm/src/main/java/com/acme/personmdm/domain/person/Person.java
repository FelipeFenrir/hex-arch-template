package com.acme.personmdm.domain.person;

import com.acme.personmdm.domain.common.enumerator.PersonType;
import com.acme.shared.vo.Id;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@RequiredArgsConstructor
@ToString
@EqualsAndHashCode
@SuperBuilder(setterPrefix = "with")
public abstract class Person {
    private Id id;
    private String name;
    private String socialName;
    private PersonType personType;
    private List<PersonContactMethod> contactMethods;
    private List<PersonAddress> locations;
    private Instant createdAt;
    private Instant updatedAt;

    public void setId(String id) {
        this.id = Id.withId(id);
    }

    public void setId() {
        this.id = Id.withoutId();
    }

    public static abstract class PersonBuilder<C extends Person, B extends PersonBuilder<C, B>> {
        public B withId(String id) {
            this.id = Id.withId(id);
            return self();
        }

        public B withoutId() {
            this.id = Id.withoutId();
            return self();
        }

        public B personTypeByStr(String personType) {
            this.personType = PersonType.fromCode(personType);
            return self();
        }
    }

}
