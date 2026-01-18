package dev.harakki.comics.catalog.application;

import com.github.slugify.Slugify;
import dev.harakki.comics.catalog.domain.SlugSequence;
import dev.harakki.comics.catalog.infrastructure.SlugSequenceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;

@Slf4j
@RequiredArgsConstructor
@Service
public class SlugGenerator {

    private final Slugify slugify;

    private final SlugSequenceRepository slugSequenceRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public String generate(String sourceName, Function<String, Boolean> existenceCheck) {
        String slugPrefix = slugify.slugify(sourceName);
        if (!existenceCheck.apply(slugPrefix)) {
            return slugPrefix;
        }

        int retries = 3;
        while (retries > 0) {
            try {
                SlugSequence sequence = slugSequenceRepository.findBySlugPrefixWithLock(slugPrefix)
                        .orElse(new SlugSequence(slugPrefix, 1L));

                // Generate the next slug value (first one will be slugPrefix-2)
                long nextVal = sequence.getCounter() + 1;
                sequence.setCounter(nextVal);

                slugSequenceRepository.save(sequence);

                var newSlug = slugPrefix + "-" + nextVal;

                // Double-check uniqueness
                if (!existenceCheck.apply(newSlug)) {
                    return newSlug;
                }

                log.warn("Slug collision detected: {}, retrying...", newSlug);
                retries--;
            } catch (Exception e) {
                log.error("Error generating slug for prefix: {}", slugPrefix, e);
                retries--;
                if (retries == 0) {
                    throw e;
                }
            }
        }

        // Fallback to random suffix if all retries failed
        return slugPrefix + "-" + System.currentTimeMillis() + "-" + ThreadLocalRandom.current().nextInt(1000);
    }

}
