package com.bloodbank.controller;

import com.bloodbank.dto.HospitalRequest;
import com.bloodbank.entity.Hospital;
import com.bloodbank.service.HospitalService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Hospital management — ADMIN only.
 */
@RestController
@RequestMapping("/api/hospitals")
public class HospitalController {

    @Autowired
    private HospitalService hospitalService;

    @GetMapping
    public ResponseEntity<List<Hospital>> getAllHospitals() {
        return ResponseEntity.ok(hospitalService.getAllHospitals());
    }

    @GetMapping("/active")
    public ResponseEntity<List<Hospital>> getActiveHospitals() {
        return ResponseEntity.ok(hospitalService.getActiveHospitals());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Hospital> getHospital(@PathVariable Long id) {
        return ResponseEntity.ok(hospitalService.getById(id));
    }

    @GetMapping("/search")
    public ResponseEntity<List<Hospital>> searchHospitals(@RequestParam String query) {
        return ResponseEntity.ok(hospitalService.searchHospitals(query));
    }

    @PostMapping
    public ResponseEntity<Hospital> createHospital(@Valid @RequestBody HospitalRequest request,
                                                    Authentication auth) {
        return ResponseEntity.ok(hospitalService.createHospital(request, auth.getName()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Hospital> updateHospital(@PathVariable Long id,
                                                    @Valid @RequestBody HospitalRequest request,
                                                    Authentication auth) {
        return ResponseEntity.ok(hospitalService.updateHospital(id, request, auth.getName()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteHospital(@PathVariable Long id, Authentication auth) {
        hospitalService.deleteHospital(id, auth.getName());
        return ResponseEntity.ok(Map.of("message", "Hospital deleted successfully"));
    }

    @PutMapping("/{id}/toggle-active")
    public ResponseEntity<Hospital> toggleActive(@PathVariable Long id, Authentication auth) {
        return ResponseEntity.ok(hospitalService.toggleActive(id, auth.getName()));
    }

    @PutMapping("/{id}/reset-password")
    public ResponseEntity<?> resetPassword(@PathVariable Long id,
                                            @RequestBody Map<String, String> body,
                                            Authentication auth) {
        String newPassword = body.get("password");
        if (newPassword == null || newPassword.length() < 6) {
            return ResponseEntity.badRequest().body(Map.of("error", "Password must be at least 6 characters"));
        }
        hospitalService.resetPassword(id, newPassword, auth.getName());
        return ResponseEntity.ok(Map.of("message", "Password reset successfully"));
    }
}
