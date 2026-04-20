package com.acme.productcatalog.domain.product;

import com.acme.productcatalog.domain.product.parameterization.ProductStatus;
import com.acme.shared.stereotypes.test.UnitTest;
import com.acme.shared.vo.Id;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.time.Instant;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mockStatic;

@UnitTest
@DisplayName("SimpleProduct")
class SimpleProductTest {

    private static final String PRODUCT_UUID = "32733d75-2cea-4e5b-ad1d-add1e0d93a6b";
    private static final String SOURCE_ID = "250.1";
    private static final String NAME = "Acme Product";
    private static final String SURNAME = "Acme Product from Unit Test";
    private static final String DESCRIPTION = "Acme is a product from ACME enterprise, blep blep!";

    @BeforeEach
    void setUp() {

    }

    @Test
    @DisplayName("Then I create simple product using the factory method createNew")
    void simpleProductCreateNewFactory() {

        final Instant expectedInstant = Instant.parse("2025-11-30T10:00:00Z");

        // Use try-with-resources to mock the static Instant class
        try (MockedStatic<Instant> mockedInstant = mockStatic(Instant.class)) {

            // Configure the mock to return the expected instant when now() is called
            mockedInstant.when(Instant::now).thenReturn(expectedInstant);

            // Code under test that calls Instant.now()
            var simpleProduct = SimpleProduct.createNew(SOURCE_ID, NAME, SURNAME, DESCRIPTION);

            assertAll(
                    () -> {
                        assertNotNull(simpleProduct);
                        assertInstanceOf(SimpleProduct.class, simpleProduct);
                        assertInstanceOf(Product.class, simpleProduct);
                        assertEquals(SOURCE_ID, simpleProduct.sourceId());
                        assertEquals(NAME, simpleProduct.name());
                        assertEquals(SURNAME, simpleProduct.surname());
                        assertEquals(DESCRIPTION, simpleProduct.description());
                        assertEquals(ProductStatus.DRAFT, simpleProduct.status());
                        assertNotNull(simpleProduct.productCapabilities());
                        assertTrue(simpleProduct.productCapabilities().isEmpty());
                        assertEquals(expectedInstant, simpleProduct.createdAt());
                        assertEquals(expectedInstant, simpleProduct.updatedAt());
                    }
            );
        }
    }

    @Test
    @DisplayName("Then I create simple product using the factory method rehydrate")
    void simpleProductRehydrateFactory() {
        final Instant expectedInstant = Instant.parse("2025-11-30T10:00:00Z");

        var simpleProduct = SimpleProduct.rehydrate(
                Id.withId(PRODUCT_UUID),
                SOURCE_ID,
                NAME,
                SURNAME,
                DESCRIPTION,
                ProductStatus.ACTIVE,
                Set.of(),
                expectedInstant,
                expectedInstant
        );

        assertAll(
                () -> {
                    assertNotNull(simpleProduct);
                    assertInstanceOf(SimpleProduct.class, simpleProduct);
                    assertInstanceOf(Product.class, simpleProduct);
                    assertEquals(SOURCE_ID, simpleProduct.sourceId());
                    assertEquals(NAME, simpleProduct.name());
                    assertEquals(SURNAME, simpleProduct.surname());
                    assertEquals(DESCRIPTION, simpleProduct.description());
                    assertEquals(ProductStatus.ACTIVE, simpleProduct.status());
                    assertNotNull(simpleProduct.productCapabilities());
                    assertTrue(simpleProduct.productCapabilities().isEmpty());
                    assertEquals(expectedInstant, simpleProduct.createdAt());
                    assertEquals(expectedInstant, simpleProduct.updatedAt());
                }
        );
    }

