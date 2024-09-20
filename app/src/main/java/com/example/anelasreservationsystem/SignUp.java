package com.example.anelasreservationsystem;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;

public class SignUp extends AppCompatActivity {

    private TextView dobTextView;
    private EditText fullNameEditText, emailEditText, usernameEditText, passwordEditText, confirmPasswordEditText, phoneNumberEditText;
    private Button signUpButton;
    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up);

        FirebaseDatabase.getInstance().getReference().keepSynced(false);
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        // Check if a user is already signed in
        if (mAuth.getCurrentUser() != null) {
            // User is signed in, redirect to MainActivity (login page)
            Intent intent = new Intent(SignUp.this, MainActivity.class);
            startActivity(intent);
            finish(); // Close the SignUp activity
        }

        // Initialize views
        dobTextView = findViewById(R.id.dobTextView);
        fullNameEditText = findViewById(R.id.editTextText);
        emailEditText = findViewById(R.id.editTextText5);
        usernameEditText = findViewById(R.id.username);
        passwordEditText = findViewById(R.id.editTextText2);
        confirmPasswordEditText = findViewById(R.id.editTextText6);
        phoneNumberEditText = findViewById(R.id.phnum); // Initialize the phone number EditText
        signUpButton = findViewById(R.id.btnsignup);

        // Set up padding for system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Set up click listener for the Date of Birth TextView
        dobTextView.setOnClickListener(v -> showDatePickerDialog());

        // Set up click listener for the Sign Up button
        signUpButton.setOnClickListener(v -> signUpUser());
    }

    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(SignUp.this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    // Update the TextView with the selected date
                    dobTextView.setText(selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear);
                }, year, month, day);
        datePickerDialog.show();
    }

    private void signUpUser() {
        String fullName = fullNameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String username = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();
        String phoneNumber = phoneNumberEditText.getText().toString().trim(); // Get phone number

        // Validate input fields
        if (fullName.isEmpty() || email.isEmpty() || username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || phoneNumber.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate phone number format (e.g., 10-digit number)
        if (!phoneNumber.matches("\\d{10}")) {
            Toast.makeText(this, "Invalid phone number. It must be 10 digits.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a new user with Firebase Authentication
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Save user info in Firebase Realtime Database using UID as the key
                        String uid = mAuth.getCurrentUser().getUid(); // Get the user ID
                        User userData = new User(fullName, email, username, phoneNumber); // Pass phone number
                        databaseReference.child(uid).setValue(userData) // Save user data under UID
                                .addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful()) {
                                        // Sign-up successful, redirect to MainActivity (login page)
                                        Intent intent = new Intent(SignUp.this, MainActivity.class);
                                        startActivity(intent);
                                        finish(); // Close the SignUp activity
                                        Toast.makeText(SignUp.this, "Sign up successful! Please log in.", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(SignUp.this, "Failed to save user data", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        // If sign-up fails, display a message to the user
                        Toast.makeText(SignUp.this, "Sign up failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // User class to store user details
    public static class User {
        public String fullName;
        public String email;
        public String username;
        public String phoneNumber; // Add phone number field

        public User() {
            // Default constructor required for calls to DataSnapshot.getValue(User.class)
        }

        public User(String fullName, String email, String username, String phoneNumber) { // Add phone number parameter
            this.fullName = fullName;
            this.email = email;
            this.username = username;
            this.phoneNumber = phoneNumber; // Initialize phone number
        }
    }
}
