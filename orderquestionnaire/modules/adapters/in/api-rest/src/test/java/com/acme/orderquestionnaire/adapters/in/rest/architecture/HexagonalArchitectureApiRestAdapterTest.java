package com.acme.orderquestionnaire.adapters.in.rest.architecture;

import com.acme.shared.stereotypes.core.UseCase;
import com.acme.shared.stereotypes.test.ArchitectureTest;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.DisplayName;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@ArchitectureTest
@DisplayName("Hexagonal Architecture: API Rest Adapter")
@AnalyzeClasses(
        packages = "com.acme.orderquestionnaire.adapters.in.rest",
        importOptions = ImportOption.DoNotIncludeTests.class
)
class HexagonalArchitectureApiRestAdapterTest {

    @ArchTest
    static final ArchRule adapters_should_not_depend_on_bootstrap =
            noClasses().that().resideInAPackage("..adapters.in.rest..")
                    .should().dependOnClassesThat().resideInAnyPackage("com.acme.orderquestionnaire.config..");

    @ArchTest
    static final ArchRule adapters_should_not_be_use_cases =
            noClasses().that().resideInAPackage("..adapters.in.rest..")
                    .should().beAnnotatedWith(UseCase.class);
}

