package com.bloodbank.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bloodbank.entity.BloodInventory;
import com.bloodbank.entity.Donation;
import com.bloodbank.entity.Donor;
import com.bloodbank.repository.DonationRepository;
import com.bloodbank.repository.DonorRepository;

/**
 * Donation service — registers donations, updates inventory with donor linkage,
 * and links donations to camps.
 */
@Service
public class DonationService {

    @Autowired private DonationRepository    donationRepository;
    @Autowired private DonorService          donorService;
    @Autowired private DonorRepository       donorRepository;
    @Autowired private InventoryService      inventoryService;
    @Autowired private DonationCampService   donationCampService;
    @Autowired private AuditLogService       auditLogService;

    public List<Donation> getAllDonations() {
        return donationRepository.findAll();
    }

    public List<Donation> getDonationsByDonor(Long donorId) {
        return donationRepository.findByDonorId(donorId);
    }

    public List<Donation> getDonationsByCamp(Long campId) {
        return donationRepository.findByCampId(campId);
    }

    /**
     * Registers a donation:
     * 1. Validates 90-day eligibility
     * 2. Creates Donation record
     * 3. Updates donor's lastDonationDate
     * 4. Adds blood to inventory with a unique batch code linked to this donor
     *    (42-day expiry for whole blood)
     * 5. Updates camp analytics if linked to a camp
     */
    public Donation registerDonation(Long donorId, int quantity, String notes,
                                      Long campId, String performedBy) {

        Donor donor = donorService.getDonorById(donorId);

        if (!donorService.isDonorEligible(donor)) {
            throw new RuntimeException(
                "Donor is not eligible to donate yet. Must wait 90 days since last donation.");
        }

        // ── Create donation record ────────────────────────────────────────────
        Donation donation = new Donation();
        donation.setDonor(donor);
        donation.setDonationDate(LocalDate.now());
        donation.setQuantity(quantity);
        donation.setNotes(notes);
        donation.setBloodGroup(donor.getBloodGroup());
        donation.setCampId(campId);

        // ── Update donor's last donation date ─────────────────────────────────
        donor.setLastDonationDate(LocalDate.now());
        donorRepository.save(donor);

        // ── Add to inventory — link donorId so we can email them later ────────
        BloodInventory batch = inventoryService.addStock(
                donor.getBloodGroup(),
                quantity,
                LocalDate.now().plusDays(42),   // 42-day expiry for whole blood
                donor.getId()                   // ← donor linkage
        );

        Donation saved = donationRepository.save(donation);

        // ── Update camp analytics if linked ───────────────────────────────────
        if (campId != null) {
            donationCampService.updateCampStats(campId, quantity);
        }

        auditLogService.log(performedBy, "STAFF", "REGISTER_DONATION", "Donation",
                saved.getId(),
                "Donor: " + donor.getName()
                + " | " + donor.getBloodGroup()
                + " x" + quantity + " units"
                + " | Batch: " + batch.getBatchCode());

        return saved;
    }

    public List<Donation> getDonationsByDateRange(LocalDate start, LocalDate end) {
        return donationRepository.findByDateRange(start, end);
    }

    public long getTotalDonations() {
        return donationRepository.count();
    }
}
