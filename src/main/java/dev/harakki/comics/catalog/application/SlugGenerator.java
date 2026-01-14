package dev.harakki.comics.catalog.application;

import com.github.slugify.Slugify;
import dev.harakki.comics.catalog.domain.SlugSequence;
import dev.harakki.comics.catalog.infrastructure.SlugSequenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;

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
        SlugSequence sequence = slugSequenceRepository.findBySlugPrefixWithLock(slugPrefix)
                .orElse(new SlugSequence(slugPrefix, 1L));

        // Generate the next slug value (first one will be slugPrefix-2)
        long nextVal = sequence.getCounter() + 1;
        sequence.setCounter(nextVal);

        slugSequenceRepository.save(sequence);

        return slugPrefix + "-" + nextVal;
    }

}
