package com.acme.productcatalog.domain.product.decorator;

import com.acme.productcatalog.domain.product.ProductKind;
import com.acme.shared.stereotypes.test.UnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@UnitTest
@DisplayName("BundleCompositionPolicies")
class BundleCompositionPoliciesTest {

    private Map<ProductKind, Set<ProductKind>> bundleCompositionRules;

    @BeforeEach
    void setUp() {
        bundleCompositionRules =  new HashMap<>();
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
    }

    @Test
    @DisplayName("Then I create a Bundle Composition Policies constructor method")
    void compositionPoliciesConstructorTest() {
        var bundleCompositionPolicies = new BundleCompositionPolicies(bundleCompositionRules);

        assertAll(
                () -> {
                    assertNotNull(bundleCompositionPolicies);
                    assertInstanceOf(BundleCompositionPolicies.class, bundleCompositionPolicies);
                    assertNotNull(bundleCompositionPolicies.compositionPolicies());
                }
        );
    }

    @Test
    @DisplayName("Then I verify Composition Policies from a Product Kind")
    void allowsBundleItemKindTest() {
        var bundleCompositionPolicies = new BundleCompositionPolicies(bundleCompositionRules);

        var trueCheck = bundleCompositionPolicies.allowsBundleItemKind(ProductKind.TANGIBLE, ProductKind.TANGIBLE);

        assertAll(
                () -> {
                    assertNotNull(bundleCompositionPolicies);
                    assertInstanceOf(BundleCompositionPolicies.class, bundleCompositionPolicies);
                    assertNotNull(bundleCompositionPolicies.compositionPolicies());
                    assertTrue(trueCheck);
                }
        );

        var falseCheck = bundleCompositionPolicies.allowsBundleItemKind(ProductKind.TANGIBLE, ProductKind.CAPITALIZATION);

        assertAll(
                () -> {
                    assertNotNull(bundleCompositionPolicies);
                    assertInstanceOf(BundleCompositionPolicies.class, bundleCompositionPolicies);
                    assertNotNull(bundleCompositionPolicies.compositionPolicies());
                    assertFalse(falseCheck);
                }
        );
    }


}