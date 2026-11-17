package com.hoan.qlpt.util;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class Notifier {

    public static void send(String userId, String title, String body) {
        send(userId, title, body, "", "");
    }

    // type + refId để bấm vào thông báo thì mở đúng màn hình
    public static void send(String userId, String title, String body, String type, String refId) {
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
        m.put("type", type == null ? "" : type);
        m.put("refId", refId == null ? "" : refId);
        ref.set(m);
    }
}