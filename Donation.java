package com.bloodbank.ui.model;

import java.time.LocalDate;

public class Donation {
    public Long id;
    public Donor donor;
    public LocalDate donationDate;
    public int quantity;
    public String notes;
    public String bloodGroup;
    /** Optional link to a donation camp */
    public Long campId;
}
