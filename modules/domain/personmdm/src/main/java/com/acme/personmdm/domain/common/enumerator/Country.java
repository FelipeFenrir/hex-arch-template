package com.acme.personmdm.domain.common.enumerator;

import java.util.Arrays;
import java.util.Optional;

public enum Country {
    // Código do país (ex: "BR", "US") no padrão ISO 3166.
    AFGHANISTAN("AF", "Afghanistan", "AFG"),
    ALBANIA("AL", "Albania", "ALB"),
    ARGENTINA("AR", "Argentina", "ARG"),
    AUSTRALIA("AU", "Australia", "AUS"),
    AUSTRIA("AT", "Austria", "AUT"),
    BELGIUM("BE", "Belgium", "BEL"),
    BRAZIL("BR", "Brasil", "BRA"),
    CANADA("CA", "Canada", "CAN"),
    CHINA("CN", "China", "CHN"),
    COLOMBIA("CO", "Colombia", "COL"),
    FRANCE("FR", "France", "FRA"),
    GERMANY("DE", "Germany", "DEU"),
    INDIA("IN", "India", "IND"),
    ITALY("IT", "Italy", "ITA"),
    JAPAN("JP", "Japan", "JPN"),
    MEXICO("MX", "Mexico", "MEX"),
    PORTUGAL("PT", "Portugal", "PRT"),
    SPAIN("ES", "Spain", "ESP"),
    UNITED_KINGDOM("GB", "United Kingdom", "GBR"),
    UNITED_STATES("US", "United States", "USA");

    private final String alpha2Code;
    private final String countryName;
    private final String alpha3Code;

    private static final String ALPHA2CODE = "alpha2code";
    private static final String ALPHA3CODE = "alpha3code";

    public static Optional<Country> fromAlpha2Code(String alpha2Code) {
        return fromCode(alpha2Code, ALPHA2CODE);
    }

    public static Optional<Country> fromAlpha3Code(String alpha3Code) {
        return fromCode(alpha3Code, ALPHA3CODE);
    }

    private static Optional<Country> fromCode(String code, String type) {
        return Optional.ofNullable(code)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .flatMap(s -> Arrays.stream(values())
                        .filter(country ->
                                (country.alpha2Code.equalsIgnoreCase(s) && type.equalsIgnoreCase(ALPHA2CODE)) ||
                                        (country.alpha3Code.equalsIgnoreCase(s) && type.equalsIgnoreCase(ALPHA3CODE))
                        )
                        .findFirst());
    }

    Country(final String alpha2Code, final String countryName, final String alpha3Code) {
        this.alpha2Code = alpha2Code;
        this.countryName = countryName;
        this.alpha3Code = alpha3Code;
    }

    public String alpha2Code() {
        return alpha2Code;
    }
    public String alpha3Code() {
        return alpha3Code;
    }
    public String countryName() {
        return countryName;
    }
}
