package co.edu.unimagdalena.finalproject_brasilia2.services.mappers;

import co.edu.unimagdalena.finalproject_brasilia2.api.dto.BusDtos.*;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.Bus;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.assertj.core.api.Assertions.assertThat;

class BusMapperTest {
    private final BusMapper mapper = Mappers.getMapper(BusMapper.class);

    @Test
    void toEntity_shouldMapCreateRequest() {
        var req = new BusCreateRequest("ABC-123", 40, true);
        Bus entity = mapper.toEntity(req);

        assertThat(entity.getPlate()).isEqualTo("ABC-123");
        assertThat(entity.getCapacity()).isEqualTo(40);
        assertThat(entity.isStatus()).isTrue();
        assertThat(entity.getId()).isNull();
        assertThat(entity.getAmenities()).isNull();
    }

    @Test
    void toResponse_shouldMapEntity() {
        var bus = Bus.builder()
                .id(5L)
                .plate("XYZ-789")
                .capacity(50)
                .status(true)
                .build();

        BusResponse dto = mapper.toResponse(bus);

        assertThat(dto.id()).isEqualTo(5L);
        assertThat(dto.licensePlate()).isEqualTo("XYZ-789");
        assertThat(dto.capacity()).isEqualTo(50);
        assertThat(dto.status()).isTrue();
    }

    @Test
    void patch_shouldIgnoreNulls() {
        var entity = Bus.builder()
                .id(1L)
                .plate("OLD-111")
                .capacity(30)
                .status(true)
                .build();
        var changes = new BusUpdateRequest("NEW-222", null, false);

        mapper.patch(entity, changes);

        assertThat(entity.getPlate()).isEqualTo("NEW-222");
        assertThat(entity.getCapacity()).isEqualTo(30);
        assertThat(entity.isStatus()).isFalse();
    }

    @Test
    void patch_shouldUpdateCapacity() {
        var entity = Bus.builder()
                .id(1L)
                .plate("OLD-111")
                .capacity(30)
                .status(true)
                .build();
        var changes = new BusUpdateRequest(null, 45, true);

        mapper.patch(entity, changes);

        assertThat(entity.getPlate()).isEqualTo("OLD-111");
        assertThat(entity.getCapacity()).isEqualTo(45);
        assertThat(entity.isStatus()).isTrue();
    }
}