    @Test
    @DisplayName("Then I create simple product using the factory method rehydrate with String Id")
    void simpleProductRehydrateFactoryWithStringId() {
        final Instant expectedInstant = Instant.parse("2025-11-30T10:00:00Z");

        var simpleProduct = SimpleProduct.rehydrate(
                PRODUCT_UUID,
                SOURCE_ID,
                NAME,
                SURNAME,
                DESCRIPTION,
                ProductStatus.ACTIVE,
                Set.of(),
                expectedInstant,
                expectedInstant
        );

        assertAll(
                () -> {
                    assertNotNull(simpleProduct);
                    assertInstanceOf(SimpleProduct.class, simpleProduct);
                    assertInstanceOf(Product.class, simpleProduct);
                    assertEquals(SOURCE_ID, simpleProduct.sourceId());
                    assertEquals(NAME, simpleProduct.name());
                    assertEquals(SURNAME, simpleProduct.surname());
                    assertEquals(DESCRIPTION, simpleProduct.description());
                    assertEquals(ProductStatus.ACTIVE, simpleProduct.status());
                    assertNotNull(simpleProduct.productCapabilities());
                    assertTrue(simpleProduct.productCapabilities().isEmpty());
                    assertEquals(expectedInstant, simpleProduct.createdAt());
                    assertEquals(expectedInstant, simpleProduct.updatedAt());
                }
        );
    }

    @Test
    @DisplayName("Then I create simple product using the factory method rehydrate with String Id and null description")
    void simpleProductRehydrateFactoryWithStringIdAndNullDescription() {
        final Instant expectedInstant = Instant.parse("2025-11-30T10:00:00Z");

        var simpleProduct = SimpleProduct.rehydrate(
                PRODUCT_UUID,
                SOURCE_ID,
                NAME,
                SURNAME,
                null,
                ProductStatus.ACTIVE,
                Set.of(),
                expectedInstant,
                expectedInstant
        );

        assertAll(
                () -> {
                    assertNotNull(simpleProduct);
                    assertInstanceOf(SimpleProduct.class, simpleProduct);
                    assertInstanceOf(Product.class, simpleProduct);
                    assertEquals(SOURCE_ID, simpleProduct.sourceId());
                    assertEquals(NAME, simpleProduct.name());
                    assertEquals(SURNAME, simpleProduct.surname());
                    assertNull(simpleProduct.description());
                    assertEquals(ProductStatus.ACTIVE, simpleProduct.status());
                    assertNotNull(simpleProduct.productCapabilities());
                    assertTrue(simpleProduct.productCapabilities().isEmpty());
                    assertEquals(expectedInstant, simpleProduct.createdAt());
                    assertEquals(expectedInstant, simpleProduct.updatedAt());
                }
        );
    }

    @Test
    @DisplayName("Then I create simple product using the factory method rehydrate with String Id and null description and null surname")
    void simpleProductRehydrateFactoryWithStringIdAndNullDescriptionAndNullSurname() {
        final Instant expectedInstant = Instant.parse("2025-11-30T10:00:00Z");

        var simpleProduct = SimpleProduct.rehydrate(
                PRODUCT_UUID,
                SOURCE_ID,
                NAME,
                null,
                null,
                ProductStatus.ACTIVE,
                Set.of(),
                expectedInstant,
                expectedInstant
        );

        assertAll(
                () -> {
                    assertNotNull(simpleProduct);
                    assertInstanceOf(SimpleProduct.class, simpleProduct);
                    assertInstanceOf(Product.class, simpleProduct);
                    assertEquals(SOURCE_ID, simpleProduct.sourceId());
                    assertEquals(NAME, simpleProduct.name());
                    assertNull(simpleProduct.surname());
                    assertNull(simpleProduct.description());
                    assertEquals(ProductStatus.ACTIVE, simpleProduct.status());
                    assertNotNull(simpleProduct.productCapabilities());
                    assertTrue(simpleProduct.productCapabilities().isEmpty());
                    assertEquals(expectedInstant, simpleProduct.createdAt());
                    assertEquals(expectedInstant, simpleProduct.updatedAt());
                }
        );
    }

