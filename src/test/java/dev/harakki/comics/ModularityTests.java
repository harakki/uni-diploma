package dev.harakki.comics;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

class ModularityTests {

    ApplicationModules modules = ApplicationModules.of(ComicsApplication.class);

    @Test
    void printAllModules() {
        // Prints all detected modules for verification
        modules.forEach(System.out::println);
    }

    @Test
    void verifyModularStructure() {
        // Verifies that the module structure is valid
        // Will fail if there are violations of module boundaries or cyclic dependencies
        modules.verify();
    }

    @Test
    void writeModuleDocumentation() {
        // Generates documentation for the module structure
        // This creates diagrams showing module relationships
        new Documenter(modules)
                .writeModulesAsPlantUml()
                .writeIndividualModulesAsPlantUml();
    }

}
