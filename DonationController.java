package com.bloodbank.controller;

import com.bloodbank.entity.Donation;
import com.bloodbank.service.DonationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Donation controller — updated to support camp linkage.
 */
@RestController
@RequestMapping("/api/donations")
public class DonationController {

    @Autowired
    private DonationService donationService;

    @GetMapping
    public ResponseEntity<List<Donation>> getAllDonations() {
        return ResponseEntity.ok(donationService.getAllDonations());
    }

    @GetMapping("/donor/{donorId}")
    public ResponseEntity<List<Donation>> getDonationsByDonor(@PathVariable Long donorId) {
        return ResponseEntity.ok(donationService.getDonationsByDonor(donorId));
    }

    @GetMapping("/camp/{campId}")
    public ResponseEntity<List<Donation>> getDonationsByCamp(@PathVariable Long campId) {
        return ResponseEntity.ok(donationService.getDonationsByCamp(campId));
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerDonation(@RequestBody Map<String, Object> body,
                                               Authentication auth) {
        try {
            Long donorId = Long.parseLong(body.get("donorId").toString());
            int quantity = Integer.parseInt(body.get("quantity").toString());
            String notes = body.getOrDefault("notes", "").toString();
            Long campId = body.containsKey("campId") && body.get("campId") != null
                    ? Long.parseLong(body.get("campId").toString()) : null;

            Donation donation = donationService.registerDonation(donorId, quantity, notes,
                    campId, auth.getName());
            return ResponseEntity.ok(donation);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/range")
    public ResponseEntity<List<Donation>> getDonationsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return ResponseEntity.ok(donationService.getDonationsByDateRange(start, end));
    }
}
