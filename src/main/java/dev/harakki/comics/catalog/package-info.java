@ApplicationModule(
        allowedDependencies = {
                "shared",
                "media :: api",
                "analytics :: api"
        }
)
package dev.harakki.comics.catalog;

import org.springframework.modulith.ApplicationModule;
