package co.edu.unimagdalena.finalproject_brasilia2.services.mappers;

import co.edu.unimagdalena.finalproject_brasilia2.api.dto.ParcelDtos.*;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.*;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.enums.ParcelStatus;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class ParcelMapperTest {
    private final ParcelMapper mapper = Mappers.getMapper(ParcelMapper.class);

    @Test
    void toEntity_shouldMapCreateRequest() {
        var req = new ParcelCreateRequest("P-001", "Juan Sender", "3001234567",
                "Maria Receiver", "3009876543", 1L, 2L);
        Parcel entity = mapper.toEntity(req);

        assertThat(entity.getCode()).isEqualTo("P-001");
        assertThat(entity.getSenderName()).isEqualTo("Juan Sender");
        assertThat(entity.getSenderPhone()).isEqualTo("3001234567");
        assertThat(entity.getReceiverName()).isEqualTo("Maria Receiver");
        assertThat(entity.getReceiverPhone()).isEqualTo("3009876543");
        assertThat(entity.getId()).isNull();
        assertThat(entity.getFromStop()).isNull();
        assertThat(entity.getToStop()).isNull();
        assertThat(entity.getPrice()).isNull();
        assertThat(entity.getStatus()).isNull();
        assertThat(entity.getDeliveryOtp()).isNull();
    }

    @Test
    void toResponse_shouldMapEntity() {
        var fromStop = Stop.builder().id(10L).build();
        var toStop = Stop.builder().id(20L).build();

        var parcel = Parcel.builder()
                .id(5L)
                .code("P-002")
                .senderName("Pedro Sender")
                .senderPhone("3111111111")
                .receiverName("Ana Receiver")
                .receiverPhone("3222222222")
                .fromStop(fromStop)
                .toStop(toStop)
                .price(new BigDecimal("25000.00"))
                .status(ParcelStatus.IN_TRANSIT)
                .deliveryOtp("12345678")
                .build();

        ParcelResponse dto = mapper.toResponse(parcel);

        assertThat(dto.id()).isEqualTo(5L);
        assertThat(dto.code()).isEqualTo("P-002");
        assertThat(dto.senderName()).isEqualTo("Pedro Sender");
        assertThat(dto.senderPhone()).isEqualTo("3111111111");
        assertThat(dto.receiverName()).isEqualTo("Ana Receiver");
        assertThat(dto.receiverPhone()).isEqualTo("3222222222");
        assertThat(dto.fromStopId()).isEqualTo(10L);
        assertThat(dto.toStopId()).isEqualTo(20L);
    }

    @Test
    void patch_shouldIgnoreNulls() {
        var entity = Parcel.builder()
                .id(1L)
                .senderName("Old Sender")
                .senderPhone("3000000000")
                .receiverName("Old Receiver")
                .receiverPhone("3000000001")
                .build();
        var changes = new ParcelUpdateRequest("New Sender", null, null, "3999999999", null, null);

        mapper.patch(entity, changes);

        assertThat(entity.getSenderName()).isEqualTo("New Sender");
        assertThat(entity.getSenderPhone()).isEqualTo("3000000000");
        assertThat(entity.getReceiverName()).isEqualTo("Old Receiver");
        assertThat(entity.getReceiverPhone()).isEqualTo("3999999999");
    }
}

