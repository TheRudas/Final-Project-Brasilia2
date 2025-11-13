package co.edu.unimagdalena.finalproject_brasilia2.services.impl;

import co.edu.unimagdalena.finalproject_brasilia2.api.dto.SeatDtos.*;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.Seat;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.enums.SeatType;
import co.edu.unimagdalena.finalproject_brasilia2.domain.repositories.BusRepository;
import co.edu.unimagdalena.finalproject_brasilia2.domain.repositories.SeatRepository;
import co.edu.unimagdalena.finalproject_brasilia2.exceptions.NotFoundException;
import co.edu.unimagdalena.finalproject_brasilia2.services.SeatService;
import co.edu.unimagdalena.finalproject_brasilia2.services.mappers.SeatMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class SeatServiceImpl implements SeatService {
    private final SeatRepository seatRepository;
    private final BusRepository busRepository;
    private final SeatMapper mapper;

    @Override
    @Transactional
    public SeatResponse create(SeatCreateRequest request) {
        //bus ezzzists
        var bus = busRepository.findById(request.busId()).orElseThrow(
                () -> new NotFoundException("bus %d not found".formatted(request.busId()))
        );
        //uniqueness of the fokin number in this mlp vain
        if (seatRepository.existsByBusIdAndNumber(request.busId(), request.number())){
            throw new IllegalStateException("seat %s already exists in this bus".formatted(request.number()));
        }

        var seat = mapper.toEntity(request);
        seat.setBus(bus);
        return mapper.toResponse(seatRepository.save(seat));
    }

    @Override
    @Transactional
    public SeatResponse update(Long id, SeatUpdateRequest request) {
        var seat = seatRepository.findById(id).orElseThrow(
                () -> new NotFoundException("seat %d not found".formatted(id))
        );
        // Validate seat number uniqueness if a joker change it
        if(request.number() != null && !request.number().equals(seat.getNumber())){
            if(seatRepository.existsByBusIdAndNumber(seat.getBus().getId(), request.number())){
                throw new IllegalStateException("seat %s already exists in this bus".formatted(request.number()));
            }
        }
        mapper.patch(seat, request);
        return mapper.toResponse(seatRepository.save(seat));
    }

    @Override
    public SeatResponse get(Long id) {
        return mapper.toResponse(seatRepository.findById(id).orElseThrow(
                () -> new NotFoundException("seat %d not found".formatted(id))
        ));
    }

    @Transactional
    @Override
    public void delete(Long id) {
        var seat = seatRepository.findById(id).orElseThrow(
                () -> new NotFoundException("seat %d not found".formatted(id))
        );
        seatRepository.delete(seat);
    }

    @Override
    public List<SeatResponse> getByBusId(Long busId) {
        List<Seat> seats = seatRepository.findByBusId(busId);
        if (seats.isEmpty()) {
            throw new NotFoundException("bus %d has no seats".formatted(busId));
        }
        return seats.stream().map(mapper::toResponse).toList();
    }

    @Override
    public List<SeatResponse> getByBusIdAndSeatType(Long busId, SeatType seatType) {
        List<Seat> seats = seatRepository.findByBusIdAndSeatType(busId, seatType);
        if (seats.isEmpty()) {
            throw new NotFoundException("bus %d has no %s seats".formatted(busId, seatType));
        }
        return seats.stream().map(mapper::toResponse).toList();
    }

    @Override
    public SeatResponse getByBusIdAndNumber(Long busId, String number) {
        return seatRepository.findByBusIdAndNumber(busId, number).map(mapper::toResponse).orElseThrow(
                () -> new NotFoundException("seat number %s not found in this bus".formatted(number))
        );
    }

    @Override
    public List<SeatResponse> getByBusIdOrderByNumberAsc(Long busId) {
        List<Seat> seats = seatRepository.findByBusIdOrderByNumberAsc(busId);
        if (seats.isEmpty()) {
            throw new NotFoundException("bus %d has no seats".formatted(busId));
        }
        return seats.stream().map(mapper::toResponse).toList();
    }

    @Override
    public Long countByBusId(Long busId) {
        return seatRepository.countByBusId(busId);
    }
}
