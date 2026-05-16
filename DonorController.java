package com.bloodbank.controller;

import com.bloodbank.entity.Donor;
import com.bloodbank.service.DonorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/donors")
public class DonorController {

    @Autowired
    private DonorService donorService;

    @GetMapping
    public ResponseEntity<List<Donor>> getAllDonors() {
        return ResponseEntity.ok(donorService.getAllDonors());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Donor> getDonor(@PathVariable Long id) {
        return ResponseEntity.ok(donorService.getDonorById(id));
    }

    @PostMapping
    public ResponseEntity<Donor> createDonor(@RequestBody Donor donor) {
        return ResponseEntity.ok(donorService.createDonor(donor));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Donor> updateDonor(@PathVariable Long id, @RequestBody Donor donor) {
        return ResponseEntity.ok(donorService.updateDonor(id, donor));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteDonor(@PathVariable Long id) {
        donorService.deleteDonor(id);
        return ResponseEntity.ok(Map.of("message", "Donor deleted successfully"));
    }

    @GetMapping("/search")
    public ResponseEntity<List<Donor>> searchDonors(@RequestParam String query) {
        return ResponseEntity.ok(donorService.searchDonors(query));
    }

    @GetMapping("/blood-group/{bloodGroup}")
    public ResponseEntity<List<Donor>> getDonorsByBloodGroup(@PathVariable String bloodGroup) {
        return ResponseEntity.ok(donorService.getDonorsByBloodGroup(bloodGroup));
    }

    @GetMapping("/eligible")
    public ResponseEntity<List<Donor>> getEligibleDonors() {
        return ResponseEntity.ok(donorService.getAllEligibleDonors());
    }

    @GetMapping("/eligible/{bloodGroup}")
    public ResponseEntity<List<Donor>> getEligibleDonorsByBloodGroup(@PathVariable String bloodGroup) {
        return ResponseEntity.ok(donorService.getEligibleDonorsByBloodGroup(bloodGroup));
    }

    @GetMapping("/{id}/eligible")
    public ResponseEntity<Map<String, Boolean>> checkEligibility(@PathVariable Long id) {
        Donor donor = donorService.getDonorById(id);
        return ResponseEntity.ok(Map.of("eligible", donorService.isDonorEligible(donor)));
    }
}
