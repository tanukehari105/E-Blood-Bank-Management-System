package com.bloodbank.service;

import com.bloodbank.dto.LoginRequest;
import com.bloodbank.dto.LoginResponse;
import com.bloodbank.entity.Hospital;
import com.bloodbank.entity.User;
import com.bloodbank.repository.HospitalRepository;
import com.bloodbank.repository.UserRepository;
import com.bloodbank.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Auth service — supports login for ADMIN, STAFF, and HOSPITAL roles.
 * Checks User table first, then Hospital table.
 */
@Service
public class AuthService {

    @Autowired private UserRepository userRepository;
    @Autowired private HospitalRepository hospitalRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtUtil jwtUtil;
    @Autowired private AuditLogService auditLogService;

    public LoginResponse login(LoginRequest request) {
        // 1. Try User table (ADMIN / STAFF)
        var userOpt = userRepository.findByUsername(request.getUsername());
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                throw new RuntimeException("Invalid credentials");
            }
            String token = jwtUtil.generateToken(user.getUsername(), user.getRole());
            auditLogService.log(user.getUsername(), user.getRole(), "LOGIN",
                    "User", user.getId(), "Successful login");
            return new LoginResponse(token, user.getUsername(), user.getRole(), user.getFullName());
        }

        // 2. Try Hospital table
        var hospitalOpt = hospitalRepository.findByUsername(request.getUsername());
        if (hospitalOpt.isPresent()) {
            Hospital hospital = hospitalOpt.get();
            if (!hospital.isActive()) {
                throw new RuntimeException("Hospital account is deactivated. Contact administrator.");
            }
            if (!passwordEncoder.matches(request.getPassword(), hospital.getPassword())) {
                throw new RuntimeException("Invalid credentials");
            }
            String token = jwtUtil.generateToken(hospital.getUsername(), "HOSPITAL");
            auditLogService.log(hospital.getUsername(), "HOSPITAL", "LOGIN",
                    "Hospital", hospital.getId(), "Hospital login: " + hospital.getHospitalName());

            LoginResponse response = new LoginResponse(token, hospital.getUsername(), "HOSPITAL",
                    hospital.getHospitalName());
            response.setHospitalName(hospital.getHospitalName());
            response.setHospitalId(hospital.getId());
            return response;
        }

        throw new RuntimeException("Invalid credentials");
    }
}
