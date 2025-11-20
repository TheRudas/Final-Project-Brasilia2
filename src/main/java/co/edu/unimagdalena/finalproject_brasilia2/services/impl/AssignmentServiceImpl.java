package co.edu.unimagdalena.finalproject_brasilia2.services.impl;

import co.edu.unimagdalena.finalproject_brasilia2.api.dto.AssignmentDtos;
import co.edu.unimagdalena.finalproject_brasilia2.api.dto.AssignmentDtos.*;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.Assignment;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.Trip;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.User;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.enums.UserRole;
import co.edu.unimagdalena.finalproject_brasilia2.domain.repositories.AssignmentRepository;
import co.edu.unimagdalena.finalproject_brasilia2.domain.repositories.TripRepository;
import co.edu.unimagdalena.finalproject_brasilia2.domain.repositories.UserRepository;
import co.edu.unimagdalena.finalproject_brasilia2.exceptions.NotFoundException;
import co.edu.unimagdalena.finalproject_brasilia2.services.AssignmentService;
import co.edu.unimagdalena.finalproject_brasilia2.services.mappers.AssignmentMapper;
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
    private final AssignmentMapper mapper;


    @Override
    @Transactional
    public AssignmentDtos.AssignmentResponse create(AssignmentDtos.AssignmentCreateRequest request) {

        Trip trip = tripRepository.findById(request.tripId())
                .orElseThrow(() -> new NotFoundException("Trip %d not found".formatted(request.tripId())));

        // Validate that a trip only has ONE Assignment
        if (repository.existsByTripId(request.tripId())) {
            throw new IllegalStateException(
                    "Trip %d already has an assignment".formatted(request.tripId())
            );
        }

        User driver = userRepository.findById(request.driverId())
                .orElseThrow(() -> new NotFoundException("Driver %d not found".formatted(request.driverId())));

        // Validate driver role
        if (driver.getRole() != UserRole.DRIVER) {
            throw new IllegalArgumentException(
                    "User %d is not a DRIVER (current role: %s)".formatted(driver.getId(), driver.getRole())
            );
        }

        User dispatcher = userRepository.findById(request.dispatcherId())
                .orElseThrow(() -> new NotFoundException("Dispatcher %d not found".formatted(request.dispatcherId())));

        // Validate dispatcher role
        if (dispatcher.getRole() != UserRole.DISPATCHER) {
            throw new IllegalArgumentException(
                    "User %d is not a DISPATCHER (current role: %s)".formatted(dispatcher.getId(), dispatcher.getRole())
            );
        }

        Assignment entity = Assignment.builder()
                .trip(trip)
                .driver(driver)
                .dispatcher(dispatcher)
                .checkListOk(request.checkListOk())
                .assignedAt(OffsetDateTime.now())
                .build();

        return mapper.toResponse(repository.save(entity));
    }

    @Override
    @Transactional
    public AssignmentDtos.AssignmentResponse update(Long id, AssignmentDtos.AssignmentUpdateRequest request) {

        var assignment = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Assignment %d not found".formatted(id)));

        if (request.driverId() != null) {
            var newDriver = userRepository.findById(request.driverId())
                    .orElseThrow(() -> new NotFoundException("Driver %d not found".formatted(request.driverId())));

            // Validate driver role
            if (newDriver.getRole() != UserRole.DRIVER) {
                throw new IllegalArgumentException("User %d is not a DRIVER".formatted(newDriver.getId()));
            }

            assignment.setDriver(newDriver);
        }

        if (request.dispatcherId() != null) {
            var newDispatcher = userRepository.findById(request.dispatcherId())
                    .orElseThrow(() -> new NotFoundException("Dispatcher %d not found".formatted(request.dispatcherId())));

            // Validate dispatcher role
            if (newDispatcher.getRole() != UserRole.DISPATCHER) {
                throw new IllegalArgumentException("User %d is not a DISPATCHER".formatted(newDispatcher.getId()));
            }

            assignment.setDispatcher(newDispatcher);
        }

        if (request.checkListOk() != null) {
            assignment.setCheckListOk(request.checkListOk());
        }

        return mapper.toResponse(repository.save(assignment));
    }

    @Override
    public AssignmentDtos.AssignmentResponse get(Long id) {
        return repository.findById(id)
                .map(mapper::toResponse)
                .orElseThrow(() -> new NotFoundException("Assignment %d not found".formatted(id)));
    }

    // ----------------------------------------------------------------------
    // DELETE
    // ----------------------------------------------------------------------
    @Override
    @Transactional
    public void delete(Long id) {
        var assignment = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Assignment %d not found or already deleted".formatted(id)));

        repository.delete(assignment);
    }

    // ----------------------------------------------------------------------
    // APPROVE CHECKLIST
    // ----------------------------------------------------------------------
    @Override
    @Transactional
    public AssignmentDtos.AssignmentResponse approveChecklist(Long assignmentId) {
        var assignment = repository.findById(assignmentId)
                .orElseThrow(() -> new NotFoundException("Assignment %d not found".formatted(assignmentId)));

        if (assignment.isCheckListOk()) {
            throw new IllegalStateException("Checklist is already approved for assignment %d".formatted(assignmentId));
        }

        assignment.setCheckListOk(true);

        return mapper.toResponse(repository.save(assignment));
    }

    // ----------------------------------------------------------------------
    // GET BY TRIP ID
    // ----------------------------------------------------------------------
    @Override
    public Page<AssignmentDtos.AssignmentResponse> getByTripId(Long tripId, Pageable pageable) {
        var result = repository.findByTripId(tripId, pageable);

        if (result.isEmpty()) {
            throw new NotFoundException("No assignments found for trip %d".formatted(tripId));
        }

        return result.map(mapper::toResponse);
    }

    // ----------------------------------------------------------------------
    // GET BY DRIVER ID
    // ----------------------------------------------------------------------
    @Override
    public Page<AssignmentDtos.AssignmentResponse> getByDriverId(Long driverId, Pageable pageable) {
        var result = repository.findByDriverId(driverId, pageable);

        if (result.isEmpty()) {
            throw new NotFoundException("No assignments found for driver %d".formatted(driverId));
        }

        return result.map(mapper::toResponse);
    }
}
