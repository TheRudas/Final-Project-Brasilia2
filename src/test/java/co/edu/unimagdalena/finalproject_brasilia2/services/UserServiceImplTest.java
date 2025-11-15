package co.edu.unimagdalena.finalproject_brasilia2.services;

import co.edu.unimagdalena.finalproject_brasilia2.api.dto.UserDtos.UserCreateRequest;
import co.edu.unimagdalena.finalproject_brasilia2.api.dto.UserDtos.UserUpdateRequest;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.User;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.enums.UserRole;
import co.edu.unimagdalena.finalproject_brasilia2.domain.repositories.UserRepository;
import co.edu.unimagdalena.finalproject_brasilia2.exceptions.NotFoundException;
import co.edu.unimagdalena.finalproject_brasilia2.services.impl.UserServiceImpl;
import co.edu.unimagdalena.finalproject_brasilia2.services.mappers.UserMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Spy
    private UserMapper mapper = Mappers.getMapper(UserMapper.class);

    @InjectMocks
    private UserServiceImpl service;

    // ============= CREATE TESTS =============

    @Test
    void shouldCreateUserSuccessfully() {
        // Given
        var request = new UserCreateRequest(
                "Juan Perez",
                "juan.perez@example.com",
                "3001234567",
                UserRole.PASSENGER,
                "password123"
        );

        when(userRepository.findByEmail("juan.perez@example.com")).thenReturn(Optional.empty());
        when(userRepository.findByPhone("3001234567")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(1L);
            return u;
        });

        // When
        var response = service.create(request);

        // Then
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("Juan Perez");
        assertThat(response.email()).isEqualTo("juan.perez@example.com");
        assertThat(response.phone()).isEqualTo("3001234567");
        assertThat(response.role()).isEqualTo(UserRole.PASSENGER);
        assertThat(response.status()).isTrue();
        assertThat(response.createdAt()).isNotNull();

        verify(userRepository).findByEmail("juan.perez@example.com");
        verify(userRepository).findByPhone("3001234567");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenEmailAlreadyExists() {
        // Given
        var existingUser = User.builder()
                .id(1L)
                .email("juan.perez@example.com")
                .build();

        var request = new UserCreateRequest(
                "Juan Perez",
                "juan.perez@example.com",
                "3001234567",
                UserRole.PASSENGER,
                "password123"
        );

        when(userRepository.findByEmail("juan.perez@example.com"))
                .thenReturn(Optional.of(existingUser));

        // When / Then
        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User with this email already exists");

        verify(userRepository).findByEmail("juan.perez@example.com");
        verify(userRepository, never()).findByPhone(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenPhoneAlreadyExists() {
        // Given
        var existingUser = User.builder()
                .id(1L)
                .phone("3001234567")
                .build();

        var request = new UserCreateRequest(
                "Juan Perez",
                "juan.perez@example.com",
                "3001234567",
                UserRole.PASSENGER,
                "password123"
        );

        when(userRepository.findByEmail("juan.perez@example.com")).thenReturn(Optional.empty());
        when(userRepository.findByPhone("3001234567")).thenReturn(Optional.of(existingUser));

        // When / Then
        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User with phone already exists");

        verify(userRepository).findByEmail("juan.perez@example.com");
        verify(userRepository).findByPhone("3001234567");
        verify(userRepository, never()).save(any());
    }

    // ============= UPDATE TESTS =============

    @Test
    void shouldUpdateUserSuccessfully() {
        // Given
        var existingUser = User.builder()
                .id(1L)
                .name("Juan Perez")
                .email("juan.perez@example.com")
                .phone("3001234567")
                .role(UserRole.PASSENGER)
                .status(true)
                .passwordHash("hashedPassword")
                .createdAt(OffsetDateTime.now())
                .build();

        var updateRequest = new UserUpdateRequest(
                "Juan Carlos Perez",
                "juancarlos@example.com",
                "3009876543",
                UserRole.CLERK,
                true
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        var response = service.update(1L, updateRequest);

        // Then
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("Juan Carlos Perez");
        assertThat(response.email()).isEqualTo("juancarlos@example.com");
        assertThat(response.phone()).isEqualTo("3009876543");
        assertThat(response.role()).isEqualTo(UserRole.CLERK);
        assertThat(response.status()).isTrue();

        verify(userRepository).findById(1L);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void shouldUpdateOnlyNameWhenOtherFieldsAreNull() {
        // Given
        var existingUser = User.builder()
                .id(1L)
                .name("Juan Perez")
                .email("juan.perez@example.com")
                .phone("3001234567")
                .role(UserRole.PASSENGER)
                .status(true)
                .passwordHash("hashedPassword")
                .createdAt(OffsetDateTime.now())
                .build();

        var updateRequest = new UserUpdateRequest(
                "Nuevo Nombre",
                null,
                null,
                null,
                true
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        var response = service.update(1L, updateRequest);

        // Then
        assertThat(response.name()).isEqualTo("Nuevo Nombre");
        assertThat(response.email()).isEqualTo("juan.perez@example.com"); // No cambio
        assertThat(response.phone()).isEqualTo("3001234567"); // No cambio
        assertThat(response.role()).isEqualTo(UserRole.PASSENGER); // No cambio

        verify(userRepository).findById(1L);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void shouldUpdateUserStatusToInactive() {
        // Given
        var existingUser = User.builder()
                .id(1L)
                .name("Juan Perez")
                .email("juan.perez@example.com")
                .phone("3001234567")
                .role(UserRole.PASSENGER)
                .status(true)
                .passwordHash("hashedPassword")
                .createdAt(OffsetDateTime.now())
                .build();

        var updateRequest = new UserUpdateRequest(
                null,
                null,
                null,
                null,
                false
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        var response = service.update(1L, updateRequest);

        // Then
        assertThat(response.status()).isFalse();

        verify(userRepository).findById(1L);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void shouldThrowNotFoundExceptionWhenUpdateNonExistentUser() {
        // Given
        var updateRequest = new UserUpdateRequest(
                "Nuevo Nombre",
                "nuevo@example.com",
                "3009876543",
                UserRole.ADMIN,
                true
        );
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> service.update(99L, updateRequest))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("User 99 not found");

        verify(userRepository).findById(99L);
        verify(userRepository, never()).save(any());
    }

    // ============= GET BY ID TESTS =============

    @Test
    void shouldGetUserById() {
        // Given
        var user = User.builder()
                .id(1L)
                .name("Maria Garcia")
                .email("maria.garcia@example.com")
                .phone("3001234567")
                .role(UserRole.DISPATCHER)
                .status(true)
                .passwordHash("hashedPassword")
                .createdAt(OffsetDateTime.now())
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // When
        var response = service.get(1L);

        // Then
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("Maria Garcia");
        assertThat(response.email()).isEqualTo("maria.garcia@example.com");
        assertThat(response.phone()).isEqualTo("3001234567");
        assertThat(response.role()).isEqualTo(UserRole.DISPATCHER);
        assertThat(response.status()).isTrue();

        verify(userRepository).findById(1L);
    }

    @Test
    void shouldThrowNotFoundExceptionWhenGetNonExistentUser() {
        // Given
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> service.get(99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("User 99 not found");

        verify(userRepository).findById(99L);
    }

    // ============= GET BY EMAIL TESTS =============

    @Test
    void shouldGetUserByEmail() {
        // Given
        var user = User.builder()
                .id(1L)
                .name("Carlos Ruiz")
                .email("carlos.ruiz@example.com")
                .phone("3001234567")
                .role(UserRole.DRIVER)
                .status(true)
                .passwordHash("hashedPassword")
                .createdAt(OffsetDateTime.now())
                .build();

        when(userRepository.findByEmail("carlos.ruiz@example.com"))
                .thenReturn(Optional.of(user));

        // When
        var response = service.getByEmail("carlos.ruiz@example.com");

        // Then
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("Carlos Ruiz");
        assertThat(response.email()).isEqualTo("carlos.ruiz@example.com");

        verify(userRepository).findByEmail("carlos.ruiz@example.com");
    }

    @Test
    void shouldThrowNotFoundExceptionWhenEmailNotExists() {
        // Given
        when(userRepository.findByEmail("noexiste@example.com"))
                .thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> service.getByEmail("noexiste@example.com"))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("User with email \"noexiste@example.com\" not found");

        verify(userRepository).findByEmail("noexiste@example.com");
    }

    // ============= GET BY PHONE TESTS =============

    @Test
    void shouldGetUserByPhone() {
        // Given
        var user = User.builder()
                .id(1L)
                .name("Ana Lopez")
                .email("ana.lopez@example.com")
                .phone("3009876543")
                .role(UserRole.CLERK)
                .status(true)
                .passwordHash("hashedPassword")
                .createdAt(OffsetDateTime.now())
                .build();

        when(userRepository.findByPhone("3009876543")).thenReturn(Optional.of(user));

        // When
        var response = service.getByPhone("3009876543");

        // Then
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("Ana Lopez");
        assertThat(response.phone()).isEqualTo("3009876543");

        verify(userRepository).findByPhone("3009876543");
    }

    @Test
    void shouldThrowNotFoundExceptionWhenPhoneNotExists() {
        // Given
        when(userRepository.findByPhone("3001111111")).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> service.getByPhone("3001111111"))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("User with phone \"3001111111\" not found");

        verify(userRepository).findByPhone("3001111111");
    }

    // ============= GET BY ROLE TESTS =============

    @Test
    void shouldGetUsersByRole() {
        // Given
        var user1 = User.builder()
                .id(1L)
                .name("Pasajero 1")
                .email("pasajero1@example.com")
                .phone("3001234567")
                .role(UserRole.PASSENGER)
                .status(true)
                .passwordHash("hashedPassword")
                .createdAt(OffsetDateTime.now())
                .build();

        var user2 = User.builder()
                .id(2L)
                .name("Pasajero 2")
                .email("pasajero2@example.com")
                .phone("3009876543")
                .role(UserRole.PASSENGER)
                .status(true)
                .passwordHash("hashedPassword")
                .createdAt(OffsetDateTime.now())
                .build();

        when(userRepository.findByRole(UserRole.PASSENGER))
                .thenReturn(List.of(user1, user2));

        // When
        var result = service.getByRole(UserRole.PASSENGER);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).role()).isEqualTo(UserRole.PASSENGER);
        assertThat(result.get(1).role()).isEqualTo(UserRole.PASSENGER);

        verify(userRepository).findByRole(UserRole.PASSENGER);
    }

    @Test
    void shouldThrowNotFoundExceptionWhenNoUsersWithRole() {
        // Given
        when(userRepository.findByRole(UserRole.ADMIN)).thenReturn(List.of());

        // When / Then
        assertThatThrownBy(() -> service.getByRole(UserRole.ADMIN))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Any user has the \"ADMIN\" role");

        verify(userRepository).findByRole(UserRole.ADMIN);
    }

    // ============= GET ACTIVE BY ROLE TESTS =============

    @Test
    void shouldGetActiveUsersByRole() {
        // Given
        var user1 = User.builder()
                .id(1L)
                .name("Driver 1")
                .email("driver1@example.com")
                .phone("3001234567")
                .role(UserRole.DRIVER)
                .status(true)
                .passwordHash("hashedPassword")
                .createdAt(OffsetDateTime.now())
                .build();

        var user2 = User.builder()
                .id(2L)
                .name("Driver 2")
                .email("driver2@example.com")
                .phone("3009876543")
                .role(UserRole.DRIVER)
                .status(true)
                .passwordHash("hashedPassword")
                .createdAt(OffsetDateTime.now())
                .build();

        when(userRepository.findByRoleAndStatus(UserRole.DRIVER, true))
                .thenReturn(List.of(user1, user2));

        // When
        var result = service.getActiveByRole(UserRole.DRIVER);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).role()).isEqualTo(UserRole.DRIVER);
        assertThat(result.get(0).status()).isTrue();
        assertThat(result.get(1).role()).isEqualTo(UserRole.DRIVER);
        assertThat(result.get(1).status()).isTrue();

        verify(userRepository).findByRoleAndStatus(UserRole.DRIVER, true);
    }

    @Test
    void shouldThrowNotFoundExceptionWhenNoActiveUsersWithRole() {
        // Given
        when(userRepository.findByRoleAndStatus(UserRole.CLERK, true))
                .thenReturn(List.of());

        // When / Then
        assertThatThrownBy(() -> service.getActiveByRole(UserRole.CLERK))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("No active users with this role");

        verify(userRepository).findByRoleAndStatus(UserRole.CLERK, true);
    }

    // ============= GET BY STATUS TESTS =============

    @Test
    void shouldGetActiveUsers() {
        // Given
        var user1 = User.builder()
                .id(1L)
                .name("Usuario Activo 1")
                .email("activo1@example.com")
                .phone("3001234567")
                .role(UserRole.PASSENGER)
                .status(true)
                .passwordHash("hashedPassword")
                .createdAt(OffsetDateTime.now())
                .build();

        var user2 = User.builder()
                .id(2L)
                .name("Usuario Activo 2")
                .email("activo2@example.com")
                .phone("3009876543")
                .role(UserRole.CLERK)
                .status(true)
                .passwordHash("hashedPassword")
                .createdAt(OffsetDateTime.now())
                .build();

        var page = new PageImpl<>(List.of(user1, user2));
        var pageable = PageRequest.of(0, 10);

        when(userRepository.findByStatus(true, pageable)).thenReturn(page);

        // When
        var result = service.getByStatus(true, pageable);

        // Then
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).status()).isTrue();
        assertThat(result.getContent().get(1).status()).isTrue();

        verify(userRepository).findByStatus(true, pageable);
    }

    @Test
    void shouldGetInactiveUsers() {
        // Given
        var user1 = User.builder()
                .id(1L)
                .name("Usuario Inactivo 1")
                .email("inactivo1@example.com")
                .phone("3001234567")
                .role(UserRole.PASSENGER)
                .status(false)
                .passwordHash("hashedPassword")
                .createdAt(OffsetDateTime.now())
                .build();

        var page = new PageImpl<>(List.of(user1));
        var pageable = PageRequest.of(0, 10);

        when(userRepository.findByStatus(false, pageable)).thenReturn(page);

        // When
        var result = service.getByStatus(false, pageable);

        // Then
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).status()).isFalse();

        verify(userRepository).findByStatus(false, pageable);
    }

    @Test
    void shouldThrowNotFoundExceptionWhenNoUsersWithActiveStatus() {
        // Given
        var page = new PageImpl<User>(List.of());
        var pageable = PageRequest.of(0, 10);

        when(userRepository.findByStatus(true, pageable)).thenReturn(page);

        // When / Then
        assertThatThrownBy(() -> service.getByStatus(true, pageable))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("No users with status: \"Active\"");

        verify(userRepository).findByStatus(true, pageable);
    }

    @Test
    void shouldThrowNotFoundExceptionWhenNoUsersWithInactiveStatus() {
        // Given
        var page = new PageImpl<User>(List.of());
        var pageable = PageRequest.of(0, 10);

        when(userRepository.findByStatus(false, pageable)).thenReturn(page);

        // When / Then
        assertThatThrownBy(() -> service.getByStatus(false, pageable))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("No users with status: \"Inactive\"");

        verify(userRepository).findByStatus(false, pageable);
    }

    // ============= DEACTIVATE TESTS =============

    @Test
    void shouldDeactivateUserSuccessfully() {
        // Given
        var user = User.builder()
                .id(1L)
                .name("Juan Perez")
                .email("juan.perez@example.com")
                .phone("3001234567")
                .role(UserRole.PASSENGER)
                .status(true)
                .passwordHash("hashedPassword")
                .createdAt(OffsetDateTime.now())
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        service.deactivate(1L);

        // Then
        assertThat(user.isStatus()).isFalse();
        verify(userRepository).findById(1L);
        verify(userRepository).save(user);
    }

    @Test
    void shouldThrowNotFoundExceptionWhenDeactivateNonExistentUser() {
        // Given
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> service.deactivate(99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("User with id: 99 not found");

        verify(userRepository).findById(99L);
        verify(userRepository, never()).save(any());
    }

    // ============= ACTIVATE TESTS =============

    @Test
    void shouldActivateUserSuccessfully() {
        // Given
        var user = User.builder()
                .id(1L)
                .name("Juan Perez")
                .email("juan.perez@example.com")
                .phone("3001234567")
                .role(UserRole.PASSENGER)
                .status(false)
                .passwordHash("hashedPassword")
                .createdAt(OffsetDateTime.now())
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        service.activate(1L);

        // Then
        assertThat(user.isStatus()).isTrue();
        verify(userRepository).findById(1L);
        verify(userRepository).save(user);
    }

    @Test
    void shouldThrowNotFoundExceptionWhenActivateNonExistentUser() {
        // Given
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> service.activate(99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("User with id: 99 not found");

        verify(userRepository).findById(99L);
        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldActivateAlreadyActiveUser() {
        // Given
        var user = User.builder()
                .id(1L)
                .name("Juan Perez")
                .email("juan.perez@example.com")
                .phone("3001234567")
                .role(UserRole.PASSENGER)
                .status(true) // Ya esta activo
                .passwordHash("hashedPassword")
                .createdAt(OffsetDateTime.now())
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        service.activate(1L);

        // Then
        assertThat(user.isStatus()).isTrue(); // Sigue activo
        verify(userRepository).findById(1L);
        verify(userRepository).save(user);
    }

    @Test
    void shouldDeactivateAlreadyInactiveUser() {
        // Given
        var user = User.builder()
                .id(1L)
                .name("Juan Perez")
                .email("juan.perez@example.com")
                .phone("3001234567")
                .role(UserRole.PASSENGER)
                .status(false) // Ya esta inactivo
                .passwordHash("hashedPassword")
                .createdAt(OffsetDateTime.now())
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        service.deactivate(1L);

        // Then
        assertThat(user.isStatus()).isFalse(); // Sigue inactivo
        verify(userRepository).findById(1L);
        verify(userRepository).save(user);
    }
}
