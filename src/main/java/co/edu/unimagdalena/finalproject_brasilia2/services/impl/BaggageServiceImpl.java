package co.edu.unimagdalena.finalproject_brasilia2.services.impl;

import co.edu.unimagdalena.finalproject_brasilia2.api.dto.BaggageDtos.*;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.Baggage;
import co.edu.unimagdalena.finalproject_brasilia2.domain.repositories.BaggageRepository;
import co.edu.unimagdalena.finalproject_brasilia2.domain.repositories.TicketRepository;
import co.edu.unimagdalena.finalproject_brasilia2.exceptions.NotFoundException;
import co.edu.unimagdalena.finalproject_brasilia2.services.BaggageService;
import co.edu.unimagdalena.finalproject_brasilia2.services.mappers.BaggageMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

    @Service
    @Transactional(readOnly = true)
    @RequiredArgsConstructor
public class BaggageServiceImpl implements BaggageService {
    private final BaggageRepository baggageRepository;
    private final TicketRepository ticketRepository;
    private final BaggageMapper mapper;

    @Override
    @Transactional
    public BaggageResponse create(BaggageCreateRequest request) {
        var ticket = ticketRepository.findById(request.ticketId()).orElseThrow(
                () -> new NotFoundException("Ticket not found with id: " + request.ticketId())
        );
        //verify uniqueness of tagCode
        if(baggageRepository.findByTagCode(request.tagCode()).isPresent()) {
            throw new IllegalStateException("Baggage tag %s already exists".formatted(request.tagCode())); //'state' cause is valid but already registered in Kevin DB
        }
        var baggage = mapper.toEntity(request);
        baggage.setTicket(ticket);
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
    public List<BaggageResponse> getByPassengerId(Long passengerId) {
        List<Baggage> baggage = baggageRepository.findByTicket_Passenger_Id(passengerId);
        if(baggage.isEmpty()){
            throw new NotFoundException("Passenger with id %d hasn't baggage".formatted(passengerId));
        }
        return baggage.stream().map(mapper::toResponse).toList();
    }

    @Override
    public Page<BaggageResponse> getByWeightGreaterThanOrEqual(BigDecimal weightKg, Pageable pageable) {
        Page<Baggage> baggage = baggageRepository.findByWeightKgGreaterThanEqual(weightKg, pageable);
        if(baggage.isEmpty()){
            throw new NotFoundException("Baggage >= than %s not found".formatted(weightKg));
        }
        return baggage.map(mapper::toResponse);
    }

    @Override
    public Page<BaggageResponse> getByWeightLessThanOrEqual(BigDecimal weightKg, Pageable pageable) {
        Page<Baggage> baggage = baggageRepository.findByWeightKgLessThanEqual(weightKg, pageable);
        if(baggage.isEmpty()){
            throw new NotFoundException("Baggage <= than %s not found".formatted(weightKg));
        }
        return baggage.map(mapper::toResponse);
    }

    @Override
    public Page<BaggageResponse> getByWeightBetween(BigDecimal minKg, BigDecimal maxKg, Pageable pageable) {
        Page<Baggage> baggage = baggageRepository.findByWeightKgBetween(minKg, maxKg, pageable);
        if(baggage.isEmpty()){
            throw new NotFoundException("Baggage between %s and %s not found".formatted(minKg, maxKg));
        }
        return baggage.map(mapper::toResponse);
    }
}
