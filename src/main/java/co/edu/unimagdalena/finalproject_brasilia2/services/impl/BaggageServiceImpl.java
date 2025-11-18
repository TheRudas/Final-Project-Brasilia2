package co.edu.unimagdalena.finalproject_brasilia2.services.impl;

import co.edu.unimagdalena.finalproject_brasilia2.api.dto.BaggageDtos.BaggageCreateRequest;
import co.edu.unimagdalena.finalproject_brasilia2.api.dto.BaggageDtos.BaggageResponse;
import co.edu.unimagdalena.finalproject_brasilia2.api.dto.BaggageDtos.BaggageUpdateRequest;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.Baggage;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.enums.TicketStatus;
import co.edu.unimagdalena.finalproject_brasilia2.domain.repositories.BaggageRepository;
import co.edu.unimagdalena.finalproject_brasilia2.domain.repositories.TicketRepository;
import co.edu.unimagdalena.finalproject_brasilia2.exceptions.NotFoundException;
import co.edu.unimagdalena.finalproject_brasilia2.services.BaggageService;
import co.edu.unimagdalena.finalproject_brasilia2.services.ConfigService;
import co.edu.unimagdalena.finalproject_brasilia2.services.mappers.BaggageMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class BaggageServiceImpl implements BaggageService {
    private final BaggageRepository baggageRepository;
    private final TicketRepository ticketRepository;
    private final BaggageMapper mapper;
    private final ConfigService configService;

    @Override
    @Transactional
    public BaggageResponse create(BaggageCreateRequest request) {
        var ticket = ticketRepository.findById(request.ticketId()).orElseThrow(
                () -> new NotFoundException("Ticket not found with id: " + request.ticketId())
        );

        //verify if the ticket was sold really
        if(ticket.getStatus() != TicketStatus.SOLD) {
            throw new IllegalStateException("Cannot add baggage to a NON-SOLD ticket with id: " + request.ticketId());
        }

        var baggage = mapper.toEntity(request);
        baggage.setTicket(ticket);

        // Auto-generate unique tagCode
        baggage.setTagCode(generateTagCode());

        // Calculate fee by weight automatically
        var calculatedFee = calculateFee(request.weightKg());
        baggage.setFee(calculatedFee);

        return mapper.toResponse(baggageRepository.save(baggage));
    }

    @Override
    @Transactional
    public BaggageResponse update(Long id, BaggageUpdateRequest request) {
        var baggage = baggageRepository.findById(id).orElseThrow(() -> new NotFoundException("Baggage %d not found".formatted(id)));

        mapper.patch(baggage, request);
        return mapper.toResponse(baggageRepository.save(baggage));
    }

    @Override
    public BaggageResponse get(Long id) {
        return baggageRepository.findById(id).map(mapper::toResponse).orElseThrow(
                () -> new NotFoundException("Baggage %d not found".formatted(id))
        );
    }

    @Override
    public void delete(Long id) {
        var baggage = baggageRepository.findById(id).orElseThrow(
                () -> new NotFoundException("Baggage %d not found or was deleted yet".formatted(id))
        );
        baggageRepository.delete(baggage);
    }

    @Override
    public BaggageResponse getByTagCode(String tagCode) {
        return mapper.toResponse(baggageRepository.findByTagCode(tagCode).orElseThrow(
                () -> new NotFoundException("Baggage tag %s not found".formatted(tagCode))));
    }

    @Override
    public List<BaggageResponse> listByPassengerId(Long passengerId) {
        List<Baggage> baggage = baggageRepository.findByTicket_Passenger_Id(passengerId);
        if(baggage.isEmpty()){
            throw new NotFoundException("Passenger with id %d hasn't baggage".formatted(passengerId));
        }
        return baggage.stream().map(mapper::toResponse).toList();
    }

    @Override
    public Page<BaggageResponse> listByWeightGreaterThanOrEqual(BigDecimal weightKg, Pageable pageable) {
        Page<Baggage> baggage = baggageRepository.findByWeightKgGreaterThanEqual(weightKg, pageable);
        if(baggage.isEmpty()){
            throw new NotFoundException("Baggage >= than %s not found".formatted(weightKg));
        }
        return baggage.map(mapper::toResponse);
    }

    @Override
    public Page<BaggageResponse> listByWeightLessThanOrEqual(BigDecimal weightKg, Pageable pageable) {
        Page<Baggage> baggage = baggageRepository.findByWeightKgLessThanEqual(weightKg, pageable);
        if(baggage.isEmpty()){
            throw new NotFoundException("Baggage <= than %s not found".formatted(weightKg));
        }
        return baggage.map(mapper::toResponse);
    }

    @Override
    public Page<BaggageResponse> listByWeightBetween(BigDecimal minKg, BigDecimal maxKg, Pageable pageable) {
        Page<Baggage> baggage = baggageRepository.findByWeightKgBetween(minKg, maxKg, pageable);
        if(baggage.isEmpty()){
            throw new NotFoundException("Baggage between %s and %s not found".formatted(minKg, maxKg));
        }
        return baggage.map(mapper::toResponse);
    }

    @Override
    public List<BaggageResponse> listByTicketId(Long ticketId) {
        if(!ticketRepository.existsById(ticketId)){
            throw new NotFoundException("Ticket not found with id: " + ticketId);
        }
        List<Baggage> baggage = baggageRepository.findAllByTicketId(ticketId);
        if(baggage.isEmpty()){
            throw new NotFoundException("No baggage found for ticket with id: " + ticketId);
        }
        return baggage.stream().map(mapper::toResponse).toList();
    }

    @Override
    public Long countByTripId(Long tripId) {
        return baggageRepository.countByTripId(tripId);
    }

    @Override
    public BigDecimal sumWeightByTripId(Long tripId) {
        return baggageRepository.sumWeightByTripId(tripId);
    }

    //OYE GELDA ESCUCHATE ESTO
    // First "max" kg free, then "configurable fee" barras per kile
    private BigDecimal calculateFee(BigDecimal weightKg) {
        BigDecimal MAX_FREE_WEIGHT_KG = configService.getValue("BAGGAGE_MAX_FREE_WEIGHT_KG");
        BigDecimal FEE_PER_EXCESS_KG = configService.getValue("BAGGAGE_FEE_PER_EXCESS_KG");
        if (weightKg.compareTo(MAX_FREE_WEIGHT_KG) > 0) {
            BigDecimal excessWeight = weightKg.subtract(MAX_FREE_WEIGHT_KG);
            return excessWeight.multiply(FEE_PER_EXCESS_KG);
        }
        return BigDecimal.ZERO;
    }

    private String generateTagCode() {
        // Format: BAG-YYYYMMDD-XXXX
        String prefix = "BAG-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String suffix = UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        return prefix + "-" + suffix;
        // Example: BAG-20251117-A3F9
    }
}
