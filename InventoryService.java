package com.bloodbank.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bloodbank.entity.BloodAllocation;
import com.bloodbank.entity.BloodInventory;
import com.bloodbank.repository.BloodAllocationRepository;
import com.bloodbank.repository.BloodInventoryRepository;
import com.bloodbank.util.QrCodeUtil;

/**
 * Inventory service — manages blood stock with FIFO deduction,
 * unique batch codes, donor tracking, allocation records, and QR codes.
 */
@Service
public class InventoryService {

    private static final int LOW_STOCK_THRESHOLD = 5;
    private static final int EXPIRY_WARNING_DAYS  = 7;

    @Autowired private BloodInventoryRepository inventoryRepository;
    @Autowired private BloodAllocationRepository allocationRepository;
    @Autowired private QrCodeUtil qrCodeUtil;

    // ── CRUD ──────────────────────────────────────────────────────────────────

    public List<BloodInventory> getAllInventory() {
        return inventoryRepository.findAll();
    }

    public BloodInventory getById(Long id) {
        return inventoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Inventory record not found: " + id));
    }

    public BloodInventory addInventory(BloodInventory inventory) {
        inventory.setAddedDate(LocalDate.now());
        BloodInventory saved = inventoryRepository.save(inventory);
        // Generate batch code after we have the DB id
        if (saved.getBatchCode() == null) {
            saved.setBatchCode(generateBatchCode(saved));
            saved = inventoryRepository.save(saved);
        }
        return saved;
    }

    public BloodInventory updateInventory(Long id, BloodInventory updated) {
        BloodInventory existing = getById(id);
        existing.setBloodGroup(updated.getBloodGroup());
        existing.setQuantity(updated.getQuantity());
        existing.setExpiryDate(updated.getExpiryDate());
        existing.setSource(updated.getSource());
        return inventoryRepository.save(existing);
    }

    public void deleteInventory(Long id) {
        inventoryRepository.deleteById(id);
    }

    public void removeExpiredBlood() {
        List<BloodInventory> expired = inventoryRepository.findExpiredBlood(LocalDate.now());
        inventoryRepository.deleteAll(expired);
    }

    // ── Stock Queries ─────────────────────────────────────────────────────────

    public Map<String, Integer> getStockByBloodGroup() {
        List<Object[]> results = inventoryRepository.getAvailableStockByBloodGroup(LocalDate.now());
        Map<String, Integer> stock = new LinkedHashMap<>();
        for (String bg : new String[]{"A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"}) {
            stock.put(bg, 0);
        }
        for (Object[] row : results) {
            stock.put((String) row[0], ((Number) row[1]).intValue());
        }
        return stock;
    }

