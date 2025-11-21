package co.edu.unimagdalena.finalproject_brasilia2.domain.repositories;


import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.Config;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConfigRepository extends JpaRepository<Config, String> {
    boolean existsByKey(String key);
}


