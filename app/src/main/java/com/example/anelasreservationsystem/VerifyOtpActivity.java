package com.example.anelasreservationsystem;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

public class VerifyOtpActivity extends AppCompatActivity {

    private EditText otpEditText;
    private Button verifyButton;
    private String verificationId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_otp);

        // Initialize views
        otpEditText = findViewById(R.id.otpEditText);
        verifyButton = findViewById(R.id.verifyButton);

        // Get the verification ID from the intent
        verificationId = getIntent().getStringExtra("verificationId");

        // Set up click listener for the Verify button
        verifyButton.setOnClickListener(v -> verifyOtp());
    }

    private void verifyOtp() {
        String otp = otpEditText.getText().toString().trim();

        if (otp.isEmpty()) {
            Toast.makeText(this, "Please enter the OTP", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a credential using the verification ID and the OTP
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, otp);

        // Sign in with the credential
        FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // OTP verification successful
                        Toast.makeText(VerifyOtpActivity.this, "OTP verified successfully", Toast.LENGTH_SHORT).show();
                        // Proceed to reset password
                        resetPassword();
                    } else {
                        // If sign-in fails, display a message to the user
                        Toast.makeText(VerifyOtpActivity.this, "OTP verification failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void resetPassword() {
        String newPassword = "userInputNewPassword"; // Get this from an EditText input
        FirebaseAuth.getInstance().getCurrentUser().updatePassword(newPassword)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(VerifyOtpActivity.this, "Password reset successfully", Toast.LENGTH_SHORT).show();
                        // Redirect to login or main activity
                        Intent intent = new Intent(VerifyOtpActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(VerifyOtpActivity.this, "Failed to reset password", Toast.LENGTH_SHORT).show();
                    }
                });
    }

}
