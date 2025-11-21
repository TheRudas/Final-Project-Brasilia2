package co.edu.unimagdalena.finalproject_brasilia2.services.mappers;

import co.edu.unimagdalena.finalproject_brasilia2.api.dto.FareRuleDtos.*;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.*;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class FareRuleMapperTest {
    private final FareRuleMapper mapper = Mappers.getMapper(FareRuleMapper.class);

    @Test
    void toEntity_shouldMapCreateRequest() {
        var req = new FareRuleCreateRequest(1L, 2L, 3L, new BigDecimal("50000.00"));
        FareRule entity = mapper.toEntity(req);

        assertThat(entity.getBasePrice()).isEqualByComparingTo(new BigDecimal("50000.00"));
        assertThat(entity.getId()).isNull();
    }

    @Test
    void toResponse_shouldMapEntity() {
        var route = Route.builder().id(10L).build();
        var fromStop = Stop.builder().id(20L).build();
        var toStop = Stop.builder().id(30L).build();
        Set<String> discounts = new HashSet<>();
        discounts.add("STUDENT");

        var fareRule = FareRule.builder()
                .id(5L)
                .route(route)
                .fromStop(fromStop)
                .toStop(toStop)
                .basePrice(new BigDecimal("75000.00"))
                .discounts(discounts)
                .dynamicPricing(true)
                .build();

        FareRuleResponse dto = mapper.toResponse(fareRule);

        assertThat(dto.id()).isEqualTo(5L);
        assertThat(dto.routeId()).isEqualTo(10L);
        assertThat(dto.fromStopId()).isEqualTo(20L);
        assertThat(dto.toStopId()).isEqualTo(30L);
        assertThat(dto.basePrice()).isEqualByComparingTo(new BigDecimal("75000.00"));
        assertThat(dto.dynamicPricing()).isTrue();
    }

    @Test
    void patch_shouldIgnoreNulls() {
        var entity = FareRule.builder()
                .id(1L)
                .basePrice(new BigDecimal("50000.00"))
                .build();
        var changes = new FareRuleUpdateRequest(null, null, null, new BigDecimal("60000.00"));

        mapper.patch(entity, changes);

        assertThat(entity.getBasePrice()).isEqualByComparingTo(new BigDecimal("60000.00"));
    }
}

