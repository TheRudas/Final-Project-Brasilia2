package co.edu.unimagdalena.finalproject_brasilia2.services.impl;

import co.edu.unimagdalena.finalproject_brasilia2.api.dto.CashClosureDtos.*;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.CashClosure;
import co.edu.unimagdalena.finalproject_brasilia2.domain.repositories.CashClosureRepository;
import co.edu.unimagdalena.finalproject_brasilia2.domain.repositories.UserRepository;
import co.edu.unimagdalena.finalproject_brasilia2.exceptions.NotFoundException;
import co.edu.unimagdalena.finalproject_brasilia2.services.CashClosureService;
import co.edu.unimagdalena.finalproject_brasilia2.services.mappers.CashClosureMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CashClosureServiceImpl implements CashClosureService {

    private final CashClosureRepository cashClosureRepository;
    private final UserRepository userRepository;
    private final CashClosureMapper cashClosureMapper;

    @Override
    @Transactional
    public CashClosureResponse create(CashClosureCreateRequest request) {
        log.info("Creating cash closure for clerk: {} on date: {}", request.clerkId(), request.closureDate());

        // Validate clerk exists
        var clerk = userRepository.findById(request.clerkId())
                .orElseThrow(() -> new NotFoundException("Clerk not found: " + request.clerkId()));

        // Validate clerk hasn't already closed for this date
        if (cashClosureRepository.existsByClerkIdAndClosureDate(request.clerkId(), request.closureDate())) {
            throw new IllegalStateException("Cash closure already exists for clerk " + request.clerkId() + " on " + request.closureDate());
        }

        // Create cash closure
        CashClosure cashClosure = CashClosure.builder()
                .clerk(clerk)
                .closureDate(request.closureDate())
                .totalCash(request.totalCash())
                .totalTicketsSold(request.totalTicketsSold())
                .totalParcelsRegistered(request.totalParcelsRegistered())
                .totalBaggageFees(request.totalBaggageFees())
                .notes(request.notes())
                .createdAt(OffsetDateTime.now())
                .build();

        CashClosure saved = cashClosureRepository.save(cashClosure);

        log.info("Cash closure created successfully: {} - Total: ${}", saved.getId(), saved.getTotalCash().add(saved.getTotalBaggageFees()));

        return cashClosureMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public CashClosureResponse get(Long id) {
        return cashClosureRepository.findById(id)
                .map(cashClosureMapper::toResponse)
                .orElseThrow(() -> new NotFoundException("Cash closure not found: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public CashClosureResponse getByClerkAndDate(Long clerkId, LocalDate date) {
        return cashClosureRepository.findByClerkIdAndClosureDate(clerkId, date)
                .map(cashClosureMapper::toResponse)
                .orElseThrow(() -> new NotFoundException("Cash closure not found for clerk " + clerkId + " on " + date));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CashClosureResponse> getByClerk(Long clerkId) {
        return cashClosureRepository.findByClerkId(clerkId).stream()
                .map(cashClosureMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CashClosureResponse> getByDateRange(LocalDate startDate, LocalDate endDate) {
        return cashClosureRepository.findByClosureDateBetween(startDate, endDate).stream()
                .map(cashClosureMapper::toResponse)
                .toList();
    }
}