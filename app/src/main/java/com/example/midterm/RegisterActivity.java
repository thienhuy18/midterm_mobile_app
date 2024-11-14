package com.example.midterm;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private Spinner inputStatusSpinner;

    private EditText inputName, inputAge, inputPhone, inputEmail, inputPassword;
    private Button buttonSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        inputName = findViewById(R.id.inputName);
        inputAge = findViewById(R.id.inputAge);
        inputPhone = findViewById(R.id.inputPhone);
        inputStatusSpinner = findViewById(R.id.inputStatusSpinner);
        inputEmail = findViewById(R.id.inputEmail);
        inputPassword = findViewById(R.id.inputPassword);
        buttonSave = findViewById(R.id.buttonSave);

        buttonSave.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {
        String name = inputName.getText().toString().trim();
        String ageText = inputAge.getText().toString().trim();
        String phone = inputPhone.getText().toString().trim();
        String status = inputStatusSpinner.getSelectedItem().toString();
        String email = inputEmail.getText().toString().trim();
        String password = inputPassword.getText().toString().trim();

        if (name.isEmpty() || ageText.isEmpty() || phone.isEmpty() || status.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(RegisterActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        int age;
        try {
            age = Integer.parseInt(ageText);
        } catch (NumberFormatException e) {
            Toast.makeText(RegisterActivity.this, "Age must be a number", Toast.LENGTH_SHORT).show();
            return;
        }

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            saveUserData(user.getUid(), name, age, phone, status);
                        }
                    } else {
                        Toast.makeText(RegisterActivity.this, "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveUserData(String userId, String name, int age, String phone, String status) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("userId", userId);
        userData.put("name", name);
        userData.put("age", age);
        userData.put("phone", phone);
        userData.put("status", status);

        db.collection("users").document(userId)
                .set(userData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(RegisterActivity.this, "Registration successful", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(RegisterActivity.this, "Error saving data: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
