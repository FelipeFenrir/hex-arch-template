package com.acme.productcatalog.domain.serviceobject.insurance;

import com.acme.productcatalog.domain.serviceobject.ServiceObject;
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
public class InsuredItem implements ServiceObject {
    private String id;
    private InsuredItemType insuredItemType;
}
