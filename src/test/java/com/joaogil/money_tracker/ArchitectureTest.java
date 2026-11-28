package com.joaogil.money_tracker;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import jakarta.persistence.Entity;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

@AnalyzeClasses(packages = "com.joaogil.money_tracker", importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

    @ArchTest
    static final ArchRule domain_is_pure = noClasses().that().resideInAPackage("..domain..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "org.springframework..", "jakarta..", "com.fasterxml..", "tools.jackson..",
                    "..application..", "..adapter..");

    @ArchTest
    static final ArchRule application_does_not_know_adapters = noClasses().that().resideInAPackage("..application..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "..adapter..", "org.springframework.web..", "org.springframework.data..",
                    "org.springframework.jdbc..", "org.springframework.security..", "jakarta.persistence..");

    @ArchTest
    static final ArchRule controllers_only_use_input_ports = classes().that().resideInAPackage("..adapter.in.web..")
            .should().onlyDependOnClassesThat().resideOutsideOfPackages(
                    "..application.service..", "..application.port.out..", "..adapter.out..");

    @ArchTest
    static final ArchRule jpa_entities_stay_in_persistence = classes().that().areAnnotatedWith(Entity.class)
            .should().resideInAPackage("..adapter.out.persistence..");

    @ArchTest
    static final ArchRule persistence_uses_plain_jpa = noClasses()
            .should().dependOnClassesThat().resideInAnyPackage(
                    "org.springframework.data.repository..", "org.springframework.data.jpa.repository..",
                    "org.springframework.data.domain..")
            .because("persistence adapters use the EntityManager directly, not Spring Data repositories");

    @ArchTest
    static final ArchRule layers = layeredArchitecture().consideringOnlyDependenciesInLayers()
            .layer("Domain").definedBy("..domain..")
            .layer("Application").definedBy("..application..")
            .layer("Adapter").definedBy("..adapter..")
            .layer("Config").definedBy("..config..")
            .whereLayer("Adapter").mayOnlyBeAccessedByLayers("Config")
            .whereLayer("Application").mayOnlyBeAccessedByLayers("Adapter", "Config")
            .whereLayer("Domain").mayOnlyBeAccessedByLayers("Application", "Adapter", "Config");
}