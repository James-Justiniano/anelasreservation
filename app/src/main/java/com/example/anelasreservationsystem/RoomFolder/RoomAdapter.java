package com.example.anelasreservationsystem.RoomFolder;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.anelasreservationsystem.R;
import com.example.anelasreservationsystem.RoomDetailFolder.RoomDetailActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RoomAdapter extends RecyclerView.Adapter<RoomAdapter.RoomViewHolder> {

    private List<Room> roomList;
    private Context context;

    // Constructor to initialize the room list and context
    public RoomAdapter(Context context, List<Room> roomList) {
        this.context = context;
        this.roomList = roomList;
    }

    @NonNull
    @Override
    public RoomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_room, parent, false);
        return new RoomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RoomViewHolder holder, int position) {
        Room room = roomList.get(position);
        holder.nameTextView.setText(room.getName());
        holder.priceTextView.setText(room.getPrice());

        // Load the first image URL from the list
        if (room.getImageURLs() != null && !room.getImageURLs().isEmpty()) {
            Glide.with(holder.imageView.getContext())
                    .load(room.getImageURLs().get(0)) // Get the first image URL
                    .into(holder.imageView);
        }

        // Set click listener for the item view
        holder.itemView.setOnClickListener(v -> {
            Toast.makeText(context, "Room clicked: " + room.getName(), Toast.LENGTH_SHORT).show();
            Log.d("RoomAdapter", "Room clicked: " + room.getName());

            // Start a new activity to show room details
            Intent intent = new Intent(context, RoomDetailActivity.class);
            intent.putExtra("roomId", room.getId()); // Pass room ID
            intent.putExtra("name", room.getName());
            intent.putExtra("price", room.getPrice());
            intent.putExtra("description", room.getDescription());
            intent.putStringArrayListExtra("imageURLs", new ArrayList<>(room.getImageURLs())); // Pass image URLs

            // Pass amenities and their prices
            Map<String, Integer> amenities = room.getAmenities();
            if (amenities != null) {
                intent.putExtra("amenities", (HashMap<String, Integer>) amenities); // Cast to HashMap
            }

            // Pass the number of adults and children
            intent.putExtra("numberOfAdults", room.getAdults());
            intent.putExtra("numberOfChildren", room.getChildren());

            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return roomList.size();
    }

    public static class RoomViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView;
        TextView priceTextView;
        ImageView imageView;

        public RoomViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.textViewRoomName);
            priceTextView = itemView.findViewById(R.id.textViewRoomPrice);
            imageView = itemView.findViewById(R.id.imageViewRoom);
        }
    }
}