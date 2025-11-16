package co.edu.unimagdalena.finalproject_brasilia2.services.impl;

import co.edu.unimagdalena.finalproject_brasilia2.api.dto.AssignmentDtos;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.Assignment;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.Trip;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.User;
import co.edu.unimagdalena.finalproject_brasilia2.domain.repositories.AssignmentRepository;
import co.edu.unimagdalena.finalproject_brasilia2.domain.repositories.TripRepository;
import co.edu.unimagdalena.finalproject_brasilia2.domain.repositories.UserRepository;
import co.edu.unimagdalena.finalproject_brasilia2.exceptions.NotFoundException;
import co.edu.unimagdalena.finalproject_brasilia2.services.AssignmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AssignmentServiceImpl implements AssignmentService {

    private final AssignmentRepository repository;
    private final TripRepository tripRepository;
    private final UserRepository userRepository;

    // ----------------------------------------------------------------------
    // CREATE
    // ----------------------------------------------------------------------
    @Override
    @Transactional
    public AssignmentDtos.AssignmentResponse create(AssignmentDtos.AssignmentCreateRequest request) {

        Trip trip = tripRepository.findById(request.tripId())
                .orElseThrow(() ->
                        new NotFoundException("Trip %d not found".formatted(request.tripId()))
                );

        User driver = userRepository.findById(request.driverId())
                .orElseThrow(() ->
                        new NotFoundException("Driver %d not found".formatted(request.driverId()))
                );

        User dispatcher = userRepository.findById(request.dispatcherId())
                .orElseThrow(() ->
                        new NotFoundException("Dispatcher %d not found".formatted(request.dispatcherId()))
                );

        Assignment entity = Assignment.builder()
                .trip(trip)
                .driver(driver)
                .dispatcher(dispatcher)
                .checkListOk(request.checkListOk())
                .assignedAt(OffsetDateTime.now())
                .build();

        Assignment saved = repository.save(entity);

        return toResponse(saved);
    }

    // ----------------------------------------------------------------------
    // UPDATE (PATCH)
    // ----------------------------------------------------------------------
    @Override
    @Transactional
    public AssignmentDtos.AssignmentResponse update(Long id, AssignmentDtos.AssignmentUpdateRequest request) {

        Assignment assignment = repository.findById(id)
                .orElseThrow(() ->
                        new NotFoundException("Assignment %d not found".formatted(id))
                );

        if (request.driverId() != null) {
            User newDriver = userRepository.findById(request.driverId())
                    .orElseThrow(() ->
                            new NotFoundException("Driver %d not found".formatted(request.driverId()))
                    );
            assignment.setDriver(newDriver);
        }

        if (request.dispatcherId() != null) {
            User newDispatcher = userRepository.findById(request.dispatcherId())
                    .orElseThrow(() ->
                            new NotFoundException("Dispatcher %d not found".formatted(request.dispatcherId()))
                    );
            assignment.setDispatcher(newDispatcher);
        }

        if (request.checkListOk() != null) {
            assignment.setCheckListOk(request.checkListOk());
        }

        Assignment saved = repository.save(assignment);

        return toResponse(saved);
    }

    // ----------------------------------------------------------------------
    // GET BY ID
    // ----------------------------------------------------------------------
    @Override
    public AssignmentDtos.AssignmentResponse get(Long id) {
        return repository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() ->
                        new NotFoundException("Assignment %d not found".formatted(id))
                );
    }

    // ----------------------------------------------------------------------
    // DELETE
    // ----------------------------------------------------------------------
    @Override
    @Transactional
    public void delete(Long id) {
        Assignment assignment = repository.findById(id)
                .orElseThrow(() ->
                        new NotFoundException("Assignment %d not found or already deleted".formatted(id))
                );

        repository.delete(assignment);
    }

    // ----------------------------------------------------------------------
    // GET BY TRIP ID
    // ----------------------------------------------------------------------
    @Override
    public Page<AssignmentDtos.AssignmentResponse> getByTripId(Long tripId, Pageable pageable) {
        Page<Assignment> result = repository.findByTripId(tripId, pageable);

        if (result.isEmpty()) {
            throw new NotFoundException("No assignments found for trip %d".formatted(tripId));
        }

        return result.map(this::toResponse);
    }

    // ----------------------------------------------------------------------
    // GET BY DRIVER ID
    // ----------------------------------------------------------------------
    @Override
    public Page<AssignmentDtos.AssignmentResponse> getByDriverId(Long driverId, Pageable pageable) {
        Page<Assignment> result = repository.findByDriverId(driverId, pageable);

        if (result.isEmpty()) {
            throw new NotFoundException("No assignments found for driver %d".formatted(driverId));
        }

        return result.map(this::toResponse);
    }

    // ----------------------------------------------------------------------
    // MAPPER MANUAL (como haces en BusServiceImpl)
    // ----------------------------------------------------------------------
    private AssignmentDtos.AssignmentResponse toResponse(Assignment a) {
        return new AssignmentDtos.AssignmentResponse(
                a.getId(),
                a.getTrip().getId(),
                a.getDriver().getId(),
                a.getDispatcher().getId(),
                a.isCheckListOk(),
                a.getAssignedAt()
        );
    }
}
