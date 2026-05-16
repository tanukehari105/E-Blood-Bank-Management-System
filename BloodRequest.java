package com.bloodbank.ui.model;

public class BloodRequest {
    public Long id;
    public String patientName;
    public String hospital;
    public Long hospitalId;
    public String bloodGroup;
    public int units;
    public int approvedUnits;
    public String status;
    public String urgencyLevel;
    public String requestDate;
    public String processedDate;
    public String notes;
    public String contactNumber;

    public boolean isCritical() { return "CRITICAL".equals(urgencyLevel); }
    public boolean isUrgent() { return "URGENT".equals(urgencyLevel); }
    public boolean isPending() { return "PENDING".equals(status); }
    public boolean isApproved() { return "APPROVED".equals(status) || "PARTIALLY_APPROVED".equals(status); }
}
