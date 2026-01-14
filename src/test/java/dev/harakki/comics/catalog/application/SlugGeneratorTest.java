package dev.harakki.comics.catalog.application;

import com.github.slugify.Slugify;
import dev.harakki.comics.catalog.domain.SlugSequence;
import dev.harakki.comics.catalog.infrastructure.SlugSequenceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.function.Function;

import static org.mockito.Mockito.*;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ExtendWith(MockitoExtension.class)
class SlugGeneratorTest {

    @Spy
    private Slugify slugify = Slugify.builder().transliterator(true).build();

    @Mock
    private SlugSequenceRepository slugSequenceRepository;

    @InjectMocks
    private SlugGenerator generator;

    @Test
    void shouldGenerateSimpleSlug() {
        String slug = generator.generate("Chainsaw Man", _ -> false);
        assertThat(slug).isEqualTo("chainsaw-man");
        verify(slugSequenceRepository, never()).findBySlugPrefixWithLock(any());
    }

    @Test
    void shouldRetryOnCollision() {
        Function<String, Boolean> existenceCheck = s -> s.equals("naruto");

        when(slugSequenceRepository.findBySlugPrefixWithLock("naruto"))
                .thenReturn(Optional.of(new SlugSequence("naruto", 1L)));

        String slug = generator.generate("Naruto", existenceCheck);

        assertThat(slug).isEqualTo("naruto-2");
    }

}
