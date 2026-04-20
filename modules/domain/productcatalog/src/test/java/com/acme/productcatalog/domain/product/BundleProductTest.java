package com.acme.productcatalog.domain.product;

import com.acme.productcatalog.domain.product.parameterization.ProductStatus;
import com.acme.productcatalog.domain.product.decorator.BundleCapability;
import com.acme.productcatalog.domain.product.decorator.BundleCompositionPolicies;
import com.acme.productcatalog.domain.product.decorator.BundleItem;

import com.acme.shared.stereotypes.test.UnitTest;
import com.acme.shared.vo.Id;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;


import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mockStatic;
import org.mockito.MockedStatic;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@UnitTest
@DisplayName("BundleProduct")
class BundleProductTest {

    private static final String PRODUCT_UUID = "32733d75-2cea-4e5b-ad1d-add1e0d93a6b";
    private static final String SOURCE_ID = "250.1";
    private static final String NAME = "Acme Product";
    private static final String SURNAME = "Acme Product from Unit Test";
    private static final String DESCRIPTION = "Acme is a product from ACME enterprise, blep blep!";

    private static final String BUNDLE_ITEM_UUID_1 = "a8489b7d-d8f0-4f64-b812-88b00730a456";
    private static final String BUNDLE_ITEM_UUID_2 = "c2d648f1-655f-4bef-a81b-ed767d09ae69";
    private static final String BUNDLE_ITEM_UUID_3 = "76ff3932-b4f3-4361-93a9-76d6d01de875";
    private static final int MINIMUM_QUANTITY = 0;
    private static final int MAXIMUM_QUANTITY = 1;

    private BundleCapability bundleCapability;

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

        BundleCompositionPolicies bundleCompositionPolicies = new BundleCompositionPolicies(bundleCompositionRules);

        BundleItem item1 = BundleItem.rehydrate(
                BUNDLE_ITEM_UUID_1,
                ProductKind.TANGIBLE,
                true,
                MINIMUM_QUANTITY,
                MAXIMUM_QUANTITY
        );

        BundleItem item2 = BundleItem.rehydrate(
                BUNDLE_ITEM_UUID_2,
                ProductKind.TANGIBLE,
                true,
                MINIMUM_QUANTITY,
                MAXIMUM_QUANTITY
        );

        BundleItem item3 = BundleItem.rehydrate(
                BUNDLE_ITEM_UUID_3,
                ProductKind.TANGIBLE,
                true,
                MINIMUM_QUANTITY,
                MAXIMUM_QUANTITY
        );

