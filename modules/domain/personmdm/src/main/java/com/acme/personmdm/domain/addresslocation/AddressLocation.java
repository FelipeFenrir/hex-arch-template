package com.acme.personmdm.domain.addresslocation;

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
public class AddressLocation {
    private String addressLine1;
    private String addressLine2;    // Opcional, como a linha de endereço 2 em muitos lugares.
    private String addressLine3;    // Opcional, como a linha de endereço 3 em muitos lugares.
    private String city;
    private String stateProvince;   // Adapta-se para "estado" no Brasil ou "província" em outros países.
    private String postalCode;      // A nomenclatura "cep" para Brasil ou "zip code" para os EUA é generalizada para "postal code".
    private String countryCode;     // Código do país (ex: "BR", "US") no padrão ISO 3166.
}
