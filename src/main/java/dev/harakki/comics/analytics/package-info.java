@ApplicationModule(
        allowedDependencies = {
                "shared",
                "library :: api"
        }
)
package dev.harakki.comics.analytics;

import org.springframework.modulith.ApplicationModule;
