package com.hoan.qlpt.util;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.Toast;

import androidx.activity.ComponentActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

/** Cho người dùng chọn nguồn ảnh: chụp bằng camera hoặc chọn từ thư viện. Phải tạo trong onCreate. */
public class ImageSourceHelper {

    public interface OnImages { void onImages(List<Uri> uris); }

    // Giữ qua thay đổi cấu hình (xoay màn hình) khi camera đang mở
    private static Uri pendingPhoto;

    private final ComponentActivity activity;
    private final int maxImages;
    private final ActivityResultLauncher<Integer> gallery;
    private final ActivityResultLauncher<Uri> camera;

    public ImageSourceHelper(ComponentActivity activity, int maxImages, OnImages callback) {
        this.activity = activity;
        this.maxImages = maxImages;
        gallery = activity.registerForActivityResult(new LocalImagePicker(), callback::onImages);
        camera = activity.registerForActivityResult(new ActivityResultContracts.TakePicture(), ok -> {
            Uri photo = pendingPhoto;
            pendingPhoto = null;
            if (Boolean.TRUE.equals(ok) && photo != null) {
                callback.onImages(Collections.singletonList(photo));
            }
        });
    }

    public void show() {
        if (!cameraAvailable()) {
            gallery.launch(maxImages);
            return;
        }
        new AlertDialog.Builder(activity)
            .setTitle("Chọn ảnh")
            .setItems(new String[]{"Chụp ảnh", "Chọn từ thư viện"}, (d, which) -> {
                if (which == 0) openCamera(); else gallery.launch(maxImages);
            })
            .setNegativeButton("Hủy", null)
            .show();
    }

    private boolean cameraAvailable() {
        PackageManager pm = activity.getPackageManager();
        return pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)
                && new Intent(MediaStore.ACTION_IMAGE_CAPTURE).resolveActivity(pm) != null;
    }

    private void openCamera() {
        try {
            File dir = new File(activity.getCacheDir(), "camera");
            if (!dir.exists() && !dir.mkdirs()) throw new IOException("Không tạo được thư mục");
            File file = File.createTempFile("cam_", ".jpg", dir);
            pendingPhoto = FileProvider.getUriForFile(activity,
                    activity.getPackageName() + ".fileprovider", file);
            camera.launch(pendingPhoto);
        } catch (Exception e) {
            Toast.makeText(activity, "Không mở được camera: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }
}
