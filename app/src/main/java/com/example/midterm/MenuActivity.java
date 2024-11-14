package com.example.midterm;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class MenuActivity extends AppCompatActivity {

    private Button buttonUserManagement, buttonViewLoginHistory, buttonLogout, buttonStudentManagement,buttonMyAccount;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        auth = FirebaseAuth.getInstance();
        buttonMyAccount = findViewById(R.id.buttonMyAccount);

        buttonUserManagement = findViewById(R.id.buttonUserManagement);
        buttonStudentManagement = findViewById(R.id.buttonStudentManagement);

        buttonViewLoginHistory = findViewById(R.id.buttonViewLoginHistory);
        buttonLogout = findViewById(R.id.buttonLogout);

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
}
