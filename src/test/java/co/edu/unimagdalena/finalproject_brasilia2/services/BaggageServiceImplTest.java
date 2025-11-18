package co.edu.unimagdalena.finalproject_brasilia2.services;

import co.edu.unimagdalena.finalproject_brasilia2.api.dto.BaggageDtos.BaggageCreateRequest;
import co.edu.unimagdalena.finalproject_brasilia2.api.dto.BaggageDtos.BaggageUpdateRequest;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.Baggage;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.Ticket;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.User;
import co.edu.unimagdalena.finalproject_brasilia2.domain.repositories.BaggageRepository;
import co.edu.unimagdalena.finalproject_brasilia2.domain.repositories.TicketRepository;
import co.edu.unimagdalena.finalproject_brasilia2.exceptions.NotFoundException;
import co.edu.unimagdalena.finalproject_brasilia2.services.impl.BaggageServiceImpl;
import co.edu.unimagdalena.finalproject_brasilia2.services.mappers.BaggageMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BaggageServiceImplTest {

    @Mock
    private BaggageRepository baggageRepository;

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private ConfigService configService;

    @Spy
    private BaggageMapper mapper = Mappers.getMapper(BaggageMapper.class);

    @InjectMocks
    private BaggageServiceImpl service;

    @Test
    void shouldCreateBaggageSuccessfully() {
        // Given
        var passenger = User.builder()
                .id(1L)
                .name("Juan Perez")
                .build();

        var ticket = Ticket.builder()
                .id(5L)
                .passenger(passenger)
                .status(co.edu.unimagdalena.finalproject_brasilia2.domain.entities.enums.TicketStatus.SOLD)
                .build();

        var request = new BaggageCreateRequest(
                5L,
                new BigDecimal("25.00"), // 25kg
                new BigDecimal("0"), // Fee será calculado automáticamente
                "BAG-ABC12345"
        );

        when(ticketRepository.findById(5L)).thenReturn(Optional.of(ticket));
        when(baggageRepository.findByTagCode("BAG-ABC12345")).thenReturn(Optional.empty());
        when(configService.getValue("BAGGAGE_MAX_FREE_WEIGHT_KG")).thenReturn(new BigDecimal("20"));
        when(configService.getValue("BAGGAGE_FEE_PER_EXCESS_KG")).thenReturn(new BigDecimal("2000"));
        when(baggageRepository.save(any(Baggage.class))).thenAnswer(inv -> {
            Baggage b = inv.getArgument(0);
            b.setId(10L);
            return b;
        });

        // When
        var response = service.create(request);

        // Then
        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.ticketId()).isEqualTo(5L);
        assertThat(response.passengerName()).isEqualTo("Juan Perez");
        assertThat(response.weightKg()).isEqualByComparingTo(new BigDecimal("25.00"));
        // Fee calculado: (25 - 20) * 2000 = 10000
        assertThat(response.fee()).isEqualByComparingTo(new BigDecimal("10000"));
        assertThat(response.tagCode()).isEqualTo("BAG-ABC12345");

        verify(ticketRepository).findById(5L);
        verify(baggageRepository).findByTagCode("BAG-ABC12345");
        verify(configService).getValue("BAGGAGE_MAX_FREE_WEIGHT_KG");
        verify(configService).getValue("BAGGAGE_FEE_PER_EXCESS_KG");
        verify(baggageRepository).save(any(Baggage.class));
    }

    @Test
    void shouldThrowNotFoundExceptionWhenTicketNotExists() {
        // Given
        var request = new BaggageCreateRequest(
                99L,
                new BigDecimal("15.50"),
                new BigDecimal("25000.00"),
                "BAG-ABC12345"
        );
        when(ticketRepository.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Ticket not found with id: 99");

        verify(ticketRepository).findById(99L);
        verify(baggageRepository, never()).findByTagCode(any());
        verify(baggageRepository, never()).save(any());
    }

    @Test
    void shouldThrowIllegalStateExceptionWhenTagCodeAlreadyExists() {
        // Given
        var ticket = Ticket.builder()
                .id(5L)
                .status(co.edu.unimagdalena.finalproject_brasilia2.domain.entities.enums.TicketStatus.SOLD)
                .build();
        var existingBaggage = Baggage.builder()
                .id(1L)
                .tagCode("BAG-ABC12345")
                .build();

        var request = new BaggageCreateRequest(
                5L,
                new BigDecimal("15.50"),
                new BigDecimal("25000.00"),
                "BAG-ABC12345"
        );

        when(ticketRepository.findById(5L)).thenReturn(Optional.of(ticket));
        when(baggageRepository.findByTagCode("BAG-ABC12345"))
                .thenReturn(Optional.of(existingBaggage));

        // When / Then
        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Baggage tag BAG-ABC12345 already exists");

        verify(ticketRepository).findById(5L);
        verify(baggageRepository).findByTagCode("BAG-ABC12345");
        verify(baggageRepository, never()).save(any());
    }

    @Test
    void shouldThrowIllegalStateExceptionWhenTicketNotSold() {
        // Given
        var ticket = Ticket.builder()
                .id(5L)
                .status(co.edu.unimagdalena.finalproject_brasilia2.domain.entities.enums.TicketStatus.CANCELLED)
                .build();

        var request = new BaggageCreateRequest(
                5L,
                new BigDecimal("15.50"),
                new BigDecimal("0"),
                "BAG-ABC12345"
        );

        when(ticketRepository.findById(5L)).thenReturn(Optional.of(ticket));
        when(baggageRepository.findByTagCode("BAG-ABC12345")).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot add baggage to a NON-SOLD ticket");

        verify(ticketRepository).findById(5L);
        verify(baggageRepository).findByTagCode("BAG-ABC12345");
        verify(configService, never()).getValue(any());
        verify(baggageRepository, never()).save(any());
    }

    @Test
    void shouldUpdateBaggageSuccessfully() {
        // Given
        var passenger = User.builder().id(1L).name("Ana Lopez").build();
        var ticket = Ticket.builder().id(5L).passenger(passenger).build();

        var existingBaggage = Baggage.builder()
                .id(10L)
                .ticket(ticket)
                .weightKg(new BigDecimal("15.50"))
                .fee(new BigDecimal("25000.00"))
                .tagCode("BAG-ABC12345")
                .build();

        var updateRequest = new BaggageUpdateRequest(
                new BigDecimal("20.00"),
                new BigDecimal("30000.00")
        );

        when(baggageRepository.findById(10L)).thenReturn(Optional.of(existingBaggage));
        when(baggageRepository.save(any(Baggage.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        var response = service.update(10L, updateRequest);

        // Then
        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.weightKg()).isEqualByComparingTo(new BigDecimal("20.00"));
        assertThat(response.fee()).isEqualByComparingTo(new BigDecimal("30000.00"));
        assertThat(response.tagCode()).isEqualTo("BAG-ABC12345"); // No deberia cambiar

        verify(baggageRepository).findById(10L);
        verify(baggageRepository).save(any(Baggage.class));
    }

    @Test
    void shouldUpdateOnlyWeightKgWhenFeeIsNull() {
        // Given
        var ticket = Ticket.builder().id(5L).build();
        var existingBaggage = Baggage.builder()
                .id(10L)
                .ticket(ticket)
                .weightKg(new BigDecimal("15.50"))
                .fee(new BigDecimal("25000.00"))
                .tagCode("BAG-ABC12345")
                .build();

        var updateRequest = new BaggageUpdateRequest(
                new BigDecimal("20.00"),
                null // Solo actualizar peso
        );

        when(baggageRepository.findById(10L)).thenReturn(Optional.of(existingBaggage));
        when(baggageRepository.save(any(Baggage.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        var response = service.update(10L, updateRequest);

        // Then
        assertThat(response.weightKg()).isEqualByComparingTo(new BigDecimal("20.00"));
        assertThat(response.fee()).isEqualByComparingTo(new BigDecimal("25000.00")); // No cambio
    }

    @Test
    void shouldThrowNotFoundExceptionWhenUpdateNonExistentBaggage() {
        // Given
        var updateRequest = new BaggageUpdateRequest(
                new BigDecimal("20.00"),
                new BigDecimal("30000.00")
        );
        when(baggageRepository.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> service.update(99L, updateRequest))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Baggage 99 not found");

        verify(baggageRepository).findById(99L);
        verify(baggageRepository, never()).save(any());
    }

    @Test
    void shouldGetBaggageById() {
        // Given
        var passenger = User.builder().id(1L).name("Carlos Ruiz").build();
        var ticket = Ticket.builder().id(5L).passenger(passenger).build();

        var baggage = Baggage.builder()
                .id(10L)
                .ticket(ticket)
                .weightKg(new BigDecimal("18.00"))
                .fee(new BigDecimal("28000.00"))
                .tagCode("BAG-XYZ78910")
                .build();

        when(baggageRepository.findById(10L)).thenReturn(Optional.of(baggage));

        // When
        var response = service.get(10L);

        // Then
        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.ticketId()).isEqualTo(5L);
        assertThat(response.passengerName()).isEqualTo("Carlos Ruiz");
        assertThat(response.weightKg()).isEqualByComparingTo(new BigDecimal("18.00"));
        assertThat(response.fee()).isEqualByComparingTo(new BigDecimal("28000.00"));
        assertThat(response.tagCode()).isEqualTo("BAG-XYZ78910");

        verify(baggageRepository).findById(10L);
    }

    @Test
    void shouldThrowNotFoundExceptionWhenGetNonExistentBaggage() {
        // Given
        when(baggageRepository.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> service.get(99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Baggage 99 not found");

        verify(baggageRepository).findById(99L);
    }

    @Test
    void shouldDeleteBaggageSuccessfully() {
        // Given
        var baggage = Baggage.builder()
                .id(10L)
                .tagCode("BAG-ABC12345")
                .build();

        when(baggageRepository.findById(10L)).thenReturn(Optional.of(baggage));
        doNothing().when(baggageRepository).delete(baggage);

        // When
        service.delete(10L);

        // Then
        verify(baggageRepository).findById(10L);
        verify(baggageRepository).delete(baggage);
    }

    @Test
    void shouldThrowNotFoundExceptionWhenDeleteNonExistentBaggage() {
        // Given
        when(baggageRepository.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> service.delete(99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Baggage 99 not found or was deleted yet");

        verify(baggageRepository).findById(99L);
        verify(baggageRepository, never()).delete(any());
    }

    // ============= GET BY TAG CODE TESTS =============

    @Test
    void shouldGetBaggageByTagCode() {
        // Given
        var passenger = User.builder().id(1L).name("Maria Garcia").build();
        var ticket = Ticket.builder().id(5L).passenger(passenger).build();

        var baggage = Baggage.builder()
                .id(10L)
                .ticket(ticket)
                .weightKg(new BigDecimal("12.50"))
                .fee(new BigDecimal("20000.00"))
                .tagCode("BAG-XYZ78910")
                .build();

        when(baggageRepository.findByTagCode("BAG-XYZ78910"))
                .thenReturn(Optional.of(baggage));

        // When
        var response = service.getByTagCode("BAG-XYZ78910");

        // Then
        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.tagCode()).isEqualTo("BAG-XYZ78910");
        assertThat(response.passengerName()).isEqualTo("Maria Garcia");

        verify(baggageRepository).findByTagCode("BAG-XYZ78910");
    }

    @Test
    void shouldThrowNotFoundExceptionWhenTagCodeNotExists() {
        // Given
        when(baggageRepository.findByTagCode("BAG-INVALID"))
                .thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> service.getByTagCode("BAG-INVALID"))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Baggage tag BAG-INVALID not found");

        verify(baggageRepository).findByTagCode("BAG-INVALID");
    }

    // ============= GET BY PASSENGER ID TESTS =============

    @Test
    void shouldGetBaggagesByUserId() {
        // Given
        var passenger = User.builder().id(1L).name("Pedro Sanchez").build();
        var ticket1 = Ticket.builder().id(5L).passenger(passenger).build();
        var ticket2 = Ticket.builder().id(6L).passenger(passenger).build();

        var baggage1 = Baggage.builder()
                .id(10L)
                .ticket(ticket1)
                .weightKg(new BigDecimal("15.00"))
                .fee(new BigDecimal("25000.00"))
                .tagCode("BAG-AAA11111")
                .build();

        var baggage2 = Baggage.builder()
                .id(11L)
                .ticket(ticket2)
                .weightKg(new BigDecimal("20.00"))
                .fee(new BigDecimal("35000.00"))
                .tagCode("BAG-BBB22222")
                .build();

        when(baggageRepository.findByTicket_Passenger_Id(1L))
                .thenReturn(List.of(baggage1, baggage2));

        // When
        var result = service.listByPassengerId(1L);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.getFirst().id()).isEqualTo(10L);
        assertThat(result.get(0).tagCode()).isEqualTo("BAG-AAA11111");
        assertThat(result.get(0).passengerName()).isEqualTo("Pedro Sanchez");
        assertThat(result.get(1).id()).isEqualTo(11L);
        assertThat(result.get(1).tagCode()).isEqualTo("BAG-BBB22222");

        verify(baggageRepository).findByTicket_Passenger_Id(1L);
    }

    @Test
    void shouldThrowNotFoundExceptionWhenUserHasNoBaggages() {
        // Given
        when(baggageRepository.findByTicket_Passenger_Id(99L))
                .thenReturn(List.of());

        // When / Then
        assertThatThrownBy(() -> service.listByPassengerId(99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Passenger with id 99 hasn't baggage");

        verify(baggageRepository).findByTicket_Passenger_Id(99L);
    }

    // ============= GET BY WEIGHT GREATER THAN OR EQUAL TESTS =============

    @Test
    void shouldGetBaggagesByWeightGreaterThanOrEqual() {
        // Given
        var passenger = User.builder().id(1L).name("Luis Martinez").build();
        var ticket1 = Ticket.builder().id(5L).passenger(passenger).build();
        var ticket2 = Ticket.builder().id(6L).passenger(passenger).build();

        var baggage1 = Baggage.builder()
                .id(10L)
                .ticket(ticket1)
                .weightKg(new BigDecimal("20.00"))
                .fee(new BigDecimal("30000.00"))
                .tagCode("BAG-CCC33333")
                .build();

        var baggage2 = Baggage.builder()
                .id(11L)
                .ticket(ticket2)
                .weightKg(new BigDecimal("25.00"))
                .fee(new BigDecimal("40000.00"))
                .tagCode("BAG-DDD44444")
                .build();

        var page = new PageImpl<>(List.of(baggage1, baggage2));
        var pageable = PageRequest.of(0, 10);

        when(baggageRepository.findByWeightKgGreaterThanEqual(new BigDecimal("20.00"), pageable))
                .thenReturn(page);

        // When
        var result = service.listByWeightGreaterThanOrEqual(new BigDecimal("20.00"), pageable);

        // Then
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).weightKg())
                .isEqualByComparingTo(new BigDecimal("20.00"));
        assertThat(result.getContent().get(1).weightKg())
                .isEqualByComparingTo(new BigDecimal("25.00"));

        verify(baggageRepository).findByWeightKgGreaterThanEqual(new BigDecimal("20.00"), pageable);
    }

    @Test
    void shouldThrowNotFoundExceptionWhenNoBaggagesMatchWeightGreaterThan() {
        // Given
        var page = new PageImpl<Baggage>(List.of());
        var pageable = PageRequest.of(0, 10);

        when(baggageRepository.findByWeightKgGreaterThanEqual(new BigDecimal("50.00"), pageable))
                .thenReturn(page);

        // When / Then
        assertThatThrownBy(() -> service.listByWeightGreaterThanOrEqual(new BigDecimal("50.00"), pageable))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Baggage >= than 50.00 not found");

        verify(baggageRepository).findByWeightKgGreaterThanEqual(new BigDecimal("50.00"), pageable);
    }

    // ============= GET BY WEIGHT LESS THAN OR EQUAL TESTS =============

    @Test
    void shouldGetBaggagesByWeightLessThanOrEqual() {
        // Given
        var passenger = User.builder().id(1L).name("Sofia Hernandez").build();
        var ticket1 = Ticket.builder().id(5L).passenger(passenger).build();
        var ticket2 = Ticket.builder().id(6L).passenger(passenger).build();

        var baggage1 = Baggage.builder()
                .id(10L)
                .ticket(ticket1)
                .weightKg(new BigDecimal("8.00"))
                .fee(new BigDecimal("15000.00"))
                .tagCode("BAG-EEE55555")
                .build();

        var baggage2 = Baggage.builder()
                .id(11L)
                .ticket(ticket2)
                .weightKg(new BigDecimal("10.00"))
                .fee(new BigDecimal("18000.00"))
                .tagCode("BAG-FFF66666")
                .build();

        var page = new PageImpl<>(List.of(baggage1, baggage2));
        var pageable = PageRequest.of(0, 10);

        when(baggageRepository.findByWeightKgLessThanEqual(new BigDecimal("10.00"), pageable))
                .thenReturn(page);

        // When
        var result = service.listByWeightLessThanOrEqual(new BigDecimal("10.00"), pageable);

        // Then
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).weightKg())
                .isEqualByComparingTo(new BigDecimal("8.00"));
        assertThat(result.getContent().get(1).weightKg())
                .isEqualByComparingTo(new BigDecimal("10.00"));

        verify(baggageRepository).findByWeightKgLessThanEqual(new BigDecimal("10.00"), pageable);
    }

    @Test
    void shouldThrowNotFoundExceptionWhenNoBaggagesMatchWeightLessThan() {
        // Given
        var page = new PageImpl<Baggage>(List.of());
        var pageable = PageRequest.of(0, 10);

        when(baggageRepository.findByWeightKgLessThanEqual(new BigDecimal("1.00"), pageable))
                .thenReturn(page);

        // When / Then
        assertThatThrownBy(() -> service.listByWeightLessThanOrEqual(new BigDecimal("1.00"), pageable))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Baggage <= than 1.00 not found");

        verify(baggageRepository).findByWeightKgLessThanEqual(new BigDecimal("1.00"), pageable);
    }

    // ============= GET BY WEIGHT BETWEEN TESTS =============

    @Test
    void shouldGetBaggagesByWeightBetween() {
        // Given
        var passenger = User.builder().id(1L).name("Diego Torres").build();
        var ticket1 = Ticket.builder().id(5L).passenger(passenger).build();
        var ticket2 = Ticket.builder().id(6L).passenger(passenger).build();

        var baggage1 = Baggage.builder()
                .id(10L)
                .ticket(ticket1)
                .weightKg(new BigDecimal("15.00"))
                .fee(new BigDecimal("25000.00"))
                .tagCode("BAG-GGG77777")
                .build();

        var baggage2 = Baggage.builder()
                .id(11L)
                .ticket(ticket2)
                .weightKg(new BigDecimal("18.00"))
                .fee(new BigDecimal("28000.00"))
                .tagCode("BAG-HHH88888")
                .build();

        var page = new PageImpl<>(List.of(baggage1, baggage2));
        var pageable = PageRequest.of(0, 10);

        when(baggageRepository.findByWeightKgBetween(
                new BigDecimal("10.00"),
                new BigDecimal("20.00"),
                pageable
        )).thenReturn(page);

        // When
        var result = service.listByWeightBetween(
                new BigDecimal("10.00"),
                new BigDecimal("20.00"),
                pageable
        );

        // Then
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).weightKg())
                .isEqualByComparingTo(new BigDecimal("15.00"));
        assertThat(result.getContent().get(1).weightKg())
                .isEqualByComparingTo(new BigDecimal("18.00"));

        verify(baggageRepository).findByWeightKgBetween(
                new BigDecimal("10.00"),
                new BigDecimal("20.00"),
                pageable
        );
    }

    @Test
    void shouldThrowNotFoundExceptionWhenNoBaggagesMatchWeightBetween() {
        // Given
        var page = new PageImpl<Baggage>(List.of());
        var pageable = PageRequest.of(0, 10);

        when(baggageRepository.findByWeightKgBetween(
                new BigDecimal("100.00"),
                new BigDecimal("200.00"),
                pageable
        )).thenReturn(page);

        // When / Then
        assertThatThrownBy(() -> service.listByWeightBetween(
                new BigDecimal("100.00"),
                new BigDecimal("200.00"),
                pageable
        ))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Baggage between 100.00 and 200.00 not found");

        verify(baggageRepository).findByWeightKgBetween(
                new BigDecimal("100.00"),
                new BigDecimal("200.00"),
                pageable
        );
    }

    // ============= GET ALL BY TICKET ID TESTS =============

    @Test
    void shouldGetAllBaggagesByTicketId() {
        // Given
        var passenger = User.builder().id(1L).name("Roberto Diaz").build();
        var ticket = Ticket.builder().id(5L).passenger(passenger).build();

        var baggage1 = Baggage.builder()
                .id(10L)
                .ticket(ticket)
                .weightKg(new BigDecimal("12.00"))
                .fee(new BigDecimal("20000.00"))
                .tagCode("BAG-III99999")
                .build();

        var baggage2 = Baggage.builder()
                .id(11L)
                .ticket(ticket)
                .weightKg(new BigDecimal("8.00"))
                .fee(new BigDecimal("15000.00"))
                .tagCode("BAG-JJJ00000")
                .build();

        when(ticketRepository.existsById(5L)).thenReturn(true);
        when(baggageRepository.findAllByTicketId(5L))
                .thenReturn(List.of(baggage1, baggage2));

        // When
        var result = service.listByTicketId(5L);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.getFirst().id()).isEqualTo(10L);
        assertThat(result.getFirst().ticketId()).isEqualTo(5L);
        assertThat(result.get(0).tagCode()).isEqualTo("BAG-III99999");
        assertThat(result.get(0).passengerName()).isEqualTo("Roberto Diaz");
        assertThat(result.get(1).id()).isEqualTo(11L);
        assertThat(result.get(1).tagCode()).isEqualTo("BAG-JJJ00000");

        verify(ticketRepository).existsById(5L);
        verify(baggageRepository).findAllByTicketId(5L);
    }
}
