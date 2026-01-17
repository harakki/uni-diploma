package dev.harakki.comics.analytics.infrastructure;

import dev.harakki.comics.analytics.domain.TitleRating;
import dev.harakki.comics.analytics.dto.TitleRatingResponse;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface TitleRatingMapper {

    TitleRatingResponse toResponse(TitleRating entity);

}
