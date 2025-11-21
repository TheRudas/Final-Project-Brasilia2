package co.edu.unimagdalena.finalproject_brasilia2.services.mappers;

import co.edu.unimagdalena.finalproject_brasilia2.api.dto.TicketDtos.*;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.*;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.enums.*;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class TicketMapperTest {
    private final TicketMapper mapper = Mappers.getMapper(TicketMapper.class);

    @Test
    void toEntity_shouldMapCreateRequest() {
        var req = new TicketCreateRequest(1L, 2L, "A5", 3L, 4L,
                new BigDecimal("50000.00"), PaymentMethod.CARD, PassengerType.ADULT);
        Ticket entity = mapper.toEntity(req);

        assertThat(entity.getSeatNumber()).isEqualTo("A5");
        assertThat(entity.getPrice()).isEqualByComparingTo(new BigDecimal("50000.00"));
        assertThat(entity.getPaymentMethod()).isEqualTo(PaymentMethod.CARD);
        assertThat(entity.getPassengerType()).isEqualTo(PassengerType.ADULT);
    }

    @Test
    void toResponse_shouldMapEntity() {
        var bus = Bus.builder().plate("ABC123").build();
        var trip = Trip.builder().id(9L).bus(bus)
                .departureTime(OffsetDateTime.now()).build();
        var passenger = User.builder().id(4L).name("Carlos Ruiz").build();
        var fromStop = Stop.builder().id(1L).build();
        var toStop = Stop.builder().id(5L).build();

        var t = Ticket.builder()
                .id(20L).trip(trip).passenger(passenger)
                .seatNumber("B3").fromStop(fromStop).toStop(toStop)
                .price(new BigDecimal("45000.00")).paymentMethod(PaymentMethod.CASH)
                .passengerType(PassengerType.CHILD)
                .status(TicketStatus.SOLD).qrCode("QR-XYZ789").build();

        TicketResponse dto = mapper.toResponse(t);

        assertThat(dto.id()).isEqualTo(20L);
        assertThat(dto.tripId()).isEqualTo(9L);
        assertThat(dto.passengerId()).isEqualTo(4L);
        assertThat(dto.passengerName()).isEqualTo("Carlos Ruiz");
        assertThat(dto.busPlate()).isEqualTo("ABC123");
        assertThat(dto.seatNumber()).isEqualTo("B3");
        assertThat(dto.passengerType()).isEqualTo(PassengerType.CHILD);
    }

    @Test
    void patch_shouldIgnoreNulls() {
        var entity = Ticket.builder().id(1L).seatNumber("A1")
                .price(new BigDecimal("50000.00")).paymentMethod(PaymentMethod.CARD)
                .status(TicketStatus.SOLD).passengerType(PassengerType.CHILD).build();
        var changes = new TicketUpdateRequest(null, new BigDecimal("55000.00"),
                null, TicketStatus.CANCELLED);

        mapper.patch(entity, changes);

        assertThat(entity.getSeatNumber()).isEqualTo("A1");
        assertThat(entity.getPrice()).isEqualByComparingTo(new BigDecimal("55000.00"));
        assertThat(entity.getPaymentMethod()).isEqualTo(PaymentMethod.CARD);
        assertThat(entity.getStatus()).isEqualTo(TicketStatus.CANCELLED);
    }
}