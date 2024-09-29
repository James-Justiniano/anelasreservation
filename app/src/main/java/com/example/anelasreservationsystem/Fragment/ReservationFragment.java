package com.example.anelasreservationsystem.Fragment;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.anelasreservationsystem.PendingItemFolder.PendingFragment;
import com.example.anelasreservationsystem.imageslideradapter.DrawableImageSliderAdapter;
import com.example.anelasreservationsystem.R;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;


import java.util.ArrayList;
import java.util.List;

public class ReservationFragment extends Fragment {

    private ViewPager2 imageSlider;
    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private Handler sliderHandler = new Handler();

    private Runnable sliderRunnable = new Runnable() {
        @Override
        public void run() {
            if (imageSlider.getAdapter() != null) {
                int currentItem = imageSlider.getCurrentItem();
                int nextItem = (currentItem + 1) % imageSlider.getAdapter().getItemCount(); // Loop back to the start
                imageSlider.setCurrentItem(nextItem, true); // Smooth scroll to the next item
                sliderHandler.postDelayed(this, 3000); // Slide every 3 seconds
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reservation, container, false);

        // Initialize Image Slider
        initializeImageSlider(view);

        // Initialize TabLayout and ViewPager
        tabLayout = view.findViewById(R.id.tabLayout);
        viewPager = view.findViewById(R.id.viewPager);
        viewPager.setAdapter(new ReservationPagerAdapter(requireActivity())); // Use requireActivity()

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("Pending");

                    break;
                case 1:
                    tab.setText("Completed");
                    break;
                case 2:
                    tab.setText("Cancelled");
                    break;
            }
        }).attach();

        return view;
    }

    private void initializeImageSlider(View view) {
        imageSlider = view.findViewById(R.id.imageSlider);
        List<Integer> imageResources = new ArrayList<>();

        // Add drawable resources
        imageResources.add(R.drawable.logoanela);
        imageResources.add(R.drawable.imageslider1);
        imageResources.add(R.drawable.imageslider2);
        imageResources.add(R.drawable.imageslider3);
        imageResources.add(R.drawable.imageslider4);

        // Check if context is null before passing to adapter
        if (getContext() != null) {
            // Set up the adapter
            DrawableImageSliderAdapter drawableImageSliderAdapter = new DrawableImageSliderAdapter(getContext(), imageResources);
            imageSlider.setAdapter(drawableImageSliderAdapter);
        }

        // Start auto sliding
        sliderHandler.postDelayed(sliderRunnable, 3000); // Start sliding after 3 seconds
    }

    @Override
    public void onResume() {
        super.onResume();
        sliderHandler.postDelayed(sliderRunnable, 3000); // Resume auto sliding
    }

    @Override
    public void onPause() {
        super.onPause();
        sliderHandler.removeCallbacks(sliderRunnable); // Stop auto sliding when fragment is not visible
    }

    private static class ReservationPagerAdapter extends FragmentStateAdapter {

        public ReservationPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0:
                    return new PendingFragment();
                case 1:
                    return new CompletedFragment();
                case 2:
                    return new CancelledFragment();
                default:
                    return new PendingFragment(); // Default case
            }
        }

        @Override
        public int getItemCount() {
            return 3; // Total number of tabs
        }
    }
}
