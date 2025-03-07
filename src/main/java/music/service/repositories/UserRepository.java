package music.service.repositories;

import java.util.List;
import music.service.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    User findUserById(long id);

    List<User> findUserByUsername(String username);
}
