package com.example.anelasreservationsystem.Profile;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.anelasreservationsystem.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfilePage extends AppCompatActivity {

    private TextView fullNameTextView, emailTextView, aboutemail, aboutfn;
    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_page);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        // Initialize views
        fullNameTextView = findViewById(R.id.txtfn);
        emailTextView = findViewById(R.id.txtgmail);

        aboutemail = findViewById(R.id.editgmail);
        aboutfn = findViewById(R.id.fname);

        if (currentUser != null) {
            // Use the user's UID to get the database reference
            String uid = currentUser.getUid();

            // Get a reference to the database using the UID as the key
            databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(uid);

            // Load user data from Firebase
            loadUserData();
        } else {
            Toast.makeText(ProfilePage.this, "User not logged in.", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadUserData() {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String fullName = snapshot.child("fullName").getValue(String.class);
                    String email = snapshot.child("email").getValue(String.class);

                    // Display data in TextViews
                    if (fullName != null && email != null) {
                        fullNameTextView.setText(fullName);
                        emailTextView.setText(email);

                        aboutemail.setText(email);
                        aboutfn.setText(fullName);
                    } else {
                        Toast.makeText(ProfilePage.this, "User data not found.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(ProfilePage.this, "User data not found.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(ProfilePage.this, "Failed to load user data.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
