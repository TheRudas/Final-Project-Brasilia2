package co.edu.unimagdalena.finalproject_brasilia2.api.dto;

import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.enums.IncidentEntityType;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.enums.IncidentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.time.OffsetDateTime;

public class IncidentDtos {
    public record IncidentCreateRequest(@NotNull IncidentEntityType entityType, @NotNull Long entityId, @NotNull
                                        IncidentType type, @Size(max = 255) @NotBlank String note) implements Serializable {}

    public record IncidentUpdateRequest(@Size(max = 255) String note, IncidentType type) implements Serializable {}

    public record IncidentResponse(Long id, IncidentEntityType entityType, Long entityId, IncidentType type,
                                   String note, OffsetDateTime createdAt) implements Serializable {}
}
