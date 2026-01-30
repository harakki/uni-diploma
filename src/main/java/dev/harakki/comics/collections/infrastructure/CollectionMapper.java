package dev.harakki.comics.collections.infrastructure;

import dev.harakki.comics.collections.domain.Collection;
import dev.harakki.comics.collections.dto.CollectionCreateRequest;
import dev.harakki.comics.collections.dto.CollectionUpdateRequest;
import dev.harakki.comics.collections.dto.UserCollectionResponse;
import org.mapstruct.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CollectionMapper {

    Collection toEntity(CollectionCreateRequest dto);

    UserCollectionResponse toResponse(Collection entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Collection partialUpdate(CollectionUpdateRequest dto, @MappingTarget Collection entity);

    default List<UUID> safeList(List<UUID> list) {
        return list == null ? new ArrayList<>() : list;
    }

}
