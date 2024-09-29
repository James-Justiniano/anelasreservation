package com.example.anelasreservationsystem;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.anelasreservationsystem.CartFolder.CartItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class CheckoutActivity extends AppCompatActivity {

    private RecyclerView checkoutRecyclerView;
    private CheckoutAdapter checkoutAdapter;
    private TextView checkoutTotalPriceTextView, checkoutAmenitiesPriceTextView, checkoutPricePerNightTextView;
    private Button proceedToCheckoutButton, uploadProofButton;
    private ImageView proofOfPaymentImageView;
    private List<CartItem> checkoutCartItems;

    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri proofImageUri;
    private StorageReference storageReference;
    private DatabaseReference reservationsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        // Initialize Firebase references
        storageReference = FirebaseStorage.getInstance().getReference("proof_of_payment");
        reservationsRef = FirebaseDatabase.getInstance().getReference("reservations");

        // Initialize views
        initializeViews();

        // Get the List<CartItem> or CartItem from the intent
        ArrayList<CartItem> cartItems = (ArrayList<CartItem>) getIntent().getSerializableExtra("cartItems");
        CartItem cartItem = (CartItem) getIntent().getSerializableExtra("cartItem");

        // Initialize the list of checkout items
        checkoutCartItems = new ArrayList<>();

        if (cartItems != null && !cartItems.isEmpty()) {
            checkoutCartItems.addAll(cartItems);
        } else if (cartItem != null) {
            checkoutCartItems.add(cartItem);
        } else {
            showEmptyCartMessage();
            return;
        }

        // Set up RecyclerView
        setupRecyclerView();

        // Display total prices
        displayTotalPrices();

        // Handle proof of payment upload
        uploadProofButton.setOnClickListener(v -> openFileChooser());

        // Handle Pay Now (Checkout) button click
        proceedToCheckoutButton.setOnClickListener(v -> uploadImageAndCreateReservation());
    }

    private void initializeViews() {
        checkoutRecyclerView = findViewById(R.id.checkoutRecyclerView);
        checkoutTotalPriceTextView = findViewById(R.id.checkoutTotalPriceTextView);
        checkoutAmenitiesPriceTextView = findViewById(R.id.checkoutAmenitiesPriceTextView);
        checkoutPricePerNightTextView = findViewById(R.id.checkoutPricePerNightTextView);
        proofOfPaymentImageView = findViewById(R.id.proofOfPaymentImageView);
        uploadProofButton = findViewById(R.id.uploadProofOfPaymentButton);
        proceedToCheckoutButton = findViewById(R.id.proceedToCheckoutButton);
        checkoutRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void showEmptyCartMessage() {
        Toast.makeText(this, "No items in the cart to checkout.", Toast.LENGTH_SHORT).show();
        finish(); // Close the activity if no items
    }

    private void setupRecyclerView() {
        checkoutAdapter = new CheckoutAdapter(checkoutCartItems, this);
        checkoutRecyclerView.setAdapter(checkoutAdapter);
    }

    private void displayTotalPrices() {
        double totalPrice = 0.0;
        double subtotalRooms = 0.0;
        double totalAmenitiesPrice = 0.0;

        for (CartItem item : checkoutCartItems) {
            totalPrice += item.getTotalPrice();
            totalAmenitiesPrice += item.getAmenitiesPrice();
            subtotalRooms += item.getTotalPrice() - item.getAmenitiesPrice();
        }

        checkoutTotalPriceTextView.setText("₱" + String.format("%.2f", totalPrice));
        checkoutAmenitiesPriceTextView.setText("₱" + String.format("%.2f", totalAmenitiesPrice));
        checkoutPricePerNightTextView.setText("₱" + String.format("%.2f", subtotalRooms));
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            proofImageUri = data.getData();
            proofOfPaymentImageView.setImageURI(proofImageUri); // Display selected image
        }
    }

    private void uploadImageAndCreateReservation() {
        if (proofImageUri != null) {
            try {
                // Create a reference in Firebase Storage for the image
                StorageReference fileReference = storageReference.child(System.currentTimeMillis() + ".jpg");

                // Open InputStream from the URI
                InputStream stream = getContentResolver().openInputStream(proofImageUri);

                // Upload the InputStream to Firebase Storage
                if (stream != null) {
                    UploadTask uploadTask = fileReference.putStream(stream);

                    // Handle success
                    uploadTask.addOnSuccessListener(taskSnapshot -> {
                                fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                                    String imageUrl = uri.toString();
                                    createReservation(imageUrl);  // Create reservation with the image URL
                                    Toast.makeText(CheckoutActivity.this, "Image uploaded successfully!", Toast.LENGTH_SHORT).show();
                                });
                            })
                            // Handle failure
                            .addOnFailureListener(e -> {
                                Toast.makeText(CheckoutActivity.this, "Image upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                e.printStackTrace(); // Log the error
                            })
                            // Always close the InputStream after the task
                            .addOnCompleteListener(task -> {
                                try {
                                    if (stream != null) {
                                        stream.close();  // Close the InputStream after the upload is completed
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            });
                } else {
                    Toast.makeText(this, "Unable to open the selected image", Toast.LENGTH_SHORT).show();
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(this, "File not found: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Please upload proof of payment", Toast.LENGTH_SHORT).show();
        }
    }


    private void createReservation(String proofImageUrl) {
        // Get the current user ID
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            String userId = user.getUid();
            String email = user.getEmail();
            String phoneNumber = user.getPhoneNumber(); // This will be null if the phone number is not set

            // Generate unique reservation ID
            String reservationId = reservationsRef.push().getKey();

            // Create a Reservation object containing the userID, email, phoneNumber, cart items, status, and proof image URL
            Reservation reservation = new Reservation(userId, email, phoneNumber, checkoutCartItems, "pending", proofImageUrl);

            // Store the reservation under the specific user in Firebase
            reservationsRef.child(userId) // Store reservations under the user ID
                    .child(reservationId) // Add a reservation node with unique reservationId
                    .setValue(reservation)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Notify user of success
                            Toast.makeText(CheckoutActivity.this, "Reservation created successfully", Toast.LENGTH_SHORT).show();
                            finish(); // Close the activity after successful reservation
                        } else {
                            // Notify user of failure
                            Toast.makeText(CheckoutActivity.this, "Failed to create reservation", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(CheckoutActivity.this, "User not authenticated", Toast.LENGTH_SHORT).show();
        }
    }

}
