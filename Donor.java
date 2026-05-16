package com.bloodbank.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

/**
 * Donor entity — updated with email field for notification support.
 */
@Entity
@Table(name = "donors")
public class Donor {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private String name;

    private int age;
    private String gender;

    @Column(name = "blood_group", nullable = false)
    private String bloodGroup;

    private String contact;
    private String address;

    /** Email for emergency alerts and camp invitations */
    private String email;

    @Column(name = "last_donation_date")
    private LocalDate lastDonationDate;

    @Column(name = "is_active")
    private boolean active = true;

    // ── Getters & Setters ──────────────────────────────────────────────────────
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public String getBloodGroup() { return bloodGroup; }
    public void setBloodGroup(String bloodGroup) { this.bloodGroup = bloodGroup; }
    public String getContact() { return contact; }
    public void setContact(String contact) { this.contact = contact; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public LocalDate getLastDonationDate() { return lastDonationDate; }
    public void setLastDonationDate(LocalDate lastDonationDate) { this.lastDonationDate = lastDonationDate; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
