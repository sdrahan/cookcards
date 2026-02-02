package app.cookcards.webapp.user;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public boolean emailExists(String email) {
        return userRepository.existsByEmail(normalizeEmail(email));
    }

    @Transactional
    public User createUser(String email, String rawPassword) {
        String normalizedEmail = normalizeEmail(email);
        User user = new User();
        user.setUuid(UUID.randomUUID().toString());
        user.setEmail(normalizedEmail);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setRole("USER");
        return userRepository.save(user);
    }

    public User requireByEmail(String email) {
        String normalizedEmail = normalizeEmail(email);
        return userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new IllegalStateException("User not found for email: " + normalizedEmail));
    }

    public static String normalizeEmail(String email) {
        if (email == null) {
            return null;
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
