package com.hoan.qlpt.adapter;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.hoan.qlpt.R;

import java.util.List;

public class ImagePreviewAdapter extends RecyclerView.Adapter<ImagePreviewAdapter.VH> {

    public interface OnRemove {
        void onRemove(int position);
    }

    private final List<Uri> uris;
    private final OnRemove onRemove;

    public ImagePreviewAdapter(List<Uri> uris, OnRemove onRemove) {
        this.uris = uris;
        this.onRemove = onRemove;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_image_preview, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Glide.with(h.img).load(uris.get(position)).centerCrop().into(h.img);
        h.btnRemove.setVisibility(onRemove == null ? View.GONE : View.VISIBLE);
        h.btnRemove.setOnClickListener(v -> {
            int p = h.getBindingAdapterPosition();
            if (p != RecyclerView.NO_POSITION && onRemove != null) onRemove.onRemove(p);
        });
    }

    @Override
    public int getItemCount() {
        return uris.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView img;
        View btnRemove;

        VH(View v) {
            super(v);
            img = v.findViewById(R.id.img);
            btnRemove = v.findViewById(R.id.btnRemove);
        }
    }
}