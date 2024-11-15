package com.example.midterm;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
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

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
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
    private ImageView profileImageView;
    private String[] avatarNames = {"avatar1", "avatar2", "avatar3", "avatar4","avatar5","avatar6","avatar7","avatar8","avatar9","avatar10"};
    private int[] avatarResIds = {R.drawable.avatar1, R.drawable.avatar2, R.drawable.avatar3, R.drawable.avatar4, R.drawable.avatar5, R.drawable.avatar6, R.drawable.avatar7, R.drawable.avatar8, R.drawable.avatar9, R.drawable.avatar10};





    private Button buttonAddUser, buttonLogout;
    private RecyclerView recyclerViewUsers;
    private UserAdapter userAdapter;
    private List<User> userList = new ArrayList<>();
    private ListenerRegistration userListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("MainActivity", "onCreate called");
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



        buttonAddUser.setOnClickListener(v -> showAddUserDialog());


        buttonLogout.setOnClickListener(v -> logoutUser());


        listenForUserUpdates();



        loadUserAvatar();
        listenForAvatarChanges();

    }

    private void initializeViews() {



        buttonAddUser = findViewById(R.id.buttonAddUser);
        buttonLogout = findViewById(R.id.buttonLogout);

        recyclerViewUsers = findViewById(R.id.recyclerViewUsers);

        recyclerViewUsers.setLayoutManager(new LinearLayoutManager(this));
        userAdapter = new UserAdapter(userList, this, this);
        recyclerViewUsers.setAdapter(userAdapter);
        profileImageView = findViewById(R.id.profileImageView);



    }




    private void showAddUserDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View addUserView = inflater.inflate(R.layout.dialog_add_user, null);

        EditText addName = addUserView.findViewById(R.id.addName);
        EditText addAge = addUserView.findViewById(R.id.addAge);
        EditText addPhone = addUserView.findViewById(R.id.addPhone);
        Spinner addStatusSpinner = addUserView.findViewById(R.id.addStatusSpinner);
        EditText addEmail = addUserView.findViewById(R.id.addEmail);
        EditText addPassword = addUserView.findViewById(R.id.addPassword);
        Button btnAddUser = addUserView.findViewById(R.id.btnAddUser);

        AlertDialog addUserDialog = new AlertDialog.Builder(this)
                .setView(addUserView)
                .setTitle("Add New User")
                .create();

        btnAddUser.setOnClickListener(view -> {
            String name = addName.getText().toString().trim();
            String ageText = addAge.getText().toString().trim();
            String phone = addPhone.getText().toString().trim();
            String status = addStatusSpinner.getSelectedItem().toString();
            String email = addEmail.getText().toString().trim();
            String password = addPassword.getText().toString().trim();

            if (name.isEmpty() || ageText.isEmpty() || phone.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill out all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            int age = Integer.parseInt(ageText);
            User newUser = new User(name, age, phone, status);

            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser == null) {
                Toast.makeText(this, "No user is currently logged in", Toast.LENGTH_SHORT).show();
                return;
            }

            SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("originalUserId", currentUser.getUid());
            editor.apply();

            addUserToList(email, password, newUser);

            addUserDialog.dismiss();
        });

        addUserDialog.show();
    }



    private void addUserToList(String email, String password, User user) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "No user is currently logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String originalEmail = prefs.getString("originalEmail", null);
        String originalPassword = prefs.getString("originalPassword", null);

        if (originalEmail == null || originalPassword == null) {
            Toast.makeText(this, "Original user credentials are missing", Toast.LENGTH_SHORT).show();
            return;
        }


        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = auth.getCurrentUser();
                        if (firebaseUser != null) {
                            String userId = firebaseUser.getUid();

                            Map<String, Object> userMap = new HashMap<>();
                            userMap.put("name", user.getName());
                            userMap.put("age", user.getAge());
                            userMap.put("phone", user.getPhone());
                            userMap.put("status", user.getStatus());

                            db.collection("users").document(userId)
                                    .set(userMap)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(MainActivity.this, "User added successfully", Toast.LENGTH_SHORT).show();


                                        auth.signOut();
                                        auth.signInWithEmailAndPassword(originalEmail, originalPassword)
                                                .addOnCompleteListener(reAuthTask -> {
                                                    if (reAuthTask.isSuccessful()) {
                                                        Toast.makeText(MainActivity.this, "Re-authenticated as original user", Toast.LENGTH_SHORT).show();

                                                    } else {
                                                        Toast.makeText(MainActivity.this, "Failed to re-authenticate", Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e("MainActivity", "Failed to add user to Firestore", e);
                                        Toast.makeText(MainActivity.this, "Failed to add user", Toast.LENGTH_SHORT).show();
                                    });
                        }
                    } else {
                        Toast.makeText(MainActivity.this, "Failed to create user: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }









    private void logoutUser() {
        auth.signOut();
        startActivity(new Intent(MainActivity.this, LoginActivity.class));
        finish();
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
                    Log.d("MainActivity", "Document data: " + doc.getData());

                    String name = doc.getString("name");
                    long age;
                    try {
                        age = doc.getLong("age");
                    } catch (Exception ex) {
                        Log.w("MainActivity", "Field 'age' is not a number", ex);
                        age = 0;
                    }

                    String phone = doc.getString("phone");
                    String status = doc.getString("status");
                    String email = doc.getString("email");
                    String password = doc.getString("password");

                    Log.d("MainActivity", "Fetched email: " + email);

                    User user = new User(name, (int) age, phone, status,email, password);
                    user.setDocumentId(documentId);

                    userList.add(user);
                }
                userAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onUserDeleteClick(User user) {
        Log.d("UserAdapter", "Delete button clicked for: " + user.getName());
        deleteUser(user);
    }








    private void deleteUser(User user) {
        Log.d("MainActivity", "Delete user clicked: " + user.getName());


        deleteUserFromFirestore(user);
        deleteFirebaseAuthUser(user);
    }

    private void deleteUserFromFirestore(User user) {
        db.collection("users").document(user.getDocumentId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(MainActivity.this, "User data deleted from Firestore", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(MainActivity.this, "Failed to delete user data from Firestore", Toast.LENGTH_SHORT).show();
                });
    }

    private void deleteFirebaseAuthUser(User user) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null && currentUser.getUid().equals(user.getDocumentId())) {
            currentUser.delete()
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(MainActivity.this, "User deleted from Firebase Authentication", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(MainActivity.this, "Failed to delete user from Firebase Authentication", Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(MainActivity.this, "Cannot delete another user from Firebase Authentication", Toast.LENGTH_SHORT).show();
        }
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

