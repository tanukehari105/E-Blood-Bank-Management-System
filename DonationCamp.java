package com.bloodbank.ui.model;

public class DonationCamp {
    public Long id;
    public String campName;
    public String organizerName;
    public String location;
    public String campDate;
    public String startTime;
    public String endTime;
    public String contactNumber;
    public int expectedDonors;
    public String description;
    public boolean active;
    public int actualDonors;
    public int totalUnitsCollected;

    public String getStatusText() {
        return active ? "Active" : "Cancelled";
    }

    public String getTimeRange() {
        String s = startTime != null ? startTime : "";
        String e = endTime != null ? endTime : "";
        return s + (e.isBlank() ? "" : " - " + e);
    }
}
