package dev.harakki.comics.catalog.application;

import com.github.slugify.Slugify;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;

@RequiredArgsConstructor
@Service
public class SlugGenerator {

    private final Slugify slugify;
    
    private final static int SLUG_SUFFIX_LENGTH = 4;

    public String generate(String sourceName, Function<String, Boolean> existenceCheck) {
        String baseSlug = slugify.slugify(sourceName);
        if (!existenceCheck.apply(baseSlug)) {
            return baseSlug;
        }
        int attempts = 1;
        while (attempts < 10) {
            String candidate = baseSlug + "-" + generateShortSuffix();
            if (!existenceCheck.apply(candidate)) {
                return candidate;
            }
            attempts++;
        }
        throw new IllegalStateException("Could not generate unique slug for: " + sourceName);
    }

    private String generateShortSuffix() {
        String alphabet = "0123456789abcdefghijklmnopqrstuvwxyz";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < SLUG_SUFFIX_LENGTH; i++) {
            int index = ThreadLocalRandom.current().nextInt(alphabet.length());
            sb.append(alphabet.charAt(index));
        }
        return sb.toString();
    }

}
