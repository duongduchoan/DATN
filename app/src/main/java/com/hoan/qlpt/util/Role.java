package com.hoan.qlpt.util;

public class Role {
    public static final int ADMIN = 1;
    public static final int LANDLORD = 2;
    public static final int TENANT = 3;

    public static String name(int roleId) {
        switch (roleId) {
            case ADMIN: return "Admin";
            case LANDLORD: return "Chủ trọ";
            case TENANT: return "Người thuê";
            default: return "Chưa xác định";
        }
    }
}