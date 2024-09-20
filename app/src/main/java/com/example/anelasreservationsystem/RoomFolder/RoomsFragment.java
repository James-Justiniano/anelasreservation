
package com.example.anelasreservationsystem.RoomFolder;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.anelasreservationsystem.R;
import com.example.anelasreservationsystem.RoomDetailFolder.RoomDetailAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class RoomsFragment extends Fragment {

    private RecyclerView recyclerView;
    private RoomDetailAdapter roomAdapter; // Assuming this is the adapter for displaying room details
    private List<Room> roomList;
    private DatabaseReference databaseReference;

    public RoomsFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_room, container, false);

        // Initialize RecyclerView
        recyclerView = view.findViewById(R.id.recyclerViewRooms);

        // Set up a GridLayoutManager with 2 columns
        int numberOfColumns = 2; // Adjust this value to control how many items are in a row
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), numberOfColumns));
        recyclerView.setHasFixedSize(true);

        // Initialize the room list and adapter
        roomList = new ArrayList<>();
        roomAdapter = new RoomDetailAdapter(getContext(), roomList); // Use RoomDetailAdapter
        recyclerView.setAdapter(roomAdapter);

        // Initialize Firebase reference
        databaseReference = FirebaseDatabase.getInstance().getReference("rooms");

        // Fetch data from Firebase
        fetchRoomData();

        return view;
    }

    private void fetchRoomData() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                roomList.clear(); // Clear the list before adding new data
                for (DataSnapshot roomSnapshot : dataSnapshot.getChildren()) {
                    // Get the unique key (ID) for the room
                    String roomId = roomSnapshot.getKey();
                    // Get the room details
                    Room room = roomSnapshot.getValue(Room.class);
                    if (room != null) {
                        // Set the room ID to the room object
                        room.setId(roomId); // Assuming you have a setId method in your Room class
                        roomList.add(room);
                    }
                }
                roomAdapter.notifyDataSetChanged(); // Notify adapter of data changes
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle possible errors
            }
        });
    }

}