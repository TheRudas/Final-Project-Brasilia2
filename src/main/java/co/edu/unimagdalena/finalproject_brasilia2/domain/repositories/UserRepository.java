package co.edu.unimagdalena.finalproject_brasilia2.domain.repositories;

import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.User;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.enums.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByName(String username);
    Optional<User> findByPhone(String phone);
    Optional<User> findByEmail(String email);
    List<User> findByRole(UserRole role);
    Page<User> findByStatus(boolean status, Pageable pageable);
    List<User> findByRoleAndStatus(UserRole role, boolean status);
}
