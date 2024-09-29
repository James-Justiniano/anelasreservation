package com.example.anelasreservationsystem.CottageFolder;

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

import com.example.anelasreservationsystem.CottageDetailFolder.CottageDetailAdapter;
import com.example.anelasreservationsystem.R;
import com.example.anelasreservationsystem.RoomDetailFolder.RoomDetailAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class CottageFragment extends Fragment {

    private RecyclerView recyclerView;
    private CottageDetailAdapter cottageAdapter;
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

        // Set up a GridLayoutManager with 2 columns
        int numberOfColumns = 2;
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), numberOfColumns));
        recyclerView.setHasFixedSize(true);

        // Initialize the cottage list
        cottageList = new ArrayList<>();

        // Pass the context to the adapter
        cottageAdapter = new CottageDetailAdapter(getContext(), cottageList);
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
                    String cottageId = cottageSnapshot.getKey();
                    Cottage cottage = cottageSnapshot.getValue(Cottage.class);
                    if (cottage != null) {
                        // Log the data being fetched
                        Log.d("CottageFragment", "Cottage Name: " + cottage.getName());
                        Log.d("CottageFragment", "Image URL: " + cottage.getImageURLs());
                        Log.d("CottageFragment", "Price: " + cottage.getPrice());
                        cottage.setId(cottageId);
                        cottageList.add(cottage);
                    } else {
                        Log.d("CottageFragment", "Cottage data is null for: " + cottageSnapshot.getKey());
                    }
                }
                Log.d("CottageFragment", "Total Cottages Fetched: " + cottageList.size()); // Log total cottages fetched
                cottageAdapter.notifyDataSetChanged(); // Notify adapter of data changes
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("CottageFragment", "Database error: " + databaseError.getMessage());
            }
        });
    }

}
