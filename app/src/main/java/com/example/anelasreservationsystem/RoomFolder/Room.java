package com.example.anelasreservationsystem.RoomFolder;

import com.example.anelasreservationsystem.Amenity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Room {
    private String id;
    private String name;
    private List<String> imageURLs; // List of image URLs
    private String price; // Keep this as String
    private String description;
    private Map<String, Amenity> amenities; // Map of Amenity objects
    private int adults;   // Number of adults as int
    private int children; // Number of children as int

    // Default constructor required for calls to DataSnapshot.getValue(Room.class)
    public Room() {
        this.imageURLs = new ArrayList<>(); // Initialize to avoid NullPointerException
        this.amenities = new HashMap<>();   // Initialize amenities map
        this.adults = 0;                    // Default value for adults
        this.children = 0;                  // Default value for children
    }

    public Room(String id, String name, List<String> imageURLs, String price, String description,
                Map<String, Amenity> amenities, int adults, int children) {
        this.id = id;
        this.name = name;
        this.imageURLs = imageURLs;
        this.price = price;               // Set as String
        this.description = description;
        this.amenities = amenities;       // Initialize amenities map
        this.adults = adults;             // Set number of adults as int
        this.children = children;         // Set number of children as int
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getImageURLs() {
        return imageURLs; // Return the list of image URLs
    }

    public void setImageURLs(List<String> imageURLs) {
        this.imageURLs = imageURLs;
    }

    public String getPrice() {
        return price; // Return the price as a String
    }

    public void setPrice(String price) {
        this.price = price; // Setter for price
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Map<String, Amenity> getAmenities() {
        return amenities; // Getter for amenities map
    }

    public void setAmenities(Map<String, Amenity> amenities) {
        this.amenities = amenities; // Setter for amenities map
    }

    public int getAdults() {
        return adults; // Getter for number of adults
    }

    public void setAdults(int adults) {
        this.adults = adults; // Setter for number of adults
    }

    public int getChildren() {
        return children; // Getter for number of children
    }

    public void setChildren(int children) {
        this.children = children; // Setter for number of children
    }
}
