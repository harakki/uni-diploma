@ApplicationModule(
        allowedDependencies = {
                "shared",
                "media :: api"
        }
)
package dev.harakki.comics.catalog;

import org.springframework.modulith.ApplicationModule;
