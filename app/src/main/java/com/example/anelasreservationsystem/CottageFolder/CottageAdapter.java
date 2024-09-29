package com.example.anelasreservationsystem.CottageFolder;

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
import com.example.anelasreservationsystem.Amenity;
import com.example.anelasreservationsystem.CottageDetailFolder.CottageDetailActivity;
import com.example.anelasreservationsystem.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CottageAdapter extends RecyclerView.Adapter<CottageAdapter.CottageViewHolder> {

    private List<Cottage> cottageList;
    private Context context;

    public CottageAdapter(Context context, List<Cottage> cottageList) {
        this.cottageList = cottageList;
        this.context = context;
    }

    @NonNull
    @Override
    public CottageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cottage, parent, false);
        return new CottageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CottageViewHolder holder, int position) {
        Cottage cottage = cottageList.get(position);
        holder.nameTextView.setText(cottage.getName());
        holder.priceTextView.setText(cottage.getPrice());


        if (cottage.getImageURLs() != null && !cottage.getImageURLs().isEmpty()) {
            Glide.with(holder.imageView.getContext())
                    .load(cottage.getImageURLs().get(0)) // Get the first image URL
                    .into(holder.imageView);
        }


        holder.itemView.setOnClickListener(v -> {
            Toast.makeText(context, "Cottage clicked: " + cottage.getName(), Toast.LENGTH_SHORT).show();
            Log.d("CottageAdapter", "Cottage clicked: " + cottage.getName());

            // Start a new activity to show room details
            Intent intent = new Intent(context, CottageDetailActivity.class);
            intent.putExtra("cottageId", cottage.getId()); // Pass room ID
            intent.putExtra("name", cottage.getName());
            intent.putExtra("price", cottage.getPrice());
            intent.putExtra("description", cottage.getDescription());
            intent.putStringArrayListExtra("imageURLs", new ArrayList<>(cottage.getImageURLs())); // Pass image URLs

            // Pass amenities as a list of Amenity objects
            HashMap<String, Amenity> amenities = new HashMap<>(cottage.getAmenities());
            if (amenities != null) {
                ArrayList<Amenity> amenityList = new ArrayList<>(amenities.values());
                intent.putExtra("amenitiesList", amenityList); // Pass as Serializable
            }

            // Pass the number of adults and children
            intent.putExtra("numberOfAdults", cottage.getAdults());
            intent.putExtra("numberOfChildren", cottage.getChildren());

            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return cottageList.size();
    }

    public static class CottageViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView;
        TextView priceTextView;
        ImageView imageView;

        public CottageViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.textViewCottageName);
            priceTextView = itemView.findViewById(R.id.textViewCottagePrice);
            imageView = itemView.findViewById(R.id.imageViewCottage);
        }
    }
}
