package com.example.midterm;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MenuActivity extends AppCompatActivity {

    private Button buttonUserManagement, buttonViewLoginHistory, buttonLogout, buttonStudentManagement,buttonMyAccount;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

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
}
