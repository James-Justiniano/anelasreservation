package com.example.anelasreservationsystem;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;

public class CottageAdapter extends RecyclerView.Adapter<CottageAdapter.CottageViewHolder> {

    private List<Cottage> cottageList;

    public CottageAdapter(List<Cottage> cottageList) {
        this.cottageList = cottageList;
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
        // Use Glide to load the image with a placeholder and error image
        String imageUrl = cottage.getImageURL();
        Log.d("CottageAdapter", "Image URL: " + imageUrl);
        Glide.with(holder.imageView.getContext())
                .load(cottage.getImageURL())
                .placeholder(R.drawable.placeholder_image) // Placeholder image
                .error(R.drawable.error_image) // Error image
                .into(holder.imageView);
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
