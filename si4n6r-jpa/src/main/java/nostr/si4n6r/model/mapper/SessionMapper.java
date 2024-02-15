package nostr.si4n6r.model.mapper;

import nostr.si4n6r.model.Session;
import nostr.si4n6r.model.dto.SessionDto;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring")
public interface SessionMapper {
    Session toEntity(SessionDto sessionDto);

    SessionDto toDto(Session session);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Session partialUpdate(SessionDto sessionDto, @MappingTarget Session session);

    Session toEntity(Session session);
}