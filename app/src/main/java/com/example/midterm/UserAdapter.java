package com.example.midterm;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private List<User> userList;
    private OnUserDeleteClickListener onUserDeleteClickListener;
    private OnUserEditClickListener onUserEditClickListener;

    public UserAdapter(List<User> userList, OnUserDeleteClickListener onUserDeleteClickListener, OnUserEditClickListener onUserEditClickListener) {
        this.userList = userList;
        this.onUserDeleteClickListener = onUserDeleteClickListener;
        this.onUserEditClickListener = onUserEditClickListener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);
        holder.name.setText(user.getName());
        holder.age.setText(String.valueOf(user.getAge()));
        holder.phone.setText(user.getPhone());
        holder.status.setText(user.getStatus());

        holder.deleteButton.setOnClickListener(v -> {
            if (onUserDeleteClickListener != null) {
                onUserDeleteClickListener.onUserDeleteClick(user);
            }
        });

        holder.editButton.setOnClickListener(v -> {  // Set up edit button listener
            if (onUserEditClickListener != null) {
                onUserEditClickListener.onUserEditClick(user);
            }
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView name, age, phone, status;
        Button deleteButton, editButton;  // Add edit button

        public UserViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.userName);
            age = itemView.findViewById(R.id.userAge);
            phone = itemView.findViewById(R.id.userPhone);
            status = itemView.findViewById(R.id.userStatus);
            deleteButton = itemView.findViewById(R.id.deleteButton);
            editButton = itemView.findViewById(R.id.editButton);  // Initialize edit button
        }
    }

    public interface OnUserDeleteClickListener {
        void onUserDeleteClick(User user);
    }

    public interface OnUserEditClickListener {  // New interface for editing
        void onUserEditClick(User user);
    }
}
