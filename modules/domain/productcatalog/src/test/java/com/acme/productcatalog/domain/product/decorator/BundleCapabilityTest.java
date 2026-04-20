package com.acme.productcatalog.domain.product.decorator;

import com.acme.productcatalog.domain.exception.EmptyBundleProductException;
import com.acme.productcatalog.domain.exception.ProductCompositionPolicyViolatedException;
import com.acme.productcatalog.domain.product.ProductKind;
import com.acme.shared.stereotypes.test.UnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;


@UnitTest
@DisplayName("BundleCapability")
class BundleCapabilityTest {

    private static final String UUID_1 = "a8489b7d-d8f0-4f64-b812-88b00730a456";
    private static final String UUID_2 = "c2d648f1-655f-4bef-a81b-ed767d09ae69";
    private static final String UUID_3 = "76ff3932-b4f3-4361-93a9-76d6d01de875";
    private static final int MINIMUM_QUANTITY = 0;
    private static final int MAXIMUM_QUANTITY = 1;

    private BundleItem item1;
    private BundleItem item2;
    private BundleItem item3;

    private BundleCompositionPolicies bundleCompositionPolicies;

    @BeforeEach
    void setUp() {
        Map<ProductKind, Set<ProductKind>> bundleCompositionRules = new HashMap<>();
        bundleCompositionRules.put(ProductKind.TANGIBLE, Set.of(
                ProductKind.TANGIBLE,
                ProductKind.BUNDLE,
                ProductKind.INSURANCE,
                ProductKind.SERVICE));
        bundleCompositionRules.put(ProductKind.INSURANCE, Set.of(
                ProductKind.BUNDLE,
                ProductKind.INSURANCE));
        bundleCompositionRules.put(ProductKind.SERVICE, Set.of(
                ProductKind.BUNDLE,
                ProductKind.INSURANCE,
                ProductKind.SERVICE));
        bundleCompositionRules.put(ProductKind.CAPITALIZATION, Set.of(
                ProductKind.BUNDLE,
                ProductKind.CAPITALIZATION));
        bundleCompositionRules.put(ProductKind.BUNDLE, Set.of(
                ProductKind.TANGIBLE,
                ProductKind.BUNDLE,
                ProductKind.INSURANCE,
                ProductKind.SERVICE,
                ProductKind.CAPITALIZATION));

        bundleCompositionPolicies = new BundleCompositionPolicies(bundleCompositionRules);

        item1 = BundleItem.rehydrate(
                UUID_1,
                ProductKind.TANGIBLE,
                true,
                MINIMUM_QUANTITY,
                MAXIMUM_QUANTITY
        );

        item2 = BundleItem.rehydrate(
                UUID_2,
                ProductKind.TANGIBLE,
                true,
                MINIMUM_QUANTITY,
                MAXIMUM_QUANTITY
        );

        item3 = BundleItem.rehydrate(
                UUID_3,
                ProductKind.TANGIBLE,
                true,
                MINIMUM_QUANTITY,
                MAXIMUM_QUANTITY
        );
    }

    @Test
    @DisplayName("Then I create bundle capability using the factory method createNew")
    void bundleCapabilityCreateNewFactory() {

        var bundleCapability = BundleCapability.createNew(
                ProductKind.BUNDLE,
                List.of(item1, item2, item3),
                bundleCompositionPolicies);

        assertAll(
                () -> {
                    assertNotNull(bundleCapability);
                    assertInstanceOf(BundleCapability.class, bundleCapability);
                    assertNotNull(bundleCapability.bundleItems());
                    assertFalse(bundleCapability.bundleItems().isEmpty());
                    assertEquals(3, bundleCapability.bundleItems().size());
                    assertTrue(bundleCapability.bundleItems().contains(item1));
                    assertTrue(bundleCapability.bundleItems().contains(item2));
                    assertTrue(bundleCapability.bundleItems().contains(item3));
                }
        );
    }

    @Test
    @DisplayName("Then I create bundle capability without bundle items")
    void bundleCapabilityCreateWithEmptyListOfBundleItens() {

        var exception_1 = assertThrows(EmptyBundleProductException.class, () -> BundleCapability.createNew(
                ProductKind.BUNDLE,
                List.of(),
                bundleCompositionPolicies
        ));

        assertAll(
                () -> {
                    assertNotNull(exception_1);
                    assertNotNull(exception_1.getMessage());
                    assertEquals("Bundle must have items", exception_1.getMessage());
                }
        );

        var exception_2 = assertThrows(EmptyBundleProductException.class, () -> BundleCapability.createNew(
                ProductKind.BUNDLE,
                null,
                bundleCompositionPolicies
        ));

        assertAll(
                () -> {
                    assertNotNull(exception_2);
                    assertNotNull(exception_2.getMessage());
                    assertEquals("Bundle must have items", exception_2.getMessage());
                }
        );
    }

