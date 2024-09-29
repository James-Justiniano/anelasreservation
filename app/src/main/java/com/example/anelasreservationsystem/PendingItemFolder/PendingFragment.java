package com.example.anelasreservationsystem.PendingItemFolder;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;



import com.example.anelasreservationsystem.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class PendingFragment extends Fragment {

    private RecyclerView pendingRecyclerView;
    private PendingAdapter pendingAdapter;
    private List<PendingItem> pendingItemList;
    private DatabaseReference reservationsRef;

    public PendingFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pending, container, false);

        pendingRecyclerView = view.findViewById(R.id.pendingRecyclerView);
        pendingRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        pendingItemList = new ArrayList<>();
        pendingAdapter = new PendingAdapter(pendingItemList);
        pendingRecyclerView.setAdapter(pendingAdapter);

        // Fetch pending items from Firebase
        reservationsRef = FirebaseDatabase.getInstance().getReference("reservations");
        reservationsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d("PendingFragment", "Data fetched: " + dataSnapshot.toString());
                pendingItemList.clear();

                for (DataSnapshot userReservationSnapshot : dataSnapshot.getChildren()) {
                    for (DataSnapshot reservationSnapshot : userReservationSnapshot.getChildren()) {
                        // Get the status from the reservationSnapshot
                        String status = reservationSnapshot.child("status").getValue(String.class);
                        Log.d("PendingFragment", "Reservation status: " + status);

                        if ("pending".equals(status)) {
                            // Assuming items are structured under 'items' directly
                            for (DataSnapshot itemSnapshot : reservationSnapshot.child("items").getChildren()) {
                                // Fetch the data
                                String cartItemId = itemSnapshot.child("cartItemId").getValue(String.class);
                                String roomType = itemSnapshot.child("roomType").getValue(String.class);
                                String checkInDate = itemSnapshot.child("checkInDate").getValue(String.class);
                                String checkOutDate = itemSnapshot.child("checkOutDate").getValue(String.class);
                                double totalPrice = itemSnapshot.child("totalPrice").getValue(Double.class);
                                String roomId = itemSnapshot.child("roomId").getValue(String.class);
                                int numberOfNights = itemSnapshot.child("numberOfNights").getValue(Integer.class);

                                // Fetch the list of image URLs
                                List<String> imageUrls = new ArrayList<>();
                                for (DataSnapshot imageUrlSnapshot : itemSnapshot.child("imageUrls").getChildren()) {
                                    String imageUrl = imageUrlSnapshot.getValue(String.class);
                                    imageUrls.add(imageUrl);
                                }

                                int quantity = itemSnapshot.child("quantity").getValue(Integer.class); // Fetch quantity
                                int selectedAdults = itemSnapshot.child("selectedAdults").getValue(Integer.class); // Fetch selected adults
                                int selectedChildren = itemSnapshot.child("selectedChildren").getValue(Integer.class); // Fetch selected children
                                double amenitiesPrice = itemSnapshot.child("amenitiesPrice").getValue(Double.class); // Fetch amenities price

                                // Create and add the PendingItem with a List of image URLs
                                PendingItem pendingItem = new PendingItem(cartItemId, roomType, checkInDate, checkOutDate, totalPrice, roomId,
                                        numberOfNights, imageUrls, quantity, selectedAdults, selectedChildren, amenitiesPrice);
                                pendingItemList.add(pendingItem);
                                Log.d("PendingFragment", "Added item: " + pendingItem.toString());
                            }
                        }
                    }
                }
                pendingAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle potential errors here
            }
        });

        return view;
    }
}
