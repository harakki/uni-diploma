@ApplicationModule(
        allowedDependencies = {
                "shared",
                "library :: api",
                "catalog :: api",
                "content :: api",
                "collections :: api"
        }
)
package dev.harakki.comics.analytics;

import org.springframework.modulith.ApplicationModule;
