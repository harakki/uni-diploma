@ApplicationModule(
        allowedDependencies = {
                "shared",
                "analytics :: api"
        }
)
package dev.harakki.comics.collections;

import org.springframework.modulith.ApplicationModule;
