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
        holder.name.setText("Name: "+user.getName());
        holder.age.setText(String.valueOf("Age: "+user.getAge()));
        holder.phone.setText("Phone: "+user.getPhone());
        holder.status.setText("Status: "+user.getStatus());
        holder.role.setText("Role: "+user.getRole());
        holder.deleteButton.setOnClickListener(v -> {
            if (onUserDeleteClickListener != null) {
                onUserDeleteClickListener.onUserDeleteClick(user);
            }
        });

        holder.editButton.setOnClickListener(v -> {
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
        TextView name, age, phone, status, role;
        Button deleteButton, editButton;

        public UserViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.userName);
            age = itemView.findViewById(R.id.userAge);
            phone = itemView.findViewById(R.id.userPhone);
            status = itemView.findViewById(R.id.userStatus);
            role = itemView.findViewById(R.id.role);
            deleteButton = itemView.findViewById(R.id.deleteButton);
            editButton = itemView.findViewById(R.id.editButton);
        }
    }

    public interface OnUserDeleteClickListener {
        void onUserDeleteClick(User user);
    }

    public interface OnUserEditClickListener {
        void onUserEditClick(User user);
    }
}
