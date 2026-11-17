package com.hoan.qlpt.util;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class ImageUploader {

    public interface Progress {
        void on(int done, int total);
    }

    public interface Done {
        void on(List<String> urls);
    }

    public interface Fail {
        void on(String message);
    }

    private static final String PRESET = "qlpt_unsigned";
    private static final ExecutorService POOL = Executors.newFixedThreadPool(3);
    private static final Handler MAIN = new Handler(Looper.getMainLooper());

    // Nén rồi tải song song nhiều ảnh. Danh sách URL trả về đúng thứ tự ảnh đã chọn.
    // progress có thể null. Mọi callback chạy trên luồng chính.
    public static void uploadAll(Context ctx, List<Uri> uris, Progress progress, Done done, Fail fail) {
        final int total = uris.size();
        if (total == 0) {
            done.on(new ArrayList<>());
            return;
        }

        final Context app = ctx.getApplicationContext();
        final String[] urls = new String[total];
        final AtomicInteger finished = new AtomicInteger(0);
        final AtomicBoolean failed = new AtomicBoolean(false);
        if (progress != null) progress.on(0, total);

        for (int i = 0; i < total; i++) {
            final int idx = i;
            final Uri uri = uris.get(i);
            POOL.execute(() -> {
                final File file;
                try {
                    file = ImageCompressor.compress(app, uri);
                } catch (Exception e) {
                    failNow(failed, fail, "Không xử lý được ảnh: " + e.getMessage());
                    return;
                }
                // Gọi Cloudinary từ luồng chính cho an toàn
                MAIN.post(() -> MediaManager.get().upload(file.getAbsolutePath())
                        .unsigned(PRESET)
                        .callback(new UploadCallback() {
                            @Override
                            public void onStart(String id) {
                            }

                            @Override
                            public void onProgress(String id, long b, long t) {
                            }

                            @Override
                            public void onSuccess(String id, Map r) {
                                file.delete();
                                MAIN.post(() -> {
                                    urls[idx] = (String) r.get("secure_url");
                                    int n = finished.incrementAndGet();
                                    if (failed.get()) return;
                                    if (progress != null) progress.on(n, total);
                                    if (n == total) done.on(new ArrayList<>(Arrays.asList(urls)));
                                });
                            }

                            @Override
                            public void onError(String id, ErrorInfo e) {
                                file.delete();
                                failNow(failed, fail, e.getDescription());
                            }

                            @Override
                            public void onReschedule(String id, ErrorInfo e) {
                                file.delete();
                                failNow(failed, fail, e.getDescription());
                            }
                        })
                        .dispatch());
            });
        }
    }

    private static void failNow(AtomicBoolean failed, Fail fail, String msg) {
        if (failed.compareAndSet(false, true)) MAIN.post(() -> fail.on(msg));
    }
}