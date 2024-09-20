package com.example.anelasreservationsystem;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.anelasreservationsystem.CartFolder.CartItem;
import com.example.anelasreservationsystem.R;

import java.util.List;

public class CheckoutAdapter extends RecyclerView.Adapter<CheckoutAdapter.CheckoutViewHolder> {

    private List<CartItem> cartItems;
    private Context context;

    public CheckoutAdapter(List<CartItem> cartItems, Context context) {
        this.cartItems = cartItems;
        this.context = context;
    }

    @NonNull
    @Override
    public CheckoutViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.checkout_item, parent, false);
        return new CheckoutViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CheckoutViewHolder holder, int position) {
        CartItem cartItem = cartItems.get(position);

        // Set room type and price details
        holder.roomNameTextView.setText(cartItem.getRoomType());

        // Display the price per night
        holder.roomNumberofNightsTextView .setText("Nights: " + cartItem.getNumberOfNights());

        // Display the total price
        holder.roomTotalTextView.setText("Total: ₱" + String.format("%.2f", cartItem.getTotalPrice()));

        // Display the check-in and check-out dates
        holder.checkInDateTextView.setText("Check-in: " + cartItem.getCheckInDate());
        holder.checkOutDateTextView.setText("Check-out: " + cartItem.getCheckOutDate());

        // Display the quantity
        holder.roomQuantityTextView.setText( cartItem.getQuantity()+ "x" );

        // Display the amenities price
        holder.amenitiesPriceTextView.setText("Amenities Price: ₱" + String.format("%.2f", cartItem.getAmenitiesPrice()));

        // Handle image loading with Glide
        if (cartItem.getImageUrls() != null && !cartItem.getImageUrls().isEmpty()) {
            Glide.with(context)
                    .load(cartItem.getImageUrls().get(0))  // Load the first image in the list
                    .placeholder(R.drawable.placeholder_image)  // Show a placeholder while loading
                    .into(holder.roomImageView);
        } else {
            holder.roomImageView.setImageResource(R.drawable.placeholder_image);  // Use a placeholder image if no image is available
        }
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    public static class CheckoutViewHolder extends RecyclerView.ViewHolder {

        TextView roomNameTextView, roomNumberofNightsTextView , roomTotalTextView;
        TextView checkInDateTextView, checkOutDateTextView, roomQuantityTextView, amenitiesPriceTextView;
        ImageView roomImageView;

        public CheckoutViewHolder(@NonNull View itemView) {
            super(itemView);
            roomNameTextView = itemView.findViewById(R.id.checkoutRoomNameTextView);
            roomNumberofNightsTextView = itemView.findViewById(R.id.checkoutroomNumberofNightsTextView );
            roomTotalTextView = itemView.findViewById(R.id.checkoutRoomTotalTextView);
            checkInDateTextView = itemView.findViewById(R.id.checkoutRoomCheckInDateTextView);
            checkOutDateTextView = itemView.findViewById(R.id.checkoutRoomCheckOutDateTextView);
            roomQuantityTextView = itemView.findViewById(R.id.checkoutRoomQuantityTextView);
            amenitiesPriceTextView = itemView.findViewById(R.id.checkoutRoomAmenitiesPriceTextView);
            roomImageView = itemView.findViewById(R.id.checkoutRoomImageView);
        }
    }
}
