package com.acme.personmdm.domain.person;

import com.acme.personmdm.domain.addresslocation.AddressLocation;
import com.acme.personmdm.domain.common.enumerator.ParameterizationStatus;
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
public class PersonAddress {
    private Id id;
    private AddressLocation location;
    private ParameterizationStatus status;
    private boolean isPrincipal;
    private boolean isBillingAddress;

    public void setId(String id) {
        this.id = Id.withId(id);
    }

    public void setId() {
        this.id = Id.withoutId();
    }
}
