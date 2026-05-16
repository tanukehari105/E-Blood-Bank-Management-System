package com.bloodbank.ui.util;

/**
 * Session manager — stores authenticated user state including hospital support.
 */
public class SessionManager {

    private static String token;
    private static String username;
    private static String role;
    private static String fullName;
    private static Long hospitalId;
    private static String hospitalName;

    public static void setSession(String token, String username, String role, String fullName) {
        SessionManager.token = token;
        SessionManager.username = username;
        SessionManager.role = role;
        SessionManager.fullName = fullName;
        SessionManager.hospitalId = null;
        SessionManager.hospitalName = null;
    }

    public static void setHospitalSession(String token, String username, String hospitalName, Long hospitalId) {
        SessionManager.token = token;
        SessionManager.username = username;
        SessionManager.role = "HOSPITAL";
        SessionManager.fullName = hospitalName;
        SessionManager.hospitalId = hospitalId;
        SessionManager.hospitalName = hospitalName;
    }

    public static void clearSession() {
        token = null;
        username = null;
        role = null;
        fullName = null;
        hospitalId = null;
        hospitalName = null;
    }

    public static String getToken() { return token; }
    public static String getUsername() { return username; }
    public static String getRole() { return role; }
    public static String getFullName() { return fullName; }
    public static Long getHospitalId() { return hospitalId; }
    public static String getHospitalName() { return hospitalName; }

    public static boolean isAdmin() { return "ADMIN".equals(role); }
    public static boolean isStaff() { return "STAFF".equals(role); }
    public static boolean isHospital() { return "HOSPITAL".equals(role); }
    public static boolean isAdminOrStaff() { return isAdmin() || isStaff(); }
}
