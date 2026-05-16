package com.bloodbank.service;

import com.bloodbank.dto.HospitalRequest;
import com.bloodbank.entity.Hospital;
import com.bloodbank.exception.BusinessException;
import com.bloodbank.exception.ResourceNotFoundException;
import com.bloodbank.repository.HospitalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service for hospital account management (ADMIN-only operations).
 */
@Service
public class HospitalService {

    @Autowired private HospitalRepository hospitalRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private AuditLogService auditLogService;

    public List<Hospital> getAllHospitals() {
        return hospitalRepository.findAll();
    }

    public List<Hospital> getActiveHospitals() {
        return hospitalRepository.findByActiveTrue();
    }

    public Hospital getById(Long id) {
        return hospitalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hospital not found with id: " + id));
    }

    public Hospital getByUsername(String username) {
        return hospitalRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Hospital not found: " + username));
    }

    public Hospital createHospital(HospitalRequest req, String adminUsername) {
        if (hospitalRepository.existsByUsername(req.getUsername())) {
            throw new BusinessException("Username already taken: " + req.getUsername());
        }
        if (hospitalRepository.existsByEmail(req.getEmail())) {
            throw new BusinessException("Email already registered: " + req.getEmail());
        }

        Hospital hospital = new Hospital();
        hospital.setHospitalName(req.getHospitalName());
        hospital.setEmail(req.getEmail());
        hospital.setPhone(req.getPhone());
        hospital.setAddress(req.getAddress());
        hospital.setUsername(req.getUsername());
        hospital.setPassword(passwordEncoder.encode(req.getPassword()));
        hospital.setActive(true);

        Hospital saved = hospitalRepository.save(hospital);
        auditLogService.log(adminUsername, "ADMIN", "CREATE_HOSPITAL", "Hospital",
                saved.getId(), "Created hospital: " + saved.getHospitalName());
        return saved;
    }

    public Hospital updateHospital(Long id, HospitalRequest req, String adminUsername) {
        Hospital existing = getById(id);

        // Check username uniqueness (excluding self)
        hospitalRepository.findByUsername(req.getUsername())
                .ifPresent(h -> {
                    if (!h.getId().equals(id)) throw new BusinessException("Username already taken");
                });

        existing.setHospitalName(req.getHospitalName());
        existing.setEmail(req.getEmail());
        existing.setPhone(req.getPhone());
        existing.setAddress(req.getAddress());
        existing.setUsername(req.getUsername());

        // Only update password if provided
        if (req.getPassword() != null && !req.getPassword().isBlank()) {
            existing.setPassword(passwordEncoder.encode(req.getPassword()));
        }

        Hospital saved = hospitalRepository.save(existing);
        auditLogService.log(adminUsername, "ADMIN", "UPDATE_HOSPITAL", "Hospital",
                id, "Updated hospital: " + saved.getHospitalName());
        return saved;
    }

    public void deleteHospital(Long id, String adminUsername) {
        Hospital hospital = getById(id);
        auditLogService.log(adminUsername, "ADMIN", "DELETE_HOSPITAL", "Hospital",
                id, "Deleted hospital: " + hospital.getHospitalName());
        hospitalRepository.deleteById(id);
    }

    public Hospital toggleActive(Long id, String adminUsername) {
        Hospital hospital = getById(id);
        hospital.setActive(!hospital.isActive());
        Hospital saved = hospitalRepository.save(hospital);
        String action = saved.isActive() ? "ACTIVATE_HOSPITAL" : "DEACTIVATE_HOSPITAL";
        auditLogService.log(adminUsername, "ADMIN", action, "Hospital",
                id, hospital.getHospitalName() + " is now " + (saved.isActive() ? "active" : "inactive"));
        return saved;
    }

    public Hospital resetPassword(Long id, String newPassword, String adminUsername) {
        Hospital hospital = getById(id);
        hospital.setPassword(passwordEncoder.encode(newPassword));
        Hospital saved = hospitalRepository.save(hospital);
        auditLogService.log(adminUsername, "ADMIN", "RESET_HOSPITAL_PASSWORD", "Hospital",
                id, "Password reset for: " + hospital.getHospitalName());
        return saved;
    }

    public List<Hospital> searchHospitals(String query) {
        return hospitalRepository.searchHospitals(query);
    }
}
