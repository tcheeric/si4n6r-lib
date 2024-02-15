package nostr.si4n6r.model.mapper;

import nostr.si4n6r.model.Response;
import nostr.si4n6r.model.dto.ResponseDto;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring", uses = {MethodMapper.class, SessionMapper.class})
public interface ResponseMapper {
    Response toEntity(ResponseDto responseDto);

    ResponseDto toDto(Response response);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Response partialUpdate(ResponseDto responseDto, @MappingTarget Response response);
}