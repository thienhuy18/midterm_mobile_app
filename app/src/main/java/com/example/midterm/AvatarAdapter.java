package com.example.midterm;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AvatarAdapter extends RecyclerView.Adapter<AvatarAdapter.ViewHolder> {

    private final List<Integer> avatarDrawableIds;
    private final Context context;
    private final AvatarSelectListener avatarSelectListener;

    public AvatarAdapter(List<Integer> avatarDrawableIds, Context context, AvatarSelectListener avatarSelectListener) {
        this.avatarDrawableIds = avatarDrawableIds;
        this.context = context;
        this.avatarSelectListener = avatarSelectListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_avatar, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        int avatarDrawableId = avatarDrawableIds.get(position);
        holder.avatarImageView.setImageResource(avatarDrawableId);
        holder.itemView.setOnClickListener(v -> avatarSelectListener.onAvatarSelected(avatarDrawableId));
    }

    @Override
    public int getItemCount() {
        return avatarDrawableIds.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView avatarImageView;

        public ViewHolder(View itemView) {
            super(itemView);
            avatarImageView = itemView.findViewById(R.id.avatarImageView);
        }
    }

    public interface AvatarSelectListener {
        void onAvatarSelected(int avatarDrawableId);
    }
}
