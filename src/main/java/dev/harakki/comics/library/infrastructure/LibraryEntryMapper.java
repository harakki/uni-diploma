package dev.harakki.comics.library.infrastructure;

import dev.harakki.comics.library.domain.LibraryEntry;
import dev.harakki.comics.library.dto.LibraryEntryCreateRequest;
import dev.harakki.comics.library.dto.LibraryEntryResponse;
import dev.harakki.comics.library.dto.LibraryEntryUpdateRequest;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface LibraryEntryMapper {

    LibraryEntry toEntity(LibraryEntryCreateRequest dto);

    LibraryEntryResponse toResponse(LibraryEntry libraryEntry);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    LibraryEntry partialUpdate(LibraryEntryUpdateRequest dto, @MappingTarget LibraryEntry libraryEntry);

}