    @Test
    @DisplayName("Then I create simple product using the factory method rehydrate with String Id and null description and null surname and null name")
    void simpleProductRehydrateFactoryWithStringIdAndNullDescriptionAndNullSurnameAndNullName() {
        final Instant expectedInstant = Instant.parse("2025-11-30T10:00:00Z");

        var simpleProduct = SimpleProduct.rehydrate(
                PRODUCT_UUID,
                SOURCE_ID,
                null,
                null,
                null,
                ProductStatus.ACTIVE,
                Set.of(),
                expectedInstant,
                expectedInstant
        );

        assertAll(
                () -> {
                    assertNotNull(simpleProduct);
                    assertInstanceOf(SimpleProduct.class, simpleProduct);
                    assertInstanceOf(Product.class, simpleProduct);
                    assertEquals(SOURCE_ID, simpleProduct.sourceId());
                    assertNull(simpleProduct.name());
                    assertNull(simpleProduct.surname());
                    assertNull(simpleProduct.description());
                    assertEquals(ProductStatus.ACTIVE, simpleProduct.status());
                    assertNotNull(simpleProduct.productCapabilities());
                    assertTrue(simpleProduct.productCapabilities().isEmpty());
                    assertEquals(expectedInstant, simpleProduct.createdAt());
                    assertEquals(expectedInstant, simpleProduct.updatedAt());
                }
        );
    }

    @Test
    @DisplayName("Then I create simple product using the factory method rehydrate with String Id and null description and null surname and null name and null sourceId")
    void simpleProductRehydrateFactoryWithStringIdAndNullDescriptionAndNullSurnameAndNullNameAndNullSourceId() {
        final Instant expectedInstant = Instant.parse("2025-11-30T10:00:00Z");

        var simpleProduct = SimpleProduct.rehydrate(
                PRODUCT_UUID,
                null,
                null,
                null,
                null,
                ProductStatus.ACTIVE,
                Set.of(),
                expectedInstant,
                expectedInstant
        );

        assertAll(
                () -> {
                    assertNotNull(simpleProduct);
                    assertInstanceOf(SimpleProduct.class, simpleProduct);
                    assertInstanceOf(Product.class, simpleProduct);
                    assertNull(simpleProduct.sourceId());
                    assertNull(simpleProduct.name());
                    assertNull(simpleProduct.surname());
                    assertNull(simpleProduct.description());
                    assertEquals(ProductStatus.ACTIVE, simpleProduct.status());
                    assertNotNull(simpleProduct.productCapabilities());
                    assertTrue(simpleProduct.productCapabilities().isEmpty());
                    assertEquals(expectedInstant, simpleProduct.createdAt());
                    assertEquals(expectedInstant, simpleProduct.updatedAt());
                }
        );
    }

    @Test
    @DisplayName("Then I create simple product using the factory method rehydrate with String Id and null description and null surname and null name and null sourceId and null status")
    void simpleProductRehydrateFactoryWithStringIdAndNullDescriptionAndNullSurnameAndNullNameAndNullSourceIdAndNullStatus() {
        final Instant expectedInstant = Instant.parse("2025-11-30T10:00:00Z");

        var simpleProduct = SimpleProduct.rehydrate(
                PRODUCT_UUID,
                null,
                null,
                null,
                null,
                null,
                Set.of(),
                expectedInstant,
                expectedInstant
        );

        assertAll(
                () -> {
                    assertNotNull(simpleProduct);
                    assertInstanceOf(SimpleProduct.class, simpleProduct);
                    assertInstanceOf(Product.class, simpleProduct);
                    assertNull(simpleProduct.sourceId());
                    assertNull(simpleProduct.name());
                    assertNull(simpleProduct.surname());
                    assertNull(simpleProduct.description());
                    assertNull(simpleProduct.status());
                    assertNotNull(simpleProduct.productCapabilities());
                    assertTrue(simpleProduct.productCapabilities().isEmpty());
                    assertEquals(expectedInstant, simpleProduct.createdAt());
                    assertEquals(expectedInstant, simpleProduct.updatedAt());
                }
        );
    }
}