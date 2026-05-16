package com.bloodbank.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * DonationCamp entity — represents a blood donation camp event.
 * Only ADMIN can create/edit/cancel camps.
 * When created, eligible donors are automatically emailed invitations.
 */
@Entity
@Table(name = "donation_camps")
public class DonationCamp {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotBlank(message = "Camp name is required")
    @Column(name = "camp_name", nullable = false)
    private String campName;

    @Column(name = "organizer_name")
    private String organizerName;

    @Column(nullable = false)
    private String location;

    @Column(name = "camp_date", nullable = false)
    private LocalDate campDate;

    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "end_time")
    private LocalTime endTime;

    @Column(name = "contact_number")
    private String contactNumber;

    @Column(name = "expected_donors")
    private int expectedDonors;

    @Column(length = 1000)
    private String description;

    @Column(name = "is_active")
    private boolean active = true;

    // Analytics fields (updated as donations come in)
    @Column(name = "actual_donors")
    private int actualDonors = 0;

    @Column(name = "total_units_collected")
    private int totalUnitsCollected = 0;

    // ── Getters & Setters ──────────────────────────────────────────────────────
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCampName() { return campName; }
    public void setCampName(String campName) { this.campName = campName; }
    public String getOrganizerName() { return organizerName; }
    public void setOrganizerName(String organizerName) { this.organizerName = organizerName; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public LocalDate getCampDate() { return campDate; }
    public void setCampDate(LocalDate campDate) { this.campDate = campDate; }
    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }
    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }
    public String getContactNumber() { return contactNumber; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }
    public int getExpectedDonors() { return expectedDonors; }
    public void setExpectedDonors(int expectedDonors) { this.expectedDonors = expectedDonors; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public int getActualDonors() { return actualDonors; }
    public void setActualDonors(int actualDonors) { this.actualDonors = actualDonors; }
    public int getTotalUnitsCollected() { return totalUnitsCollected; }
    public void setTotalUnitsCollected(int totalUnitsCollected) { this.totalUnitsCollected = totalUnitsCollected; }
}
