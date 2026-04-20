package com.acme.personmdm.domain.contactmethod;

import com.acme.personmdm.domain.common.enumerator.ContactMethodType;
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
public class ContactMethod {
    private ContactMethodType type;
    private String methodValue;
    private String methodDescription;
    private String countryCode;
    private String areaCode;
    private boolean isPrincipal;

    public void setType(ContactMethodType type) {
        this.type = type;
    }

    public void setType(String type) {
        this.setType(ContactMethodType.fromCode(type));
    }

    public static class ContactMethodBuilder {

        public ContactMethodBuilder withType(ContactMethodType type) {
            this.type = type;
            return this;
        }

        public ContactMethodBuilder withType(String type) {
            return withType(ContactMethodType.fromCode(type));
        }
    }
}
