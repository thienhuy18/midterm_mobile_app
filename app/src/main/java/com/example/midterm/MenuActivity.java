package com.example.midterm;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

public class MenuActivity extends AppCompatActivity {


    private Button buttonUserManagement, buttonViewLoginHistory, buttonLogout, buttonStudentManagement,buttonMyAccount;
    private FirebaseAuth auth;
    private ImageView profileImageView; // Declare ImageView for profile picture
    private FirebaseFirestore db;

    private String[] avatarNames = {"avatar1", "avatar2", "avatar3", "avatar4","avatar5","avatar6","avatar7","avatar8","avatar9","avatar10"};
    private int[] avatarResIds = {R.drawable.avatar1, R.drawable.avatar2, R.drawable.avatar3, R.drawable.avatar4, R.drawable.avatar5, R.drawable.avatar6, R.drawable.avatar7, R.drawable.avatar8, R.drawable.avatar9, R.drawable.avatar10};




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_menu);

        db = FirebaseFirestore.getInstance();

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        buttonMyAccount = findViewById(R.id.buttonMyAccount);
        buttonUserManagement = findViewById(R.id.buttonUserManagement);
        buttonStudentManagement = findViewById(R.id.buttonStudentManagement);
        buttonViewLoginHistory = findViewById(R.id.buttonViewLoginHistory);
        buttonLogout = findViewById(R.id.buttonLogout);

        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser != null) {
            String userId = currentUser.getUid();
            db.collection("users").document(userId).get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    String role = documentSnapshot.getString("role");
                    adjustButtonVisibility(role);
                } else {
                    Toast.makeText(MenuActivity.this, "User data not found", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(e -> {
                Toast.makeText(MenuActivity.this, "Failed to fetch user data", Toast.LENGTH_SHORT).show();
            });
        }

        buttonUserManagement.setOnClickListener(v -> {
            Intent intent = new Intent(MenuActivity.this, MainActivity.class);
            startActivity(intent);
        });

        buttonViewLoginHistory.setOnClickListener(v -> {
            Intent intent = new Intent(MenuActivity.this, LoginHistoryActivity.class);
            startActivity(intent);
        });

        buttonStudentManagement.setOnClickListener(v -> {
            Intent intent = new Intent(MenuActivity.this, StudentManagementActivity.class);
            startActivity(intent);
        });

        buttonMyAccount.setOnClickListener(v -> {
            Intent intent = new Intent(MenuActivity.this, MyAccountActivity.class);
            startActivity(intent);
        });

        buttonLogout.setOnClickListener(v -> {
            auth.signOut();
            Toast.makeText(MenuActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(MenuActivity.this, LoginActivity.class));
            finish();
        });
        profileImageView = findViewById(R.id.profileImageView);

        loadUserAvatar();
        listenForAvatarChanges();

    }

    private void adjustButtonVisibility(String role) {
        if ("Admin".equals(role)) {
            // Admin can see all buttons
            buttonUserManagement.setVisibility(View.VISIBLE);
            buttonViewLoginHistory.setVisibility(View.VISIBLE);
            buttonStudentManagement.setVisibility(View.VISIBLE);
            buttonMyAccount.setVisibility(View.VISIBLE);
            buttonLogout.setVisibility(View.VISIBLE);
        } else if ("User".equals(role)) {
            // User can see all buttons except User Management and Login History
            buttonUserManagement.setVisibility(View.GONE);
            buttonViewLoginHistory.setVisibility(View.GONE);
            buttonStudentManagement.setVisibility(View.VISIBLE);
            buttonMyAccount.setVisibility(View.VISIBLE);
            buttonLogout.setVisibility(View.VISIBLE);
        } else if ("Student".equals(role)) {
            // Student can only see My Account and Logout
            buttonUserManagement.setVisibility(View.GONE);
            buttonViewLoginHistory.setVisibility(View.GONE);
            buttonStudentManagement.setVisibility(View.GONE);
            buttonMyAccount.setVisibility(View.VISIBLE);
            buttonLogout.setVisibility(View.VISIBLE);
        } else {
            // If role is null, only show My Account and Logout buttons
            buttonUserManagement.setVisibility(View.GONE);
            buttonViewLoginHistory.setVisibility(View.GONE);
            buttonStudentManagement.setVisibility(View.GONE);
            buttonMyAccount.setVisibility(View.VISIBLE);
            buttonLogout.setVisibility(View.VISIBLE);
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
