package com.bloodbank;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.bloodbank.entity.User;
import com.bloodbank.repository.UserRepository;

@SpringBootApplication
public class BloodBankApplication {

    public static void main(String[] args) {
        SpringApplication.run(BloodBankApplication.class, args);
    }

    @Bean
    CommandLineRunner initData(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (userRepository.findByUsername("admin").isEmpty()) {
                User admin = new User();
                admin.setUsername("admin");
                admin.setPassword(passwordEncoder.encode("admin"));
                admin.setRole("ADMIN");
                admin.setFullName("System Administrator");
                userRepository.save(admin);
            }
            if (userRepository.findByUsername("staff").isEmpty()) {
                User staff = new User();
                staff.setUsername("staff");
                staff.setPassword(passwordEncoder.encode("staff123"));
                staff.setRole("STAFF");
                staff.setFullName("Blood Bank Staff");
                userRepository.save(staff);
            }
        };
    }
}
