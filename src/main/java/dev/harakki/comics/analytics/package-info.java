@ApplicationModule(
        allowedDependencies = {
                "shared",
                "catalog :: api"
        }
)
package dev.harakki.comics.analytics;

import org.springframework.modulith.ApplicationModule;
