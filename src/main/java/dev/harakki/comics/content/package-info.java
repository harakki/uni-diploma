@ApplicationModule(
        allowedDependencies = {
                "shared",
                "media :: api",
                "analytics :: api"
        }
)
package dev.harakki.comics.content;

import org.springframework.modulith.ApplicationModule;