    @Test
    @DisplayName("Then I create bundle capability with invalid bundle items")
    void bundleCapabilityCreateWithInvalidBundleItens() {

        var invalidItem = BundleItem.rehydrate(
                UUID_3,
                ProductKind.CAPITALIZATION,
                true,
                MINIMUM_QUANTITY,
                MAXIMUM_QUANTITY
        );

        var exception_1 = assertThrows(ProductCompositionPolicyViolatedException.class, () -> BundleCapability.createNew(
                ProductKind.INSURANCE,
                List.of(invalidItem),
                bundleCompositionPolicies
        ));

        assertAll(
                () -> {
                    assertNotNull(exception_1);
                    assertNotNull(exception_1.getMessage());
                    assertEquals(
                            "Products of kind "
                            .concat(ProductKind.INSURANCE.name())
                            .concat(" cannot contain bundle items from kind ")
                            .concat(invalidItem.productKind().name()),
                            exception_1.getMessage()
                    );
                }
        );
    }

    @Test
    @DisplayName("Then I add a bundle item in a bundle capability")
    void bundleCapabilityAddBundleItem() {

        List<BundleItem> items = new ArrayList<>();
        items.add(item1);
        items.add(item2);
        items.add(item3);


        var bundleCapability = BundleCapability.createNew(
                ProductKind.BUNDLE,
                items,
                bundleCompositionPolicies);

        assertAll(
                () -> {
                    assertNotNull(bundleCapability);
                    assertInstanceOf(BundleCapability.class, bundleCapability);
                    assertNotNull(bundleCapability.bundleItems());
                    assertFalse(bundleCapability.bundleItems().isEmpty());
                    assertEquals(3, bundleCapability.bundleItems().size());
                    assertTrue(bundleCapability.bundleItems().contains(item1));
                    assertTrue(bundleCapability.bundleItems().contains(item2));
                    assertTrue(bundleCapability.bundleItems().contains(item3));
                }
        );

        var item4 = BundleItem.rehydrate(
                "f880b8ad-40d4-4c6e-8a7b-58e2d5b2cb83",
                ProductKind.TANGIBLE,
                true,
                MINIMUM_QUANTITY,
                MAXIMUM_QUANTITY
        );

        bundleCapability.addBundleItem(ProductKind.BUNDLE, item4, bundleCompositionPolicies);

        assertAll(
                () -> {
                    assertNotNull(bundleCapability);
                    assertInstanceOf(BundleCapability.class, bundleCapability);
                    assertNotNull(bundleCapability.bundleItems());
                    assertFalse(bundleCapability.bundleItems().isEmpty());
                    assertEquals(4, bundleCapability.bundleItems().size());
                    assertTrue(bundleCapability.bundleItems().contains(item1));
                    assertTrue(bundleCapability.bundleItems().contains(item2));
                    assertTrue(bundleCapability.bundleItems().contains(item3));
                    assertTrue(bundleCapability.bundleItems().contains(item4));
                }
        );
    }

    @Test
    @DisplayName("Then I remove a bundle item in a bundle capability")
    void bundleCapabilityRemoveBundleItem() {

        List<BundleItem> items = new ArrayList<>();
        items.add(item1);
        items.add(item2);
        items.add(item3);


        var bundleCapability = BundleCapability.createNew(
                ProductKind.BUNDLE,
                items,
                bundleCompositionPolicies);

        bundleCapability.removeBundleItem(item3);

        assertAll(
                () -> {
                    assertNotNull(bundleCapability);
                    assertInstanceOf(BundleCapability.class, bundleCapability);
                    assertNotNull(bundleCapability.bundleItems());
                    assertFalse(bundleCapability.bundleItems().isEmpty());
                    assertEquals(2, bundleCapability.bundleItems().size());
                    assertTrue(bundleCapability.bundleItems().contains(item1));
                    assertTrue(bundleCapability.bundleItems().contains(item2));
                    assertFalse(bundleCapability.bundleItems().contains(item3));
                }
        );
    }

    @Test
    @DisplayName("Then I remove a bundle item in a bundle capability with one item")
    void bundleCapabilityRemoveBundleItemToZero() {

        List<BundleItem> items = new ArrayList<>();
        items.add(item1);


        var bundleCapability = BundleCapability.createNew(
                ProductKind.BUNDLE,
                items,
                bundleCompositionPolicies);

        bundleCapability.removeBundleItem(item1);

        assertAll(
                () -> {
                    assertNotNull(bundleCapability);
                    assertInstanceOf(BundleCapability.class, bundleCapability);
                    assertNotNull(bundleCapability.bundleItems());
                    assertTrue(bundleCapability.bundleItems().isEmpty());
                }
        );
    }

    @Test
    @DisplayName("Then I create bundle capability using the factory method rehydrate")
    void bundleCapabilityRehydrateFactory() {

        List<BundleItem> items = new ArrayList<>();
        items.add(item1);
        items.add(item2);
        items.add(item3);

        var bundleCapability = BundleCapability.rehydrate(items);

        assertAll(
                () -> {
                    assertNotNull(bundleCapability);
                    assertInstanceOf(BundleCapability.class, bundleCapability);
                    assertNotNull(bundleCapability.bundleItems());
                    assertFalse(bundleCapability.bundleItems().isEmpty());
                    assertEquals(3, bundleCapability.bundleItems().size());
                    assertTrue(bundleCapability.bundleItems().contains(item1));
                    assertTrue(bundleCapability.bundleItems().contains(item2));
                    assertTrue(bundleCapability.bundleItems().contains(item3));
                }
        );
    }
}