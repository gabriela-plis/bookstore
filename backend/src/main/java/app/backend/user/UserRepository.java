package app.backend.user;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;

import java.util.Optional;

public interface UserRepository extends ListCrudRepository<UserEntity, Integer> {

    @Query("""
            SELECT u FROM UserEntity u
            LEFT JOIN FETCH u.roles
            WHERE u.email = :email
        """)
    Optional<UserEntity> findByEmail(String email);

}
