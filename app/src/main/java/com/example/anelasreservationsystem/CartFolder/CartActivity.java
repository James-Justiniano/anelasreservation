package com.example.anelasreservationsystem.CartFolder;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.anelasreservationsystem.CheckoutActivity;
import com.example.anelasreservationsystem.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class CartActivity extends AppCompatActivity {

    private RecyclerView cartRecyclerView;
    private CartAdapter cartAdapter;
    private TextView totalPriceTextView;
    private Button proceedToCheckoutButton;
    private Button continueShoppingButton;

    private List<CartItem> cartItemList = new ArrayList<>();
    private DatabaseReference cartRef;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        // Initialize Firebase
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        cartRef = FirebaseDatabase.getInstance().getReference("cart").child(userId);

        // Set up the RecyclerView
        cartRecyclerView = findViewById(R.id.cartRecyclerView);
        cartRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        totalPriceTextView = findViewById(R.id.totalPriceTextView);
        proceedToCheckoutButton = findViewById(R.id.proceedToCheckoutButton);
        continueShoppingButton = findViewById(R.id.continueShoppingButton);

        // Load cart items from Firebase
        loadCartItems();

        // Set up button click listeners
        proceedToCheckoutButton.setOnClickListener(v -> proceedToCheckout());
        continueShoppingButton.setOnClickListener(v -> continueShopping());
    }

    private void loadCartItems() {
        cartRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                cartItemList.clear();
                double totalPrice = 0.0;

                for (DataSnapshot cartItemSnapshot : snapshot.getChildren()) {
                    CartItem cartItem = cartItemSnapshot.getValue(CartItem.class);
                    if (cartItem != null) {
                        cartItemList.add(cartItem);
                        totalPrice += cartItem.getTotalPrice();
                    }
                }
                Log.d("CartActivity", "Loaded cart items count: " + cartItemList.size());

                // Initialize the adapter after loading the cart items
                if (cartItemList.isEmpty()) {
                    // Handle empty cart case
                    Toast.makeText(CartActivity.this, "Your cart is empty.", Toast.LENGTH_SHORT).show();
                } else {
                    cartAdapter = new CartAdapter(cartItemList, CartActivity.this);
                    cartRecyclerView.setAdapter(cartAdapter); // Set the adapter to the RecyclerView
                }

                // Update total price
                totalPriceTextView.setText("Total Price: ₱" + String.format("%.2f", totalPrice));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CartActivity.this, "Failed to load cart items: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void proceedToCheckout() {
        // Inflate the custom layout for the dialog
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_terms_conditions, null);
        CheckBox agreeCheckBox = dialogView.findViewById(R.id.agreeCheckBox);
        Button acceptButton = dialogView.findViewById(R.id.acceptButton);
        Button cancelButton = dialogView.findViewById(R.id.cancelButton);

        AlertDialog.Builder builder = new AlertDialog.Builder(CartActivity.this);
        builder.setView(dialogView); // Set the custom view

        // Create and show the dialog
        AlertDialog dialog = builder.create();
        dialog.show();

        // Set click listeners for buttons
        acceptButton.setOnClickListener(v -> {
            if (agreeCheckBox.isChecked()) {
                // Get selected items from the adapter
                List<CartItem> selectedItems = cartAdapter.getSelectedItems();

                Log.d("CartActivity", "Proceeding to checkout with selected items: " + selectedItems);

                if (selectedItems != null && !selectedItems.isEmpty()) {
                    Intent intent = new Intent(CartActivity.this, CheckoutActivity.class);
                    intent.putExtra("cartItems", new ArrayList<>(selectedItems)); // Convert to ArrayList
                    startActivity(intent);
                } else {
                    Toast.makeText(CartActivity.this, "Please select items to proceed to checkout.", Toast.LENGTH_SHORT).show();
                }
                dialog.dismiss(); // Dismiss dialog after accepting
            } else {
                Toast.makeText(CartActivity.this, "You must agree to the terms and conditions.", Toast.LENGTH_SHORT).show();
            }
        });

        cancelButton.setOnClickListener(v -> dialog.dismiss()); // Dismiss dialog on cancel
    }

    private void continueShopping() {
        // Navigate back to the previous activity or main shopping screen
        finish(); // or you can use startActivity(new Intent(CartActivity.this, MainActivity.class));
    }
}
