package com.hoan.qlpt.util;

import java.util.List;

public class Amenities {

    public static final String[] ALL = {"Giường", "Nệm", "Tủ quần áo", "Thang máy", "Wifi",
            "Máy lạnh", "Kệ bếp", "Nước nóng", "Tủ lạnh", "Gác"};

    // Dữ liệu cũ lưu "Điều hòa", "Gác lửng": quy về tên mới
    public static String canon(String s) {
        if ("Điều hòa".equals(s)) return "Máy lạnh";
        if ("Gác lửng".equals(s)) return "Gác";
        return s;
    }

    public static boolean has(List<String> list, String name) {
        if (list == null) return false;
        for (String x : list) if (name.equals(canon(x))) return true;
        return false;
    }
}