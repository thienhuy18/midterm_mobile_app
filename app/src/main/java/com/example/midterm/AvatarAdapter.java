package com.example.midterm;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class AvatarAdapter extends RecyclerView.Adapter<AvatarAdapter.AvatarViewHolder> {

    private final int[] avatarResIds;
    private final OnAvatarClickListener listener;

    public interface OnAvatarClickListener {
        void onAvatarClick(int avatarResId, String avatarName);
    }

    public AvatarAdapter(int[] avatarResIds, OnAvatarClickListener listener) {
        this.avatarResIds = avatarResIds;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AvatarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_avatar, parent, false);
        return new AvatarViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AvatarViewHolder holder, int position) {
        int avatarResId = avatarResIds[position];
        holder.avatarImageView.setImageResource(avatarResId);
        holder.itemView.setOnClickListener(v -> listener.onAvatarClick(avatarResId, "avatar" + (position + 1)));
    }

    @Override
    public int getItemCount() {
        return avatarResIds.length;
    }

    public static class AvatarViewHolder extends RecyclerView.ViewHolder {
        ImageView avatarImageView;

        public AvatarViewHolder(@NonNull View itemView) {
            super(itemView);
            avatarImageView = itemView.findViewById(R.id.avatarImageView);
        }
    }
}
