package com.acme.personmdm.domain.person;

import com.acme.personmdm.domain.common.enumerator.ParameterizationStatus;
import com.acme.personmdm.domain.contactmethod.ContactMethod;
import com.acme.shared.vo.Id;
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
public class PersonContactMethod {
    private Id id;
    private ContactMethod contactMethod;
    private ParameterizationStatus status;

    public void setId(String id) {
        this.id = Id.withId(id);
    }

    public void setId() {
        this.id = Id.withoutId();
    }
}
