package com.hoan.qlpt.util;

import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.DocumentsContract;
import android.provider.MediaStore;

import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Bộ chọn ảnh từ thư viện. Đầu vào: số ảnh tối đa. Đầu ra: danh sách Uri đã chọn.
 * Máy có Photo Picker thì dùng trình chọn chuẩn; máy cũ thì dùng bộ chọn tệp chỉ lấy ảnh trong máy.
 */
public class LocalImagePicker extends ActivityResultContract<Integer, List<Uri>> {

    // false: trình duyệt tệp hệ thống, mở thẳng thư mục Pictures
    // true: mở thẳng ứng dụng Thư viện ảnh của máy
    private static final boolean USE_GALLERY_APP = false;

    private int max = 1;

    @NonNull
    @Override
    public Intent createIntent(@NonNull Context context, Integer maxItems) {
        max = (maxItems == null || maxItems < 1) ? 1 : maxItems;

        // Máy đời cao có Photo Picker: dùng trình chọn ảnh chuẩn của hệ thống
        if (ActivityResultContracts.PickVisualMedia.isPhotoPickerAvailable(context)) {
            PickVisualMediaRequest req = new PickVisualMediaRequest.Builder()
                    .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                    .build();
            return max > 1
                    ? new ActivityResultContracts.PickMultipleVisualMedia(max).createIntent(context, req)
                    : new ActivityResultContracts.PickVisualMedia().createIntent(context, req);
        }

        // Máy cũ (LDPlayer Android 9...): chỉ lấy ảnh trong máy
        Intent i;
        if (USE_GALLERY_APP) {
            i = new Intent(Intent.ACTION_PICK);
            i.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        } else {
            i = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            i.addCategory(Intent.CATEGORY_OPENABLE);
            i.setType("image/*");
            if (Build.VERSION.SDK_INT >= 26) {
                i.putExtra(DocumentsContract.EXTRA_INITIAL_URI, Uri.parse(
                        "content://com.android.externalstorage.documents/document/primary%3APictures"));
            }
        }
        i.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        if (max > 1) i.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        return i;
    }

    @Override
    public List<Uri> parseResult(int resultCode, @Nullable Intent intent) {
        List<Uri> out = new ArrayList<>();
        if (resultCode != Activity.RESULT_OK || intent == null) return out;

        ClipData clip = intent.getClipData();
        if (clip != null) {
            for (int i = 0; i < clip.getItemCount() && out.size() < max; i++) {
                Uri u = clip.getItemAt(i).getUri();
                if (u != null) out.add(u);
            }
        } else if (intent.getData() != null) {
            out.add(intent.getData());
        }
        return out;
    }
}
