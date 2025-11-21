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
    private UserMapper userMapper = Mappers.getMapper(UserMapper.class);

    @InjectMocks
    private UserServiceImpl service;

    @Test
    void shouldCreateUserSuccessfully() {
        // Given
        var request = new UserCreateRequest(
                "Juan Perez",
                "juan@example.com",
                "3001234567",
                UserRole.PASSENGER,
                "password123"
        );

        when(userRepository.findByEmail("juan@example.com")).thenReturn(Optional.empty());
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
        assertThat(response.email()).isEqualTo("juan@example.com");
        assertThat(response.phone()).isEqualTo("3001234567");
        assertThat(response.role()).isEqualTo(UserRole.PASSENGER);

        verify(userRepository).findByEmail("juan@example.com");
        verify(userRepository).findByPhone("3001234567");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenEmailAlreadyExists() {
        // Given
        var request = new UserCreateRequest(
                "Juan Perez",
                "juan@example.com",
                "3001234567",
                UserRole.PASSENGER,
                "password123"
        );

        when(userRepository.findByEmail("juan@example.com"))
                .thenReturn(Optional.of(User.builder().email("juan@example.com").build()));

        // When / Then
        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User with this email already exists");

        verify(userRepository).findByEmail("juan@example.com");
        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenPhoneAlreadyExists() {
        // Given
        var request = new UserCreateRequest(
                "Juan Perez",
                "juan@example.com",
                "3001234567",
                UserRole.PASSENGER,
                "password123"
        );

        when(userRepository.findByEmail("juan@example.com")).thenReturn(Optional.empty());
        when(userRepository.findByPhone("3001234567"))
                .thenReturn(Optional.of(User.builder().phone("3001234567").build()));

        // When / Then
        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User with phone already exists");

        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldUpdateUserSuccessfully() {
        // Given
        var existingUser = User.builder()
                .id(1L)
                .name("Juan Perez")
                .email("juan@example.com")
                .phone("3001234567")
                .role(UserRole.PASSENGER)
                .status(true)
                .createdAt(OffsetDateTime.now())
                .build();

        var updateRequest = new UserUpdateRequest(
                "Juan Carlos Perez",
                "juan.carlos@example.com",
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
        assertThat(response.email()).isEqualTo("juan.carlos@example.com");
        assertThat(response.phone()).isEqualTo("3009876543");
        assertThat(response.role()).isEqualTo(UserRole.CLERK);

        verify(userRepository).findById(1L);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void shouldThrowNotFoundExceptionWhenUpdateNonExistentUser() {
        // Given
        var updateRequest = new UserUpdateRequest(
                "Juan Perez",
                "juan@example.com",
                "3001234567",
                UserRole.PASSENGER,
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

    @Test
    void shouldGetUserById() {
        // Given
        var user = User.builder()
                .id(1L)
                .name("Juan Perez")
                .email("juan@example.com")
                .phone("3001234567")
                .role(UserRole.PASSENGER)
                .status(true)
                .createdAt(OffsetDateTime.now())
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // When
        var response = service.get(1L);

        // Then
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("Juan Perez");

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

    @Test
    void shouldDeleteUserSuccessfully() {
        // Given
        var user = User.builder()
                .id(1L)
                .name("Juan Perez")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        doNothing().when(userRepository).delete(user);

        // When
        service.delete(1L);

        // Then
        verify(userRepository).findById(1L);
        verify(userRepository).delete(user);
    }

    @Test
    void shouldThrowNotFoundExceptionWhenDeleteNonExistentUser() {
        // Given
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> service.delete(99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("User 99 not found");

        verify(userRepository).findById(99L);
        verify(userRepository, never()).delete(any());
    }

    @Test
    void shouldListUsers() {
        // Given
        var user1 = User.builder().id(1L).name("User 1").build();
        var user2 = User.builder().id(2L).name("User 2").build();
        var pageable = PageRequest.of(0, 10);
        var page = new PageImpl<>(List.of(user1, user2));

        when(userRepository.findAll(pageable)).thenReturn(page);

        // When
        var result = service.list(pageable);

        // Then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).name()).isEqualTo("User 1");

        verify(userRepository).findAll(pageable);
    }

    @Test
    void shouldGetUserByEmail() {
        // Given
        var user = User.builder()
                .id(1L)
                .name("Juan Perez")
                .email("juan@example.com")
                .build();

        when(userRepository.findByEmail("juan@example.com"))
                .thenReturn(Optional.of(user));

        // When
        var response = service.getByEmail("juan@example.com");

        // Then
        assertThat(response.email()).isEqualTo("juan@example.com");

        verify(userRepository).findByEmail("juan@example.com");
    }

    @Test
    void shouldGetUsersByRole() {
        // Given
        var user1 = User.builder().id(1L).name("Driver 1").role(UserRole.DRIVER).build();
        var user2 = User.builder().id(2L).name("Driver 2").role(UserRole.DRIVER).build();

        when(userRepository.findByRole(UserRole.DRIVER))
                .thenReturn(List.of(user1, user2));

        // When
        var result = service.getByRole(UserRole.DRIVER);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).role()).isEqualTo(UserRole.DRIVER);
        assertThat(result.get(1).role()).isEqualTo(UserRole.DRIVER);

        verify(userRepository).findByRole(UserRole.DRIVER);
    }

    @Test
    void shouldThrowNotFoundExceptionWhenNoUsersWithRole() {
        // Given
        when(userRepository.findByRole(UserRole.ADMIN))
                .thenReturn(List.of());

        // When / Then
        assertThatThrownBy(() -> service.getByRole(UserRole.ADMIN))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Any user has the \"ADMIN\" role");

        verify(userRepository).findByRole(UserRole.ADMIN);
    }

    @Test
    void shouldGetUserByPhone() {
        // Given
        var user = User.builder()
                .id(1L)
                .name("Juan Perez")
                .phone("3001234567")
                .build();

        when(userRepository.findByPhone("3001234567"))
                .thenReturn(Optional.of(user));

        // When
        var response = service.getByPhone("3001234567");

        // Then
        assertThat(response.phone()).isEqualTo("3001234567");

        verify(userRepository).findByPhone("3001234567");
    }

    @Test
    void shouldGetActiveUsersByRole() {
        // Given
        var user = User.builder()
                .id(1L)
                .name("Active Driver")
                .role(UserRole.DRIVER)
                .status(true)
                .build();

        when(userRepository.findByRoleAndStatus(UserRole.DRIVER, true))
                .thenReturn(List.of(user));

        // When
        var result = service.getActiveByRole(UserRole.DRIVER);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).status()).isTrue();

        verify(userRepository).findByRoleAndStatus(UserRole.DRIVER, true);
    }

    @Test
    void shouldGetUsersByStatus() {
        // Given
        var user = User.builder()
                .id(1L)
                .name("Active User")
                .status(true)
                .build();

        var pageable = PageRequest.of(0, 10);
        var page = new PageImpl<>(List.of(user));

        when(userRepository.findByStatus(true, pageable))
                .thenReturn(page);

        // When
        var result = service.getByStatus(true, pageable);

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).status()).isTrue();

        verify(userRepository).findByStatus(true, pageable);
    }

    @Test
    void shouldDeactivateUser() {
        // Given
        var user = User.builder()
                .id(1L)
                .name("Juan Perez")
                .status(true)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        service.deactivate(1L);

        // Then
        verify(userRepository).findById(1L);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void shouldActivateUser() {
        // Given
        var user = User.builder()
                .id(1L)
                .name("Juan Perez")
                .status(false)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        service.activate(1L);

        // Then
        verify(userRepository).findById(1L);
        verify(userRepository).save(any(User.class));
    }
}

