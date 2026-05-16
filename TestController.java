package com.bloodbank.controller;

import com.bloodbank.entity.DonationCamp;
import com.bloodbank.entity.Donor;
import com.bloodbank.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Test / diagnostic controller.
 * Provides endpoints to verify auth and email configuration.
 */
@RestController
@RequestMapping("/api/test")
public class TestController {

    @Autowired
    private EmailService emailService;

    /** Verify JWT auth is working */
    @GetMapping("/auth")
    public ResponseEntity<Map<String, Object>> testAuth() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Map<String, Object> response = new HashMap<>();
        response.put("username", auth.getName());
        response.put("authorities", auth.getAuthorities().toString());
        response.put("isAuthenticated", auth.isAuthenticated());
        return ResponseEntity.ok(response);
    }

    /**
     * Send a test email to verify SMTP config.
     * POST /api/test/email  { "to": "someone@gmail.com" }
     *
     * This sends a real "blood usage notification" style email so you can
     * confirm the template and delivery are working.
     */
    @PostMapping("/email")
    public ResponseEntity<Map<String, String>> testEmail(@RequestBody Map<String, String> body) {
        String to = body.get("to");
        if (to == null || to.isBlank() || !to.contains("@")) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Provide a valid 'to' email address"));
        }

        // Build a fake donor pointing at the test address
        Donor fakeDonor = new Donor();
        fakeDonor.setId(0L);
        fakeDonor.setName("Test Donor");
        fakeDonor.setEmail(to);
        fakeDonor.setBloodGroup("A+");

        // Send a blood-usage notification as the test
        emailService.sendBloodUsageNotification(fakeDonor, "A+", 2, "Test Hospital");

        return ResponseEntity.ok(Map.of(
            "message", "Test email queued for: " + to,
            "note",    "Check your inbox in a few seconds. Also check Spam folder."
        ));
    }

    /**
     * Send a test camp invitation email.
     * POST /api/test/camp-email  { "to": "someone@gmail.com" }
     */
    @PostMapping("/camp-email")
    public ResponseEntity<Map<String, String>> testCampEmail(@RequestBody Map<String, String> body) {
        String to = body.get("to");
        if (to == null || to.isBlank() || !to.contains("@")) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Provide a valid 'to' email address"));
        }

        Donor fakeDonor = new Donor();
        fakeDonor.setId(0L);
        fakeDonor.setName("Test Donor");
        fakeDonor.setEmail(to);

        DonationCamp fakeCamp = new DonationCamp();
        fakeCamp.setId(0L);
        fakeCamp.setCampName("Blood Donation Drive 2026");
        fakeCamp.setOrganizerName("Blood Bank Admin");
        fakeCamp.setLocation("City Community Hall, Main Street");
        fakeCamp.setCampDate(LocalDate.now().plusDays(7));
        fakeCamp.setStartTime(LocalTime.of(9, 0));
        fakeCamp.setEndTime(LocalTime.of(17, 0));
        fakeCamp.setContactNumber("+91-9876543210");
        fakeCamp.setDescription("Join us for our annual blood donation drive. Every drop counts!");

        emailService.sendCampInvitation(fakeDonor, fakeCamp);

        return ResponseEntity.ok(Map.of(
            "message", "Test camp invitation email queued for: " + to,
            "note",    "Check your inbox in a few seconds. Also check Spam folder."
        ));
    }
}
