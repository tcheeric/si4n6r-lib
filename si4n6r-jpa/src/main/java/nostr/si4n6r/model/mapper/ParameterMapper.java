package nostr.si4n6r.model.mapper;

import nostr.si4n6r.model.Parameter;
import nostr.si4n6r.model.dto.ParameterDto;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring", uses = {RequestMapper.class})
public interface ParameterMapper {
    Parameter toEntity(ParameterDto parameterDto);

    ParameterDto toDto(Parameter parameter);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Parameter partialUpdate(ParameterDto parameterDto, @MappingTarget Parameter parameter);
}