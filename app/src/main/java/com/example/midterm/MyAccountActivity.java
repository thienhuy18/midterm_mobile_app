package com.example.midterm;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class MyAccountActivity extends AppCompatActivity {
    private static final int EDIT_INFO_REQUEST = 2;


    private ImageView profileImageView;
    private Button buttonChangeProfilePicture, buttonEditInfo;
    private TextView nameTextView, ageTextView, phoneTextView ;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private String[] avatarNames = {"avatar1", "avatar2", "avatar3", "avatar4","avatar5","avatar6","avatar7","avatar8","avatar9","avatar10"};
    private int[] avatarResIds = {R.drawable.avatar1, R.drawable.avatar2, R.drawable.avatar3, R.drawable.avatar4,R.drawable.avatar5,R.drawable.avatar6,R.drawable.avatar7,R.drawable.avatar8,R.drawable.avatar9,R.drawable.avatar10};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_account);

        profileImageView = findViewById(R.id.profileImageView);
        buttonChangeProfilePicture = findViewById(R.id.buttonChangeProfilePicture);
        nameTextView = findViewById(R.id.nameTextView);
        ageTextView = findViewById(R.id.ageTextView);
        phoneTextView = findViewById(R.id.phoneTextView);

        buttonEditInfo = findViewById(R.id.buttonEditInfo);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        loadUserProfile();

        checkUserRole();
        buttonEditInfo.setOnClickListener(v -> {
            Intent intent = new Intent(MyAccountActivity.this, EditInfoActivity.class);
            startActivityForResult(intent, EDIT_INFO_REQUEST);
        });

        buttonChangeProfilePicture.setOnClickListener(v -> showAvatarSelectionDialog());
    }

    private void loadUserProfile() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) return;

        String userId = currentUser.getUid();

        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String selectedAvatar = documentSnapshot.getString("selectedAvatar");
                        if (selectedAvatar != null) {
                            int avatarIndex = java.util.Arrays.asList(avatarNames).indexOf(selectedAvatar);
                            if (avatarIndex != -1) {
                                profileImageView.setImageResource(avatarResIds[avatarIndex]);
                            }
                        }

                        String name = documentSnapshot.getString("name");
                        Long age = documentSnapshot.getLong("age");
                        String phone = documentSnapshot.getString("phone");


                        nameTextView.setText(name != null ? name : "N/A");
                        ageTextView.setText(age != null ? String.valueOf(age) : "N/A");
                        phoneTextView.setText(phone != null ? phone : "N/A");

                    }
                })
                .addOnFailureListener(e -> Toast.makeText(MyAccountActivity.this, "Failed to load user information", Toast.LENGTH_SHORT).show());
    }

    private void showAvatarSelectionDialog() {

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_avatar_selection, null);
        RecyclerView avatarRecyclerView = dialogView.findViewById(R.id.avatarRecyclerView);

        AlertDialog avatarDialog = new AlertDialog.Builder(this)
                .setTitle("Select Avatar")
                .setView(dialogView)
                .setNegativeButton("Cancel", null)
                .create();

        // Set up the adapter with avatars and handle the click event to update profile
        AvatarAdapter adapter = new AvatarAdapter(avatarResIds, (avatarResId, avatarName) -> {
            saveSelectedAvatar(avatarName, avatarResId); // Update Firestore and UI with selected avatar
            avatarDialog.dismiss(); // Dismiss dialog after selecting avatar
        });

        avatarRecyclerView.setLayoutManager(new GridLayoutManager(this, 2)); // Set grid layout with 2 columns
        avatarRecyclerView.setAdapter(adapter); // Attach adapter to RecyclerView

        // Show the dialog after setting everything up
        avatarDialog.show();
    }



    private void saveSelectedAvatar(String avatarName, int avatarResId) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) return;

        String userId = currentUser.getUid();

        db.collection("users").document(userId)
                .update("selectedAvatar", avatarName)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(MyAccountActivity.this, "Profile picture updated", Toast.LENGTH_SHORT).show();
                    profileImageView.setImageResource(avatarResId);
                })
                .addOnFailureListener(e -> Toast.makeText(MyAccountActivity.this, "Failed to update profile picture: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == EDIT_INFO_REQUEST && resultCode == RESULT_OK) {
            loadUserProfile();
        }
    }
    private void checkUserRole() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) return;

        String userId = currentUser.getUid();

        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String role = documentSnapshot.getString("role");
                        if (role != null && role.equalsIgnoreCase("Employee")) {
                            buttonEditInfo.setVisibility(View.GONE); // Hide Edit Info for employees
                        }
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(MyAccountActivity.this, "Failed to check user role", Toast.LENGTH_SHORT).show());
    }

}
