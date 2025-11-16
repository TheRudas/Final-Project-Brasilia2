package co.edu.unimagdalena.finalproject_brasilia2.api.dto;

import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.User;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;

public class AssignmentDtos {
   public record AssignmentCreateRequest(@NotNull Long tripId, Long driverId, Long dispatcherId,
                                         boolean checkListOk) implements java.io.Serializable {}
    public record AssignmentUpdateRequest(Long driverId, Long dispatcherId,
                                         Boolean checkListOk) implements java.io.Serializable {}
    public record AssignmentResponse(Long id, Long tripId, Long driverId, Long dispatcherId,
                                     boolean checkListOk, OffsetDateTime assignedAt) implements java.io.Serializable {}

}
