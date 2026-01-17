package app.backend.user;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {

    private final UserService service;
    private static final Logger logger = LogManager.getLogger(UserController.class);

    @GetMapping("/all")
    public List<UserDTO> getUsers() {
        logger.info("GET /user/all - Fetching all users");
        return service.getAllUsers();
    }

    @GetMapping("/{id}")
    public UserDTO getUser(@PathVariable int id) {
        logger.info("GET /user/{} - Fetching user by id", id);
        return service.getById(id);
    }

    @GetMapping
    public UserDTO getUser(Principal principal) {
        logger.info("GET /user - Fetching current user: email={}", principal.getName());
        return service.getByEmail(principal.getName());
    }

    @PostMapping("/register")
    public UserDTO register(@RequestBody @Valid RegisteredUserDTO newUser) {
        logger.info("POST /user/register - New user registration: email={}", newUser.email());
        return service.register(newUser);
    }

    @PutMapping
    public UserDTO update(@RequestBody @Valid UserDTO updatedUser, Principal principal) {
        logger.info("PUT /user - Updating user: email={}", principal.getName());
        return service.update(updatedUser, principal.getName());
    }

    @PutMapping("/password")
    public void resetPassword(@RequestBody @Valid ResetPasswordDTO passwords, Principal principal) {
        logger.warn("PUT /user/password - Password reset for user: email={}", principal.getName());
        service.resetPassword(passwords, principal.getName());
    }

}
