package co.edu.unimagdalena.finalproject_brasilia2.api.dto;

import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.Route;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.Stop;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.io.Serializable;
import java.math.BigDecimal;

public class FareRuleDtos {
    public record FareRuleCreateRequest(@NotNull Long routeId,@NotNull Long fromStopId,@NotNull Long toStopId,
                                       @Positive BigDecimal basePrice) implements Serializable {
    }

    public record FareRuleUpdateRequest(@NotNull Long routeId, @NotNull Long fromStopId,@NotNull Long toStopId,
                                        @Positive  BigDecimal basePrice) implements Serializable {
    }
    public record FareRuleResponse(@NotNull Long id, @NotNull Long routeId, @NotNull Long fromStopId, @NotNull Long toStopId,
                                   @Positive BigDecimal basePrice, @NotBlank String discount, boolean dynamicPricing) implements Serializable {
    }

}