        bundleCapability = BundleCapability.createNew(
                ProductKind.BUNDLE,
                List.of(item1, item2, item3),
                bundleCompositionPolicies);
    }

    @Test
    @DisplayName("Then I create bundle product using the factory method createNew")
    void bundleProductCreateNewFactory() {

        final Instant expectedInstant = Instant.parse("2025-11-30T10:00:00Z");

        // Use try-with-resources to mock the static Instant class
        try (MockedStatic<Instant> mockedInstant = mockStatic(Instant.class)) {

            // Configure the mock to return the expected instant when now() is called
            mockedInstant.when(Instant::now).thenReturn(expectedInstant);

            // Code under test that calls Instant.now()
            var bundleProduct = BundleProduct.createNew(SOURCE_ID, NAME, SURNAME, DESCRIPTION, Set.of(bundleCapability));

            assertAll(
                    () -> {
                        assertNotNull(bundleProduct);
                        assertInstanceOf(BundleProduct.class, bundleProduct);
                        assertInstanceOf(Product.class, bundleProduct);
                        assertEquals(SOURCE_ID, bundleProduct.sourceId());
                        assertEquals(NAME, bundleProduct.name());
                        assertEquals(SURNAME, bundleProduct.surname());
                        assertEquals(DESCRIPTION, bundleProduct.description());
                        assertEquals(ProductKind.BUNDLE, bundleProduct.productKind());
                        assertEquals(ProductStatus.DRAFT, bundleProduct.status());
                        assertNotNull(bundleProduct.productCapabilities());
                        assertEquals(1, bundleProduct.productCapabilities().size());
                        assertEquals(expectedInstant, bundleProduct.createdAt());
                        assertEquals(expectedInstant, bundleProduct.updatedAt());
                    }
            );
        }
    }

    @Test
    @DisplayName("Then I create bundle product using the factory method rehydrate")
    void bundleProductRehydrateFactory() {
        final Instant expectedInstant = Instant.parse("2025-11-30T10:00:00Z");

        var bundleProduct = BundleProduct.rehydrate(
                Id.withId(PRODUCT_UUID),
                SOURCE_ID,
                NAME,
                SURNAME,
                DESCRIPTION,
                ProductStatus.ACTIVE,
                Set.of(bundleCapability),
                expectedInstant,
                expectedInstant
        );

        assertAll(
                () -> {
                    assertNotNull(bundleProduct);
                    assertInstanceOf(BundleProduct.class, bundleProduct);
                    assertInstanceOf(Product.class, bundleProduct);
                    assertEquals(SOURCE_ID, bundleProduct.sourceId());
                    assertEquals(NAME, bundleProduct.name());
                    assertEquals(SURNAME, bundleProduct.surname());
                    assertEquals(DESCRIPTION, bundleProduct.description());
                    assertEquals(ProductKind.BUNDLE, bundleProduct.productKind());
                    assertEquals(ProductStatus.ACTIVE, bundleProduct.status());
                    assertNotNull(bundleProduct.productCapabilities());
                    assertEquals(1, bundleProduct.productCapabilities().size());
                    assertEquals(expectedInstant, bundleProduct.createdAt());
                    assertEquals(expectedInstant, bundleProduct.updatedAt());
                }
        );
    }

    @Test
    @DisplayName("Then I create bundle product using the factory method rehydrate with String Id")
    void bundleProductRehydrateFactoryWithStringId() {
        final Instant expectedInstant = Instant.parse("2025-11-30T10:00:00Z");

        var bundleProduct = BundleProduct.rehydrate(
                PRODUCT_UUID,
                SOURCE_ID,
                NAME,
                SURNAME,
                DESCRIPTION,
                ProductStatus.ACTIVE,
                Set.of(bundleCapability),
                expectedInstant,
                expectedInstant
        );

        assertAll(
                () -> {
                    assertNotNull(bundleProduct);
                    assertInstanceOf(BundleProduct.class, bundleProduct);
                    assertInstanceOf(Product.class, bundleProduct);
                    assertEquals(SOURCE_ID, bundleProduct.sourceId());
                    assertEquals(NAME, bundleProduct.name());
                    assertEquals(SURNAME, bundleProduct.surname());
                    assertEquals(DESCRIPTION, bundleProduct.description());
                    assertEquals(ProductKind.BUNDLE, bundleProduct.productKind());
                    assertEquals(ProductStatus.ACTIVE, bundleProduct.status());
                    assertNotNull(bundleProduct.productCapabilities());
                    assertEquals(1, bundleProduct.productCapabilities().size());
                    assertEquals(expectedInstant, bundleProduct.createdAt());
                    assertEquals(expectedInstant, bundleProduct.updatedAt());
                }
        );
    }

    @Test
    @DisplayName("Then I create bundle product using the factory method createNew with empty capabilities")
    void bundleProductCreateNewWithEmptyCapabilities() {
        final Instant expectedInstant = Instant.parse("2025-11-30T10:00:00Z");

        // Use try-with-resources to mock the static Instant class
        try (MockedStatic<Instant> mockedInstant = mockStatic(Instant.class)) {

            // Configure the mock to return the expected instant when now() is called
            mockedInstant.when(Instant::now).thenReturn(expectedInstant);

            // Code under test that calls Instant.now()
            var bundleProduct = BundleProduct.createNew(SOURCE_ID, NAME, SURNAME, DESCRIPTION, Set.of());

            assertAll(
                    () -> {
                        assertNotNull(bundleProduct);
                        assertInstanceOf(BundleProduct.class, bundleProduct);
                        assertInstanceOf(Product.class, bundleProduct);
                        assertEquals(SOURCE_ID, bundleProduct.sourceId());
                        assertEquals(NAME, bundleProduct.name());
                        assertEquals(SURNAME, bundleProduct.surname());
                        assertEquals(DESCRIPTION, bundleProduct.description());
                        assertEquals(ProductKind.BUNDLE, bundleProduct.productKind());
                        assertEquals(ProductStatus.DRAFT, bundleProduct.status());
                        assertNotNull(bundleProduct.productCapabilities());
                        assertEquals(0, bundleProduct.productCapabilities().size());
                        assertEquals(expectedInstant, bundleProduct.createdAt());
                        assertEquals(expectedInstant, bundleProduct.updatedAt());
                    }
            );
        }
    }

    @Test
    @DisplayName("Then I create bundle product using the factory method createNew with null capabilities")
    void bundleProductCreateNewWithNullCapabilities() {
        final Instant expectedInstant = Instant.parse("2025-11-30T10:00:00Z");

        // Use try-with-resources to mock the static Instant class
        try (MockedStatic<Instant> mockedInstant = mockStatic(Instant.class)) {

            // Configure the mock to return the expected instant when now() is called
            mockedInstant.when(Instant::now).thenReturn(expectedInstant);

            // Code under test that calls Instant.now()
            var bundleProduct = BundleProduct.createNew(SOURCE_ID, NAME, SURNAME, DESCRIPTION, null);

            assertAll(
                    () -> {
                        assertNotNull(bundleProduct);
                        assertInstanceOf(BundleProduct.class, bundleProduct);
                        assertInstanceOf(Product.class, bundleProduct);
                        assertEquals(SOURCE_ID, bundleProduct.sourceId());
                        assertEquals(NAME, bundleProduct.name());
                        assertEquals(SURNAME, bundleProduct.surname());
                        assertEquals(DESCRIPTION, bundleProduct.description());
                        assertEquals(ProductKind.BUNDLE, bundleProduct.productKind());
                        assertEquals(ProductStatus.DRAFT, bundleProduct.status());
                        assertNull(bundleProduct.productCapabilities());
                        assertEquals(expectedInstant, bundleProduct.createdAt());
                        assertEquals(expectedInstant, bundleProduct.updatedAt());
                    }
            );
        }
    }
}