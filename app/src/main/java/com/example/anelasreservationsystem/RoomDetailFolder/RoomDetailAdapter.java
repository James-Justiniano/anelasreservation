package com.example.anelasreservationsystem.RoomDetailFolder;
import android.content.Context;
import android.content.Intent;
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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

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
        holder.nameTextView.setText(room.getName());
        holder.priceTextView.setText(room.getPrice());

        // Check if imageURLs list is not empty before loading the image
        if (room.getImageURLs() != null && !room.getImageURLs().isEmpty()) {
            Glide.with(context).load(room.getImageURLs().get(0)).into(holder.imageView); // Show the first image
        } else {
            // Optionally, set a placeholder image or handle the absence of an image
            holder.imageView.setImageResource(R.drawable.placeholder_image); // Replace with your placeholder image resource
        }

        holder.itemView.setOnClickListener(v -> {
            // Start RoomDetailActivity
            Intent intent = new Intent(context, RoomDetailActivity.class);
            intent.putExtra("roomId", room.getId());
            intent.putExtra("name", room.getName());
            intent.putExtra("description", room.getDescription());
            intent.putExtra("price", room.getPrice());
            intent.putStringArrayListExtra("imageURLs", new ArrayList<>(room.getImageURLs())); // Pass image URLs
            intent.putExtra("amenities", (Serializable) room.getAmenities());

            intent.putExtra("adults", room.getAdults()); // Pass number of adults
            intent.putExtra("children", room.getChildren()); // Pass number of children

            // Start the activity
            context.startActivity(intent);
        });
    }


    @Override
    public int getItemCount() {
        return roomList != null ? roomList.size() : 0;
    }

    public static class RoomViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView nameTextView, priceTextView;

        public RoomViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageViewRoom);
            nameTextView = itemView.findViewById(R.id.textViewRoomName);
            priceTextView = itemView.findViewById(R.id.textViewRoomPrice);
        }
    }
}
