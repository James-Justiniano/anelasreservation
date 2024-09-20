
package com.example.anelasreservationsystem;


import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.anelasreservationsystem.CartFolder.CartActivity;
import com.example.anelasreservationsystem.CartFolder.CartItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class ReservationActivity extends AppCompatActivity {

    private TextView roomNameTextView, totalPriceTextView, cartItemCount;
    private EditText checkInEditText, checkOutEditText;
    private Button addToCartButton, checkoutButton;
    private String pricePerNight, roomId;
    private Calendar checkInDate, checkOutDate;
    private ImageView cartIcon;
    private double roomPricePerNight;
    private List<String> imageUrls = new ArrayList<>();
    private String firstCartCheckInDate, firstCartCheckOutDate;
    private LinearLayout amenitiesLayout;
    private List<Amenity> selectedAmenities = new ArrayList<>();
    private NumberPicker adultNumberPicker;
    private NumberPicker childNumberPicker;
    private int numberOfAdults;  // Maximum adults from room structure
    private int numberOfChildren; // Maximum children from room structure

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservation);

        initializeViews();
        setupInitialData();

        checkInEditText.setOnClickListener(v -> showDatePickerDialog(checkInEditText, checkInDate));
        checkOutEditText.setOnClickListener(v -> showDatePickerDialog(checkOutEditText, checkOutDate));

        addToCartButton.setOnClickListener(v -> checkForPreviousCartDates());

        cartIcon.setOnClickListener(v -> {
            Intent intent = new Intent(ReservationActivity.this, CartActivity.class);
            startActivity(intent);
        });
        checkoutButton.setOnClickListener(v -> proceedToCheckout());
        fetchImageUrls();
        updateCartItemCount();
    }

    private void initializeViews() {
        roomNameTextView = findViewById(R.id.roomNameTextView);
        totalPriceTextView = findViewById(R.id.totalPriceTextView);
        checkInEditText = findViewById(R.id.checkInEditText);
        checkoutButton = findViewById(R.id.checkoutButton);
        checkOutEditText = findViewById(R.id.checkOutEditText);
        addToCartButton = findViewById(R.id.addToCartButton);
        cartIcon = findViewById(R.id.cartIcon);
        cartItemCount = findViewById(R.id.cartItemCount);
        amenitiesLayout = findViewById(R.id.amenitiesLayout);

        adultNumberPicker = findViewById(R.id.adultNumberPicker);
        childNumberPicker = findViewById(R.id.childNumberPicker);

        checkInDate = Calendar.getInstance();
        checkOutDate = Calendar.getInstance();
    }
    private void proceedToCheckout() {
        String checkInText = checkInEditText.getText().toString().trim();
        String checkOutText = checkOutEditText.getText().toString().trim();

        // Check if the check-in and check-out fields are empty
        if (checkInText.isEmpty() || checkOutText.isEmpty()) {
            // Show a toast message if any field is empty
            Toast.makeText(this, "Please enter both check-in and check-out dates.", Toast.LENGTH_SHORT).show();
            return; // Exit the method to prevent further execution
        }

        // Inflate the custom layout for the terms and conditions dialog
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_terms_conditions, null);
        CheckBox agreeCheckBox = dialogView.findViewById(R.id.agreeCheckBox);
        Button acceptButton = dialogView.findViewById(R.id.acceptButton);
        Button cancelButton = dialogView.findViewById(R.id.cancelButton);

        AlertDialog.Builder builder = new AlertDialog.Builder(ReservationActivity.this);
        builder.setView(dialogView); // Set the custom view

        // Create and show the dialog
        AlertDialog dialog = builder.create();
        dialog.show();

        // Set click listeners for buttons in the dialog
        acceptButton.setOnClickListener(v -> {
            if (agreeCheckBox.isChecked()) {
                String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                String cartItemId = UUID.randomUUID().toString();

                // Calculate number of nights and total price
                long diffInMillis = checkOutDate.getTimeInMillis() - checkInDate.getTimeInMillis();
                long numberOfNights = diffInMillis / (1000 * 60 * 60 * 24);

                double roomTotalPrice = numberOfNights * Double.parseDouble(pricePerNight);
                double amenitiesTotalPrice = 0;
                for (Amenity amenity : selectedAmenities) {
                    amenitiesTotalPrice += Double.parseDouble(amenity.getPrice());
                }

                double totalPrice = roomTotalPrice + amenitiesTotalPrice;

                // Create CartItem object to pass to checkout
                CartItem cartItem = new CartItem(
                        cartItemId,
                        roomId,
                        roomNameTextView.getText().toString(),
                        checkInEditText.getText().toString(),
                        checkOutEditText.getText().toString(),
                        numberOfNights,
                        pricePerNight,
                        totalPrice,
                        1,  // Default quantity is 1
                        amenitiesTotalPrice,
                        imageUrls,
                        selectedAmenities
                );

                // Pass the cartItem object to CheckoutActivity
                Intent intent = new Intent(ReservationActivity.this, CheckoutActivity.class);
                intent.putExtra("cartItem", cartItem);
                startActivity(intent);
                dialog.dismiss(); // Dismiss dialog after accepting
            } else {
                Toast.makeText(ReservationActivity.this, "You must agree to the terms and conditions.", Toast.LENGTH_SHORT).show();
            }
        });

        cancelButton.setOnClickListener(v -> dialog.dismiss()); // Dismiss dialog on cancel
    }


    private void setupInitialData() {
        Intent intent = getIntent();
        if (intent != null) {
            roomId = intent.getStringExtra("roomId");
            pricePerNight = intent.getStringExtra("pricePerNight");
            roomNameTextView.setText(intent.getStringExtra("name"));

            // Ensure roomId is not null or empty before using it
            if (roomId != null && !roomId.isEmpty()) {
                fetchAmenities(); // Call your method to fetch amenities
                updateTotalPrice(); // Update price display after fetching data
                fetchRoomDetails(); // Fetch room details to get maxAdults and maxChildren
            } else {
                Log.e("ReservationActivity", "Room ID is null or empty");
                Toast.makeText(this, "Invalid room selection. Please try again.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Fetch room details including maxAdults and maxChildren
    private void fetchRoomDetails() {
        DatabaseReference roomRef = FirebaseDatabase.getInstance()
                .getReference("rooms").child(roomId);

        roomRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Fetch the max adults and children from the room structure
                    numberOfAdults = dataSnapshot.child("adults").getValue(Integer.class);
                    numberOfChildren = dataSnapshot.child("children").getValue(Integer.class);

                    // Set max values for NumberPicker
                    adultNumberPicker.setMaxValue(numberOfAdults);
                    adultNumberPicker.setMinValue(1); // Assuming at least one adult
                    childNumberPicker.setMaxValue(numberOfChildren);
                    childNumberPicker.setMinValue(0); // Assuming zero is allowed for children

                    // Update other UI elements as needed
                } else {
                    Log.e("ReservationActivity", "Room data not found");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("ReservationActivity", "Error fetching room details: " + databaseError.getMessage());
            }
        });
    }

    private void fetchAmenities() {
        DatabaseReference amenitiesRef = FirebaseDatabase.getInstance()
                .getReference("rooms").child(roomId).child("amenities");

        amenitiesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String amenityName = snapshot.getKey();

                    // Check if the value is a Long or String and convert accordingly
                    Object value = snapshot.getValue();
                    String amenityPrice;

                    if (value instanceof Long) {
                        amenityPrice = String.valueOf(value); // Convert Long to String
                    } else if (value instanceof String) {
                        amenityPrice = (String) value; // Use String directly
                    } else {
                        Log.e("ReservationActivity", "Unexpected type for amenity price: " + value.getClass());
                        continue; // Skip this iteration if the type is unexpected
                    }

                    if (amenityName != null && amenityPrice != null) {
                        Amenity amenity = new Amenity(amenityName, amenityPrice);
                        addAmenityCheckbox(amenity); // Pass the amenity to your method to handle the display
                    } else {
                        Log.e("ReservationActivity", "Amenity conversion failed for key: " + snapshot.getKey());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("ReservationActivity", "Error fetching amenities: " + databaseError.getMessage());
            }
        });
    }



    private void addAmenityCheckbox(Amenity amenity) {
        CheckBox checkBox = new CheckBox(this);
        checkBox.setText(amenity.getName() + " (₱" + amenity.getPrice() + ")");

        checkBox.setTextColor(getResources().getColor(R.color.black));

        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                selectedAmenities.add(amenity);
            } else {
                selectedAmenities.remove(amenity);
            }
            updateTotalPrice();
        });

        amenitiesLayout.addView(checkBox);
    }

    private void fetchImageUrls() {
        DatabaseReference imageRef = FirebaseDatabase.getInstance()
                .getReference("rooms").child(roomId).child("imageURLs");
        imageRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                imageUrls.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String imageUrl = snapshot.getValue(String.class);
                    if (imageUrl != null) {
                        imageUrls.add(imageUrl);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("ReservationActivity", "Error fetching image URLs: " + databaseError.getMessage());
            }
        });
    }

    private void showDatePickerDialog(EditText editText, Calendar calendar) {
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, month, day) -> {
                    calendar.set(year, month, day);
                    updateEditText(editText, calendar);
                    updateTotalPrice();
                },
                calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)
        );

        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private void updateEditText(EditText editText, Calendar calendar) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        editText.setText(sdf.format(calendar.getTime()));
    }

    private void updateTotalPrice() {
        long diffInMillis = checkOutDate.getTimeInMillis() - checkInDate.getTimeInMillis();
        long numberOfNights = diffInMillis / (1000 * 60 * 60 * 24);

        if (numberOfNights > 0) {
            double roomTotalPrice = numberOfNights * Double.parseDouble(pricePerNight);

            // Add the price of selected amenities
            double amenitiesTotalPrice = 0;
            for (Amenity amenity : selectedAmenities) {
                amenitiesTotalPrice += Double.parseDouble(amenity.getPrice());
            }

            double totalPrice = roomTotalPrice + amenitiesTotalPrice;
            totalPriceTextView.setText(String.format("₱%.2f", totalPrice));
        } else {
            totalPriceTextView.setText("₱0.00");
        }
    }


    private void checkForPreviousCartDates() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference cartRef = FirebaseDatabase.getInstance().getReference("cart").child(userId);

        cartRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                String newCheckInDate = sdf.format(checkInDate.getTime());
                String newCheckOutDate = sdf.format(checkOutDate.getTime());

                if (dataSnapshot.exists()) {
                    // Fetch the first cart item to check dates, but do not use its price
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        CartItem firstCartItem = snapshot.getValue(CartItem.class);
                        if (firstCartItem != null) {
                            firstCartCheckInDate = firstCartItem.getCheckInDate();
                            firstCartCheckOutDate = firstCartItem.getCheckOutDate();
                            break; // Only need the first item for date check
                        }
                    }

                    // Check if the dates match
                    if (firstCartCheckInDate.equals(newCheckInDate) && firstCartCheckOutDate.equals(newCheckOutDate)) {
                        // Use the current room price when saving the reservation
                        saveReservationToCart();
                    } else {
                        showDateMismatchDialog(newCheckInDate, newCheckOutDate);
                    }
                } else {
                    saveReservationToCart(); // No previous cart items, save directly
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("ReservationActivity", "Error fetching cart data: " + databaseError.getMessage());
            }
        });
    }


    private void showDateMismatchDialog(String newCheckInDate, String newCheckOutDate) {
        // Parse new dates
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date newCheckIn = sdf.parse(newCheckInDate);
            Date newCheckOut = sdf.parse(newCheckOutDate);

            long diffInMillis = newCheckOut.getTime() - newCheckIn.getTime();
            long numberOfNights = diffInMillis / (1000 * 60 * 60 * 24);

            new AlertDialog.Builder(this)
                    .setTitle("Dates Mismatch")
                    .setMessage("Your cart has different check-in/check-out dates. Do you want to use the same dates for this room?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        // Use the previous check-in and check-out dates
                        checkInEditText.setText(firstCartCheckInDate);
                        checkOutEditText.setText(firstCartCheckOutDate);

                        // Calculate the total price using the current room price and the previous dates
                        updateTotalPriceBasedOnNewDates(firstCartCheckInDate, firstCartCheckOutDate); // Use previous dates here

                        // Save reservation to cart with updated details
                        saveReservationToCart();
                    })
                    .setNegativeButton("No", (dialog, which) -> {
                        // Directly save the reservation to cart with the new dates
                        updateTotalPriceBasedOnNewDates(newCheckInDate, newCheckOutDate); // Use new dates
                        saveReservationToCart();
                    })
                    .show();
        } catch (ParseException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error parsing dates", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateTotalPriceBasedOnNewDates(String checkInDate, String checkOutDate) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date checkIn = sdf.parse(checkInDate);
            Date checkOut = sdf.parse(checkOutDate);

            long diffInMillis = checkOut.getTime() - checkIn.getTime();
            long numberOfNights = diffInMillis / (1000 * 60 * 60 * 24);

            // Calculate the total price based on the number of nights and the current room's price
            double totalRoomPrice = numberOfNights * roomPricePerNight;; // Use currentRoom price

            // Update the total price in the UI
            updateTotalPrice(totalRoomPrice);
        } catch (ParseException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error parsing dates", Toast.LENGTH_SHORT).show();
        }
    }



    private void updateTotalPrice(double newRoomTotalPrice) {
        // Update the total price in the UI
        totalPriceTextView.setText("₱" + String.format("%.2f", newRoomTotalPrice));
    }



    private void saveReservationToCart() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String cartItemId = UUID.randomUUID().toString();

        // Calculate the number of nights using the new check-in and check-out dates from the UI
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        try {
            Date checkInDate = sdf.parse(checkInEditText.getText().toString());
            Date checkOutDate = sdf.parse(checkOutEditText.getText().toString());

            long diffInMillis = checkOutDate.getTime() - checkInDate.getTime();
            long numberOfNights = diffInMillis / (1000 * 60 * 60 * 24);

            // Calculate the total price
            double roomTotalPrice = numberOfNights * Double.parseDouble(pricePerNight);

            // Add the price of selected amenities
            double amenitiesTotalPrice = 0;
            for (Amenity amenity : selectedAmenities) {
                amenitiesTotalPrice += Double.parseDouble(amenity.getPrice());
            }

            double totalPrice = roomTotalPrice + amenitiesTotalPrice;
            int quantity = 1; // Default quantity is 1, adjust if necessary

            // Create CartItem with all necessary details
            CartItem cartItem = new CartItem(
                    cartItemId,
                    roomId,
                    roomNameTextView.getText().toString(), // This is the roomType; change as needed
                    checkInEditText.getText().toString(),
                    checkOutEditText.getText().toString(),
                    numberOfNights,
                    pricePerNight,
                    totalPrice,
                    quantity,
                    amenitiesTotalPrice,
                    imageUrls,
                    selectedAmenities
            );

            // Save to Firebase
            DatabaseReference cartRef = FirebaseDatabase.getInstance().getReference("cart").child(userId).child(cartItemId);
            cartRef.setValue(cartItem).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(ReservationActivity.this, "Room added to cart!", Toast.LENGTH_SHORT).show();
                    updateCartItemCount();
                } else {
                    Toast.makeText(ReservationActivity.this, "Failed to add room to cart.", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (ParseException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error parsing check-in or check-out date", Toast.LENGTH_SHORT).show();
        }
    }


    private void updateCartItemCount() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference cartRef = FirebaseDatabase.getInstance().getReference("cart").child(userId);

        cartRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int count = (int) dataSnapshot.getChildrenCount();
                cartItemCount.setText(String.valueOf(count));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("ReservationActivity", "Error fetching cart item count: " + databaseError.getMessage());
            }
        });
    }
}