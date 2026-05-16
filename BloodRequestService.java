package com.bloodbank.service;

import com.bloodbank.entity.BloodAllocation;
import com.bloodbank.entity.BloodRequest;
import com.bloodbank.entity.Donor;
import com.bloodbank.exception.BusinessException;
import com.bloodbank.exception.ResourceNotFoundException;
import com.bloodbank.repository.BloodRequestRepository;
import com.bloodbank.repository.DonorRepository;
import com.bloodbank.repository.HospitalRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * Blood request service — handles the full lifecycle of a blood request.
 *
 * Key behaviours:
 *  • On APPROVE: FIFO allocation with full tracking, then emails every donor
 *    whose blood was actually used ("your donation saved a life").
 *  • On REJECT / insufficient stock: emails eligible donors of that blood group
 *    as an emergency alert.
 *  • Hospital email notifications on approve/reject.
 */
@Service
public class BloodRequestService {

    private static final Logger log = LoggerFactory.getLogger(BloodRequestService.class);

    @Autowired private BloodRequestRepository requestRepository;
    @Autowired private InventoryService       inventoryService;
    @Autowired private DonorService           donorService;
    @Autowired private DonorRepository        donorRepository;
    @Autowired private HospitalRepository     hospitalRepository;
    @Autowired private EmailService           emailService;
    @Autowired private AuditLogService        auditLogService;

    // ── Queries ───────────────────────────────────────────────────────────────

    public List<BloodRequest> getAllRequests() {
        return requestRepository.findAllOrderByDateDesc();
    }

    public BloodRequest getById(Long id) {
        return requestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found: " + id));
    }

    public List<BloodRequest> getPendingRequests() {
        return requestRepository.findByStatus("PENDING");
    }

    public List<BloodRequest> getCriticalPendingRequests() {
        return requestRepository.findCriticalPendingRequests();
    }

    public List<BloodRequest> getUrgentPendingRequests() {
        return requestRepository.findUrgentPendingRequests();
    }

    public List<BloodRequest> getRequestsByHospital(Long hospitalId) {
        return requestRepository.findByHospitalIdOrderByRequestDateDesc(hospitalId);
    }

    public long getPendingCount() {
        return requestRepository.countPendingRequests();
    }

    public List<Donor> findMatchingDonors(String bloodGroup) {
        return donorService.getEligibleDonorsByBloodGroup(bloodGroup);
    }

    // ── Create ────────────────────────────────────────────────────────────────

    public BloodRequest createRequest(BloodRequest request, String performedBy) {
        request.setStatus("PENDING");
        request.setRequestDate(LocalDate.now());
        if (request.getUrgencyLevel() == null || request.getUrgencyLevel().isBlank()) {
            request.setUrgencyLevel("NORMAL");
        }
        BloodRequest saved = requestRepository.save(request);
        auditLogService.log(performedBy, "STAFF", "CREATE_REQUEST", "BloodRequest",
                saved.getId(),
                "New " + saved.getUrgencyLevel() + " request for "
                + saved.getBloodGroup() + " x" + saved.getUnits());
        return saved;
    }

    // ── Approve ───────────────────────────────────────────────────────────────

