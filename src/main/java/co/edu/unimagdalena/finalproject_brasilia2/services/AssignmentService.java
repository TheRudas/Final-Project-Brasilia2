package co.edu.unimagdalena.finalproject_brasilia2.services;

import co.edu.unimagdalena.finalproject_brasilia2.api.dto.AssignmentDtos;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AssignmentService {
    AssignmentDtos.AssignmentResponse create(AssignmentDtos.AssignmentCreateRequest request);

    AssignmentDtos.AssignmentResponse update(Long id, AssignmentDtos.AssignmentUpdateRequest request);

    AssignmentDtos.AssignmentResponse get(Long id);

    void delete(Long id);

    AssignmentDtos.AssignmentResponse approveChecklist(Long assignmentId);

    Page<AssignmentDtos.AssignmentResponse> getByTripId(Long tripId, Pageable pageable);

    Page<AssignmentDtos.AssignmentResponse> getByDriverId(Long DriverId, Pageable pageable);



}
//tripd id and driverID DispatcherID in response are mapped manually