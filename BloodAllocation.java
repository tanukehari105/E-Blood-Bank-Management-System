package com.bloodbank.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

/**
 * BloodAllocation entity — tracks exactly which inventory batch was used
 * for which blood request (FIFO audit trail).
 */
@Entity
@Table(name = "blood_allocations")
public class BloodAllocation {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "request_id", nullable = false)
    private Long requestId;

    @Column(name = "inventory_id")
    private Long inventoryId;

    @Column(name = "donor_id")
    private Long donorId;

    @Column(name = "donor_name")
    private String donorName;

    @Column(name = "blood_group")
    private String bloodGroup;

    @Column(name = "units_allocated", nullable = false)
    private int unitsAllocated;

    @Column(name = "allocation_date", nullable = false)
    private LocalDate allocationDate;

    @Column(name = "batch_expiry_date")
    private LocalDate batchExpiryDate;

    // ── Getters & Setters ──────────────────────────────────────────────────────
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getRequestId() { return requestId; }
    public void setRequestId(Long requestId) { this.requestId = requestId; }
    public Long getInventoryId() { return inventoryId; }
    public void setInventoryId(Long inventoryId) { this.inventoryId = inventoryId; }
    public Long getDonorId() { return donorId; }
    public void setDonorId(Long donorId) { this.donorId = donorId; }
    public String getDonorName() { return donorName; }
    public void setDonorName(String donorName) { this.donorName = donorName; }
    public String getBloodGroup() { return bloodGroup; }
    public void setBloodGroup(String bloodGroup) { this.bloodGroup = bloodGroup; }
    public int getUnitsAllocated() { return unitsAllocated; }
    public void setUnitsAllocated(int unitsAllocated) { this.unitsAllocated = unitsAllocated; }
    public LocalDate getAllocationDate() { return allocationDate; }
    public void setAllocationDate(LocalDate allocationDate) { this.allocationDate = allocationDate; }
    public LocalDate getBatchExpiryDate() { return batchExpiryDate; }
    public void setBatchExpiryDate(LocalDate batchExpiryDate) { this.batchExpiryDate = batchExpiryDate; }
}
