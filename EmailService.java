package com.bloodbank.service;

import com.bloodbank.entity.DonationCamp;
import com.bloodbank.entity.Donor;
import jakarta.annotation.PostConstruct;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Email service — sends HTML emails for all notification types.
 *
 * All public methods are @Async so they run in a background thread and
 * don't block the HTTP response. Errors are logged but never thrown to callers.
 *
 * If mailSender is null (SMTP not configured), every method logs a warning
 * and returns immediately — the app continues to work without email.
 */
@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${app.mail.from:itsvanshikarawat@gmail.com}")
    private String fromEmail;

    /** Log at startup whether email is configured so it's obvious in the console */
    @PostConstruct
    public void logEmailStatus() {
        if (mailSender == null || fromEmail == null || fromEmail.isBlank()) {
            log.warn("=======================================================");
            log.warn("  EMAIL NOT CONFIGURED — no emails will be sent.");
            log.warn("  Set spring.mail.username and spring.mail.password");
            log.warn("  in application.properties to enable email.");
            log.warn("=======================================================");
        } else {
            log.info("Email configured — sending from: {}", fromEmail);
        }
    }

    // ── Public API ────────────────────────────────────────────────────────────

    @Async
    public void sendEmergencyDonorAlert(Donor donor, String bloodGroup, int unitsNeeded) {
        if (!canSend(donor.getEmail())) return;
        send(donor.getEmail(),
             "🚨 URGENT: Blood Donation Needed - " + bloodGroup,
             buildEmergencyAlertHtml(donor.getName(), bloodGroup, unitsNeeded));
    }

    @Async
    public void sendRequestApprovedToHospital(String hospitalEmail, String hospitalName,
                                               String patientName, String bloodGroup,
                                               int units, Long requestId) {
        if (!canSend(hospitalEmail)) return;
        send(hospitalEmail,
             "✅ Blood Request Approved - Request #" + requestId,
             buildRequestApprovedHtml(hospitalName, patientName, bloodGroup, units, requestId));
    }

    @Async
    public void sendRequestRejectedToHospital(String hospitalEmail, String hospitalName,
                                               String patientName, String bloodGroup,
                                               int units, Long requestId, String reason) {
        if (!canSend(hospitalEmail)) return;
        send(hospitalEmail,
             "❌ Blood Request Update - Request #" + requestId,
             buildRequestRejectedHtml(hospitalName, patientName, bloodGroup, units, requestId, reason));
    }

    @Async
    public void sendBloodUsageNotification(Donor donor, String bloodGroup,
                                            int units, String patientHospital) {
        if (!canSend(donor.getEmail())) return;
        send(donor.getEmail(),
             "💙 Your Donation Saved a Life",
             buildBloodUsageHtml(donor.getName(), bloodGroup, units, patientHospital));
    }

    /**
     * Camp invitation — called for every active donor when admin creates a camp.
     * NOT @Async here so the caller (DonationCampService) can catch and log errors.
     * DonationCampService calls this in a loop; each call is fast (queued to SMTP).
     */
    @Async
    public void sendCampInvitation(Donor donor, DonationCamp camp) {
        if (!canSend(donor.getEmail())) return;
        log.info("Sending camp invitation to {} <{}>", donor.getName(), donor.getEmail());
        send(donor.getEmail(),
             "💉 Donation Camp Invitation - " + camp.getCampName(),
             buildCampInvitationHtml(donor.getName(), camp));
    }

    @Async
    public void sendCampReminder(Donor donor, DonationCamp camp) {
        if (!canSend(donor.getEmail())) return;
        send(donor.getEmail(),
             "⏰ Reminder: Donation Camp Tomorrow - " + camp.getCampName(),
             buildCampReminderHtml(donor.getName(), camp));
    }

    @Async
    public void sendCampCancellation(Donor donor, DonationCamp camp) {
        if (!canSend(donor.getEmail())) return;
        send(donor.getEmail(),
             "❌ Camp Cancelled - " + camp.getCampName(),
             buildCampCancellationHtml(donor.getName(), camp));
    }

    // ── Core send method ──────────────────────────────────────────────────────

    private void send(String to, String subject, String htmlBody) {
        if (mailSender == null) {
            log.warn("mailSender is null — email NOT sent to: {} | Subject: {}", to, subject);
            return;
        }
        try {
            // Sanitize the from address — use hardcoded value if injection produced garbage
            String from = (fromEmail != null && fromEmail.contains("@"))
                    ? fromEmail.trim()
                    : "itsvanshikarawat@gmail.com";

            log.debug("Sending email: from=[{}] to=[{}] subject=[{}]", from, to, subject);

            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");
            helper.setFrom(from);
            helper.setTo(to.trim());
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            mailSender.send(msg);
            log.info("✅ Email sent → {} | {}", to, subject);
        } catch (MessagingException e) {
            log.error("❌ Failed to send email to {} | Error: {}", to, e.getMessage(), e);
        } catch (Exception e) {
            log.error("❌ Unexpected error sending email to {} | {}", to, e.getMessage(), e);
        }
    }

    private boolean canSend(String email) {
        if (mailSender == null) {
            log.warn("Email skipped (mailSender not configured) for: {}", email);
            return false;
        }
        if (email == null || email.isBlank() || !email.contains("@")) {
            log.debug("Email skipped — invalid address: {}", email);
            return false;
        }
        return true;
    }

    // ── HTML Templates ────────────────────────────────────────────────────────

    private String buildEmergencyAlertHtml(String donorName, String bloodGroup, int units) {
        return wrap("🚨 Emergency Blood Request",
            "<p>Dear <strong>" + donorName + "</strong>,</p>" +
            "<div style='background:#fff3cd;border-left:4px solid #e74c3c;padding:15px;margin:15px 0;border-radius:4px'>" +
            "<h3 style='color:#e74c3c;margin:0'>⚠️ Urgent Blood Requirement</h3>" +
            "<p style='margin:8px 0 0'>Blood Group: <strong style='color:#e74c3c;font-size:18px'>" + bloodGroup + "</strong></p>" +
            "<p style='margin:4px 0'>Units Needed: <strong>" + units + "</strong></p></div>" +
            "<p>Your donation can help save a life. Please visit your nearest blood bank as soon as possible.</p>" +
            "<div style='background:#e8f5e9;padding:12px;border-radius:4px;margin-top:15px'>" +
            "<p style='margin:0;color:#2e7d32'><strong>Every drop counts. Be a hero today.</strong></p></div>");
    }

    private String buildRequestApprovedHtml(String hospitalName, String patientName,
                                             String bloodGroup, int units, Long requestId) {
        return wrap("✅ Blood Request Approved",
            "<p>Dear <strong>" + hospitalName + "</strong>,</p>" +
            "<p>Your blood request has been <strong style='color:#27ae60'>APPROVED</strong>.</p>" +
            "<table style='width:100%;border-collapse:collapse;margin:15px 0'>" +
            row("Request ID", "#" + requestId, true) +
            row("Patient Name", patientName, false) +
            row("Blood Group", "<span style='color:#e74c3c;font-weight:bold'>" + bloodGroup + "</span>", true) +
            row("Units Approved", units + " units", false) +
            "</table>" +
            "<p>Please collect the blood from our facility at your earliest convenience.</p>");
    }

    private String buildRequestRejectedHtml(String hospitalName, String patientName,
                                             String bloodGroup, int units, Long requestId, String reason) {
        return wrap("❌ Blood Request Update",
            "<p>Dear <strong>" + hospitalName + "</strong>,</p>" +
            "<p>We regret to inform you that your blood request could not be fulfilled at this time.</p>" +
            "<table style='width:100%;border-collapse:collapse;margin:15px 0'>" +
            row("Request ID", "#" + requestId, true) +
            row("Patient Name", patientName, false) +
            row("Blood Group", bloodGroup, true) +
            row("Reason", "<span style='color:#e74c3c'>" + (reason == null || reason.isBlank() ? "Insufficient stock" : reason) + "</span>", false) +
            "</table>" +
            "<p>Please contact us for alternative arrangements.</p>");
    }

    private String buildBloodUsageHtml(String donorName, String bloodGroup, int units, String hospital) {
        return wrap("💙 Your Donation Made a Difference",
            "<p>Dear <strong>" + donorName + "</strong>,</p>" +
            "<div style='background:#e8f5e9;border-left:4px solid #27ae60;padding:15px;margin:15px 0;border-radius:4px'>" +
            "<h3 style='color:#27ae60;margin:0'>🎉 Thank You, Hero!</h3>" +
            "<p style='margin:8px 0 0'>Your donated blood (<strong>" + bloodGroup + "</strong>, " + units + " units) " +
            "has been used to help a patient at <strong>" + hospital + "</strong>.</p></div>" +
            "<p>Your selfless act directly contributed to saving a life. We are deeply grateful.</p>" +
            "<p>You will be eligible to donate again in 90 days. We hope to see you again!</p>");
    }

    private String buildCampInvitationHtml(String donorName, DonationCamp camp) {
        return wrap("💉 You're Invited to Donate Blood",
            "<p>Dear <strong>" + donorName + "</strong>,</p>" +
            "<p>You are cordially invited to our upcoming blood donation camp!</p>" +
            "<div style='background:#e3f2fd;border:1px solid #90caf9;padding:20px;border-radius:8px;margin:15px 0'>" +
            "<h2 style='color:#1565c0;margin:0 0 15px'>" + esc(camp.getCampName()) + "</h2>" +
            "<table style='width:100%'>" +
            campRow("📅 Date",      camp.getCampDate() != null ? camp.getCampDate().toString() : "") +
            campRow("⏰ Time",      (camp.getStartTime() != null ? camp.getStartTime().toString() : "") +
                                    " - " + (camp.getEndTime() != null ? camp.getEndTime().toString() : "")) +
            campRow("📍 Venue",     esc(camp.getLocation())) +
            campRow("👤 Organizer", esc(camp.getOrganizerName())) +
            campRow("📞 Contact",   esc(camp.getContactNumber())) +
            "</table></div>" +
            (camp.getDescription() != null && !camp.getDescription().isBlank()
                ? "<p><em>" + esc(camp.getDescription()) + "</em></p>" : "") +
            "<p>Your participation can save up to 3 lives. We look forward to seeing you!</p>");
    }

    private String buildCampReminderHtml(String donorName, DonationCamp camp) {
        return wrap("⏰ Camp Reminder - Tomorrow",
            "<p>Dear <strong>" + donorName + "</strong>,</p>" +
            "<p>This is a friendly reminder that the blood donation camp is <strong>tomorrow</strong>!</p>" +
            "<div style='background:#fff8e1;border:1px solid #ffe082;padding:15px;border-radius:8px;margin:15px 0'>" +
            "<p><strong>📅 " + esc(camp.getCampName()) + "</strong></p>" +
            "<p>📍 " + esc(camp.getLocation()) + " | ⏰ " +
            (camp.getStartTime() != null ? camp.getStartTime().toString() : "") + "</p></div>" +
            "<p><strong>Tips before donating:</strong></p>" +
            "<ul><li>Stay well hydrated</li><li>Eat a healthy meal</li>" +
            "<li>Get a good night's sleep</li><li>Bring a valid ID</li></ul>");
    }

    private String buildCampCancellationHtml(String donorName, DonationCamp camp) {
        return wrap("❌ Camp Cancellation Notice",
            "<p>Dear <strong>" + donorName + "</strong>,</p>" +
            "<div style='background:#ffebee;border-left:4px solid #e74c3c;padding:15px;margin:15px 0;border-radius:4px'>" +
            "<p style='margin:0;color:#c62828'>The blood donation camp <strong>" + esc(camp.getCampName()) +
            "</strong> scheduled for <strong>" + camp.getCampDate() + "</strong> has been <strong>CANCELLED</strong>.</p></div>" +
            "<p>We apologize for any inconvenience. We will notify you of future camp dates.</p>" +
            "<p>Thank you for your continued support.</p>");
    }

    // ── Template helpers ──────────────────────────────────────────────────────

    private String wrap(String title, String content) {
        return "<!DOCTYPE html><html><head><meta charset='UTF-8'>" +
            "<meta name='viewport' content='width=device-width,initial-scale=1'></head>" +
            "<body style='font-family:Segoe UI,Arial,sans-serif;background:#f5f5f5;margin:0;padding:20px'>" +
            "<div style='max-width:600px;margin:0 auto;background:#fff;border-radius:8px;" +
            "overflow:hidden;box-shadow:0 2px 10px rgba(0,0,0,0.1)'>" +
            "<div style='background:linear-gradient(135deg,#c0392b,#e74c3c);padding:25px;text-align:center'>" +
            "<h1 style='color:#fff;margin:0;font-size:22px'>🩸 Blood Bank Management System</h1>" +
            "<p style='color:rgba(255,255,255,0.85);margin:5px 0 0;font-size:14px'>Smart Healthcare Platform</p></div>" +
            "<div style='padding:25px'>" +
            "<h2 style='color:#2c3e50;border-bottom:2px solid #e74c3c;padding-bottom:10px'>" + title + "</h2>" +
            content +
            "<hr style='border:none;border-top:1px solid #eee;margin:20px 0'>" +
            "<p style='color:#7f8c8d;font-size:12px;text-align:center'>" +
            "This is an automated message from Blood Bank Management System.<br>" +
            "Please do not reply to this email.</p>" +
            "</div></div></body></html>";
    }

    private String row(String label, String value, boolean shaded) {
        String bg = shaded ? "background:#f8f9fa;" : "";
        return "<tr style='" + bg + "'>" +
               "<td style='padding:8px;border:1px solid #dee2e6'><strong>" + label + "</strong></td>" +
               "<td style='padding:8px;border:1px solid #dee2e6'>" + value + "</td></tr>";
    }

    private String campRow(String label, String value) {
        return "<tr><td style='padding:5px 0;font-weight:bold'>" + label + ":</td>" +
               "<td style='padding:5px 0'>" + value + "</td></tr>";
    }

    /** Escape HTML special characters to prevent injection */
    private String esc(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&#39;");
    }
}
