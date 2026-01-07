package dev.harakki.comics.catalog.infrastructure;

import dev.harakki.comics.catalog.domain.Title;
import dev.harakki.comics.catalog.dto.TitleCreateRequest;
import dev.harakki.comics.catalog.dto.TitleResponse;
import dev.harakki.comics.catalog.dto.TitleUpdateRequest;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING, uses = {AuthorMapper.class, PublisherMapper.class, TagMapper.class})
public interface TitleMapper {

    Title toEntity(TitleCreateRequest dto);

    TitleResponse toResponse(Title title);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Title partialUpdate(TitleUpdateRequest dto, @MappingTarget Title title);

}
