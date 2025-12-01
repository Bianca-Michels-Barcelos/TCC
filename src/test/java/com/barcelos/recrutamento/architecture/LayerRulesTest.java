package com.barcelos.recrutamento.architecture;

import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;
import org.junit.jupiter.api.Test;

public class LayerRulesTest {

    @Test
    void domainShouldNotDependOnSpringOrJpa() {
        var classes = new ClassFileImporter().importPackages("com.barcelos.recrutamento");
        ArchRuleDefinition.noClasses().that()
                .resideInAnyPackage("..model..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "org.springframework", "jakarta.persistence"
                ).check(classes);
    }

    @Test
    void jpaRepositoriesMustUseEntitiesNotDomain() {
        var classes = new ClassFileImporter().importPackages("com.barcelos.recrutamento");
        ArchRuleDefinition.noClasses().that()
                .resideInAnyPackage("..data.spring..")
                .should().dependOnClassesThat().resideInAnyPackage("..core.model..")
                .because("JPA repositories must use *Entity* types")
                .check(classes);
    }
}
