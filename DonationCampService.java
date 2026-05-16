package com.bloodbank.service;

import com.bloodbank.entity.DonationCamp;
import com.bloodbank.entity.Donor;
import com.bloodbank.exception.ResourceNotFoundException;
import com.bloodbank.repository.DonationCampRepository;
import com.bloodbank.repository.DonorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * Donation camp service.
 *
 * When a camp is CREATED:
 *   → Emails ALL active donors who have an email address (not just eligible ones).
 *     Every donor should know about the camp — even those who donated recently
 *     can spread the word or attend as volunteers.
 *
 * When a camp is CANCELLED:
 *   → Emails all active donors to inform them.
 */
@Service
public class DonationCampService {

    @Autowired private DonationCampRepository campRepository;
    @Autowired private DonorRepository        donorRepository;   // direct repo for "all active"
    @Autowired private DonorService           donorService;
    @Autowired private EmailService           emailService;
    @Autowired private AuditLogService        auditLogService;

    // ── Queries ───────────────────────────────────────────────────────────────

    public List<DonationCamp> getAllCamps() {
        return campRepository.findAll();
    }

    public List<DonationCamp> getActiveCamps() {
        return campRepository.findByActiveTrue();
    }

    public List<DonationCamp> getUpcomingCamps() {
        return campRepository.findUpcomingCamps(LocalDate.now());
    }

    public List<DonationCamp> getPastCamps() {
        return campRepository.findPastCamps(LocalDate.now());
    }

    public DonationCamp getById(Long id) {
        return campRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Camp not found: " + id));
    }

    // ── Create ────────────────────────────────────────────────────────────────

    /**
     * Creates a camp and asynchronously emails ALL active donors who have an email.
     * This ensures maximum reach — every registered donor is informed.
     */
    public DonationCamp createCamp(DonationCamp camp, String adminUsername) {
        camp.setActive(true);
        DonationCamp saved = campRepository.save(camp);

        auditLogService.log(adminUsername, "ADMIN", "CREATE_CAMP", "DonationCamp",
                saved.getId(),
                "Created camp: " + saved.getCampName() + " on " + saved.getCampDate());

        // Email ALL active donors (not just eligible — everyone should know)
        List<Donor> allActiveDonors = donorRepository.findByActiveTrue();
        int emailsSent = 0;
        for (Donor donor : allActiveDonors) {
            if (donor.getEmail() != null && !donor.getEmail().isBlank()) {
                emailService.sendCampInvitation(donor, saved);
                emailsSent++;
            }
        }

        auditLogService.log(adminUsername, "ADMIN", "CAMP_INVITATIONS_SENT", "DonationCamp",
                saved.getId(),
                "Sent camp invitation to " + emailsSent + " donors for: " + saved.getCampName());

        return saved;
    }

    // ── Update ────────────────────────────────────────────────────────────────

    public DonationCamp updateCamp(Long id, DonationCamp updated, String adminUsername) {
        DonationCamp existing = getById(id);
        existing.setCampName(updated.getCampName());
        existing.setOrganizerName(updated.getOrganizerName());
        existing.setLocation(updated.getLocation());
        existing.setCampDate(updated.getCampDate());
        existing.setStartTime(updated.getStartTime());
        existing.setEndTime(updated.getEndTime());
        existing.setContactNumber(updated.getContactNumber());
        existing.setExpectedDonors(updated.getExpectedDonors());
        existing.setDescription(updated.getDescription());

        DonationCamp saved = campRepository.save(existing);
        auditLogService.log(adminUsername, "ADMIN", "UPDATE_CAMP", "DonationCamp",
                id, "Updated camp: " + saved.getCampName());
        return saved;
    }

    // ── Cancel ────────────────────────────────────────────────────────────────

    /**
     * Cancels a camp and emails all active donors who have an email.
     */
    public DonationCamp cancelCamp(Long id, String adminUsername) {
        DonationCamp camp = getById(id);
        camp.setActive(false);
        DonationCamp saved = campRepository.save(camp);

        auditLogService.log(adminUsername, "ADMIN", "CANCEL_CAMP", "DonationCamp",
                id, "Cancelled camp: " + camp.getCampName());

        // Notify all active donors
        List<Donor> allActiveDonors = donorRepository.findByActiveTrue();
        for (Donor donor : allActiveDonors) {
            if (donor.getEmail() != null && !donor.getEmail().isBlank()) {
                emailService.sendCampCancellation(donor, saved);
            }
        }

        return saved;
    }

    // ── Delete ────────────────────────────────────────────────────────────────

    public void deleteCamp(Long id, String adminUsername) {
        DonationCamp camp = getById(id);
        auditLogService.log(adminUsername, "ADMIN", "DELETE_CAMP", "DonationCamp",
                id, "Deleted camp: " + camp.getCampName());
        campRepository.deleteById(id);
    }

    // ── Analytics ─────────────────────────────────────────────────────────────

    /** Called by DonationService after a donation is registered at a camp */
    public void updateCampStats(Long campId, int unitsAdded) {
        campRepository.findById(campId).ifPresent(camp -> {
            camp.setActualDonors(camp.getActualDonors() + 1);
            camp.setTotalUnitsCollected(camp.getTotalUnitsCollected() + unitsAdded);
            campRepository.save(camp);
        });
    }
}
