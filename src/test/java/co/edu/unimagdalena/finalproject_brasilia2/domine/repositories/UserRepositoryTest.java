package co.edu.unimagdalena.finalproject_brasilia2.domine.repositories;

import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.User;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.enums.UserRole;
import co.edu.unimagdalena.finalproject_brasilia2.domain.repositories.UserRepository;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class UserRepositoryTest extends AbstractRepositoryIT{
    @Autowired
    private UserRepository userRepository;
    private User passenger;
    private User driver;
    private User clerk;

    @BeforeEach
    void setUp() {
        // Clean before each (insert davoo meme)
        userRepository.deleteAll();

        passenger = User.builder()
                .name("Cold Menares")
                .email("coldmenares@mail.com")
                .phone("3001112222")
                .role(UserRole.PASSENGER)
                .build();

        driver = User.builder()
                .name("Pedro")
                .email("pedro@mail.com")
                .phone("3003334444")
                .role(UserRole.DRIVER)
                .build();

        clerk = User.builder()
                .name("Ana")
                .email("ana@mail.com")
                .phone("3005556666")
                .role(UserRole.CLERK)
                .build();
    }

    @Test
    @DisplayName("User: find by name")
    void shouldFindByName() {
        // Given
        userRepository.save(passenger);

        // When
        var result = userRepository.findByName("Cold Menares");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Cold Menares");
    }

    @Test
    @DisplayName("User: find by phone")
    void shouldFindByPhone() {
        // Given
        userRepository.save(driver);

        // When
        var result = userRepository.findByPhone("3003334444");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getPhone()).isEqualTo("3003334444");
    }

    @Test
    @DisplayName("User: find by email")
    void shouldFindByEmail() {
        // Given
        userRepository.save(clerk);

        // When
        var result = userRepository.findByEmail("ana@mail.com");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("ana@mail.com");
    }

    @Test
    @DisplayName("User: find by role")
    void shouldFindByRole() {
        // Given
        var user1 = userRepository.save(passenger);
        var user3 = userRepository.save(driver);

        // When
        var passengers = userRepository.findByRole(UserRole.PASSENGER);
        var drivers = userRepository.findByRole(UserRole.DRIVER);

        // Then
        assertThat(passengers).hasSize(1).extracting(User::getName).containsExactlyInAnyOrder("Juan");
        assertThat(drivers).hasSize(1).extracting(User::getName).containsExactly("Pedro");
    }
}
