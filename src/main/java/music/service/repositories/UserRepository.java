package music.service.repositories;

import java.util.List;
import java.util.Optional;
import music.service.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsernameOrEmail(String username, String email);

    boolean existsByUsername(@NotBlank(message = "Username is required") String username);

    Optional<User> findByUsername(String username);

    boolean existsByEmail(@NotBlank(message = "Email is required")
                          @Email(message = "Invalid email format")
                          String email);

    Optional<User> findByEmail(String username);

    List<User> findByUsernameIn(List<String> usernames);
}
