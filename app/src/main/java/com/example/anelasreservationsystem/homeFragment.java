package com.example.anelasreservationsystem;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;
import com.bumptech.glide.Glide;
import com.example.anelasreservationsystem.CategoryFolder.Category;
import com.example.anelasreservationsystem.CategoryFolder.CategoryAdapter;
import com.example.anelasreservationsystem.CottageFolder.CottageFragment;
import com.example.anelasreservationsystem.RoomFolder.Room;
import com.example.anelasreservationsystem.RoomFolder.RoomAdapter;
import com.example.anelasreservationsystem.RoomFolder.RoomsFragment;
import com.example.anelasreservationsystem.imageslideradapter.DrawableImageSliderAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class homeFragment extends Fragment implements CategoryAdapter.OnCategoryClickListener {

    private RecyclerView recyclerViewCategories;
    private CategoryAdapter categoryAdapter;
    private List<Category> categoryList;

    private RecyclerView recyclerViewRooms;
    private RoomAdapter roomAdapter;
    private List<Room> popularRoomList;

    private ViewPager2 imageSlider;
    private List<Integer> imageList;

    // Handler for automatic sliding
    private Handler sliderHandler = new Handler();

    // Runnable to change the current item of the ViewPager2
    private Runnable sliderRunnable = new Runnable() {
        @Override
        public void run() {
            int currentItem = imageSlider.getCurrentItem();
            int nextItem = currentItem + 1;
            if (nextItem >= imageList.size()) {
                nextItem = 0;
            }
            imageSlider.setCurrentItem(nextItem);
            sliderHandler.postDelayed(this, 3000); // Slide every 3 seconds
        }
    };

    // Views for user profile
    private TextView fullNameTextView;
    private ImageView profileImageView;

    // Firebase references
    private DatabaseReference userRef;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    public homeFragment() {
        // Required empty public constructor
    }

    public static homeFragment newInstance(String param1, String param2) {
        homeFragment fragment = new homeFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            // Handle parameters if needed
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize category RecyclerView
        initializeCategoryRecyclerView(view);

        // Initialize popular rooms RecyclerView
        initializePopularRoomsRecyclerView(view);

        // Initialize Image Slider
        initializeImageSlider(view);

        // Fetch popular rooms from Firebase
        fetchPopularRooms();

        // Fetch user profile data from Firebase
        fetchUserProfileData(view);

        return view;
    }

    private void initializeCategoryRecyclerView(View view) {
        recyclerViewCategories = view.findViewById(R.id.recyclerViewCategories);
        categoryList = new ArrayList<>();
        categoryList.add(new Category("Cabins", R.drawable.cabins));
        categoryList.add(new Category("Rooms", R.drawable.catrooms));
        categoryList.add(new Category("Cottage", R.drawable.catcottage));
        categoryList.add(new Category("Category", R.drawable.cat_4));
        categoryList.add(new Category("Category", R.drawable.cat_5));

        categoryAdapter = new CategoryAdapter(categoryList, getContext(), this);
        recyclerViewCategories.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        recyclerViewCategories.setAdapter(categoryAdapter);
    }

    private void initializePopularRoomsRecyclerView(View view) {
        recyclerViewRooms = view.findViewById(R.id.recyclerViewPopularRooms);
        popularRoomList = new ArrayList<>();
        roomAdapter = new RoomAdapter(getContext(), popularRoomList);
        recyclerViewRooms.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        recyclerViewRooms.setAdapter(roomAdapter);

        // Add spacing item decoration
        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.recycler_view_item_spacing);
        recyclerViewRooms.addItemDecoration(new HorizontalSpacingItemDecoration(spacingInPixels));
    }

    private void initializeImageSlider(View view) {
        imageSlider = view.findViewById(R.id.imageSlider);

        // Add some image resources
        imageList = new ArrayList<>();
        imageList.add(R.drawable.logoanela);
        imageList.add(R.drawable.imageslider1);
        imageList.add(R.drawable.imageslider2);
        imageList.add(R.drawable.imageslider3);
        imageList.add(R.drawable.imageslider4);

        // Use DrawableImageSliderAdapter to display drawable images
        DrawableImageSliderAdapter drawableImageSliderAdapter = new DrawableImageSliderAdapter(getContext(), imageList);
        imageSlider.setAdapter(drawableImageSliderAdapter);

        // Start auto sliding
        sliderHandler.postDelayed(sliderRunnable, 3000); // Start sliding after 3 seconds
    }

    private void fetchPopularRooms() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("PopularRooms");

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                popularRoomList.clear(); // Clear the list before adding new data
                for (DataSnapshot roomSnapshot : dataSnapshot.getChildren()) {
                    Room room = roomSnapshot.getValue(Room.class);
                    if (room != null) {
                        popularRoomList.add(room); // Add the room to the list
                    }
                }
                roomAdapter.notifyDataSetChanged(); // Notify the adapter to refresh the view
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle error here
            }
        });
    }

    private void fetchUserProfileData(View view) {
        // Initialize views for full name and profile image
        fullNameTextView = view.findViewById(R.id.textView2);
        profileImageView = view.findViewById(R.id.imageView3);

        // Get the current user
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        // Ensure the user is signed in
        if (currentUser != null) {
            String userId = currentUser.getUid();

            // Reference to the Users node in the database
            userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);

            // Retrieve user data
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        // Retrieve the fullName and profileImageUrl
                        String fullName = dataSnapshot.child("fullName").getValue(String.class);
                        String profileImageUrl = dataSnapshot.child("profileImageUrl").getValue(String.class);

                        // Set the fullName to the TextView
                        fullNameTextView.setText("Hi " + fullName);

                        // Load the profile image using Glide
                        if (profileImageUrl != null) {
                            Glide.with(getContext()).load(profileImageUrl).circleCrop().into(profileImageView);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle database error
                }
            });
        }
    }

    @Override
    public void onCategoryClick(int position) {
        Fragment fragment = null;

        // Determine which fragment to display based on the clicked category
        switch (position) {
            case 0:
                fragment = new CabinsFragment();
                break;
            case 1:
                fragment = new RoomsFragment();
                break;
            case 2:
                fragment = new CottageFragment();
                break;
            default:
                break;
        }

        // Replace the fragment in the container if it's not null
        if (fragment != null) {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        sliderHandler.postDelayed(sliderRunnable, 3000); // Resume auto sliding when the fragment is visible
    }

    @Override
    public void onPause() {
        super.onPause();
        sliderHandler.removeCallbacks(sliderRunnable); // Stop auto sliding when the fragment is not visible
    }
}
