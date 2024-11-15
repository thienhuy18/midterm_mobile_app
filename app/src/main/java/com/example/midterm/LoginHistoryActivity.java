package com.example.midterm;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
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
    private FirebaseAuth auth;
    private RecyclerView recyclerViewLoginHistory;
    private LoginHistoryAdapter loginHistoryAdapter;
    private List<LoginHistory> loginHistoryList = new ArrayList<>();
    private String userId;
    private String userEmail;
    private ImageView profileImageView;
    private String[] avatarNames = {"avatar1", "avatar2", "avatar3", "avatar4","avatar5","avatar6","avatar7","avatar8","avatar9","avatar10"};
    private int[] avatarResIds = {R.drawable.avatar1, R.drawable.avatar2, R.drawable.avatar3, R.drawable.avatar4, R.drawable.avatar5, R.drawable.avatar6, R.drawable.avatar7, R.drawable.avatar8, R.drawable.avatar9, R.drawable.avatar10};




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
            emailMessageTextView.setText("My login history:  " + userEmail);
        }


        userId = getIntent().getStringExtra("userId");

        loadLoginHistory();
        auth = FirebaseAuth.getInstance();

        profileImageView = findViewById(R.id.profileImageView);


        loadUserAvatar();
        listenForAvatarChanges();
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
    private void loadUserAvatar() {
        String userId = auth.getCurrentUser().getUid(); // Get the current user's ID

        // Fetch the user's avatar from Firestore (assuming 'users' is your collection)
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Retrieve the selected avatar name (e.g., "avatar10")
                        String selectedAvatar = documentSnapshot.getString("selectedAvatar");

                        if (selectedAvatar != null && !selectedAvatar.isEmpty()) {
                            // Dynamically load the avatar image based on the selected avatar name
                            int resID = getResources().getIdentifier(selectedAvatar, "drawable", getPackageName());

                            if (resID != 0) { // Check if the resource ID is valid
                                profileImageView.setImageResource(resID);
                            } else {
                                // If the resource is not found, set the default avatar
                                profileImageView.setImageResource(R.drawable.ic_default_profile);
                            }
                        } else {
                            // If no avatar is selected, use the default avatar
                            profileImageView.setImageResource(R.drawable.ic_default_profile);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle failure (e.g., network issue, missing data)
                    profileImageView.setImageResource(R.drawable.ic_default_profile);
                });
    }

    private void listenForAvatarChanges() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) return;

        String userId = currentUser.getUid();

        // Listen for changes in the user's avatar
        db.collection("users").document(userId)
                .addSnapshotListener((documentSnapshot, e) -> {
                    if (e != null) {
                        Log.w("MenuActivity", "Listen failed.", e);
                        return;
                    }

                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        String selectedAvatar = documentSnapshot.getString("selectedAvatar");
                        if (selectedAvatar != null) {
                            // Get the corresponding resource ID for the avatar
                            int avatarIndex = java.util.Arrays.asList(avatarNames).indexOf(selectedAvatar);
                            if (avatarIndex != -1) {
                                profileImageView.setImageResource(avatarResIds[avatarIndex]); // Update the image
                            }
                        }
                    }
                });
    }


}
