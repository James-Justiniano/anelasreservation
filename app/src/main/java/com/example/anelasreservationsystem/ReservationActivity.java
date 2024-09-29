package com.example.anelasreservationsystem;


import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
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
import java.util.Map;
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
    private int selectedAdults = 1; // Default value (e.g., 1 adult)
    private int selectedChildren = 0; // Default value (e.g., 0 children)

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
                    // Convert amenity price from int to double
                    amenitiesTotalPrice += amenity.getPrice() * amenity.getQuantity();
                }

                double totalPrice = roomTotalPrice + amenitiesTotalPrice;

                // Use the selected number of adults and children
                int selectedAdults = adultNumberPicker.getValue(); // Get selected value from NumberPicker
                int selectedChildren = childNumberPicker.getValue(); // Get selected value from NumberPicker

                // Create CartItem object to pass to checkout, including adults and children
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
                        selectedAmenities,
                        selectedAdults,   // Pass selected adults
                        selectedChildren  // Pass selected children
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

    private void fetchRoomDetails() {
        DatabaseReference roomRef = FirebaseDatabase.getInstance()
                .getReference("rooms").child(roomId);

        roomRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Fetch the max adults and children from the room structure
                    Integer numberOfAdults = dataSnapshot.child("adults").getValue(Integer.class);
                    Integer numberOfChildren = dataSnapshot.child("children").getValue(Integer.class);

                    // Handle null values and set max for NumberPicker
                    adultNumberPicker.setMaxValue(numberOfAdults != null ? numberOfAdults : 1);
                    adultNumberPicker.setMinValue(1); // Assuming at least one adult

                    childNumberPicker.setMaxValue(numberOfChildren != null ? numberOfChildren : 0);
                    childNumberPicker.setMinValue(0); // Assuming zero is allowed for children

                    // Add listeners to capture selected values
                    adultNumberPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
                        @Override
                        public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                            // Update selected adults value
                            selectedAdults = newVal;
                            Log.d("ReservationActivity", "Selected Adults: " + selectedAdults);
                        }
                    });

                    childNumberPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
                        @Override
                        public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                            // Update selected children value
                            selectedChildren = newVal;
                            Log.d("ReservationActivity", "Selected Children: " + selectedChildren);
                        }
                    });

                    // Optionally, set default values
                    adultNumberPicker.setValue(1);  // Default selection for adults
                    childNumberPicker.setValue(0);  // Default selection for children

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
                    String amenityName = (String) snapshot.child("name").getValue();
                    int amenityPrice = ((Long) snapshot.child("price").getValue()).intValue();
                    int amenityQuantity = ((Long) snapshot.child("quantity").getValue()).intValue();

                    // Debugging: Log the amenity quantity
                    Log.d("Amenities", "Fetched amenity: " + amenityName + " with quantity: " + amenityQuantity);

                    Amenity amenity = new Amenity(amenityName, amenityPrice, amenityQuantity);
                    addAmenityQuantityControl(amenity);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("ReservationActivity", "Error fetching amenities: " + databaseError.getMessage());
            }
        });
    }

    private void addAmenityQuantityControl(Amenity amenity) {
        // Create a horizontal LinearLayout to hold the quantity controls
        LinearLayout amenityLayout = new LinearLayout(this);
        amenityLayout.setOrientation(LinearLayout.HORIZONTAL);
        amenityLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        // Create a TextView for the amenity name and price
        TextView amenityTextView = new TextView(this);
        amenityTextView.setText(amenity.getName() + " (₱" + amenity.getPrice() + ")");
        amenityTextView.setTextColor(getResources().getColor(R.color.black));
        amenityTextView.setLayoutParams(new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1 // Weight to allow it to take up available space
        ));

        // Create EditText for displaying quantity, starting at 0
        EditText quantityEditText = new EditText(this);
        quantityEditText.setLayoutParams(new LinearLayout.LayoutParams(
                100, // Smaller width (adjust as needed)
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        quantityEditText.setText("0"); // Start with 0 quantity
        quantityEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
        quantityEditText.setEnabled(false); // Disable editing
        quantityEditText.setTextColor(getResources().getColor(R.color.textcolor));

        // Create Buttons for increasing and decreasing quantity
        Button increaseButton = new Button(this);
        increaseButton.setText("+");
        increaseButton.setLayoutParams(new LinearLayout.LayoutParams(
                100, // Smaller width for the increase button
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        Button decreaseButton = new Button(this);
        decreaseButton.setText("-");
        decreaseButton.setLayoutParams(new LinearLayout.LayoutParams(
                100, // Smaller width for the decrease button
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        // Set OnClickListener for increase button
        increaseButton.setOnClickListener(v -> {
            int currentQuantity = Integer.parseInt(quantityEditText.getText().toString());
            int maxQuantity = amenity.getQuantity(); // Maximum allowed quantity

            // Debugging: Log the current and maximum quantity
            Log.d("Amenities", "Current quantity: " + currentQuantity + ", Max quantity: " + maxQuantity);

            // Only increase if current quantity is less than max quantity
            if (currentQuantity < maxQuantity) {
                currentQuantity++;
                quantityEditText.setText(String.valueOf(currentQuantity));
                Log.d("Amenities", "Updated quantity: " + currentQuantity); // Log after update
                updateSelectedAmenities(amenity, currentQuantity);
                updateTotalPrice(); // Call updateTotalPrice to recalculate total
            } else {
                Toast.makeText(this, "Cannot exceed maximum quantity of " + maxQuantity, Toast.LENGTH_SHORT).show();
            }
        });

        // Set OnClickListener for decrease button
        decreaseButton.setOnClickListener(v -> {
            int currentQuantity = Integer.parseInt(quantityEditText.getText().toString());
            if (currentQuantity > 0) {
                currentQuantity--;
                quantityEditText.setText(String.valueOf(currentQuantity));
                Log.d("Amenities", "Decreased quantity: " + currentQuantity); // Log after decrease
                updateSelectedAmenities(amenity, currentQuantity);
                updateTotalPrice(); // Call updateTotalPrice to recalculate total
            }
        });

        // Add views to the layout
        amenityLayout.addView(amenityTextView);
        amenityLayout.addView(decreaseButton);
        amenityLayout.addView(quantityEditText);
        amenityLayout.addView(increaseButton);

        // Add the horizontal layout to the main layout
        amenitiesLayout.addView(amenityLayout);
    }


    private void updateSelectedAmenities(Amenity amenity, int quantity) {
        boolean exists = false;
        for (Amenity selectedAmenity : selectedAmenities) {
            if (selectedAmenity.getName().equals(amenity.getName())) {
                selectedAmenity.setQuantity(quantity);
                Log.d("Amenities", "Updated " + amenity.getName() + " to quantity: " + quantity);
                exists = true;
                break;
            }
        }

        if (!exists && quantity > 0) {
            Amenity newAmenity = new Amenity(amenity.getName(), amenity.getPrice(), quantity); // Pass quantity
            selectedAmenities.add(newAmenity);
            Log.d("Amenities", "Added " + newAmenity.getName() + " with quantity: " + quantity);
        } else if (exists && quantity == 0) {
            selectedAmenities.removeIf(selectedAmenity -> selectedAmenity.getName().equals(amenity.getName()));
            Log.d("Amenities", "Removed " + amenity.getName() + " from selected amenities.");
        }
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
            // Calculate room total price
            double roomTotalPrice = numberOfNights * Double.parseDouble(pricePerNight);

            // Add the price of selected amenities
            double amenitiesTotalPrice = 0;
            for (Amenity amenity : selectedAmenities) {
                amenitiesTotalPrice += amenity.getPrice() * amenity.getQuantity(); // Calculate total based on quantity
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

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        try {
            Date checkInDate = sdf.parse(checkInEditText.getText().toString());
            Date checkOutDate = sdf.parse(checkOutEditText.getText().toString());

            if (checkInDate != null && checkOutDate != null) {
                long diffInMillis = checkOutDate.getTime() - checkInDate.getTime();
                long numberOfNights = diffInMillis / (1000 * 60 * 60 * 24);

                if (numberOfNights > 0) {
                    double roomTotalPrice = numberOfNights * Double.parseDouble(pricePerNight);

                    // Calculate the total price of selected amenities
                    double amenitiesTotalPrice = 0;
                    for (Amenity amenity : selectedAmenities) {
                        amenitiesTotalPrice += amenity.getPrice() * amenity.getQuantity(); // Multiply price by quantity
                    }

                    double totalPrice = roomTotalPrice + amenitiesTotalPrice;

                    // Get selected adults and children from NumberPicker
                    int selectedAdults = adultNumberPicker.getValue();
                    int selectedChildren = childNumberPicker.getValue();

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
                            1, // Quantity default is 1
                            amenitiesTotalPrice,
                            imageUrls,
                            selectedAmenities,
                            selectedAdults,
                            selectedChildren
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
                } else {
                    Toast.makeText(this, "Check-out date must be after check-in date.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Error parsing check-in or check-out date.", Toast.LENGTH_SHORT).show();
            }
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