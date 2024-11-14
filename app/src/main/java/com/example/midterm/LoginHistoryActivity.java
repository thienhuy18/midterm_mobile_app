package com.example.midterm;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class LoginHistoryActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private RecyclerView recyclerViewLoginHistory;
    private LoginHistoryAdapter loginHistoryAdapter;
    private List<LoginHistory> loginHistoryList = new ArrayList<>();
    private String userId;
    private String userEmail;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_history);

        db = FirebaseFirestore.getInstance();
        recyclerViewLoginHistory = findViewById(R.id.recyclerViewLoginHistory);
        recyclerViewLoginHistory.setLayoutManager(new LinearLayoutManager(this));

        loginHistoryAdapter = new LoginHistoryAdapter(loginHistoryList);
        recyclerViewLoginHistory.setAdapter(loginHistoryAdapter);
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            userEmail = currentUser.getEmail();
        }

        TextView emailMessageTextView = findViewById(R.id.emailHeaderTextView);
        if (userEmail != null) {
            emailMessageTextView.setText("Login history of " + userEmail);
        }


        userId = getIntent().getStringExtra("userId");

        loadLoginHistory();
    }

    private void loadLoginHistory() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String currentUserId = currentUser.getUid();
            Log.d("LoginHistory", "Current user ID: " + currentUserId);

            db.collection("login_history")
                    .whereEqualTo("userId", currentUserId)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            loginHistoryList.clear();
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

                            for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                Log.d("LoginHistory", "Document data: " + documentSnapshot.getData());

                                String documentUserId = documentSnapshot.getString("userId");
                                String email = documentSnapshot.getString("email");
                                Timestamp timestamp = documentSnapshot.getTimestamp("timestamp");

                                if (timestamp != null) {
                                    LoginHistory loginHistory = new LoginHistory(documentUserId, email, timestamp);
                                    loginHistoryList.add(loginHistory);
                                } else {
                                    Log.e("LoginHistory", "No valid timestamp available in document.");
                                }
                            }
                            loginHistoryAdapter.notifyDataSetChanged();
                            Log.d("LoginHistory", "Data loaded, adapter updated.");
                        } else {
                            Log.d("LoginHistory", "No data found for this user.");
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("LoginHistory", "Error loading login history", e);
                    });
        } else {
            Log.d("LoginHistory", "Current user is null.");
        }
    }


}
