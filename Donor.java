package com.bloodbank.ui.model;

import java.time.LocalDate;

public class Donor {
    public Long id;
    public String name;
    public int age;
    public String gender;
    public String bloodGroup;
    public String contact;
    public String address;
    public String email;
    public String lastDonationDate;
    public boolean active;

    public boolean isEligible() {
        if (lastDonationDate == null || lastDonationDate.isBlank()) return true;
        try {
            LocalDate last = LocalDate.parse(lastDonationDate);
            return last.plusDays(90).isBefore(LocalDate.now()) || last.plusDays(90).isEqual(LocalDate.now());
        } catch (Exception e) {
            return true;
        }
    }
}
