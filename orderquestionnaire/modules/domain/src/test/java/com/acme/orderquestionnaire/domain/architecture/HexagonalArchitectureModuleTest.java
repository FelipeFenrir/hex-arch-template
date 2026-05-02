package com.acme.orderquestionnaire.domain.architecture;

import com.acme.shared.stereotypes.adapter.OutputAdapter;
import com.acme.shared.stereotypes.core.OutputPort;
import com.acme.shared.stereotypes.core.UseCase;
import com.acme.shared.stereotypes.test.ArchitectureTest;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.DisplayName;

import static com.tngtech.archunit.lang.conditions.ArchConditions.haveSimpleNameNotEndingWith;
import static com.tngtech.archunit.lang.conditions.ArchConditions.notBeAnnotatedWith;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

@ArchitectureTest
@DisplayName("Hexagonal Architecture: Domain Module")
@AnalyzeClasses(
        packages = "com.acme.orderquestionnaire.domain",
        importOptions = ImportOption.DoNotIncludeTests.class
)
public class HexagonalArchitectureModuleTest {

    @ArchTest
    static final ArchRule services_should_have_specific_format =
            classes().that()
                    .resideInAPackage("..domain..")
                    .should(
                            haveSimpleNameNotEndingWith("Service")
                                    .and(haveSimpleNameNotEndingWith("UseCase"))
                                    .and(notBeAnnotatedWith(UseCase.class))
                                    .and(haveSimpleNameNotEndingWith("OutPort"))
                                    .and(notBeAnnotatedWith(OutputPort.class))
                                    .and(haveSimpleNameNotEndingWith("Handler"))
                                    .and(haveSimpleNameNotEndingWith("Entity"))
                                    .and(haveSimpleNameNotEndingWith("Adapter"))
                                    .and(haveSimpleNameNotEndingWith("Repository"))
                                    .and(notBeAnnotatedWith(OutputAdapter.class))
                                    .and(haveSimpleNameNotEndingWith("Controller"))
                    );
}