    /**
     * Approves a blood request:
     * 1. Checks available stock
     * 2. FIFO allocation with full tracking (records which batch / donor)
     * 3. Emails every donor whose blood was used — "your donation saved a life"
     * 4. Emails the hospital (if linked)
     * 5. If partial stock, triggers emergency donor alert for the shortfall
     */
    public BloodRequest approveRequest(Long id, String performedBy) {
        BloodRequest request = getById(id);
        if (!"PENDING".equals(request.getStatus())) {
            throw new BusinessException("Request is not in PENDING status");
        }

        // How much stock is available?
        Integer available = inventoryService.getStockByBloodGroup()
                .getOrDefault(request.getBloodGroup(), 0);

        if (available <= 0) {
            triggerEmergencyAlert(request.getBloodGroup(), request.getUnits());
            throw new BusinessException(
                "No stock available for " + request.getBloodGroup()
                + ". Emergency donor alerts have been sent.");
        }

        int toAllocate = Math.min(available, request.getUnits());
        boolean partial = toAllocate < request.getUnits();

        // ── FIFO allocation with tracking ─────────────────────────────────────
        List<BloodAllocation> allocations = inventoryService.reduceStockWithTracking(
                request.getBloodGroup(), toAllocate, id, performedBy);

        if (allocations.isEmpty()) {
            throw new BusinessException(
                "Allocation failed — insufficient stock for " + request.getBloodGroup());
        }

        request.setApprovedUnits(toAllocate);
        request.setStatus(partial ? "PARTIALLY_APPROVED" : "APPROVED");
        request.setProcessedDate(LocalDate.now());
        BloodRequest saved = requestRepository.save(request);

        auditLogService.log(performedBy, "ADMIN", "APPROVE_REQUEST", "BloodRequest",
                id, saved.getStatus() + ": " + toAllocate
                + " units of " + request.getBloodGroup());

        // ── Email every donor whose blood was used ────────────────────────────
        String hospitalName = resolveHospitalName(request);
        int donorEmailsSent = 0;
        for (BloodAllocation alloc : allocations) {
            if (alloc.getDonorId() != null) {
                donorRepository.findById(alloc.getDonorId()).ifPresent(donor -> {
                    log.info("Sending blood-usage notification to donor: {} <{}>",
                            donor.getName(), donor.getEmail());
                    emailService.sendBloodUsageNotification(
                            donor,
                            alloc.getBloodGroup(),
                            alloc.getUnitsAllocated(),
                            hospitalName);
                });
                donorEmailsSent++;
            } else {
                log.warn("Allocation #{} has no donorId — batch was added manually, no email sent.",
                        alloc.getId());
            }
        }
        log.info("Request #{} approved. Donor notifications queued: {}", id, donorEmailsSent);

        // ── Email hospital if linked ──────────────────────────────────────────
        if (request.getHospitalId() != null) {
            hospitalRepository.findById(request.getHospitalId()).ifPresent(hospital ->
                emailService.sendRequestApprovedToHospital(
                        hospital.getEmail(), hospital.getHospitalName(),
                        request.getPatientName(), request.getBloodGroup(),
                        toAllocate, id)
            );
        }

        // ── If partial, alert eligible donors for the remaining shortfall ─────
        if (partial) {
            triggerEmergencyAlert(request.getBloodGroup(),
                    request.getUnits() - toAllocate);
        }

        return saved;
    }

    // ── Reject ────────────────────────────────────────────────────────────────

    public BloodRequest rejectRequest(Long id, String reason, String performedBy) {
        BloodRequest request = getById(id);
        if (!"PENDING".equals(request.getStatus())) {
            throw new BusinessException("Request is not in PENDING status");
        }
        request.setStatus("REJECTED");
        request.setProcessedDate(LocalDate.now());
        request.setNotes(reason);
        BloodRequest saved = requestRepository.save(request);

        auditLogService.log(performedBy, "ADMIN", "REJECT_REQUEST", "BloodRequest",
                id, "Rejected: " + (reason.isBlank() ? "no reason given" : reason));

        // Email hospital if linked
        if (request.getHospitalId() != null) {
            hospitalRepository.findById(request.getHospitalId()).ifPresent(hospital ->
                emailService.sendRequestRejectedToHospital(
                        hospital.getEmail(), hospital.getHospitalName(),
                        request.getPatientName(), request.getBloodGroup(),
                        request.getUnits(), id, reason)
            );
        }

        return saved;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /** Sends emergency email alerts to all eligible donors of the required blood group */
    private void triggerEmergencyAlert(String bloodGroup, int unitsNeeded) {
        List<Donor> eligibleDonors = donorService.getEligibleDonorsByBloodGroup(bloodGroup);
        for (Donor donor : eligibleDonors) {
            emailService.sendEmergencyDonorAlert(donor, bloodGroup, unitsNeeded);
        }
    }

    /** Returns the hospital name for the "blood used" notification */
    private String resolveHospitalName(BloodRequest request) {
        if (request.getHospitalId() != null) {
            return hospitalRepository.findById(request.getHospitalId())
                    .map(h -> h.getHospitalName())
                    .orElse(request.getHospital() != null ? request.getHospital() : "a hospital");
        }
        return request.getHospital() != null ? request.getHospital() : "a hospital";
    }
}
