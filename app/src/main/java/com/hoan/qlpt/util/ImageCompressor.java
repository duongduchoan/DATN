package com.hoan.qlpt.util;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ImageCompressor {

    private static final int MAX_SIDE = 1280;
    private static final int QUALITY = 80;

    private static InputStream open(ContentResolver cr, Uri uri) throws IOException {
        InputStream in = cr.openInputStream(uri);
        if (in == null) throw new IOException("Không mở được ảnh");
        return in;
    }

    // Gọi trên luồng nền. Trả về file JPEG đã nén trong thư mục cache.
    public static File compress(Context ctx, Uri uri) throws IOException {
        ContentResolver cr = ctx.getContentResolver();

        // 1) Đọc kích thước, chưa nạp ảnh vào bộ nhớ
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        try (InputStream in = open(cr, uri)) {
            BitmapFactory.decodeStream(in, null, bounds);
        }
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) throw new IOException("Không đọc được ảnh");

        // 2) Giải mã thu nhỏ theo lũy thừa của 2 để đỡ tốn RAM
        int sample = 1;
        while (Math.max(bounds.outWidth, bounds.outHeight) / (sample * 2) >= MAX_SIDE) sample *= 2;
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inSampleSize = sample;
        Bitmap bmp;
        try (InputStream in = open(cr, uri)) {
            bmp = BitmapFactory.decodeStream(in, null, opt);
        }
        if (bmp == null) throw new IOException("Không giải mã được ảnh");

        // 3) Thu nhỏ chính xác và xoay theo EXIF
        int w = bmp.getWidth(), h = bmp.getHeight();
        float scale = Math.min(1f, (float) MAX_SIDE / Math.max(w, h));

        int degrees = 0;
        try (InputStream in = open(cr, uri)) {
            int ori = new ExifInterface(in).getAttributeInt(
                    ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            if (ori == ExifInterface.ORIENTATION_ROTATE_90) degrees = 90;
            else if (ori == ExifInterface.ORIENTATION_ROTATE_180) degrees = 180;
            else if (ori == ExifInterface.ORIENTATION_ROTATE_270) degrees = 270;
        } catch (Exception ignored) { }

        Matrix m = new Matrix();
        m.postScale(scale, scale);
        m.postRotate(degrees);
        Bitmap out = Bitmap.createBitmap(bmp, 0, 0, w, h, m, true);
        if (out != bmp) bmp.recycle();

        // 4) Ghi ra file JPEG
        File f = File.createTempFile("up_", ".jpg", ctx.getCacheDir());
        try (FileOutputStream fos = new FileOutputStream(f)) {
            out.compress(Bitmap.CompressFormat.JPEG, QUALITY, fos);
        }
        out.recycle();
        return f;
    }
}