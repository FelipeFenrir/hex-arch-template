package com.acme.orderquestionnaire.adapters.out.api.distribution.architecture;

import com.acme.shared.stereotypes.core.UseCase;
import com.acme.shared.stereotypes.test.ArchitectureTest;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.DisplayName;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@ArchitectureTest
@DisplayName("Hexagonal Architecture: API Distribution Adapter")
@AnalyzeClasses(
        packages = "com.acme.orderquestionnaire.adapters.out.api.distribution",
        importOptions = ImportOption.DoNotIncludeTests.class
)
class HexagonalArchitectureApiDistributionAdapterTest {

    @ArchTest
    static final ArchRule adapters_should_not_depend_on_bootstrap =
            noClasses().that().resideInAPackage("..adapters.out.api.distribution..")
                    .should().dependOnClassesThat().resideInAnyPackage("com.acme.orderquestionnaire.config..");

    @ArchTest
    static final ArchRule adapters_should_not_be_use_cases =
            noClasses().that().resideInAPackage("..adapters.out.api.distribution..")
                    .should().beAnnotatedWith(UseCase.class);
}


