package dev.harakki.comics.catalog.infrastructure;

import dev.harakki.comics.catalog.domain.Author;
import dev.harakki.comics.catalog.dto.AuthorCreateRequest;
import dev.harakki.comics.catalog.dto.AuthorResponse;
import dev.harakki.comics.catalog.dto.AuthorUpdateRequest;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface AuthorMapper {

    Author toEntity(AuthorCreateRequest dto);

    AuthorResponse toResponse(Author author);

    //AuthorResponseDto toDto(Author author);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Author partialUpdate(AuthorUpdateRequest dto, @MappingTarget Author author);

}
