package com.example.midterm;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class LoginHistoryAdapter extends RecyclerView.Adapter<LoginHistoryAdapter.LoginHistoryViewHolder> {

    private List<LoginHistory> loginHistoryList;

    public LoginHistoryAdapter(List<LoginHistory> loginHistoryList) {
        this.loginHistoryList = loginHistoryList;
    }

    @Override
    public LoginHistoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_login_history, parent, false);
        return new LoginHistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(LoginHistoryViewHolder holder, int position) {
        LoginHistory loginHistory = loginHistoryList.get(position);


        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String formattedTimestamp = sdf.format(loginHistory.getTimestamp().toDate());



        holder.timestampTextView.setText(formattedTimestamp);
    }

    @Override
    public int getItemCount() {
        return loginHistoryList.size();
    }

    public static class LoginHistoryViewHolder extends RecyclerView.ViewHolder {
        TextView userIdTextView;
        TextView timestampTextView;

        public LoginHistoryViewHolder(View itemView) {
            super(itemView);

            timestampTextView = itemView.findViewById(R.id.timestampTextView);
        }
    }
}