package com.acme.productcatalog.domain.serviceobject.insurance;

import com.acme.shared.stereotypes.test.UnitTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@UnitTest
@DisplayName("InsuredItem")
class InsuredItemTest {

    @Test
    @DisplayName("Should create an InsuredItem with given id and type")
    void shouldCreateInsuredItem() {
        // Given
        String id = "item123";
        InsuredItemType type = InsuredItemType.builder()
                .withCode("VEHICLE")
                .withLabel("VEHICLE")
                .build();

        // When
        InsuredItem insuredItem = InsuredItem.builder()
                .withId(id)
                .withInsuredItemType(type)
                .build();

        // Then
        assertNotNull(insuredItem);
        assertEquals(id, insuredItem.getId());
        assertEquals(type, insuredItem.getInsuredItemType());
    }

    @Test
    @DisplayName("Should have proper equals and hashCode implementations")
    void shouldHaveProperEqualsAndHashCode() {
        // Given
        InsuredItemType type = InsuredItemType.builder()
                .withCode("VEHICLE")
                .withLabel("VEHICLE")
                .build();

        InsuredItem item1 = InsuredItem.builder()
                .withId("item123")
                .withInsuredItemType(type)
                .build();

        InsuredItem item2 = InsuredItem.builder()
                .withId("item123")
                .withInsuredItemType(type)
                .build();

        // Then
        assertEquals(item1, item2);
        assertEquals(item1.hashCode(), item2.hashCode());
    }
}