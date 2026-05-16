package com.bloodbank.ui.model;

import java.util.List;
import java.util.Map;

public class DashboardStats {
    public long totalDonors;
    public long totalAvailableUnits;
    public long pendingRequests;
    public long totalDonations;
    public long totalHospitals;
    public long totalCamps;
    public long criticalRequests;
    public Map<String, Integer> stockByBloodGroup;
    public List<String> lowStockAlerts;
    public List<String> expiryAlerts;
    public List<String> emergencyAlerts;
    public List<Map<String, Object>> recentActivity;
}
