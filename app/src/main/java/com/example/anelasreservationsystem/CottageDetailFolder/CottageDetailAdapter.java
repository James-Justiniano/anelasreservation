package com.example.anelasreservationsystem.CottageDetailFolder;

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
import com.example.anelasreservationsystem.Amenity;
import com.example.anelasreservationsystem.CottageFolder.Cottage;
import com.example.anelasreservationsystem.R;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CottageDetailAdapter extends RecyclerView.Adapter<CottageDetailAdapter.CottageViewHolder> {

    private final Context context;
    private final List<Cottage> cottageList;

    public CottageDetailAdapter(Context context, List<Cottage> cottageList) {
        this.context = context;
        this.cottageList = cottageList;
    }

    @NonNull
    @Override
    public CottageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_room, parent, false);
        return new CottageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CottageViewHolder holder, int position) {
        Cottage cottage = cottageList.get(position);

        // Set cottage details in the views
        holder.nameTextView.setText(cottage.getName());
        holder.priceTextView.setText(cottage.getPrice());

        // Load the first image URL from the list using Glide
        if (cottage.getImageURLs() != null && !cottage.getImageURLs().isEmpty()) {
            Glide.with(context).load(cottage.getImageURLs().get(0)).into(holder.imageView);
        } else {
            // Set a placeholder image if no image is available
            holder.imageView.setImageResource(R.drawable.placeholder_image);
        }

        // Set click listener to open CottageDetailActivity
        holder.itemView.setOnClickListener(v -> {
            // Create intent to start CottageDetailActivity
            Intent intent = new Intent(context, CottageDetailActivity.class);
            intent.putExtra("cottageId", cottage.getId());
            intent.putExtra("name", cottage.getName());
            intent.putExtra("description", cottage.getDescription());
            intent.putExtra("price", cottage.getPrice());

            // Pass the image URLs as a list
            intent.putStringArrayListExtra("imageURLs", new ArrayList<>(cottage.getImageURLs()));

            // Pass the number of adults and children
            intent.putExtra("adults", cottage.getAdults());
            intent.putExtra("children", cottage.getChildren());

            // Pass amenities as a serializable object
            if (cottage.getAmenities() != null) {
                intent.putExtra("amenities", (Serializable) convertAmenitiesToMap(cottage.getAmenities()));
            }

            // Start CottageDetailActivity
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return cottageList != null ? cottageList.size() : 0;
    }

    public static class CottageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView nameTextView, priceTextView;

        public CottageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageViewRoom);
            nameTextView = itemView.findViewById(R.id.textViewRoomName);
            priceTextView = itemView.findViewById(R.id.textViewRoomPrice);
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
