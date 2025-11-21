package co.edu.unimagdalena.finalproject_brasilia2.services.mappers;

import co.edu.unimagdalena.finalproject_brasilia2.api.dto.CashClosureDtos.*;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.CashClosure;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface CashClosureMapper {

    @Mapping(target = "clerkId", source = "clerk.id")
    @Mapping(target = "clerkName", source = "clerk.name")
    @Mapping(target = "totalRevenue", expression = "java(cashClosure.getTotalCash().add(cashClosure.getTotalBaggageFees()))")
    CashClosureResponse toResponse(CashClosure cashClosure);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "clerk", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    CashClosure toEntity(CashClosureCreateRequest request);
}