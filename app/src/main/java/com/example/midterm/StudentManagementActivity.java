package com.example.midterm;


import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

public class StudentManagementActivity extends AppCompatActivity {

    private FirebaseFirestore database;
    private Adapter studentAdapter;
    private CertificateAdapter certificateAdapter;
    private RecyclerView studentView, certificateView;
    private ImageButton btnInsertStudent, btnDeleteCertificate, btnAddStudent, btnSave, btnUpdate, btnSort, btnDeleteStudent, btnBackFromDetail, btnBackFromAdd, btnAddCertificate;
    private View layout1, layout2, layout3;
    private EditText txtInputStudentName, txtInputStudentStudy, txtInputStudentClass, txtInputStudentCourse, txtInputStudentGender, txtInputStudentBirth, txtInputStudentAddress;
    private EditText txtDetailStudentID, txtDetailStudentName, txtDetailStudentStudy, txtDetailStudentClass, txtDetailStudentCourse, txtDetailStudentGender, txtDetailStudentBirth, txtDetailStudentAddress;
    private static final Random random = new Random();
    private static final Set<String> existingIds = new HashSet<>();
    private static final Set<String> existingCerIds = new HashSet<>();
    private static final int CSV_REQUEST_CODE = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.combine_student_layout);

        btnAddStudent = findViewById(R.id.btnAddStudent);
        btnDeleteStudent = findViewById(R.id.btnDeleteStudent);
        btnDeleteCertificate = findViewById(R.id.btnDeleteCertificate);
        btnSave = findViewById(R.id.btnSave);
        btnUpdate = findViewById(R.id.btnUpdate);
        btnSort = findViewById(R.id.btnSort);
        btnBackFromDetail = findViewById(R.id.btnBackFromDetail);
        btnBackFromAdd = findViewById(R.id.btnBackFromAdd);
        btnAddCertificate = findViewById(R.id.btnAddCertificate);
        btnInsertStudent = findViewById(R.id.btnInsertStudent);

        layout1 = findViewById(R.id.layout1);
        layout2 = findViewById(R.id.layout2);
        layout3 = findViewById(R.id.layout3);
        studentView = findViewById(R.id.viewStudent);
        certificateView = findViewById(R.id.viewCertificate);

        txtDetailStudentID = findViewById(R.id.txtDetailStudentID);
        txtDetailStudentName = findViewById(R.id.txtDetailStudentName);
        txtDetailStudentStudy = findViewById(R.id.txtDetailStudentStudy);
        txtDetailStudentClass = findViewById(R.id.txtDetailStudentClass);
        txtDetailStudentCourse = findViewById(R.id.txtDetailStudentCourse);
        txtDetailStudentGender = findViewById(R.id.txtDetailStudentGender);
        txtDetailStudentBirth = findViewById(R.id.txtDetailStudentBirth);
        txtDetailStudentAddress = findViewById(R.id.txtDetailStudentAddress);

        txtInputStudentName = findViewById(R.id.txtInputStudentName);
        txtInputStudentStudy = findViewById(R.id.txtInputStudentStudy);
        txtInputStudentClass = findViewById(R.id.txtInputStudentClass);
        txtInputStudentCourse = findViewById(R.id.txtInputStudentCourse);
        txtInputStudentGender = findViewById(R.id.txtInputStudentGender);
        txtInputStudentBirth = findViewById(R.id.txtInputStudentBirth);
        txtInputStudentAddress = findViewById(R.id.txtInputStudentAddress);

        studentView.setLayoutManager(new LinearLayoutManager(this));
        certificateView.setLayoutManager(new LinearLayoutManager(this));


        database = FirebaseFirestore.getInstance();
        displayStudents();

        //Edit text for study modification
        String[] studys = {"IT", "Accountancy", "Banking", "Marketing", "Travel"};
        txtInputStudentStudy.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(StudentManagementActivity.this);
            builder.setTitle("Select place");
            builder.setSingleChoiceItems(studys, -1, (dialog, which) -> {
                String selectedLocation = studys[which];
                txtInputStudentStudy.setText(selectedLocation);
                dialog.dismiss();
            });

            builder.show();
        });

        //Edit text for gender modification
        String[] genders = {"Male", "Female"};
        txtInputStudentGender.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(StudentManagementActivity.this);
            builder.setTitle("Select place");
            builder.setSingleChoiceItems(genders, -1, (dialog, which) -> {
                String selectedLocation = genders[which];
                txtInputStudentGender.setText(selectedLocation);
                dialog.dismiss();
            });

            builder.show();
        });

        txtInputStudentBirth.setOnClickListener(v -> showDatePickerDialog());

        //Switch to adding student layout
        btnAddStudent.setOnClickListener(v -> {
            layout1.setVisibility(View.GONE);
            layout2.setVisibility(View.VISIBLE);
        });

        //Save new student and switch back to main layout
        btnSave.setOnClickListener(v -> {
            String id = generateUniqueId(txtInputStudentCourse.getText().toString(), txtInputStudentStudy.getText().toString());
            String name = txtInputStudentName.getText().toString().trim();
            String study = txtInputStudentStudy.getText().toString().trim();
            String classroom = txtInputStudentClass.getText().toString().trim();
            String course = txtInputStudentCourse.getText().toString().trim();
            String gender = txtInputStudentGender.getText().toString().trim();
            String birth = txtInputStudentBirth.getText().toString().trim();;
            String address = txtInputStudentAddress.getText().toString().trim();

            boolean isValid = true;
            if (name.isEmpty() || study.isEmpty() || classroom.isEmpty() || course.isEmpty() || gender.isEmpty() || birth.isEmpty() || address.isEmpty()) {
                Toast.makeText(this, "Empty input field, please make sure to fill in all info", Toast.LENGTH_SHORT).show();
                isValid = false;
            }

            if (!isValid) {
                return;
            }

            addStudent(id, name, study, classroom, course, gender, birth, address); //Save to firestore
            displayStudents();

            //Clear input field
            txtInputStudentName.setText("");
            txtInputStudentStudy.setText("");
            txtInputStudentClass.setText("");
            txtInputStudentCourse.setText("");
            txtInputStudentGender.setText("");
            txtInputStudentBirth.setText("");
            txtInputStudentAddress.setText("");

            layout1.setVisibility(View.VISIBLE);
            layout2.setVisibility(View.GONE);


        });

        //Press any student data and show a layout for detail, the detail is present in a editText style to change info and press the ImageButton on the top right
        btnUpdate.setOnClickListener(v -> {
            String id = txtDetailStudentID.getText().toString().trim();
            String name = txtDetailStudentName.getText().toString().trim();
            String study = txtDetailStudentStudy.getText().toString().trim();
            String classroom = txtDetailStudentClass.getText().toString().trim();
            String course = txtDetailStudentCourse.getText().toString().trim();
            String gender = txtDetailStudentGender.getText().toString().trim();
            String birth = txtDetailStudentBirth.getText().toString().trim();;
            String address = txtDetailStudentAddress.getText().toString().trim();

            boolean isValid = true;
            if (name.isEmpty() || study.isEmpty() || classroom.isEmpty() || course.isEmpty() || gender.isEmpty() || birth.isEmpty() || address.isEmpty()) {
                Toast.makeText(this, "Empty input field, please make sure to fill in all info", Toast.LENGTH_SHORT).show();
                isValid = false;
            }

            if (!isValid) {
                return;
            }

            updateStudent(id, name, study, classroom, course, gender, birth, address);
            displayStudents();

            //Clear input field
            txtInputStudentName.setText("");
            txtInputStudentStudy.setText("");
            txtInputStudentClass.setText("");
            txtInputStudentCourse.setText("");
            txtInputStudentGender.setText("");
            txtInputStudentBirth.setText("");
            txtInputStudentAddress.setText("");

            layout1.setVisibility(View.VISIBLE);
            layout3.setVisibility(View.GONE);
        });


        //Delete checked student button
        btnDeleteStudent.setOnClickListener(v -> {
            showMsgDelete(); //Called message before delete and proceed with delete logic when click Yes
        });

        //Delete checked certificate button
        btnDeleteCertificate.setOnClickListener(v -> {
            showMsgDeleteForCertification(txtDetailStudentID.getText().toString().trim());
        });

        btnBackFromAdd.setOnClickListener(v -> {
            layout2.setVisibility(View.GONE);
            layout1.setVisibility(View.VISIBLE);
        });

        btnBackFromDetail.setOnClickListener(v -> {
            layout3.setVisibility(View.GONE);
            layout1.setVisibility(View.VISIBLE);
        });

        //A maniplate glass icon, press it show a menu with 3 choice then call showMsg depend on choice
        btnSort.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(this, v);
            popupMenu.getMenuInflater().inflate(R.menu.menu_form, popupMenu.getMenu());

            popupMenu.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                if (id == R.id.optName) {
                    showMsgSort("name");
                }
                if (id == R.id.optID) {
                    showMsgSort("id");
                }
                if (id == R.id.optStudy) {
                    showMsgSort("study");
                }
                return false;
            });

            popupMenu.show();
        });

        btnAddCertificate.setOnClickListener(v -> {
            showAddCertificateDialog(txtDetailStudentID.getText().toString().trim(), txtDetailStudentName.getText().toString().trim());
        });

        btnInsertStudent.setOnClickListener(v -> {
            openFilePicker();
        });
    }

    //These following 4 are work in progress :>
    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("text/csv");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, CSV_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CSV_REQUEST_CODE && resultCode == RESULT_OK) {
            Uri fileUri = data.getData();
            if (fileUri != null) {
                readFile(fileUri);
            }
        }
    }

    private void readFile(Uri fileUri) {
        try {
            // Use ContentResolver to read the CSV file
            InputStream inputStream = getContentResolver().openInputStream(fileUri);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;

            while ((line = reader.readLine()) != null) {
                parseStudent(line);
            }

            reader.close();
            Toast.makeText(this, "Students Imported Successfully", Toast.LENGTH_SHORT).show();

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error reading file", Toast.LENGTH_SHORT).show();
        }
    }

    private void parseStudent(String line) {
        String[] columns = line.split(",");
        if (columns.length == 7) {
            String name = columns[1].trim();
            String study = columns[2].trim();
            String classroom = columns[3].trim();
            String course = columns[4].trim();
            String gender = columns[5].trim();
            String birth = columns[6].trim();
            String address = columns[7].trim();

            String id = generateUniqueId(columns[4].trim(), columns[2].trim());
        } else {
            Log.w("CSVParseError", "Invalid student data format: " + line);
            Toast.makeText(this, "Invalid data format in file", Toast.LENGTH_SHORT).show();
        }
    }

    private void showDatePickerDialog() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String selectedDate = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
                    txtInputStudentBirth.setText(selectedDate);
                }, year, month, day);

        datePickerDialog.show();
    }

    public static String generateUniqueId(String course, String study) {
        String id;
        do {
            id = generateID(course, study);
        } while (existingIds.contains(id));
        existingIds.add(id);
        return id;
    }

    public static String generateID(String course, String study) {
        int randomInt = random.nextInt(9000) + 1000;
        String idForStudy = "";
        if (study.equals("IT")) {
            idForStudy = "IT";
        }
        else if (study.equals("Accountancy")) {
            idForStudy = "AC";
        }
        else if (study.equals("Banking")) {
            idForStudy = "BK";
        }
        else if (study.equals("Marketing")) {
            idForStudy = "MK";
        }
        else if (study.equals("Travel")) {
            idForStudy = "TR";
        }

        return idForStudy + course + randomInt;
    }

    //Auto generate ID for certificate
    public static String generateCertificationID(String studentName) {
        int randomInt = random.nextInt(900) + 100;
        return "CER" + studentName + randomInt;
    }

    //Make sure the auto id is completely uniqe
    public static String generateUniqueCertificationId(String studentName) {
        String id;
        do {
            id = generateCertificationID(studentName);
        } while (existingCerIds.contains(id));
        existingCerIds.add(id);
        return id;
    }

    //Add student function for Firebase
    private void addStudent(String id, String name, String study, String classroom, String course, String gender, String birth, String address) {
        //Create collection:
        Map<String, Object> student = new HashMap<>();
        student.put("id", id);
        student.put("name", name);
        student.put("study", study);
        student.put("classroom", classroom);
        student.put("course", course);
        student.put("gender", gender);
        student.put("birth", birth);
        student.put("address", address);

        //Add to document
        database.collection("students")
                .document(id) //Student ID
                .set(student, SetOptions.merge())
                .addOnSuccessListener(aVoid -> Log.d("Firestore", "Student added"))
                .addOnFailureListener(e -> Log.w("Firestore", "Error adding student", e));
    }

    //Add certificate function for Firebase
    private void addCertificate(String id, String title, String studentid, String date, String detail) {
        //Create collection:
        Map<String, Object> certificate = new HashMap<>();
        certificate.put("id", id);
        certificate.put("title", title);
        certificate.put("studentid", studentid);
        certificate.put("date", date);
        certificate.put("detail", detail);

        //Add to document
        database.collection("certificates")
                .document(id) //Certificate ID
                .set(certificate)
                .addOnSuccessListener(aVoid -> Log.d("Firestore", "Certificate added"))
                .addOnFailureListener(e -> Log.w("Firestore", "Error adding certificate", e));
    }

    private void updateStudent(String id, String name, String study, String classroom, String course, String gender, String birth, String address) {
        //Create collection of update student:
        Map<String, Object> updateStudent = new HashMap<>();
        updateStudent.put("name", name);
        updateStudent.put("study", study);
        updateStudent.put("classroom", classroom);
        updateStudent.put("course", course);
        updateStudent.put("gender", gender);
        updateStudent.put("birth", birth);
        updateStudent.put("address", address);

        database.collection("students").document(id)
                .update(updateStudent)
                .addOnSuccessListener(aVoid -> Log.d("Firestore", "Student updated"))
                .addOnFailureListener(e -> Log.w("Firestore", "Error updating student", e));
    }

    private void updateCertificate(String id, String title, String studentid, String date, String detail) {
        //Create collection:
        Map<String, Object> updateCertificate = new HashMap<>();
        updateCertificate.put("title", title);
        updateCertificate.put("studentid", studentid);
        updateCertificate.put("date", date);
        updateCertificate.put("detail", detail);

        //Add to document
        database.collection("certificates").document(id)
                .update(updateCertificate)
                .addOnSuccessListener(aVoid -> Log.d("Firestore", "Certificate updated"))
                .addOnFailureListener(e -> Log.w("Firestore", "Error updating certificate", e));
    }

    private void deleteStudent(String id) {
        database.collection("students").document(id)
                .delete()
                .addOnSuccessListener(aVoid -> Log.d("Firestore", "Student deleted"))
                .addOnFailureListener(e -> Log.w("Firestore", "Error deleting student", e));
    }

    private void deleteCertificate(String id) {
        database.collection("certificates").document(id)
                .delete()
                .addOnSuccessListener(aVoid -> Log.d("Firestore", "Certificate deleted"))
                .addOnFailureListener(e -> Log.w("Firestore", "Error deleting certificate", e));
    }

    private void sortStudents(String sortField, boolean ascending) {
        database.collection("students")
                .orderBy(sortField, ascending ? Query.Direction.ASCENDING : Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Student> sortedStudentList = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Student student = document.toObject(Student.class);
                            sortedStudentList.add(student);
                        }
                        studentAdapter = new Adapter(sortedStudentList);
                        studentView.setAdapter(studentAdapter);

                        studentAdapter.setOnItemClickListener(new Adapter.OnItemClickListener() {
                            @Override
                            public void onItemClick(Student student) {
                                showStudentDetails(student);
                            }
                        });
                    } else {
                        Log.w("Firestore", "Error sorting students.", task.getException());
                    }
                });
    }

    //function show data in recycleview for student
    private void displayStudents() {
        database.collection("students").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<Student> studentList = new ArrayList<>();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    Student student = document.toObject(Student.class);
                    studentList.add(student);
                }
                studentAdapter = new Adapter(studentList);
                studentView.setAdapter(studentAdapter);

                //Switch to student detail layout
                studentAdapter.setOnItemClickListener(new Adapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(Student student) {
                        Log.d("Item Clicked", "Student: " + student.getName());
                        showStudentDetails(student);
                    }
                });
            } else {
                Log.w("Firestore", "Error getting students.", task.getException());
            }
        });
    }

    //function show data in recycleview for certificate
    private void displayCertificates(String studentId) {
        database.collection("certificates")
                .whereEqualTo("studentid", studentId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Certificate> certificateList = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Certificate certificate = document.toObject(Certificate.class);
                            certificateList.add(certificate);
                        }
                        certificateAdapter = new CertificateAdapter(certificateList);
                        certificateView.setAdapter(certificateAdapter);

                        certificateAdapter.setOnItemClickListener(new CertificateAdapter.OnItemClickListener() {
                            @Override
                            public void onItemClick(Certificate certificate) {
                                Log.d("Item Clicked", "Student: " + certificate.getTitle());
                                showCertificateDetails(certificate, studentId, txtDetailStudentName.getText().toString().trim());
                            }
                        });
                    } else {
                        Log.w("Firestore", "Error getting students.", task.getException());
                    }
                });
    }

    //Basic comfirm message for delete student
    private void showMsgDelete() {
        AlertDialog.Builder builder = new AlertDialog.Builder(StudentManagementActivity.this);
        builder.setTitle("COMFIRMATION");
        builder.setMessage("Are you sure you want to proceed?");

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                List<Student> checkedStudents = studentAdapter.getCheckedStudents();

                if (checkedStudents.isEmpty()) {
                    Toast.makeText(StudentManagementActivity.this, "Need at least 1 student is selected", Toast.LENGTH_SHORT).show();
                    return;
                }

                for(Student checkedStudent: checkedStudents ) {
                    String id = checkedStudent.getId();

                    deleteStudent(id);
                    displayStudents();
                }
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    //Basic message confirm when delete certificate
    private void showMsgDeleteForCertification(String studentID) {
        AlertDialog.Builder builder = new AlertDialog.Builder(StudentManagementActivity.this);
        builder.setTitle("COMFIRMATION");
        builder.setMessage("Are you sure you want to proceed?");

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                List<Certificate> checkedCertificates = certificateAdapter.getCheckedCertificates();

                if (checkedCertificates.isEmpty()) {
                    Toast.makeText(StudentManagementActivity.this, "Need at least 1 certificate is selected", Toast.LENGTH_SHORT).show();
                    return;
                }

                for (Certificate checkedCertificate : checkedCertificates) {
                    String id = checkedCertificate.getId();

                    deleteCertificate(id);
                    displayCertificates(studentID);
                }
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    //Call Dialog ask to sort ASC or DES style following choce from menu
    private void showMsgSort(String choice) {
        AlertDialog.Builder builder = new AlertDialog.Builder(StudentManagementActivity.this);
        builder.setTitle("COMFIRMATION");
        builder.setMessage("What order you want to sort?");

        builder.setPositiveButton("Acsending", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                sortStudents(choice, true);
            }
        });

        builder.setNegativeButton("Descending", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                sortStudents(choice, false);
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showStudentDetails(Student student) {
        layout1.setVisibility(View.GONE);
        layout3.setVisibility(View.VISIBLE);

        txtDetailStudentID.setText(student.getId());
        txtDetailStudentName.setText(student.getName());
        txtDetailStudentBirth.setText(student.getBirth());
        txtDetailStudentAddress.setText(student.getAddress());
        txtDetailStudentCourse.setText(student.getCourse());
        txtDetailStudentStudy.setText(student.getStudy());
        txtDetailStudentClass.setText(student.getClassroom());
        txtDetailStudentGender.setText(student.getGender());

        displayCertificates(student.getId());

    }

    private void showAddCertificateDialog(String studentID, String studentName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Adding Certificate");

        View view = getLayoutInflater().inflate(R.layout.activity_add_certificate, null);
        builder.setView(view);

        EditText txtInputCertificateTitle = view.findViewById(R.id.txtInputCertificateTitle);
        EditText txtInputCertificateDate = view.findViewById(R.id.txtInputCertificateDate);
        txtInputCertificateDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        StudentManagementActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                String selectedDate = year + "/" + (month + 1) + "/" + day;
                                txtInputCertificateDate.setText(selectedDate);
                            }
                        },
                        year, month, day
                );

                datePickerDialog.show();
            }
        });

        EditText txtInputCertificateDetail = view.findViewById(R.id.txtInputCertificateDetail);

        builder.setPositiveButton("ADD", ((dialog, which) -> {
            String title = txtInputCertificateTitle.getText().toString().trim();
            String date = txtInputCertificateDate.getText().toString().trim();
            String detail = txtInputCertificateDetail.getText().toString().trim();

            boolean isValid = true;
            if(title.isEmpty() || date.isEmpty() || detail.isEmpty()) {
                Toast.makeText(this, "Empty input field, please make sure to fill in all info", Toast.LENGTH_SHORT).show();
                isValid = false;
            }

            if (!isValid) {
                return;
            }

            String certId = generateUniqueCertificationId(studentName);

            addCertificate(certId, title, studentID, date, detail);
            Toast.makeText(StudentManagementActivity.this, "Certificate added", Toast.LENGTH_SHORT).show();

            displayCertificates(studentID);
        }));

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void showCertificateDetails(Certificate certificate, String studentID, String studentName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Viewing Certificate");

        View view = getLayoutInflater().inflate(R.layout.activity_detail_certificate, null);
        builder.setView(view);

        EditText txtDetailCertificateID = view.findViewById(R.id.txtDetailCertificateID);
        EditText txtDetailCertificateStuID = view.findViewById(R.id.txtDetailCertificateStuID);
        EditText txtDetailCertificateTitle = view.findViewById(R.id.txtDetailCertificateTitle);
        EditText txtDetailCertificateDate = view.findViewById(R.id.txtDetailCertificateDate);
        txtDetailCertificateDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        StudentManagementActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                String selectedDate = year + "/" + (month + 1) + "/" + day;
                                txtDetailCertificateDate.setText(selectedDate);
                            }
                        },
                        year, month, day
                );

                datePickerDialog.show();
            }
        });

        EditText txtDetailCertificateDetail = view.findViewById(R.id.txtDetailCertificateDetail);

        txtDetailCertificateID.setText(certificate.getId());
        txtDetailCertificateTitle.setText(certificate.getTitle());
        txtDetailCertificateDate.setText(certificate.getDate());
        txtDetailCertificateStuID.setText(studentID);

        txtDetailCertificateDetail.setText(certificate.getDetail());

        builder.setPositiveButton("UPDATE", ((dialog, which) -> {
            String id = txtDetailCertificateID.getText().toString().trim();
            String title = txtDetailCertificateTitle.getText().toString().trim();
            String date = txtDetailCertificateDate.getText().toString().trim();
            String detail = txtDetailCertificateDetail.getText().toString().trim();

            boolean isValid = true;
            if(title.isEmpty() || date.isEmpty() || detail.isEmpty()) {
                Toast.makeText(this, "Empty input field, please make sure to fill in all info", Toast.LENGTH_SHORT).show();
                isValid = false;
            }

            if (!isValid) {
                return;
            }

            updateCertificate(id, title, studentID, date, detail);
            Toast.makeText(StudentManagementActivity.this, "Certificate updated", Toast.LENGTH_SHORT).show();

            displayCertificates(studentID);
        }));

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

}
