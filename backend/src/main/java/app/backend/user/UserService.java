package app.backend.user;

import app.backend.utils.exceptions.WrongPasswordException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final Logger logger = LogManager.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper mapper;
    private final BCryptPasswordEncoder encoder;

    public UserDTO getByEmail(String email) {
        logger.info("Fetching user by email: {}", email);
        UserEntity userEntity = userRepository.findByEmail(email)
                .orElseThrow(EntityNotFoundException::new);

        return mapper.toDTO(userEntity);
    }

    public UserDTO getById(int id) {
        logger.info("Fetching user by id: {}", id);
        UserEntity userEntity = userRepository.findById(id)
                .orElseThrow(EntityNotFoundException::new);

        return mapper.toDTO(userEntity);
    }

    public List<UserDTO> getAllUsers() {
        logger.info("Fetching all users");
        List<UserEntity> userEntities = userRepository.findAll();

        return mapper.toDTOs(userEntities);
    }

    @Transactional
    public UserDTO update(UserDTO updatedUser, String email) {
        logger.info("Updating user: email={}", email);
        UserEntity userEntity = userRepository.findByEmail(email)
                .orElseThrow(EntityNotFoundException::new);

        mapper.updateEntity(userEntity, updatedUser);

        return mapper.toDTO(userEntity);
    }

    @Transactional
    public void resetPassword(ResetPasswordDTO passwords, String email) {
        logger.warn("Password reset attempt for user: email={}", email);
        UserEntity userEntity = userRepository.findByEmail(email)
                .orElseThrow(EntityNotFoundException::new);

        if (encoder.matches(passwords.currentPassword(), userEntity.getPassword())) {
            userEntity.setPassword(encoder.encode(passwords.newPassword()));
            logger.info("Password successfully reset for user: email={}", email);
        } else {
            logger.warn("Failed password reset attempt - wrong current password for user: email={}", email);
            throw new WrongPasswordException();
        }

    }

    @Transactional
    public UserDTO register(RegisteredUserDTO user) {
        logger.info("New user registration: email={}", user.email());
        UserEntity userEntity = mapper.toEntity(user);

        userEntity.setPassword(encoder.encode(user.password()));

        RoleEntity role = roleRepository.findByName("CUSTOMER")
                .orElseThrow(EntityNotFoundException::new);
        userEntity.getRoles().add(role);

        UserEntity savedUser = userRepository.save(userEntity);
        logger.info("User successfully registered: email={}, id={}", user.email(), savedUser.getId());

        return mapper.toDTO(savedUser);
    }

}
