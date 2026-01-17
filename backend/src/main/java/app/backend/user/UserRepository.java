package app.backend.user;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;

import java.util.Optional;

public interface UserRepository extends ListCrudRepository<UserEntity, Integer> {

    @Query(
        value = """
                SELECT *
                FROM users u
                WHERE u.email = :email
            """,
        nativeQuery = true
    )
    Optional<UserEntity> findByEmail(String email);

}
