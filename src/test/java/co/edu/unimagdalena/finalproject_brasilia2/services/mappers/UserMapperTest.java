package co.edu.unimagdalena.finalproject_brasilia2.services.mappers;

import co.edu.unimagdalena.finalproject_brasilia2.api.dto.UserDtos.*;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.*;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.enums.*;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class UserMapperTest {
    private final UserMapper mapper = Mappers.getMapper(UserMapper.class);

    @Test
    void toEntity_shouldMapCreateRequest() {
        var req = new UserCreateRequest("Pedro Garcia", "pedro@example.com",
                "3001234567", UserRole.PASSENGER, "password123");
        User entity = mapper.toEntity(req);

        assertThat(entity.getName()).isEqualTo("Pedro Garcia");
        assertThat(entity.getEmail()).isEqualTo("pedro@example.com");
        assertThat(entity.getPhone()).isEqualTo("3001234567");
        assertThat(entity.getRole()).isEqualTo(UserRole.PASSENGER);
        assertThat(entity.isStatus()).isTrue();
    }

    @Test
    void toResponse_shouldMapEntity() {
        var u = User.builder()
                .id(12L).name("Ana Martinez").email("ana@example.com")
                .phone("3009876543").role(UserRole.DRIVER).status(true)
                .createdAt(OffsetDateTime.now()).build();

        UserResponse dto = mapper.toResponse(u);

        assertThat(dto.id()).isEqualTo(12L);
        assertThat(dto.name()).isEqualTo("Ana Martinez");
        assertThat(dto.role()).isEqualTo(UserRole.DRIVER);
        assertThat(dto.status()).isTrue();
    }

    @Test
    void patch_shouldIgnoreNulls() {
        var entity = User.builder().id(1L).name("Old Name")
                .email("old@example.com").phone("3001111111")
                .role(UserRole.PASSENGER).status(true).build();
        var changes = new UserUpdateRequest("New Name", null, null,
                UserRole.CLERK, false);

        mapper.patch(entity, changes);

        assertThat(entity.getName()).isEqualTo("New Name");
        assertThat(entity.getEmail()).isEqualTo("old@example.com");
        assertThat(entity.getPhone()).isEqualTo("3001111111");
        assertThat(entity.getRole()).isEqualTo(UserRole.CLERK);
        assertThat(entity.isStatus()).isFalse();
    }
}
