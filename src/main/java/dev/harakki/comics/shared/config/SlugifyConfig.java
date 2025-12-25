package dev.harakki.comics.shared.config;

import com.github.slugify.Slugify;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class SlugifyConfig {

    @Bean
    public Slugify slugify() {
        return Slugify.builder().transliterator(true).build();
    }

}
