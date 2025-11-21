package co.edu.unimagdalena.finalproject_brasilia2.services.mappers;

import co.edu.unimagdalena.finalproject_brasilia2.api.dto.PaymentDtos.*;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.Payment;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

    @Mapping(target = "ticketId", source = "ticket.id")
    @Mapping(target = "pending", expression = "java(payment.isPending())")
    @Mapping(target = "failed", expression = "java(payment.isFailed())")
    PaymentResponse toResponse(Payment payment);
}