package com.example.midterm;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

public class MyAccountActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int EDIT_INFO_REQUEST = 2;

    private ImageView profileImageView;
    private Button buttonChangeProfilePicture,buttonEditInfo;
    private TextView nameTextView, ageTextView, phoneTextView, statusTextView;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private StorageReference storageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_account);


        profileImageView = findViewById(R.id.profileImageView);
        buttonChangeProfilePicture = findViewById(R.id.buttonChangeProfilePicture);
        nameTextView = findViewById(R.id.nameTextView);
        ageTextView = findViewById(R.id.ageTextView);
        phoneTextView = findViewById(R.id.phoneTextView);
        statusTextView = findViewById(R.id.statusTextView);
        buttonEditInfo = findViewById(R.id.buttonEditInfo);


        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference("profile_pictures");

        loadUserProfile();
        buttonEditInfo.setOnClickListener(v -> {
            Intent intent = new Intent(MyAccountActivity.this, EditInfoActivity.class);
            startActivityForResult(intent, EDIT_INFO_REQUEST);

        });

        buttonChangeProfilePicture.setOnClickListener(v -> openImagePicker());


    }

    private void loadUserProfile() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) return;


        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String originalUserId = prefs.getString("originalUserId", null);


        String userIdToLoad = (originalUserId != null) ? originalUserId : currentUser.getUid();

        db.collection("users").document(userIdToLoad).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {

                        if (documentSnapshot.contains("profilePictureUrl")) {
                            String profilePictureUrl = documentSnapshot.getString("profilePictureUrl");
                            Picasso.get().load(profilePictureUrl).into(profileImageView);
                        }


                        String name = documentSnapshot.getString("name");
                        Long age = documentSnapshot.getLong("age");
                        String phone = documentSnapshot.getString("phone");
                        String status = documentSnapshot.getString("status");

                        nameTextView.setText(name != null ? name : "N/A");
                        ageTextView.setText(age != null ? String.valueOf(age) : "N/A");
                        phoneTextView.setText(phone != null ? phone : "N/A");
                        statusTextView.setText(status != null ? status : "N/A");
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(MyAccountActivity.this, "Failed to load user information", Toast.LENGTH_SHORT).show());
    }


    private void openImagePicker() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == EDIT_INFO_REQUEST && resultCode == RESULT_OK) {

            loadUserProfile();
        }

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            uploadProfilePicture(imageUri);
        }
    }

    private void uploadProfilePicture(Uri imageUri) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) return;

        String userId = currentUser.getUid();
        StorageReference fileRef = storageRef.child(userId + ".jpg");

        fileRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    saveProfilePictureUri(uri.toString());
                }))
                .addOnFailureListener(e -> Toast.makeText(MyAccountActivity.this, "Failed to upload image", Toast.LENGTH_SHORT).show());
    }

    private void saveProfilePictureUri(String downloadUrl) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) return;

        String userId = currentUser.getUid();

        db.collection("users").document(userId)
                .update("profilePictureUrl", downloadUrl)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(MyAccountActivity.this, "Profile picture updated", Toast.LENGTH_SHORT).show();
                    Picasso.get().load(downloadUrl).into(profileImageView);
                })
                .addOnFailureListener(e -> Toast.makeText(MyAccountActivity.this, "Failed to update profile picture URL", Toast.LENGTH_SHORT).show());
    }

}
