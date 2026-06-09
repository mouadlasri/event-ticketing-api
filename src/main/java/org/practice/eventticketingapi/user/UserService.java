package org.practice.eventticketingapi.user;

import org.practice.eventticketingapi.user.dto.CreateUserRequest;
import org.practice.eventticketingapi.user.dto.UserResponse;
import org.practice.eventticketingapi.user.exception.EmailAlreadyExistsException;
import org.practice.eventticketingapi.user.exception.UserNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public User getUserEntityById(UUID id) {
        return findUserById(id);
    }

    @Transactional
    public UserResponse createUser(CreateUserRequest createUserRequest) {
        String name = createUserRequest.getName();
        String email = createUserRequest.getEmail();

        if (userRepository.existsByEmailAndDeletedAtIsNull(email)) {
            throw new EmailAlreadyExistsException();
        }

        String hashedPassword = passwordEncoder.encode(createUserRequest.getPassword());

        User user = new User(
                name,
                email,
                hashedPassword
        );

        User savedUser = userRepository.save(user);

        return toUserResponse(savedUser);
    }

    private User findUserById(UUID id) {
        return userRepository.findByIdAndDeletedAtIsNull(id).orElseThrow(() -> new UserNotFoundException(id));
    }

    private UserResponse toUserResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail()
        );
    }

}
