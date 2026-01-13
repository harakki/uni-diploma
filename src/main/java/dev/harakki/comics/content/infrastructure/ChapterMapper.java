package dev.harakki.comics.content.infrastructure;

import dev.harakki.comics.content.domain.Chapter;
import dev.harakki.comics.content.dto.ChapterUpdateRequest;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface ChapterMapper {

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Chapter partialUpdate(ChapterUpdateRequest dto, @MappingTarget Chapter chapter);

}
