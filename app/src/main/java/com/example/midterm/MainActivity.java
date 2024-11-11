package com.example.midterm;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements UserAdapter.OnUserDeleteClickListener, UserAdapter.OnUserEditClickListener {

    private static final int PICK_IMAGE_REQUEST = 1;

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private StorageReference storageRef;

    private EditText inputName, inputAge, inputPhone;
    private Spinner statusSpinner;
    private ImageView profileImageView;
    private Button buttonAddUser, buttonLogout, buttonChangeProfilePicture, buttonViewLoginHistory;
    private RecyclerView recyclerViewUsers;
    private UserAdapter userAdapter;
    private List<User> userList = new ArrayList<>();
    private ListenerRegistration userListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference("profile_pictures");

        if (auth.getCurrentUser() == null) {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
            return;
        }

        initializeViews();
        loadUserProfilePicture();

        buttonAddUser.setOnClickListener(v -> addUser());
        buttonLogout.setOnClickListener(v -> logoutUser());
        buttonChangeProfilePicture.setOnClickListener(v -> openImagePicker());
        buttonViewLoginHistory.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, LoginHistoryActivity.class);
            startActivity(intent);
        });

        listenForUserUpdates();

        listenForUserUpdates();
    }

    private void initializeViews() {
        inputName = findViewById(R.id.inputName);
        inputAge = findViewById(R.id.inputAge);
        inputPhone = findViewById(R.id.inputPhone);
        statusSpinner = findViewById(R.id.statusSpinner);
        profileImageView = findViewById(R.id.profileImageView);
        buttonAddUser = findViewById(R.id.buttonAddUser);
        buttonLogout = findViewById(R.id.buttonLogout);
        buttonChangeProfilePicture = findViewById(R.id.buttonChangeProfilePicture);
        buttonViewLoginHistory = findViewById(R.id.buttonViewLoginHistory);

        recyclerViewUsers = findViewById(R.id.recyclerViewUsers);

        recyclerViewUsers.setLayoutManager(new LinearLayoutManager(this));
        userAdapter = new UserAdapter(userList, this, this);
        recyclerViewUsers.setAdapter(userAdapter);
    }

    private void addUser() {
        String name = inputName.getText().toString().trim();
        String ageText = inputAge.getText().toString().trim();
        String phone = inputPhone.getText().toString().trim();
        String status = statusSpinner.getSelectedItem().toString();

        if (name.isEmpty() || ageText.isEmpty() || phone.isEmpty()) {
            Toast.makeText(MainActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            int age = Integer.parseInt(ageText);
            Map<String, Object> user = new HashMap<>();
            user.put("name", name);
            user.put("age", age);
            user.put("phone", phone);
            user.put("status", status);

            db.collection("users").add(user)
                    .addOnSuccessListener(documentReference -> {
                        Log.d("Firestore", "User added with ID: " + documentReference.getId());
                        Toast.makeText(MainActivity.this, "User added successfully", Toast.LENGTH_SHORT).show();
                        clearInputFields();
                    })
                    .addOnFailureListener(e -> {
                        Log.w("Firestore", "Error adding user: " + e.getMessage(), e);
                        Toast.makeText(MainActivity.this, "Failed to add user: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } catch (NumberFormatException e) {
            Toast.makeText(MainActivity.this, "Age must be a number", Toast.LENGTH_SHORT).show();
        }
    }

    private void clearInputFields() {
        inputName.setText("");
        inputAge.setText("");
        inputPhone.setText("");
    }

    private void logoutUser() {
        auth.signOut();
        startActivity(new Intent(MainActivity.this, LoginActivity.class));
        finish();
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
                .addOnFailureListener(e -> {
                    Log.e("MainActivity", "Upload failed", e);
                    Toast.makeText(MainActivity.this, "Failed to upload image", Toast.LENGTH_SHORT).show();
                });
    }

    private void saveProfilePictureUri(String downloadUrl) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) return;

        String userId = currentUser.getUid();

        db.collection("users").document(userId)
                .update("profilePictureUrl", downloadUrl)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(MainActivity.this, "Profile picture updated", Toast.LENGTH_SHORT).show();
                    Picasso.get().load(downloadUrl).into(profileImageView);
                })
                .addOnFailureListener(e -> {
                    Log.e("MainActivity", "Error updating profile picture URL", e);
                    Toast.makeText(MainActivity.this, "Failed to update profile picture URL", Toast.LENGTH_SHORT).show();
                });
    }

    private void loadUserProfilePicture() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) return;

        String userId = currentUser.getUid();

        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists() && documentSnapshot.contains("profilePictureUrl")) {
                        String profilePictureUrl = documentSnapshot.getString("profilePictureUrl");
                        Picasso.get().load(profilePictureUrl).into(profileImageView);
                    }
                })
                .addOnFailureListener(e -> Log.e("MainActivity", "Error loading profile picture", e));
    }

    private void listenForUserUpdates() {
        userListener = db.collection("users").addSnapshotListener((snapshots, e) -> {
            if (e != null) {
                Log.w("Firestore", "Listen failed.", e);
                return;
            }

            if (snapshots != null) {
                userList.clear();
                for (QueryDocumentSnapshot doc : snapshots) {
                    String documentId = doc.getId();
                    String name = doc.getString("name");
                    long age = doc.getLong("age");
                    String phone = doc.getString("phone");
                    String status = doc.getString("status");

                    User user = new User(name, (int) age, phone, status);
                    user.setDocumentId(documentId);
                    userList.add(user);
                }
                userAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onUserDeleteClick(User user) {
        deleteUser(user);
    }

    private void deleteUser(User user) {
        db.collection("users").document(user.getDocumentId())
                .delete()
                .addOnSuccessListener(aVoid -> Toast.makeText(MainActivity.this, "User deleted successfully", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> {
                    Log.w("MainActivity", "Error deleting user", e);
                    Toast.makeText(MainActivity.this, "Failed to delete user", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (userListener != null) {
            userListener.remove();
        }
    }

    @Override
    public void onUserEditClick(User user) {
        showEditUserDialog(user);
    }

    private void showEditUserDialog(User user) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_user, null);
        EditText editName = dialogView.findViewById(R.id.editName);
        EditText editAge = dialogView.findViewById(R.id.editAge);
        EditText editPhone = dialogView.findViewById(R.id.editPhone);
        Spinner editStatusSpinner = dialogView.findViewById(R.id.editStatusSpinner);

        editName.setText(user.getName());
        editAge.setText(String.valueOf(user.getAge()));
        editPhone.setText(user.getPhone());

        new AlertDialog.Builder(this)
                .setTitle("Edit User")
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    String name = editName.getText().toString().trim();
                    String ageText = editAge.getText().toString().trim();
                    String phone = editPhone.getText().toString().trim();
                    String status = editStatusSpinner.getSelectedItem().toString();

                    if (name.isEmpty() || ageText.isEmpty() || phone.isEmpty()) {
                        Toast.makeText(MainActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    try {
                        int age = Integer.parseInt(ageText);

                        Map<String, Object> updatedUser = new HashMap<>();
                        updatedUser.put("name", name);
                        updatedUser.put("age", age);
                        updatedUser.put("phone", phone);
                        updatedUser.put("status", status);

                        db.collection("users").document(user.getDocumentId())
                                .update(updatedUser)
                                .addOnSuccessListener(aVoid -> Toast.makeText(MainActivity.this, "User updated successfully", Toast.LENGTH_SHORT).show())
                                .addOnFailureListener(e -> Toast.makeText(MainActivity.this, "Failed to update user: " + e.getMessage(), Toast.LENGTH_SHORT).show());

                    } catch (NumberFormatException e) {
                        Toast.makeText(MainActivity.this, "Age must be a number", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}