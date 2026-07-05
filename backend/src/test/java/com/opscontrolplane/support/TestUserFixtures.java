package com.opscontrolplane.support;

import com.opscontrolplane.users.Role;
import com.opscontrolplane.users.User;
import com.opscontrolplane.users.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;

public final class TestUserFixtures {

    private TestUserFixtures() {
    }

    public static void ensureUser(UserRepository userRepository, PasswordEncoder passwordEncoder,
                                   String email, String rawPassword, Role role) {
        if (userRepository.findByEmail(email).isEmpty()) {
            userRepository.save(new User(email, passwordEncoder.encode(rawPassword), role));
        }
    }
}
