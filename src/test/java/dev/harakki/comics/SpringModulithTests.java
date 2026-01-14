package dev.harakki.comics;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

class SpringModulithTests {

    ApplicationModules modules = ApplicationModules.of(ComicsApplication.class);

    @Test
    void verifyModularStructure() {
        modules.verify();
    }

    @Test
    void createDocumentation() {
        // Generates C4 diagrams in target/spring-modulith-docs
        new Documenter(modules)
                .writeModulesAsPlantUml()
                .writeIndividualModulesAsPlantUml();
    }

}
