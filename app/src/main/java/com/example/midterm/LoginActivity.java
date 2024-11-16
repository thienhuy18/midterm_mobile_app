package com.example.midterm;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private EditText inputEmail, inputPassword;
    private Button buttonLogin, buttonRegister;

    @Override
    protected void onStart() {
        super.onStart();
        Toast.makeText(LoginActivity.this, "Welcome to the app", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        inputEmail = findViewById(R.id.inputEmail);
        inputPassword = findViewById(R.id.inputPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        //buttonRegister = findViewById(R.id.buttonRegister);

        buttonLogin.setOnClickListener(v -> loginUser());
        //   buttonRegister.setOnClickListener(v -> {
        //     startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        //});

    }

    private void loginUser() {
        String email = inputEmail.getText().toString();
        String password = inputPassword.getText().toString();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(LoginActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {

                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            checkUserDeleted(user.getUid());
                            checkUserStatus(user);
                        }
                        Toast.makeText(LoginActivity.this, "Login successful", Toast.LENGTH_SHORT).show();


                        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString("originalEmail", email);  // Store the logged-in email
                        editor.putString("originalPassword", password);  // Store the logged-in password
                        editor.apply();


                        saveLoginHistory(user);


                        startActivity(new Intent(LoginActivity.this, MenuActivity.class));
                        finish();

                    } else {

                        Exception exception = task.getException();
                        if (exception != null && exception instanceof FirebaseAuthInvalidCredentialsException) {

                            Toast.makeText(LoginActivity.this, "Incorrect username or password", Toast.LENGTH_SHORT).show();
                        } else {

                            Toast.makeText(LoginActivity.this, "Login failed: " + exception.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                        Log.w("LoginActivity", "signInWithEmail:failure", exception);
                    }
                });
    }

    private void saveLoginHistory(FirebaseUser user) {
        if (user != null) {
            String userId = user.getUid();
            String email = user.getEmail();
            Timestamp loginTime = Timestamp.now();


            Map<String, Object> loginHistory = new HashMap<>();
            loginHistory.put("userId", userId);
            loginHistory.put("email", email);
            loginHistory.put("timestamp", loginTime);

            db.collection("login_history")
                    .add(loginHistory)
                    .addOnSuccessListener(documentReference -> {
                        Log.d("LoginActivity", "Login history saved successfully");
                    })
                    .addOnFailureListener(e -> {
                        Log.w("LoginActivity", "Error saving login history", e);
                    });
        }

    }

    private void checkUserStatus(FirebaseUser user) {
        String userId = user.getUid();

        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String status = documentSnapshot.getString("status");

                        if ("Locked".equalsIgnoreCase(status)) {
                            Toast.makeText(LoginActivity.this, "Your account is locked. Please contact support.", Toast.LENGTH_LONG).show();
                            auth.signOut();
                            startActivity(new Intent(LoginActivity.this, LoginActivity.class)); // Navigate back to login screen
                            finish();
                        } else if ("Normal".equalsIgnoreCase(status)) {
                            Toast.makeText(LoginActivity.this, "Login successful", Toast.LENGTH_SHORT).show();

                            saveLoginHistory(user);

                            SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putString("originalEmail", user.getEmail());
                            editor.apply();

                            startActivity(new Intent(LoginActivity.this, MenuActivity.class));
                            finish();
                        } else {
                            Toast.makeText(LoginActivity.this, "Account status unknown. Please contact support.", Toast.LENGTH_SHORT).show();
                            auth.signOut(); // Sign the user out
                        }
                    } else {
                        Toast.makeText(LoginActivity.this, "User data not found. Please contact support.", Toast.LENGTH_SHORT).show();
                        auth.signOut(); // Sign the user out
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(LoginActivity.this, "Failed to check account status: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    auth.signOut(); // Sign the user out
                });
    }

    private void checkUserDeleted(String userId) {
        // Check if the user document exists in Firestore
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {

                        Toast.makeText(LoginActivity.this, "Your account has been deleted. Logging out.", Toast.LENGTH_SHORT).show();
                        FirebaseAuth.getInstance().signOut();
                        startActivity(new Intent(LoginActivity.this, LoginActivity.class)); // Navigate back to login screen
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    // If there’s an error, log the user out
                    Toast.makeText(LoginActivity.this, "Failed to verify account status: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    FirebaseAuth.getInstance().signOut();
                    startActivity(new Intent(LoginActivity.this, LoginActivity.class)); // Navigate back to login screen
                    finish();
                });


    }





}
