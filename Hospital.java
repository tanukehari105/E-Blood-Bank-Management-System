package com.bloodbank.ui.model;

public class Hospital {
    public Long id;
    public String hospitalName;
    public String email;
    public String phone;
    public String address;
    public String username;
    public boolean active;
    public String createdAt;

    public String getDisplayName() {
        return hospitalName != null ? hospitalName : username;
    }

    public String getStatusText() {
        return active ? "Active" : "Inactive";
    }
}
