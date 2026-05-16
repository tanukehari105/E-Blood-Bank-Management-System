package com.bloodbank.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * BloodInventory — one batch of blood in stock.
 *
 * Each batch gets a unique batchCode (e.g. BB-A+-20260512-0001) so it can be
 * tracked end-to-end: from the donor who gave it → to the patient who received it.
 *
 * donorId links back to the Donor who produced this batch so we can email them
 * when their blood is actually used.
 */
@Entity
@Table(name = "blood_inventory")
public class BloodInventory {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "blood_group", nullable = false)
    private String bloodGroup;

    private int quantity;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "added_date")
    private LocalDate addedDate;

    private String source;

    /**
     * Unique batch code generated when blood is added to inventory.
     * Format: BB-{bloodGroup}-{YYYYMMDD}-{id}
     * Example: BB-A+-20260512-42
     * NOTE: No unique constraint — SQLite does not support ADD COLUMN UNIQUE.
     * Uniqueness is guaranteed by the generation logic (id is always unique).
     */
    @Column(name = "batch_code")
    private String batchCode;

    /**
     * The donor who donated this blood (null if sourced externally).
     */
    @Column(name = "donor_id")
    private Long donorId;

    // ── Getters & Setters ──────────────────────────────────────────────────────
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getBloodGroup() { return bloodGroup; }
    public void setBloodGroup(String bloodGroup) { this.bloodGroup = bloodGroup; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }
    public LocalDate getAddedDate() { return addedDate; }
    public void setAddedDate(LocalDate addedDate) { this.addedDate = addedDate; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public String getBatchCode() { return batchCode; }
    public void setBatchCode(String batchCode) { this.batchCode = batchCode; }
    public Long getDonorId() { return donorId; }
    public void setDonorId(Long donorId) { this.donorId = donorId; }
}
