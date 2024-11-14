package com.example.midterm;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class EditInfoActivity extends AppCompatActivity {
    private ArrayAdapter<CharSequence> adapter;


    private Spinner spinnerStatus;
    private EditText editName, editAge, editPhone;
    private Button saveButton;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_info);


        editName = findViewById(R.id.editName);
        editAge = findViewById(R.id.editAge);
        editPhone = findViewById(R.id.editPhone);
        spinnerStatus = findViewById(R.id.spinnerStatus);

        saveButton = findViewById(R.id.saveButton);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        adapter = ArrayAdapter.createFromResource(this,
                R.array.status_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatus.setAdapter(adapter);


        loadUserInfo();


        saveButton.setOnClickListener(v -> saveUserInfo());
    }

    private void loadUserInfo() {
        String userId = auth.getCurrentUser().getUid();

        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {

                        String name = documentSnapshot.getString("name");
                        Long age = documentSnapshot.getLong("age");
                        String phone = documentSnapshot.getString("phone");
                        String status = documentSnapshot.getString("status");

                        editName.setText(name != null ? name : "");
                        editAge.setText(age != null ? String.valueOf(age) : "");
                        editPhone.setText(phone != null ? phone : "");


                        if (status != null) {
                            int spinnerPosition = adapter.getPosition(status);
                            spinnerStatus.setSelection(spinnerPosition);
                        }
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(EditInfoActivity.this, "Failed to load user information", Toast.LENGTH_SHORT).show());
    }


    private void saveUserInfo() {
        String name = editName.getText().toString().trim();
        String ageString = editAge.getText().toString().trim();
        String phone = editPhone.getText().toString().trim();
        String status = spinnerStatus.getSelectedItem().toString().trim();
        if (name.isEmpty() || ageString.isEmpty() || phone.isEmpty() || status.isEmpty()) {
            Toast.makeText(EditInfoActivity.this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            int age = Integer.parseInt(ageString);


            String userId = auth.getCurrentUser().getUid();


            db.collection("users").document(userId)
                    .update("name", name, "age", age, "phone", phone, "status", status)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(EditInfoActivity.this, "Information updated successfully", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(EditInfoActivity.this, "Failed to update information", Toast.LENGTH_SHORT).show();
                    });

        } catch (NumberFormatException e) {
            Toast.makeText(EditInfoActivity.this, "Please enter a valid age", Toast.LENGTH_SHORT).show();
        }
    }
}
