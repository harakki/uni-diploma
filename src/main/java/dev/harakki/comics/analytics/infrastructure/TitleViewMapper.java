package dev.harakki.comics.analytics.infrastructure;

import dev.harakki.comics.analytics.domain.TitleView;
import dev.harakki.comics.analytics.dto.TitleViewResponse;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface TitleViewMapper {

    TitleViewResponse toResponse(TitleView entity);

}
