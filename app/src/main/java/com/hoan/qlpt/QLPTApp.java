package com.hoan.qlpt;

import android.app.Application;
import android.util.Log;

import com.cloudinary.android.MediaManager;

import java.util.HashMap;
import java.util.Map;

public class QLPTApp extends Application {

    private static final String TAG = "CloudinaryInit";

    @Override
    public void onCreate() {
        super.onCreate();

        try {
            Map<String, String> config = new HashMap<>();
            config.put("cloud_name", "m6siao0s");
            config.put("secure", "true");

            MediaManager.init(this, config);

            Log.d(TAG, "Khởi tạo Cloudinary thành công");

        } catch (IllegalStateException e) {
            Log.d(TAG, "Cloudinary đã được khởi tạo trước đó");
        }
    }
}
