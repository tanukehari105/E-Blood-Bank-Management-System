package com.bloodbank.config;

import com.bloodbank.entity.User;
import com.bloodbank.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Seeds default admin and staff accounts on first startup.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        createUserIfAbsent("admin", "admin123", "ADMIN", "System Administrator");
        createUserIfAbsent("staff", "staff123", "STAFF", "Staff User");
    }

    private void createUserIfAbsent(String username, String rawPassword, String role, String fullName) {
        if (userRepository.findByUsername(username).isEmpty()) {
            User user = new User();
            user.setUsername(username);
            user.setPassword(passwordEncoder.encode(rawPassword));
            user.setRole(role);
            user.setFullName(fullName);
            userRepository.save(user);
        }
    }
}
