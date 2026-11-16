package com.hoan.qlpt.util;

public class ImageUrl {

    // Chèn phép biến đổi vào link Cloudinary; link khác thì trả nguyên
    public static String thumb(String url, int widthPx) {
        if (url == null || !url.contains("res.cloudinary.com")) return url;
        int i = url.indexOf("/upload/");
        if (i < 0) return url;
        int cut = i + "/upload/".length();
        return url.substring(0, cut) + "f_auto,q_auto,c_limit,w_" + widthPx + "/" + url.substring(cut);
    }
}
