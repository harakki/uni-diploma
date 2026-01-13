@ApplicationModule(
        allowedDependencies = {
                "shared",
                "media :: api"
        }
)
package dev.harakki.comics.content;

import org.springframework.modulith.ApplicationModule;
