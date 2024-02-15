package nostr.si4n6r.model.mapper;

import nostr.si4n6r.model.Request;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring", uses = {SessionMapper.class, nostr.si4n6r.model.mapper.MethodMapper.class})
public interface RequestMapper {
    Request toEntity(nostr.si4n6r.model.dto.RequestDto requestDto);

    nostr.si4n6r.model.dto.RequestDto toDto(Request request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Request partialUpdate(nostr.si4n6r.model.dto.RequestDto requestDto, @MappingTarget Request request);
}