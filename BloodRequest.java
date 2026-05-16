package com.bloodbank.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

/**
 * BloodRequest entity — enhanced with urgency levels, hospital linkage,
 * PARTIALLY_APPROVED status, and quantity tracking.
 */
@Entity
@Table(name = "requests")
public class BloodRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "patient_name", nullable = false)
    private String patientName;

    /** Legacy plain-text hospital name (kept for backward compat) */
    private String hospital;

    /** FK to Hospital entity — set when request comes from a hospital login */
    @Column(name = "hospital_id")
    private Long hospitalId;

    @Column(name = "blood_group", nullable = false)
    private String bloodGroup;

    /** Requested units */
    private int units;

    /**
     * Units actually approved (for PARTIALLY_APPROVED).
     * Using Integer (boxed) so existing DB rows with NULL deserialize as 0, not an error.
     */
    @Column(name = "approved_units")
    private Integer approvedUnits = 0;

    /**
     * Status: PENDING | APPROVED | REJECTED | PARTIALLY_APPROVED
     */
    private String status;

    /**
     * Urgency: NORMAL | URGENT | CRITICAL
     */
    @Column(name = "urgency_level")
    private String urgencyLevel = "NORMAL";

    @Column(name = "request_date")
    private LocalDate requestDate;

    @Column(name = "processed_date")
    private LocalDate processedDate;

    private String notes;

    @Column(name = "contact_number")
    private String contactNumber;

    // ── Getters & Setters ──────────────────────────────────────────────────────
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }
    public String getHospital() { return hospital; }
    public void setHospital(String hospital) { this.hospital = hospital; }
    public Long getHospitalId() { return hospitalId; }
    public void setHospitalId(Long hospitalId) { this.hospitalId = hospitalId; }
    public String getBloodGroup() { return bloodGroup; }
    public void setBloodGroup(String bloodGroup) { this.bloodGroup = bloodGroup; }
    public int getUnits() { return units; }
    public void setUnits(int units) { this.units = units; }
    public int getApprovedUnits() { return approvedUnits != null ? approvedUnits : 0; }
    public void setApprovedUnits(int approvedUnits) { this.approvedUnits = approvedUnits; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getUrgencyLevel() { return urgencyLevel; }
    public void setUrgencyLevel(String urgencyLevel) { this.urgencyLevel = urgencyLevel; }
    public LocalDate getRequestDate() { return requestDate; }
    public void setRequestDate(LocalDate requestDate) { this.requestDate = requestDate; }
    public LocalDate getProcessedDate() { return processedDate; }
    public void setProcessedDate(LocalDate processedDate) { this.processedDate = processedDate; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public String getContactNumber() { return contactNumber; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }
}
