package com.example.anelasreservationsystem.RoomDetailFolder;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.anelasreservationsystem.R;
import com.example.anelasreservationsystem.RoomFolder.Room;
import com.example.anelasreservationsystem.Amenity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RoomDetailAdapter extends RecyclerView.Adapter<RoomDetailAdapter.RoomViewHolder> {

    private final Context context;
    private final List<Room> roomList;

    public RoomDetailAdapter(Context context, List<Room> roomList) {
        this.context = context;
        this.roomList = roomList;
    }

    @NonNull
    @Override
    public RoomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_room, parent, false);
        return new RoomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RoomViewHolder holder, int position) {
        Room room = roomList.get(position);

        // Set room details in the views
        holder.nameTextView.setText(room.getName());
        holder.priceTextView.setText(room.getPrice());

        // Load the first image URL from the list using Glide
        if (room.getImageURLs() != null && !room.getImageURLs().isEmpty()) {
            Glide.with(context).load(room.getImageURLs().get(0)).into(holder.imageView); // Load the first image
        } else {
            holder.imageView.setImageResource(R.drawable.placeholder_image); // Set a placeholder image
        }

        // Check room availability
        if (room.getQuantity() == 0) {  // Assuming quantity 0 means not available
            holder.overlayView.setVisibility(View.VISIBLE); // Show overlay
            holder.notAvailableTextView.setVisibility(View.VISIBLE); // Show "Not Available" text
        } else {
            holder.overlayView.setVisibility(View.GONE); // Hide overlay
            holder.notAvailableTextView.setVisibility(View.GONE); // Hide "Not Available" text
        }

        // Set click listener to open RoomDetailActivity only if enabled
        holder.itemView.setOnClickListener(v -> {
            if (room.getQuantity() > 0) {
                // Create intent to start RoomDetailActivity
                Intent intent = new Intent(context, RoomDetailActivity.class);
                intent.putExtra("roomId", room.getId());
                intent.putExtra("name", room.getName());
                intent.putExtra("description", room.getDescription());
                intent.putExtra("price", room.getPrice());

                // Pass the image URLs as a list
                intent.putStringArrayListExtra("imageURLs", new ArrayList<>(room.getImageURLs()));

                // Pass the number of adults and children
                intent.putExtra("adults", room.getAdults());
                intent.putExtra("children", room.getChildren());

                // Convert amenities to HashMap and pass it as Serializable
                if (room.getAmenities() != null) {
                    intent.putExtra("amenities", (Serializable) convertAmenitiesToMap(room.getAmenities()));
                }

                // Start RoomDetailActivity
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return roomList != null ? roomList.size() : 0;
    }

    public static class RoomViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView nameTextView, priceTextView, notAvailableTextView;
        View overlayView; // Declare the overlay view

        public RoomViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageViewRoom);
            nameTextView = itemView.findViewById(R.id.textViewRoomName);
            overlayView = itemView.findViewById(R.id.overlayView); // Initialize the overlay view
            priceTextView = itemView.findViewById(R.id.textViewRoomPrice);
            notAvailableTextView = itemView.findViewById(R.id.textViewNotAvailable);
        }
    }

    private Map<String, Integer> convertAmenitiesToMap(Map<String, Amenity> amenities) {
        Map<String, Integer> amenitiesMap = new HashMap<>();
        for (Map.Entry<String, Amenity> entry : amenities.entrySet()) {
            String amenityName = entry.getValue().getName();
            int amenityPrice = entry.getValue().getPrice();
            amenitiesMap.put(amenityName, amenityPrice);
        }
        return amenitiesMap;
    }
}
