package com.hoan.qlpt.util;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class Notifier {

    // Gửi và quên, nếu lỗi thì bỏ qua để không làm hỏng luồng chính
    public static void send(String userId, String title, String body) {
        if (userId == null) return;
        DocumentReference ref = FirebaseFirestore.getInstance()
                .collection("notifications").document();
        Map<String, Object> m = new HashMap<>();
        m.put("id", ref.getId());
        m.put("userId", userId);
        m.put("title", title);
        m.put("body", body);
        m.put("read", false);
        m.put("createdAt", System.currentTimeMillis());
        ref.set(m);
    }
}