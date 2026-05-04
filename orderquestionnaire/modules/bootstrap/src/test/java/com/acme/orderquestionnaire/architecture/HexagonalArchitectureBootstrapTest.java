package com.acme.orderquestionnaire.architecture;

import com.acme.shared.stereotypes.test.ArchitectureTest;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.DisplayName;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@ArchitectureTest
@DisplayName("Hexagonal Architecture: Bootstrap Module")
@AnalyzeClasses(
        packages = "com.acme.orderquestionnaire",
        importOptions = ImportOption.DoNotIncludeTests.class
)
class HexagonalArchitectureBootstrapTest {

    @ArchTest
    static final ArchRule domain_should_not_depend_on_bootstrap =
            noClasses().that().resideInAPackage("com.acme.orderquestionnaire.domain..")
                    .should().dependOnClassesThat().resideInAPackage("com.acme.orderquestionnaire.config..");

    @ArchTest
    static final ArchRule application_should_not_depend_on_bootstrap =
            noClasses().that().resideInAPackage("com.acme.orderquestionnaire.application..")
                    .should().dependOnClassesThat().resideInAPackage("com.acme.orderquestionnaire.config..");
}


