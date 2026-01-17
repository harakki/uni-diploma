package dev.harakki.comics.analytics.infrastructure;

import dev.harakki.comics.analytics.domain.TitleReadTime;
import dev.harakki.comics.analytics.dto.TitleReadTimeResponse;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface TitleReadTimeMapper {

    TitleReadTimeResponse toResponse(TitleReadTime entity);

}
