package com.example.anelasreservationsystem.CottageDetailFolder;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.anelasreservationsystem.ReservationActivity;
import com.example.anelasreservationsystem.imageslideradapter.ImageSliderAdapter;
import com.example.anelasreservationsystem.R;

import java.util.List;
import java.util.Map;

public class CottageDetailActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private TextView nameTextView, descriptionTextView, priceTextView, amenitiesTextView, adultsTextView, childrenTextView;
    private Button nextButton;
    private LinearLayout dotIndicator;
    private TextView[] dots;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cottage_detail);

        // Initialize UI elements
        viewPager = findViewById(R.id.viewPagerRoomImages);
        dotIndicator = findViewById(R.id.dotIndicator);
        nameTextView = findViewById(R.id.roomNameTextView);
        descriptionTextView = findViewById(R.id.roomDescriptionTextView);
        priceTextView = findViewById(R.id.roomPriceTextView);
        nextButton = findViewById(R.id.nextButton);
        amenitiesTextView = findViewById(R.id.amenitiesTextView);

        adultsTextView = findViewById(R.id.textViewAdults);
        childrenTextView = findViewById(R.id.textViewChildren);

        // Get data from Intent
        Intent intent = getIntent();
        String name = intent.getStringExtra("name");
        String description = intent.getStringExtra("description");
        String price = intent.getStringExtra("price");
        String cottageId = intent.getStringExtra("cottageId");
        List<String> imageURLs = intent.getStringArrayListExtra("imageURLs");
        Map<String, Integer> amenities = (Map<String, Integer>) intent.getSerializableExtra("amenities");

        // Get number of adults and children
        int numberOfAdults = intent.getIntExtra("adults", 0);
        int numberOfChildren = intent.getIntExtra("children", 0);

        adultsTextView.setText("Adults: " + numberOfAdults);
        childrenTextView.setText("Children: " + numberOfChildren);

        // Log received data for debugging
        Log.d("CottageDetailActivity", "Name: " + name);
        Log.d("CottageDetailActivity", "Description: " + description);
        Log.d("CottageDetailActivity", "Price: " + price);
        Log.d("CottageDetailActivity", "CottageId: " + cottageId);
        Log.d("CottageDetailActivity", "Number of Adults: " + numberOfAdults);
        Log.d("CottageDetailActivity", "Number of Children: " + numberOfChildren);

        // Set data to views with fallback text
        nameTextView.setText(name != null && !name.isEmpty() ? name : "No Name Provided");
        descriptionTextView.setText(description != null && !description.isEmpty() ? description : "No Description Provided");
        priceTextView.setText(price != null && !price.isEmpty() ? Html.fromHtml("<big>₱</big>" + price, Html.FROM_HTML_MODE_LEGACY) : "No Price Provided");

        // Set up ViewPager2 for images
        if (imageURLs != null && !imageURLs.isEmpty()) {
            ImageSliderAdapter adapter = new ImageSliderAdapter(this, imageURLs);
            viewPager.setAdapter(adapter);
            setupDotIndicator(imageURLs.size());
            updateDotIndicator(0);

            // Update dots on page change
            viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                @Override
                public void onPageSelected(int position) {
                    super.onPageSelected(position);
                    updateDotIndicator(position);
                }
            });
        }

        // Display amenities
        if (amenities != null && !amenities.isEmpty()) {
            StringBuilder amenitiesStringBuilder = new StringBuilder();
            for (Map.Entry<String, Integer> entry : amenities.entrySet()) {
                amenitiesStringBuilder.append(entry.getKey())
                        .append(": ₱")
                        .append(entry.getValue())
                        .append("\n");
            }
            amenitiesTextView.setText(amenitiesStringBuilder.toString());
        } else {
            amenitiesTextView.setText("No Amenities Available");
        }

        // Handle Next button click to proceed to reservation
        nextButton.setOnClickListener(v -> {
            Intent reservationIntent = new Intent(CottageDetailActivity.this, ReservationActivity.class);
            reservationIntent.putExtra("name", name);
            reservationIntent.putExtra("description", description);
            reservationIntent.putExtra("pricePerNight", price);
            reservationIntent.putExtra("cottageId", cottageId);
            reservationIntent.putExtra("numberOfAdults", numberOfAdults);
            reservationIntent.putExtra("numberOfChildren", numberOfChildren);
            startActivity(reservationIntent);
        });
    }

    private void setupDotIndicator(int count) {
        dots = new TextView[count];
        dotIndicator.removeAllViews();
        for (int i = 0; i < count; i++) {
            dots[i] = new TextView(this);
            dots[i].setText(Html.fromHtml("&#8226;", Html.FROM_HTML_MODE_LEGACY));
            dots[i].setTextSize(43);
            dots[i].setTextColor(getResources().getColor(R.color.grey, getApplicationContext().getTheme()));
            dotIndicator.addView(dots[i]);
        }
    }

    private void updateDotIndicator(int position) {
        for (int i = 0; i < dots.length; i++) {
            dots[i].setTextColor(getResources().getColor(R.color.grey, getApplicationContext().getTheme())); // Reset color
        }
        dots[position].setTextColor(getResources().getColor(R.color.textcolor, getApplicationContext().getTheme())); // Highlight the selected dot
    }
}
