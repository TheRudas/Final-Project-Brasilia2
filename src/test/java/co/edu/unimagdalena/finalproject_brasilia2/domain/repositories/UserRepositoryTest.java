package co.edu.unimagdalena.finalproject_brasilia2.domain.repositories;

import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.User;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

public class UserRepositoryTest extends AbstractRepositoryIT {

    @Autowired
    private UserRepository userRepository;

    private User user1;
    private User user2;
    private User user3;
    private User user4;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        OffsetDateTime now = OffsetDateTime.now();

        // Create users
        user1 = User.builder()
                .name("Juan Perez")
                .email("juan.perez@mail.com")
                .phone("3001234567")
                .passwordHash("hashed_password_123")
                .role(UserRole.PASSENGER)
                .status(true)
                .createdAt(now.minusDays(10))
                .build();

        user2 = User.builder()
                .name("Maria Garcia")
                .email("maria.garcia@mail.com")
                .phone("3107654321")
                .passwordHash("hashed_password_456")
                .role(UserRole.DRIVER)
                .status(true)
                .createdAt(now.minusDays(5))
                .build();

        user3 = User.builder()
                .name("Carlos Admin")
                .email("carlos.admin@mail.com")
                .phone("3009876543")
                .passwordHash("hashed_password_789")
                .role(UserRole.ADMIN)
                .status(false)
                .createdAt(now.minusDays(3))
                .build();

        user4 = User.builder()
                .name("Ana Lopez")
                .email("ana.lopez@mail.com")
                .phone("3154445566")
                .passwordHash("hashed_password_101")
                .role(UserRole.CLERK)
                .status(true)
                .createdAt(now)
                .build();
    }

    @Test
    @DisplayName("User: find by name")
    void shouldFindByName() {
        // Given
        userRepository.save(user1);
        userRepository.save(user2);

        // When
        var result = userRepository.findByName("Juan Perez");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("juan.perez@mail.com");
        assertThat(result.get().getPhone()).isEqualTo("3001234567");
        assertThat(result.get().getRole()).isEqualTo(UserRole.PASSENGER);
    }

    @Test
    @DisplayName("User: find by email")
    void shouldFindByEmail() {
        // Given
        userRepository.save(user1);
        userRepository.save(user2);

        // When
        var result = userRepository.findByEmail("maria.garcia@mail.com");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Maria Garcia");
        assertThat(result.get().getPhone()).isEqualTo("3107654321");
        assertThat(result.get().getRole()).isEqualTo(UserRole.DRIVER);
    }

    @Test
    @DisplayName("User: find by phone")
    void shouldFindByPhone() {
        // Given
        userRepository.save(user1);
        userRepository.save(user2);

        // When
        var result = userRepository.findByPhone("3107654321");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Maria Garcia");
        assertThat(result.get().getEmail()).isEqualTo("maria.garcia@mail.com");
        assertThat(result.get().getRole()).isEqualTo(UserRole.DRIVER);
    }

    @Test
    @DisplayName("User: find by role")
    void shouldFindByRole() {
        // Given
        userRepository.save(user1);
        userRepository.save(user2);
        userRepository.save(user3);
        userRepository.save(user4);

        // When
        var passengers = userRepository.findByRole(UserRole.PASSENGER);
        var drivers = userRepository.findByRole(UserRole.DRIVER);
        var admins = userRepository.findByRole(UserRole.ADMIN);

        // Then
        assertThat(passengers).hasSize(1);
        assertThat(passengers.get(0).getName()).isEqualTo("Juan Perez");

        assertThat(drivers).hasSize(1);
        assertThat(drivers.get(0).getName()).isEqualTo("Maria Garcia");

        assertThat(admins).hasSize(1);
        assertThat(admins.get(0).getName()).isEqualTo("Carlos Admin");
    }

    @Test
    @DisplayName("User: find by status with pagination")
    void shouldFindByStatus() {
        // Given
        userRepository.save(user1);
        userRepository.save(user2);
        userRepository.save(user3);
        userRepository.save(user4);

        // When
        var activeUsers = userRepository.findByStatus(true, PageRequest.of(0, 10));
        var inactiveUsers = userRepository.findByStatus(false, PageRequest.of(0, 10));

        // Then
        assertThat(activeUsers.getContent()).hasSize(3);
        assertThat(activeUsers.getContent())
                .extracting(User::getName)
                .containsExactlyInAnyOrder("Juan Perez", "Maria Garcia", "Ana Lopez");

        assertThat(inactiveUsers.getContent()).hasSize(1);
        assertThat(inactiveUsers.getContent().get(0).getName()).isEqualTo("Carlos Admin");
    }

    @Test
    @DisplayName("User: find by role and status")
    void shouldFindByRoleAndStatus() {
        // Given
        userRepository.save(user1);
        userRepository.save(user2);
        userRepository.save(user3);
        userRepository.save(user4);

        // When
        var activePassengers = userRepository.findByRoleAndStatus(UserRole.PASSENGER, true);
        var inactiveAdmins = userRepository.findByRoleAndStatus(UserRole.ADMIN, false);

        // Then
        assertThat(activePassengers).hasSize(1);
        assertThat(activePassengers.get(0).getName()).isEqualTo("Juan Perez");

        assertThat(inactiveAdmins).hasSize(1);
        assertThat(inactiveAdmins.get(0).getName()).isEqualTo("Carlos Admin");
    }

    @Test
    @DisplayName("User: return empty when name not found")
    void shouldReturnEmptyWhenNameNotFound() {
        // Given
        userRepository.save(user1);

        // When
        var result = userRepository.findByName("Pedro Sanchez");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("User: return empty when email not found")
    void shouldReturnEmptyWhenEmailNotFound() {
        // Given
        userRepository.save(user1);

        // When
        var result = userRepository.findByEmail("notfound@mail.com");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("User: return empty when phone not found")
    void shouldReturnEmptyWhenPhoneNotFound() {
        // Given
        userRepository.save(user1);

        // When
        var result = userRepository.findByPhone("3009999999");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("User: return empty list when role has no users")
    void shouldReturnEmptyWhenRoleHasNoUsers() {
        // Given
        userRepository.save(user1);

        // When
        var result = userRepository.findByRole(UserRole.DISPATCHER);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("User: return empty page when status has no users")
    void shouldReturnEmptyPageWhenStatusHasNoUsers() {
        // Given
        userRepository.save(user1);
        userRepository.save(user2);

        // When - all users are active
        var result = userRepository.findByStatus(false, PageRequest.of(0, 10));

        // Then
        assertThat(result.getContent()).isEmpty();
    }

    @Test
    @DisplayName("User: return empty list when role and status combination not found")
    void shouldReturnEmptyWhenRoleAndStatusNotMatch() {
        // Given
        userRepository.save(user1);
        userRepository.save(user2);

        // When
        var result = userRepository.findByRoleAndStatus(UserRole.ADMIN, true);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("User: verify password hash is stored")
    void shouldVerifyPasswordHashIsStored() {
        // Given
        userRepository.save(user1);

        // When
        var result = userRepository.findByEmail("juan.perez@mail.com");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getPasswordHash()).isEqualTo("hashed_password_123");
    }

    @Test
    @DisplayName("User: verify created at timestamp")
    void shouldVerifyCreatedAtTimestamp() {
        // Given
        OffsetDateTime now = OffsetDateTime.now();
        userRepository.save(user1);

        // When
        var result = userRepository.findByEmail("juan.perez@mail.com");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getCreatedAt()).isNotNull();
        assertThat(result.get().getCreatedAt()).isBefore(now);
    }
}