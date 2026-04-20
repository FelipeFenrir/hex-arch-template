package com.acme.orderquestionnaire.application;

import com.acme.shared.stereotypes.core.OutputPort;
import com.acme.shared.stereotypes.core.UseCase;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.core.domain.JavaClass.Predicates.simpleNameEndingWith;
import static com.tngtech.archunit.lang.conditions.ArchConditions.beAnnotatedWith;
import static com.tngtech.archunit.lang.conditions.ArchConditions.beInterfaces;
import static com.tngtech.archunit.lang.conditions.ArchConditions.bePublic;
import static com.tngtech.archunit.lang.conditions.ArchConditions.beRecords;
import static com.tngtech.archunit.lang.conditions.ArchConditions.haveSimpleNameEndingWith;
import static com.tngtech.archunit.lang.conditions.ArchConditions.haveSimpleNameStartingWith;
import static com.tngtech.archunit.lang.conditions.ArchConditions.implement;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

@AnalyzeClasses(
        packages = "com.acme.orderquestionnaire.application",
        importOptions = ImportOption.DoNotIncludeTests.class
)
public class HexagonalArchitectureModuleTest {

    @ArchTest
    static final ArchRule services_should_have_specific_format =
            classes().that()
                    .resideInAPackage("..service..")
                    // Filtro: Ignoramos as interfaces de estratégia (Strategy) e extratores (Extractor)
                    // que são classes de suporte internas
                    .and().haveSimpleNameNotEndingWith("Strategy")
                    .and().haveSimpleNameNotEndingWith("Extractor")
                    .should().notBeInterfaces()
                    .andShould().bePublic() // Simplifica o "notBePackagePrivate"
                    .andShould(
                            // Opção A: É um Service que implementa UseCase
                            haveSimpleNameEndingWith("Service").and(implement(simpleNameEndingWith("UseCase")))
                                    // OU Opção B: É um Handler
                                    .or(haveSimpleNameEndingWith("Handler"))
                                    // OU Opção C: É um Assembler
                                    .or(haveSimpleNameEndingWith("Assembler"))
                    );

    @ArchTest
    static final ArchRule output_ports_should_have_specific_format =
            classes().that().resideInAPackage("..out..")
                    .should(
                            // Regra para Portas de Saida (portas de ‘out’)
                            beInterfaces()
                                    .and(bePublic())
                                    .and(haveSimpleNameEndingWith("OutPort"))
                                    .and(beAnnotatedWith(OutputPort.class))
                    );

    @ArchTest
    static final ArchRule input_ports_use_cases_should_have_specific_format =
            classes().that().resideInAPackage("..usecase..")
                    .should(
                        // Regra para Portas de Entrada - Casos de Uso (portas de ‘in’)
                        beInterfaces()
                                .and(bePublic())
                                .and(haveSimpleNameEndingWith("UseCase"))
                                .and(beAnnotatedWith(UseCase.class))
                    );

    @ArchTest
    static final ArchRule command_dto_should_have_specific_format =
            classes()
                    .that().resideInAPackage("..command..")
                    // Ignora classes aninhadas (os records internos) para que eles não precisem seguir a regra de nome
                    .and().areNotNestedClasses()
                    .and().areNotInnerClasses()
                    .should(
                            // Regra para DTO de Commands
                            haveSimpleNameEndingWith("Command").and(beRecords()).and(bePublic())
                    )
                    .orShould(
                            // Regra para Params (podem ser Record ou ‘Interface’)
                            haveSimpleNameEndingWith("Param")
                                    .and(beRecords().or(beInterfaces()))
                                    .and(bePublic())
                    );

    @ArchTest
    static final ArchRule queries_dto_should_have_specific_format =
            classes().that()
                    .resideInAPackage("..queries..")
                    .should().beRecords()
                    .andShould().bePublic()
                    .andShould(
                            haveSimpleNameEndingWith("ById")
                                    .or(haveSimpleNameEndingWith("ByFilter"))
                                    .or(haveSimpleNameStartingWith("Search"))
                                    .or(haveSimpleNameStartingWith("Get"))
                    );

    @ArchTest
    static final ArchRule view_dto_should_have_specific_format =
            classes().that()
                    .resideInAPackage("..view..")
                    .should().beRecords()
                    .andShould().bePublic()
                    .andShould(
                            haveSimpleNameEndingWith("View")
                    );

}
