@ApplicationModule(
        allowedDependencies = {
                "shared",
                "catalog :: api",
                "library :: api"
        }
)
package dev.harakki.comics.analytics;

import org.springframework.modulith.ApplicationModule;
