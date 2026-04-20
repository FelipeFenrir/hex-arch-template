package com.acme.orderquestionnaire;

import com.acme.shared.stereotypes.core.OutputPort;
import com.acme.shared.stereotypes.core.UseCase;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.conditions.ArchConditions.haveSimpleNameNotEndingWith;
import static com.tngtech.archunit.lang.conditions.ArchConditions.notBeAnnotatedWith;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

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
                    );
}
