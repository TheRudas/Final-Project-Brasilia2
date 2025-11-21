package co.edu.unimagdalena.finalproject_brasilia2.services.mappers;

import co.edu.unimagdalena.finalproject_brasilia2.api.dto.PaymentDtos;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.Payment;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", constant = "PENDING")
    @Mapping(target = "paymentDate", ignore = true)
    @Mapping(target = "ticket", ignore = true)
    Payment toEntity(PaymentDtos.PaymentCreateRequest req);

    @Mapping(source = "ticket.id", target = "ticketId")
    @Mapping(source = "ticket.qrCode", target = "ticketQrCode")
    @Mapping(source = "ticket.passenger.name", target = "passengerName")
    PaymentDtos.PaymentResponse toResponse(Payment payment);

    List<PaymentDtos.PaymentResponse> toResponseList(List<Payment> payments);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, ignoreByDefault = true)
    @Mapping(target = "status", source = "status")
    @Mapping(target = "transactionId", source = "transactionId")
    @Mapping(target = "paymentReference", source = "paymentReference")
    @Mapping(target = "notes", source = "notes")
    void patch(@MappingTarget Payment payment, PaymentDtos.PaymentUpdateRequest req);
}

