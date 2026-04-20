package com.acme.productcatalog.domain.product.decorator;

import com.acme.productcatalog.domain.exception.BundleItemInvalidConfigurationException;
import com.acme.productcatalog.domain.product.ProductKind;
import com.acme.shared.stereotypes.test.UnitTest;
import com.acme.shared.vo.Id;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@UnitTest
@DisplayName("BundleItem")
class BundleItemTest {

    private static final String UUID = "a8489b7d-d8f0-4f64-b812-88b00730a456";
    private static final int MINIMUM_QUANTITY = 0;
    private static final int MAXIMUM_QUANTITY = 1;

    @Test
    @DisplayName("Then I create bundle item using the factory method createNew")
    void bundleItemCreateNewFactory() {
        var item = BundleItem.createNew(
                ProductKind.TANGIBLE,
                true,
                MINIMUM_QUANTITY,
                MAXIMUM_QUANTITY
        );

        assertAll(
                () -> {
                    assertNotNull(item);
                    assertInstanceOf(BundleItem.class, item);
                    assertNotNull(item.productId());
                    assertEquals(ProductKind.TANGIBLE, item.productKind());
                    assertTrue(item.isMandatory());
                    assertEquals(MINIMUM_QUANTITY, item.minimumQuantity());
                    assertEquals(MAXIMUM_QUANTITY, item.maximumQuantity());
                }
        );
    }

    @Test
    @DisplayName("Then I create bundle item using the factory method rehydrate")
    void bundleItemRehydrateFactory() {
        var id = Id.withoutId();

        var item = BundleItem.rehydrate(
                id,
                ProductKind.TANGIBLE,
                true,
                MINIMUM_QUANTITY,
                MAXIMUM_QUANTITY
        );

        assertAll(
                () -> {
                    assertNotNull(item);
                    assertInstanceOf(BundleItem.class, item);
                    assertEquals(id, item.productId());
                    assertEquals(ProductKind.TANGIBLE, item.productKind());
                    assertTrue(item.isMandatory());
                    assertEquals(MINIMUM_QUANTITY, item.minimumQuantity());
                    assertEquals(MAXIMUM_QUANTITY, item.maximumQuantity());
                }
        );
    }

    @Test
    @DisplayName("Then I create bundle item using the factory method rehydrate with String Id")
    void bundleItemRehydrateFactoryWithStringId() {
        var item = BundleItem.rehydrate(
                UUID,
                ProductKind.TANGIBLE,
                true,
                MINIMUM_QUANTITY,
                MAXIMUM_QUANTITY
        );

        assertAll(
                () -> {
                    assertNotNull(item);
                    assertInstanceOf(BundleItem.class, item);
                    assertEquals(UUID, item.productId().stringfyId());
                    assertEquals(ProductKind.TANGIBLE, item.productKind());
                    assertTrue(item.isMandatory());
                    assertEquals(MINIMUM_QUANTITY, item.minimumQuantity());
                    assertEquals(MAXIMUM_QUANTITY, item.maximumQuantity());
                }
        );
    }

    @Test
    @DisplayName("Then I create bundle item and adjust cardinality")
    void bundleItemAjustCardinality() {
        var item = BundleItem.rehydrate(
                UUID,
                ProductKind.TANGIBLE,
                true,
                MINIMUM_QUANTITY,
                MAXIMUM_QUANTITY
        );

        assertAll(
                () -> {
                    assertNotNull(item);
                    assertInstanceOf(BundleItem.class, item);
                    assertEquals(UUID, item.productId().stringfyId());
                    assertEquals(ProductKind.TANGIBLE, item.productKind());
                    assertTrue(item.isMandatory());
                    assertEquals(MINIMUM_QUANTITY, item.minimumQuantity());
                    assertEquals(MAXIMUM_QUANTITY, item.maximumQuantity());
                }
        );

        item.adjustCardinality(1, 100);

        assertAll(
                () -> {
                    assertEquals(UUID, item.productId().stringfyId());
                    assertEquals(1, item.minimumQuantity());
                    assertEquals(100, item.maximumQuantity());
                    assertNotEquals(1, MINIMUM_QUANTITY);
                    assertNotEquals(100, MAXIMUM_QUANTITY);
                }
        );
    }

    @Test
    @DisplayName("Then I create bundle item and set mandatory")
    void bundleItemSetMandatory() {
        var item = BundleItem.rehydrate(
                UUID,
                ProductKind.TANGIBLE,
                true,
                MINIMUM_QUANTITY,
                MAXIMUM_QUANTITY
        );

        assertAll(
                () -> {
                    assertNotNull(item);
                    assertInstanceOf(BundleItem.class, item);
                    assertEquals(UUID, item.productId().stringfyId());
                    assertEquals(ProductKind.TANGIBLE, item.productKind());
                    assertTrue(item.isMandatory());
                    assertEquals(MINIMUM_QUANTITY, item.minimumQuantity());
                    assertEquals(MAXIMUM_QUANTITY, item.maximumQuantity());
                }
        );

        item.isMandatory(false);

        assertAll(
                () -> {
                    assertEquals(UUID, item.productId().stringfyId());
                    assertFalse(item.isMandatory());
                }
        );
    }

    @Test
    @DisplayName("Then I create bundle item, but the minimum quantity is less than 0")
    void bundleItemWithMinimumInvalid() {
        var exception = assertThrows(BundleItemInvalidConfigurationException.class, () -> BundleItem.createNew(
                ProductKind.TANGIBLE,
                true,
                -1,
                1
        ));

        assertAll(
                () -> {
                    assertNotNull(exception);
                    assertNotNull(exception.getMessage());
                    assertEquals("Invalid cardinality of the bundle item: " +
                            "the minimum quantity is 0 or less", exception.getMessage());
                }
        );
    }

    @Test
    @DisplayName("Then I create bundle item, but the maximum quantity is less than 1")
    void bundleItemWithMaximumInvalid() {
        var exception = assertThrows(BundleItemInvalidConfigurationException.class, () -> BundleItem.createNew(
                ProductKind.TANGIBLE,
                true,
                0,
                0
        ));

        assertAll(
                () -> {
                    assertNotNull(exception);
                    assertNotNull(exception.getMessage());
                    assertEquals("Invalid cardinality of the bundle item: " +
                            "the maximum quantity is less than 1", exception.getMessage());
                }
        );
    }

    @Test
    @DisplayName("Then I create bundle item, but the minimum quantity is greater than the maximum quantity")
    void bundleItemWithMinimumGreaterMaximum() {
        var exception = assertThrows(BundleItemInvalidConfigurationException.class, () -> BundleItem.createNew(
                ProductKind.TANGIBLE,
                true,
                10,
                1
        ));

        assertAll(
                () -> {
                    assertNotNull(exception);
                    assertNotNull(exception.getMessage());
                    assertEquals("Invalid cardinality of the bundle item: " +
                            "the minimum quantity is greater than the maximum quantity", exception.getMessage());
                }
        );
    }
}