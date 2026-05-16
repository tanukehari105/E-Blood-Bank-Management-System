package com.bloodbank.controller;

import com.bloodbank.entity.DonationCamp;
import com.bloodbank.service.DonationCampService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Donation camp management — ADMIN/STAFF can view, ADMIN can create/edit/cancel.
 */
@RestController
@RequestMapping("/api/camps")
public class DonationCampController {

    @Autowired
    private DonationCampService campService;

    @GetMapping
    public ResponseEntity<List<DonationCamp>> getAllCamps() {
        return ResponseEntity.ok(campService.getAllCamps());
    }

    @GetMapping("/upcoming")
    public ResponseEntity<List<DonationCamp>> getUpcomingCamps() {
        return ResponseEntity.ok(campService.getUpcomingCamps());
    }

    @GetMapping("/past")
    public ResponseEntity<List<DonationCamp>> getPastCamps() {
        return ResponseEntity.ok(campService.getPastCamps());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DonationCamp> getCamp(@PathVariable Long id) {
        return ResponseEntity.ok(campService.getById(id));
    }

    @PostMapping
    public ResponseEntity<DonationCamp> createCamp(@RequestBody DonationCamp camp,
                                                    Authentication auth) {
        return ResponseEntity.ok(campService.createCamp(camp, auth.getName()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DonationCamp> updateCamp(@PathVariable Long id,
                                                    @RequestBody DonationCamp camp,
                                                    Authentication auth) {
        return ResponseEntity.ok(campService.updateCamp(id, camp, auth.getName()));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<DonationCamp> cancelCamp(@PathVariable Long id, Authentication auth) {
        return ResponseEntity.ok(campService.cancelCamp(id, auth.getName()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCamp(@PathVariable Long id, Authentication auth) {
        campService.deleteCamp(id, auth.getName());
        return ResponseEntity.ok(Map.of("message", "Camp deleted successfully"));
    }
}
