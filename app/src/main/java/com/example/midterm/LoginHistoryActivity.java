package com.example.midterm;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_history);

        db = FirebaseFirestore.getInstance();
        recyclerViewLoginHistory = findViewById(R.id.recyclerViewLoginHistory);
        recyclerViewLoginHistory.setLayoutManager(new LinearLayoutManager(this));

        loginHistoryAdapter = new LoginHistoryAdapter(loginHistoryList);
        recyclerViewLoginHistory.setAdapter(loginHistoryAdapter);

        // Get userId from Intent
        userId = getIntent().getStringExtra("userId");

        loadLoginHistory();
    }

    private void loadLoginHistory() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String currentUserId = currentUser.getUid();  // Get the current user's ID
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

                                // Get the timestamp field from Firestore
                                Object timestampObj = documentSnapshot.get("timestamp");
                                Timestamp timestamp = null;

                                // Check the type of the timestamp field and handle accordingly
                                if (timestampObj instanceof Timestamp) {
                                    // If it's already a Timestamp, use it directly
                                    timestamp = (Timestamp) timestampObj;
                                } else if (timestampObj instanceof Long) {
                                    // If it's stored as a Long (milliseconds), convert it to a Timestamp
                                    timestamp = new Timestamp(new Date((Long) timestampObj));
                                }

                                if (timestamp != null) {
                                    // Add LoginHistory with the Timestamp object
                                    LoginHistory loginHistory = new LoginHistory(documentUserId, timestamp);
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
