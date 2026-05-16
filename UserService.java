package com.bloodbank.service;

import com.bloodbank.entity.User;
import com.bloodbank.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public List<User> getStaffUsers() {
        return userRepository.findByRole("STAFF");
    }

    public User createUser(User user) {
        // Check if username already exists
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists");
        }

        // Validate required fields
        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            throw new RuntimeException("Username is required");
        }
        if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            throw new RuntimeException("Password is required");
        }
        if (user.getRole() == null || user.getRole().trim().isEmpty()) {
            throw new RuntimeException("Role is required");
        }

        // Encode password
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // Set default full name if not provided
        if (user.getFullName() == null || user.getFullName().trim().isEmpty()) {
            user.setFullName(user.getUsername());
        }

        return userRepository.save(user);
    }

    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Prevent deleting admin users
        if ("ADMIN".equals(user.getRole())) {
            throw new RuntimeException("Cannot delete admin users");
        }

        userRepository.deleteById(id);
    }

    public Map<String, Long> getUserCounts() {
        Map<String, Long> counts = new HashMap<>();
        counts.put("total", userRepository.count());
        counts.put("staff", (long) userRepository.findByRole("STAFF").size());
        counts.put("admin", (long) userRepository.findByRole("ADMIN").size());
        return counts;
    }
}
