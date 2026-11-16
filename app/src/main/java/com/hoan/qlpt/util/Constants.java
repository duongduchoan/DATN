package com.hoan.qlpt.util;

public class Constants {
    // Firestore Collection Names
    public static final String COLLECTION_USERS = "users";
    public static final String COLLECTION_ROOMS = "rooms";
    public static final String COLLECTION_APPOINTMENTS = "appointments";
    public static final String COLLECTION_REVIEWS = "reviews";
    public static final String COLLECTION_CONTRACTS = "contracts";
    public static final String COLLECTION_PAYMENTS = "payments";
    public static final String COLLECTION_NOTIFICATIONS = "notifications";

    // User Roles
    public static final String ROLE_GUEST = "guest";
    public static final String ROLE_TENANT = "tenant";
    public static final String ROLE_LANDLORD = "landlord";
    public static final String ROLE_ADMIN = "admin";

    // Room Status
    public static final String ROOM_PENDING = "pending";
    public static final String ROOM_APPROVED = "approved";
    public static final String ROOM_REJECTED = "rejected";
    public static final String ROOM_RENTED = "rented";

    // Appointment Status
    public static final String APPOINTMENT_PENDING = "pending";
    public static final String APPOINTMENT_APPROVED = "approved";
    public static final String APPOINTMENT_REJECTED = "rejected";
    public static final String APPOINTMENT_COMPLETED = "completed";

    // Contract Status
    public static final String CONTRACT_ACTIVE = "active";
    public static final String CONTRACT_EXPIRED = "expired";
    public static final String CONTRACT_TERMINATED = "terminated";

    // Payment Status
    public static final String PAYMENT_PENDING = "pending";
    public static final String PAYMENT_PAID = "paid";
    public static final String PAYMENT_OVERDUE = "overdue";

    // Notification Types
    public static final String NOTIF_APPOINTMENT = "appointment";
    public static final String NOTIF_PAYMENT = "payment";
    public static final String NOTIF_SYSTEM = "system";
    public static final String NOTIF_REVIEW = "review";

    // Room Types
    public static final String TYPE_PHONG_TRO = "phong_tro";
    public static final String TYPE_CHUNG_CU_MINI = "chung_cu_mini";
    public static final String TYPE_NHA_NGUYEN_CAN = "nha_nguyen_can";

    // SharedPreferences
    public static final String PREF_NAME = "qlpt_prefs";
    public static final String PREF_USER_UID = "user_uid";
    public static final String PREF_USER_ROLE = "user_role";
    public static final String PREF_USER_NAME = "user_name";
    public static final String PREF_IS_LOGGED_IN = "is_logged_in";
}