    public List<String> getLowStockAlerts() {
        Map<String, Integer> stock = getStockByBloodGroup();
        List<String> alerts = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : stock.entrySet()) {
            if (entry.getValue() <= LOW_STOCK_THRESHOLD) {
                alerts.add(entry.getKey() + " blood is low: " + entry.getValue() + " units");
            }
        }
        return alerts;
    }

    public List<String> getExpiryAlerts() {
        LocalDate today       = LocalDate.now();
        LocalDate warningDate = today.plusDays(EXPIRY_WARNING_DAYS);
        List<BloodInventory> expiring = inventoryRepository.findExpiringBlood(today, warningDate);
        List<String> alerts = new ArrayList<>();
        for (BloodInventory inv : expiring) {
            alerts.add(inv.getBloodGroup() + " (" + inv.getQuantity() + " units) expires on "
                    + inv.getExpiryDate()
                    + (inv.getBatchCode() != null ? " [" + inv.getBatchCode() + "]" : ""));
        }
        return alerts;
    }

    // ── Stock Addition (from donation) ────────────────────────────────────────

    /**
     * Adds a new blood batch to inventory after a donation.
     * Generates a unique batch code and links the donor.
     *
     * @param bloodGroup  blood group
     * @param units       quantity donated
     * @param expiryDate  expiry date (42 days for whole blood)
     * @param donorId     the donor who gave this blood (for email notification later)
     * @return the saved inventory record with its batch code
     */
    public BloodInventory addStock(String bloodGroup, int units,
                                   LocalDate expiryDate, Long donorId) {
        BloodInventory inv = new BloodInventory();
        inv.setBloodGroup(bloodGroup);
        inv.setQuantity(units);
        inv.setExpiryDate(expiryDate);
        inv.setAddedDate(LocalDate.now());
        inv.setSource("donation");
        inv.setDonorId(donorId);

        // Save first to get the auto-generated id
        BloodInventory saved = inventoryRepository.save(inv);

        // Now generate and persist the batch code using the id
        saved.setBatchCode(generateBatchCode(saved));
        return inventoryRepository.save(saved);
    }

    /** Overload without donorId — for manual/external stock additions */
    public void addStock(String bloodGroup, int units, LocalDate expiryDate) {
        addStock(bloodGroup, units, expiryDate, null);
    }

    // ── FIFO Stock Reduction with Full Tracking ───────────────────────────────

    /**
     * Reduces stock using FIFO (oldest batches first) and records every allocation.
     * Also captures the donorId from each batch so the caller can notify donors.
     *
     * @return list of BloodAllocation records created (one per batch consumed)
     */
    public List<BloodAllocation> reduceStockWithTracking(String bloodGroup, int units,
                                                          Long requestId, String performedBy) {
        List<BloodAllocation> allocations = new ArrayList<>();

        List<BloodInventory> batches = inventoryRepository.findByBloodGroup(bloodGroup);
        batches.sort(Comparator.comparing(BloodInventory::getExpiryDate)); // oldest first

        int remaining = units;
        for (BloodInventory batch : batches) {
            if (remaining <= 0) break;
            if (batch.getExpiryDate().isBefore(LocalDate.now())) continue;

            int toTake = Math.min(batch.getQuantity(), remaining);

            // Record the allocation — include donorId so we can email them
            BloodAllocation allocation = new BloodAllocation();
            allocation.setRequestId(requestId);
            allocation.setInventoryId(batch.getId());
            allocation.setBloodGroup(bloodGroup);
            allocation.setUnitsAllocated(toTake);
            allocation.setAllocationDate(LocalDate.now());
            allocation.setBatchExpiryDate(batch.getExpiryDate());
            allocation.setDonorId(batch.getDonorId());   // ← key: track which donor
            allocations.add(allocationRepository.save(allocation));

            // Consume the batch
            if (toTake >= batch.getQuantity()) {
                inventoryRepository.delete(batch);
            } else {
                batch.setQuantity(batch.getQuantity() - toTake);
                inventoryRepository.save(batch);
            }

            remaining -= toTake;
        }

        return allocations;
    }

    /** Simple reduction without tracking (kept for backward compat) */
    public boolean reduceStock(String bloodGroup, int units) {
        Integer available = inventoryRepository.getTotalAvailableByBloodGroup(bloodGroup, LocalDate.now());
        if (available == null || available < units) return false;

        List<BloodInventory> batches = inventoryRepository.findByBloodGroup(bloodGroup);
        batches.sort(Comparator.comparing(BloodInventory::getExpiryDate));

        int remaining = units;
        for (BloodInventory batch : batches) {
            if (batch.getExpiryDate().isBefore(LocalDate.now())) continue;
            if (remaining <= 0) break;
            if (batch.getQuantity() <= remaining) {
                remaining -= batch.getQuantity();
                inventoryRepository.delete(batch);
            } else {
                batch.setQuantity(batch.getQuantity() - remaining);
                inventoryRepository.save(batch);
                remaining = 0;
            }
        }
        return true;
    }

    // ── Allocation Queries ────────────────────────────────────────────────────

    public List<BloodAllocation> getAllAllocations() {
        return allocationRepository.findAll();
    }

    public List<BloodAllocation> getAllocationsByRequest(Long requestId) {
        return allocationRepository.findByRequestId(requestId);
    }

    // ── QR Code ───────────────────────────────────────────────────────────────

    /**
     * Generates a Base64-encoded QR code PNG for a specific inventory batch.
     * Encodes: batchCode, bloodGroup, donorId, addedDate, expiryDate.
     */
    public String generateQrCode(Long inventoryId) {
        BloodInventory inv = getById(inventoryId);
        String content = qrCodeUtil.buildInventoryQrContent(
                inv.getId(),
                inv.getDonorId() != null ? inv.getDonorId() : 0L,
                "N/A",
                inv.getBloodGroup(),
                inv.getAddedDate()  != null ? inv.getAddedDate().toString()  : "N/A",
                inv.getExpiryDate() != null ? inv.getExpiryDate().toString() : "N/A"
        );
        return qrCodeUtil.generateQrCodeBase64(content);
    }

    // ── Batch Code Generation ─────────────────────────────────────────────────

    /**
     * Generates a human-readable unique batch code.
     * Format: BB-{BLOODGROUP}-{YYYYMMDD}-{ID}
     * Example: BB-A+-20260512-42
     */
    private String generateBatchCode(BloodInventory inv) {
        String dateStr = inv.getAddedDate() != null
                ? inv.getAddedDate().format(DateTimeFormatter.BASIC_ISO_DATE)
                : LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        // Sanitise blood group for use in code (replace + with P, - with N)
        String bg = inv.getBloodGroup()
                .replace("+", "P")
                .replace("-", "N");
        return "BB-" + bg + "-" + dateStr + "-" + inv.getId();
    }
}
