package com.example.anelasreservationsystem;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.anelasreservationsystem.CartFolder.CartItem;

import java.util.ArrayList;
import java.util.List;

public class CheckoutActivity extends AppCompatActivity {

    private RecyclerView checkoutRecyclerView;
    private CheckoutAdapter checkoutAdapter;
    private TextView checkoutTotalPriceTextView, checkoutAmenitiesPriceTextView, checkoutPricePerNightTextView;

    private List<CartItem> checkoutCartItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        // Initialize the views
        initializeViews();

        // Get the List<CartItem> or CartItem from the intent
        ArrayList<CartItem> cartItems = (ArrayList<CartItem>) getIntent().getSerializableExtra("cartItems");
        CartItem cartItem = (CartItem) getIntent().getSerializableExtra("cartItem");

        // Initialize the list of checkout items
        checkoutCartItems = new ArrayList<>();

        // Handle null case and different item types
        if (cartItems != null && !cartItems.isEmpty()) {
            checkoutCartItems.addAll(cartItems); // Add all cart items if present
        } else if (cartItem != null) {
            checkoutCartItems.add(cartItem); // Add single cart item if present
        } else {
            showEmptyCartMessage();
            return;
        }

        // Set up RecyclerView
        setupRecyclerView();

        // Display total prices
        displayTotalPrices();
    }

    private void initializeViews() {
        checkoutRecyclerView = findViewById(R.id.checkoutRecyclerView);
        checkoutTotalPriceTextView = findViewById(R.id.checkoutTotalPriceTextView);
        checkoutAmenitiesPriceTextView = findViewById(R.id.checkoutAmenitiesPriceTextView); // Amenities price text view
        checkoutPricePerNightTextView = findViewById(R.id.checkoutPricePerNightTextView); // Price per night text view
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
        double totalPrice = 0.0;               // Total price including amenities
        double subtotalRooms = 0.0;            // Subtotal for all rooms
        double totalAmenitiesPrice = 0.0;      // Total price for amenities

        // Calculate total prices
        for (CartItem item : checkoutCartItems) {
            totalPrice += item.getTotalPrice();  // Total price including amenities
            totalAmenitiesPrice += item.getAmenitiesPrice();  // Sum up amenities prices
            subtotalRooms += item.getTotalPrice() - item.getAmenitiesPrice();  // Subtotal for rooms (total - amenities)
        }

        // Set the total price, amenities price, and subtotal in the respective TextViews
        checkoutTotalPriceTextView.setText("₱" + String.format("%.2f", totalPrice));
        checkoutAmenitiesPriceTextView.setText("₱" + String.format("%.2f", totalAmenitiesPrice));
        checkoutPricePerNightTextView.setText("₱" + String.format("%.2f", subtotalRooms)); // Display subtotal for rooms
    }
}
