package com.acme.personmdm.domain.contactmethod;

import com.acme.personmdm.domain.common.enumerator.ContactMethodType;
import com.acme.shared.stereotypes.test.UnitTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@UnitTest
@DisplayName("ContactMethod")
class ContactMethodTest {

    public static final String DESCRIPTION = "My personal phone Number.";
    public static final String PHONE = "97875431";
    public static final String COUNTRY_CODE = "55";
    public static final String AREA_CODE = "13";

    private static final ContactMethod contactMethod_with_type_string = ContactMethod.builder()
            .withType("phone")
            .withMethodDescription(DESCRIPTION)
            .withMethodValue(PHONE)
            .withCountryCode(COUNTRY_CODE)
            .withAreaCode(AREA_CODE)
            .withIsPrincipal(true)
            .build();

    private static final ContactMethod contactMethod_with_type_object = ContactMethod.builder()
            .withType(ContactMethodType.PHONE)
            .withMethodDescription(DESCRIPTION)
            .withMethodValue(PHONE)
            .withCountryCode(COUNTRY_CODE)
            .withAreaCode(AREA_CODE)
            .withIsPrincipal(true)
            .build();

    private static final ContactMethod contactMethod_with_type_string_and_nulls = ContactMethod.builder()
            .withType("phone")
            .withMethodValue(PHONE)
            .withIsPrincipal(true)
            .build();

    private static final ContactMethod contactMethod_with_type_object_and_nulls = ContactMethod.builder()
            .withType(ContactMethodType.PHONE)
            .withMethodValue(PHONE)
            .withIsPrincipal(true)
            .build();

    @Test
    @DisplayName("Then Can I if two objects are equal")
    void testEquals() {
        var obj1 = contactMethod_with_type_string;
        var obj2 = contactMethod_with_type_string;

        assertAll(
                () -> assertNotNull(obj1),
                () -> assertNotNull(obj2),
                () -> assertEquals(obj1, obj2),
                () -> assertEquals(contactMethod_with_type_string, obj1),
                () -> assertEquals(contactMethod_with_type_string, obj2),
                () -> assertEquals(contactMethod_with_type_string, contactMethod_with_type_object)
        );
    }

    @Test
    @DisplayName("Then Can I check the equality of objects")
    void canEqual() {
        assertTrue(contactMethod_with_type_string.canEqual(contactMethod_with_type_object));
    }

    @Test
    @DisplayName("Then I create and convert in to a hash code")
    void testHashCode() {
        var hash = contactMethod_with_type_string.hashCode();

        assertEquals(contactMethod_with_type_string.hashCode(), hash);
    }

    @Test
    @DisplayName("Then I create and get string of contact method")
    void testToString() {
        var contactString = contactMethod_with_type_string.toString();
        var assertion = "ContactMethod(type=PHONE, methodValue=97875431, " +
                "methodDescription=My personal phone Number., countryCode=55, " +
                "areaCode=13, isPrincipal=true)";
        assertNotNull(contactString);
        assertEquals(assertion, contactString);
    }

    @ParameterizedTest
    @DisplayName("Then I create a phone contact using the builder")
    @MethodSource("contactMethodProvider_builder")
    void builderAContactMethod(ContactMethod contactMethod) {
        Assertions.assertAll(
                () -> assertNotNull(contactMethod),
                () -> assertEquals(ContactMethodType.PHONE, contactMethod.getType()),
                () -> assertEquals(PHONE, contactMethod.getMethodValue()),
                () -> assertEquals(DESCRIPTION, contactMethod.getMethodDescription()),
                () -> assertEquals(AREA_CODE, contactMethod.getAreaCode()),
                () -> assertEquals(COUNTRY_CODE, contactMethod.getCountryCode()),
                () -> assertNotNull(contactMethod.getMethodValue()),
                () -> assertNotNull(contactMethod.isPrincipal())
        );
    }

    static Stream<Arguments> contactMethodProvider_builder() {
        return Stream.of(
                Arguments.of(contactMethod_with_type_string),
                Arguments.of(contactMethod_with_type_object)
        );
    }

    @ParameterizedTest
    @DisplayName("Then I create a phone contact using the builder with nulls")
    @MethodSource("contactMethodProvider_builder_with_nulls")
    void builderAContactMethodWithNulls(ContactMethod contactMethod) {
        Assertions.assertAll(
                () -> assertNotNull(contactMethod),
                () -> assertEquals(ContactMethodType.PHONE, contactMethod.getType()),
                () -> assertNotNull(contactMethod.getMethodValue()),
                () -> assertNotNull(contactMethod.isPrincipal()),
                () -> assertNull(contactMethod.getMethodDescription()),
                () -> assertNull(contactMethod.getCountryCode()),
                () -> assertNull(contactMethod.getAreaCode())
        );
    }

    static Stream<Arguments> contactMethodProvider_builder_with_nulls() {
        return Stream.of(
                Arguments.of(contactMethod_with_type_string_and_nulls),
                Arguments.of(contactMethod_with_type_object_and_nulls)
        );
    }
}