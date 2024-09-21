
package com.example.anelasreservationsystem.CartFolder;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.anelasreservationsystem.Amenity;
import com.example.anelasreservationsystem.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private List<CartItem> cartItemList;
    private Context context;

    public CartAdapter(List<CartItem> cartItemList, Context context) {
        this.cartItemList = cartItemList;
        this.context = context;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem cartItem = cartItemList.get(position);

        // Set values to your views
        holder.roomTypeTextView.setText(cartItem.getRoomType());
        holder.quantityTextView.setText("" + cartItem.getQuantity());
        holder.totalPriceTextView.setText("₱" + String.format("%.2f", cartItem.getTotalPrice()));

        // Display check-in and check-out dates
        holder.checkInTextView.setText("Check-In: " + cartItem.getCheckInDate());
        holder.checkOutTextView.setText("Check-Out: " + cartItem.getCheckOutDate());

        // Load the first image from the imageUrls list
        List<String> imageUrls = cartItem.getImageUrls();
        if (imageUrls != null && !imageUrls.isEmpty()) {
            String firstImageUrl = imageUrls.get(0); // Get the first image URL
            Glide.with(holder.itemView.getContext())
                    .load(firstImageUrl)
                    .into(holder.roomImageView);
        } else {
            holder.roomImageView.setImageResource(R.drawable.error_image); // Placeholder for missing images
        }



        holder.cartItemCheckbox.setChecked(cartItem.isSelected());

        // Set a click listener for the checkbox to toggle selection state
        holder.cartItemCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            cartItem.setSelected(isChecked);
            // You can also notify the listener about the selection change if needed
        });


        // Set click listener for increase button
        holder.increaseButton.setOnClickListener(v -> increaseQuantity(cartItem, position));

        // Set click listener for decrease button
        holder.decreaseButton.setOnClickListener(v -> decreaseQuantity(cartItem, position));

        // Set click listener for delete button
        holder.deleteButton.setOnClickListener(v -> removeItem(position));

        holder.amenitiesLinearLayout.removeAllViews();

        // Set the amenities
        List<Amenity> amenities = cartItem.getAmenities();

        if (amenities != null && !amenities.isEmpty()) {
            for (Amenity amenity : amenities) {
                TextView amenityTextView = new TextView(holder.itemView.getContext());

                // Display the amenity name along with its price

                amenityTextView.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT));// Add some padding if needed

                amenityTextView.setPadding(0, 4, 0, 4);
                holder.amenitiesLinearLayout.addView(amenityTextView);
            }
        } else {
            TextView noAmenitiesTextView = new TextView(holder.itemView.getContext());
            noAmenitiesTextView.setText("No amenities selected");
            holder.amenitiesLinearLayout.addView(noAmenitiesTextView);
        }

    }


    @Override
    public int getItemCount() {
        return cartItemList.size();
    }

    private void increaseQuantity(CartItem cartItem, int position) {
        Log.d("CartAdapter", "Increasing quantity for item: " + cartItem.getCartItemId() + " at position: " + position);
        getRoomFromDatabase(cartItem.getRoomId(), availableQuantity -> {
            // Check if the quantity can be increased
            if (cartItem.getQuantity() < availableQuantity) {
                cartItem.setQuantity(cartItem.getQuantity() + 1); // Increase quantity
                updateTotalPrice(cartItem); // Update total price
                notifyItemChanged(position); // Refresh the view
                updateCartInFirebase(cartItem); // Update Firebase with the new cart item
            } else {
                Toast.makeText(context, "Maximum quantity reached for this room.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void decreaseQuantity(CartItem cartItem, int position) {
        if (cartItem.getQuantity() > 1) { // Prevent quantity from going below 1
            cartItem.setQuantity(cartItem.getQuantity() - 1); // Decrease quantity
            updateTotalPrice(cartItem); // Update total price
            notifyItemChanged(position); // Refresh the view
            updateCartInFirebase(cartItem); // Update Firebase
        } else {
            Toast.makeText(context, "Quantity cannot be less than 1.", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateTotalPrice(CartItem cartItem) {
        try {
            double pricePerNight = Double.parseDouble(cartItem.getPricePerNight()); // Convert pricePerNight to double
            double amenitiesPrice = cartItem.getAmenitiesPrice(); // Get amenities price
            double newTotalPrice = (pricePerNight * cartItem.getQuantity() * cartItem.getNumberOfNights()) + (amenitiesPrice * cartItem.getQuantity()); // Include amenities price
            cartItem.setTotalPrice(newTotalPrice);
        } catch (NumberFormatException e) {
            Log.e("CartAdapter", "Failed to parse pricePerNight: " + cartItem.getPricePerNight());
            cartItem.setTotalPrice(0); // Set default or handle accordingly
        }
    }


    private void updateCartInFirebase(CartItem cartItem) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String cartItemId = cartItem.getCartItemId(); // Get the cartItemId

        // Check if userId or cartItemId is null
        if (userId == null || cartItemId == null) {
            Log.e("CartAdapter", "User ID or Cart Item ID is null.");
            return; // Exit if any ID is null
        }

        // Reference to the user's cart in Firebase
        DatabaseReference cartRef = FirebaseDatabase.getInstance().getReference("cart").child(userId).child(cartItemId);

        // Update the entire cart item
        cartRef.setValue(cartItem)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("CartAdapter", "Cart item updated successfully");
                    } else {
                        Log.e("CartAdapter", "Failed to update cart item: " + task.getException().getMessage());
                    }
                });
    }



    private void removeItem(int position) {
        // Check if the position is valid
        if (position < 0 || position >= cartItemList.size()) {
            Log.e("CartAdapter", "Invalid position: " + position + ", cartItemList size: " + cartItemList.size());
            return; // Exit if position is invalid
        }

        // Get the item to remove from the list
        CartItem itemToRemove = cartItemList.get(position);
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String cartItemIdToRemove = itemToRemove.getCartItemId();

        // Reference to the user's cart in Firebase
        DatabaseReference cartRef = FirebaseDatabase.getInstance().getReference("cart").child(userId).child(cartItemIdToRemove);

        // Remove from Firebase
        cartRef.removeValue().addOnCompleteListener(removeTask -> {
            if (removeTask.isSuccessful()) {
                // Confirm the item still exists at the same position and matches the one we want to remove
                if (position >= 0 && position < cartItemList.size() && cartItemList.get(position).equals(itemToRemove)) {
                    // Remove the item from the list and notify the adapter
                    cartItemList.remove(position);
                    notifyItemRemoved(position); // Notify about item removal
                    Log.d("CartAdapter", "Item removed successfully. New list size: " + cartItemList.size());
                } else {
                    Log.e("CartAdapter", "Item at position " + position + " has already been removed or list changed. Skipping removal.");
                }
            } else {
                Log.e("CartAdapter", "Failed to remove item from Firebase: " + removeTask.getException().getMessage());
            }
        }).addOnFailureListener(e -> {
            Log.e("CartAdapter", "Error removing item from Firebase: " + e.getMessage());
        });
    }




    private void getRoomFromDatabase(String roomId, RoomQuantityCallback callback) {
        DatabaseReference roomRef = FirebaseDatabase.getInstance().getReference("rooms").child(roomId);
        roomRef.child("quantity").get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                String availableQuantityStr = task.getResult().getValue(String.class);
                try {
                    int availableQuantity = Integer.parseInt(availableQuantityStr); // Convert string to int
                    callback.onRoomQuantityFetched(availableQuantity);
                } catch (NumberFormatException e) {
                    Log.e("CartAdapter", "Failed to convert available quantity to int: " + e.getMessage());
                    callback.onRoomQuantityFetched(0); // Default to 0 if conversion fails
                }
            } else {
                Log.e("CartAdapter", "Failed to fetch room data: " + task.getException().getMessage());
                callback.onRoomQuantityFetched(0); // Default to 0 if failed
            }
        });
    }

    // Callback interface to handle the fetched room quantity
    public interface RoomQuantityCallback {
        void onRoomQuantityFetched(int availableQuantity);
    }

    public List<CartItem> getSelectedItems() {
        List<CartItem> selectedItems = new ArrayList<>();
        for (CartItem item : cartItemList) {
            if (item.isSelected()) {
                selectedItems.add(item);
            }
        }
        return selectedItems;
    }

    public static class CartViewHolder extends RecyclerView.ViewHolder {
        public ImageView imageView;
        TextView roomTypeTextView, totalPriceTextView, quantityTextView, checkInTextView, checkOutTextView,increaseButton, decreaseButton;
        ImageView roomImageView; // Add ImageView for the room image
        Button  deleteButton;
        LinearLayout amenitiesLinearLayout;
        public CheckBox cartItemCheckbox;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            roomTypeTextView = itemView.findViewById(R.id.roomTypeTextView);
            totalPriceTextView = itemView.findViewById(R.id.totalPriceTextView);
            quantityTextView = itemView.findViewById(R.id.quantityTextView);
            checkInTextView = itemView.findViewById(R.id.checkInDateTextView);
            checkOutTextView = itemView.findViewById(R.id.checkOutDateTextView);
            roomImageView = itemView.findViewById(R.id.cartRoomImageView); // Initialize the roomImageView
            increaseButton = itemView.findViewById(R.id.increaseButton);
            amenitiesLinearLayout = itemView.findViewById(R.id.amenitiesLinearLayout);
            decreaseButton = itemView.findViewById(R.id.decreaseButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
            cartItemCheckbox = itemView.findViewById(R.id.cart_item_checkbox);
        }
    }


}

