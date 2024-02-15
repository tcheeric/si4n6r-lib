package nostr.si4n6r.model.mapper;

import nostr.si4n6r.model.Method;
import nostr.si4n6r.model.dto.MethodDto;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring")
public interface MethodMapper {
    Method toEntity(MethodDto methodDto);

    MethodDto toDto(Method method);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Method partialUpdate(MethodDto methodDto, @MappingTarget Method method);

    Method toEntity(Method method);

}