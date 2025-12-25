package dev.harakki.comics.catalog.infrastructure;

import dev.harakki.comics.catalog.domain.Tag;
import dev.harakki.comics.catalog.dto.TagCreateRequest;
import dev.harakki.comics.catalog.dto.TagResponse;
import dev.harakki.comics.catalog.dto.TagUpdateRequest;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface TagMapper {

    Tag toEntity(TagCreateRequest dto);

    TagResponse toResponse(Tag tag);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Tag partialUpdate(TagUpdateRequest dto, @MappingTarget Tag tag);

}
