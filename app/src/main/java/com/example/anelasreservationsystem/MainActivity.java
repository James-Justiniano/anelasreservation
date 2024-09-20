package com.example.anelasreservationsystem;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText usernameEditText, passwordEditText;
    private Button loginButton;
    private TextView registerTextView, forgotPasswordTextView;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        // Check if the user is already signed in
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // User is signed in, redirect to the main app activity
            Intent intent = new Intent(MainActivity.this, MainActivity2.class);
            startActivity(intent);
            finish(); // Close this activity
            return;
        }

        // Initialize views
        usernameEditText = findViewById(R.id.editTextText); // Assuming this is the ID for username input
        passwordEditText = findViewById(R.id.editTextText2);
        loginButton = findViewById(R.id.loginButtons);
        forgotPasswordTextView = findViewById(R.id.forgotPasswordTextView);
        registerTextView = findViewById(R.id.registerTextView);

        // Set up window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.skipButton), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Set up click listener for the Login button
        loginButton.setOnClickListener(v -> loginUser());

        // Set up click listener for the Register TextView
        registerTextView.setOnClickListener(v -> {
            Log.d("MainActivity", "Register TextView clicked"); // Debugging log to check click event
            Intent intent = new Intent(MainActivity.this, SignUp.class);
            startActivity(intent);
        });

        forgotPasswordTextView.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ForgotPasswordActivity.class);
            startActivity(intent);
        });
    }

    private void loginUser() {
        String username = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        // Validate input fields
        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Query Firebase to get the email associated with the entered username
        databaseReference.orderByChild("username").equalTo(username)
                .addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            // Username exists, retrieve the associated email
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                String email = snapshot.child("email").getValue(String.class);

                                if (email != null) {
                                    // Sign in with the retrieved email and password
                                    mAuth.signInWithEmailAndPassword(email, password)
                                            .addOnCompleteListener(MainActivity.this, task -> {
                                                if (task.isSuccessful()) {
                                                    // Sign-in successful, redirect to the main app activity
                                                    Intent intent = new Intent(MainActivity.this, MainActivity2.class);
                                                    startActivity(intent);
                                                    finish(); // Close the Login activity
                                                    Toast.makeText(MainActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();
                                                } else {
                                                    // If sign-in fails, display a message to the user
                                                    Toast.makeText(MainActivity.this, "Login failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                } else {
                                    Toast.makeText(MainActivity.this, "Failed to retrieve email", Toast.LENGTH_SHORT).show();
                                }
                            }
                        } else {
                            // Username does not exist
                            Toast.makeText(MainActivity.this, "Username not found", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.e("MainActivity", "Database error: " + databaseError.getMessage());
                        Toast.makeText(MainActivity.this, "Database error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Optional: Handle sign-out action (if you implement sign-out functionality)
    private void signOut() {
        mAuth.signOut();
        Toast.makeText(this, "Signed out successfully.", Toast.LENGTH_SHORT).show();
    }
}
