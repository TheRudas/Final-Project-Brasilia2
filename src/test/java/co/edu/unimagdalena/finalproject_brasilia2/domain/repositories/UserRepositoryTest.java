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

    private User passenger1;
    private User passenger2;
    private User driver1;
    private User admin;
    private User inactiveUser;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        // Create active passengers
        passenger1 = User.builder()
                .name("Juan Perez")
                .email("juan@mail.com")
                .phone("3001111111")
                .role(UserRole.PASSENGER)
                .status(true)
                .passwordHash("hash123")
                .createdAt(OffsetDateTime.now())
                .build();

        passenger2 = User.builder()
                .name("Maria Garcia")
                .email("maria@mail.com")
                .phone("3002222222")
                .role(UserRole.PASSENGER)
                .status(true)
                .passwordHash("hash456")
                .createdAt(OffsetDateTime.now())
                .build();

        // Create driver
        driver1 = User.builder()
                .name("Carlos Rodriguez")
                .email("carlos@mail.com")
                .phone("3003333333")
                .role(UserRole.DRIVER)
                .status(true)
                .passwordHash("hash789")
                .createdAt(OffsetDateTime.now())
                .build();

        // Create admin
        admin = User.builder()
                .name("Ana Martinez")
                .email("ana@mail.com")
                .phone("3004444444")
                .role(UserRole.ADMIN)
                .status(true)
                .passwordHash("hashABC")
                .createdAt(OffsetDateTime.now())
                .build();

        // Create inactive user
        inactiveUser = User.builder()
                .name("Pedro Lopez")
                .email("pedro@mail.com")
                .phone("3005555555")
                .role(UserRole.PASSENGER)
                .status(false)
                .passwordHash("hashDEF")
                .createdAt(OffsetDateTime.now().minusMonths(6))
                .build();
    }

    @Test
    @DisplayName("User: find by name")
    void shouldFindByName() {
        // Given
        userRepository.save(passenger1);

        // When
        var result = userRepository.findByName("Juan Perez");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("juan@mail.com");
    }

    @Test
    @DisplayName("User: find by phone")
    void shouldFindByPhone() {
        // Given
        userRepository.save(driver1);

        // When
        var result = userRepository.findByPhone("3003333333");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Carlos Rodriguez");
        assertThat(result.get().getRole()).isEqualTo(UserRole.DRIVER);
    }

    @Test
    @DisplayName("User: find by email")
    void shouldFindByEmail() {
        // Given
        userRepository.save(admin);

        // When
        var result = userRepository.findByEmail("ana@mail.com");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Ana Martinez");
        assertThat(result.get().getRole()).isEqualTo(UserRole.ADMIN);
    }

    @Test
    @DisplayName("User: find by role")
    void shouldFindByRole() {
        // Given
        userRepository.save(passenger1);
        userRepository.save(passenger2);
        userRepository.save(driver1);
        userRepository.save(admin);
        userRepository.save(inactiveUser);

        // When
        var passengers = userRepository.findByRole(UserRole.PASSENGER);
        var drivers = userRepository.findByRole(UserRole.DRIVER);
        var admins = userRepository.findByRole(UserRole.ADMIN);

        // Then
        assertThat(passengers).hasSize(3); // Including inactive passenger
        assertThat(drivers).hasSize(1);
        assertThat(admins).hasSize(1);

        assertThat(passengers)
                .extracting(User::getName)
                .containsExactlyInAnyOrder("Juan Perez", "Maria Garcia", "Pedro Lopez");
    }

    @Test
    @DisplayName("User: find by status")
    void shouldFindByStatus() {
        // Given
        userRepository.save(passenger1);
        userRepository.save(passenger2);
        userRepository.save(driver1);
        userRepository.save(admin);
        userRepository.save(inactiveUser);

        // When - active users
        var activeUsers = userRepository.findByStatus(true, PageRequest.of(0, 10));

        // When - inactive users
        var inactiveUsers = userRepository.findByStatus(false, PageRequest.of(0, 10));

        // Then
        assertThat(activeUsers.getContent()).hasSize(4);
        assertThat(inactiveUsers.getContent()).hasSize(1);
        assertThat(inactiveUsers.getContent().get(0).getName()).isEqualTo("Pedro Lopez");
    }

    @Test
    @DisplayName("User: find by role and status")
    void shouldFindByRoleAndStatus() {
        // Given
        userRepository.save(passenger1);
        userRepository.save(passenger2);
        userRepository.save(driver1);
        userRepository.save(inactiveUser);

        // When - active passengers only
        var activePassengers = userRepository.findByRoleAndStatus(UserRole.PASSENGER, true);

        // When - inactive passengers only
        var inactivePassengers = userRepository.findByRoleAndStatus(UserRole.PASSENGER, false);

        // Then
        assertThat(activePassengers).hasSize(2);
        assertThat(activePassengers)
                .extracting(User::getName)
                .containsExactlyInAnyOrder("Juan Perez", "Maria Garcia");

        assertThat(inactivePassengers).hasSize(1);
        assertThat(inactivePassengers.get(0).getName()).isEqualTo("Pedro Lopez");
    }

    @Test
    @DisplayName("User: return empty when name not found")
    void shouldReturnEmptyWhenNameNotFound() {
        // Given
        userRepository.save(passenger1);

        // When
        var result = userRepository.findByName("Nonexistent User");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("User: return empty when email not found")
    void shouldReturnEmptyWhenEmailNotFound() {
        // Given
        userRepository.save(passenger1);

        // When
        var result = userRepository.findByEmail("nonexistent@mail.com");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("User: return empty when phone not found")
    void shouldReturnEmptyWhenPhoneNotFound() {
        // Given
        userRepository.save(passenger1);

        // When
        var result = userRepository.findByPhone("9999999999");

        // Then
        assertThat(result).isEmpty();
    }
}