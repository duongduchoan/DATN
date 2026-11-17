package com.hoan.qlpt.adapter;

import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class ImageSliderAdapter extends RecyclerView.Adapter<ImageSliderAdapter.VH> {

    private final List<String> urls;

    public ImageSliderAdapter(List<String> urls) {
        this.urls = urls;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ImageView iv = new ImageView(parent.getContext());
        iv.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
        return new VH(iv);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Glide.with(h.img).load(com.hoan.qlpt.util.ImageUrl.thumb(urls.get(position), 1080)).centerCrop().into(h.img);
    }

    @Override
    public int getItemCount() {
        return urls.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView img;

        VH(ImageView v) {
            super(v);
            img = v;
        }
    }
}