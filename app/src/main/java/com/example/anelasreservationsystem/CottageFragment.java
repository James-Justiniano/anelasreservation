package com.example.anelasreservationsystem;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class CottageFragment extends Fragment {

    private RecyclerView recyclerView;
    private CottageAdapter cottageAdapter;
    private List<Cottage> cottageList;
    private DatabaseReference databaseReference;

    public CottageFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cottage, container, false);

        // Initialize RecyclerView
        recyclerView = view.findViewById(R.id.recyclerViewCottages);

        // Set up a GridLayoutManager with 2 columns (you can adjust the number of columns as needed)
        int numberOfColumns = 2; // Adjust this value to control how many items are in a row
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), numberOfColumns));
        recyclerView.setHasFixedSize(true);

        // Initialize the cottage list
        cottageList = new ArrayList<>();

        // Initialize the adapter and set it to the RecyclerView
        cottageAdapter = new CottageAdapter(cottageList);
        recyclerView.setAdapter(cottageAdapter);

        // Initialize Firebase reference
        databaseReference = FirebaseDatabase.getInstance().getReference("cottages");

        // Fetch data from Firebase
        fetchCottageData();

        return view;
    }

    private void fetchCottageData() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                cottageList.clear(); // Clear the list before adding new data
                for (DataSnapshot cottageSnapshot : dataSnapshot.getChildren()) {
                    Cottage cottage = cottageSnapshot.getValue(Cottage.class);
                    if (cottage != null) {
                        // Log the data being fetched
                        Log.d("CottageFragment", "Cottage Name: " + cottage.getName());
                        Log.d("CottageFragment", "Image URL: " + cottage.getImageURL()); // Use imageURL here
                        Log.d("CottageFragment", "Price: " + cottage.getPrice());

                        cottageList.add(cottage);
                    } else {
                        Log.d("CottageFragment", "Cottage data is null for: " + cottageSnapshot.getKey());
                    }
                }
                cottageAdapter.notifyDataSetChanged(); // Notify adapter of data changes
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("CottageFragment", "Database error: " + databaseError.getMessage());
            }
        });
    }

}
