package com.bloodbank.controller;

import com.bloodbank.entity.BloodRequest;
import com.bloodbank.entity.Donor;
import com.bloodbank.service.BloodRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Blood request controller — enhanced with urgency, partial approval, and hospital support.
 */
@RestController
@RequestMapping("/api/requests")
public class BloodRequestController {

    @Autowired
    private BloodRequestService requestService;

    @GetMapping
    public ResponseEntity<List<BloodRequest>> getAllRequests() {
        return ResponseEntity.ok(requestService.getAllRequests());
    }

    @GetMapping("/{id}")
    public ResponseEntity<BloodRequest> getRequest(@PathVariable Long id) {
        return ResponseEntity.ok(requestService.getById(id));
    }

    @PostMapping
    public ResponseEntity<BloodRequest> createRequest(@RequestBody BloodRequest request,
                                                       Authentication auth) {
        return ResponseEntity.ok(requestService.createRequest(request, auth.getName()));
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<?> approveRequest(@PathVariable Long id, Authentication auth) {
        try {
            BloodRequest request = requestService.approveRequest(id, auth.getName());
            return ResponseEntity.ok(request);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<?> rejectRequest(@PathVariable Long id,
                                            @RequestBody(required = false) Map<String, String> body,
                                            Authentication auth) {
        try {
            String reason = body != null ? body.getOrDefault("reason", "") : "";
            BloodRequest request = requestService.rejectRequest(id, reason, auth.getName());
            return ResponseEntity.ok(request);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/pending")
    public ResponseEntity<List<BloodRequest>> getPendingRequests() {
        return ResponseEntity.ok(requestService.getPendingRequests());
    }

    @GetMapping("/critical")
    public ResponseEntity<List<BloodRequest>> getCriticalRequests() {
        return ResponseEntity.ok(requestService.getCriticalPendingRequests());
    }

    @GetMapping("/urgent")
    public ResponseEntity<List<BloodRequest>> getUrgentRequests() {
        return ResponseEntity.ok(requestService.getUrgentPendingRequests());
    }

    @GetMapping("/hospital/{hospitalId}")
    public ResponseEntity<List<BloodRequest>> getRequestsByHospital(@PathVariable Long hospitalId) {
        return ResponseEntity.ok(requestService.getRequestsByHospital(hospitalId));
    }

    @GetMapping("/match-donors/{bloodGroup}")
    public ResponseEntity<List<Donor>> findMatchingDonors(@PathVariable String bloodGroup) {
        return ResponseEntity.ok(requestService.findMatchingDonors(bloodGroup));
    }
}
