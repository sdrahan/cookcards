package app.cookcards.webapp.user;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

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
        user.setEmail(normalizedEmail);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setRole("USER");
        user.setSettings(new UserSettings());
        return userRepository.save(user);
    }

    public User requireByEmail(String email) {
        String normalizedEmail = normalizeEmail(email);
        return userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new IllegalStateException("User not found for email: " + normalizedEmail));
    }

    @Transactional
    public UserSettings getOrCreateSettingsByEmail(String email) {
        User user = requireByEmail(email);
        if (user.getSettings() == null) {
            user.setSettings(new UserSettings());
            userRepository.save(user);
        }
        return user.getSettings();
    }

    @Transactional
    public void updateSettings(String email, UnitsMode unitsMode, TargetLanguage targetLanguage) {
        UserSettings settings = getOrCreateSettingsByEmail(email);
        settings.setUnitsMode(unitsMode == null ? UnitsMode.ORIGINAL : unitsMode);
        settings.setTargetLanguage(targetLanguage == null ? TargetLanguage.ORIGINAL : targetLanguage);
    }

    public static String normalizeEmail(String email) {
        if (email == null) {
            return null;
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
