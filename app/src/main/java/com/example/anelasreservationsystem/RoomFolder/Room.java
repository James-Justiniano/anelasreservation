package com.example.anelasreservationsystem.RoomFolder;

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
    private Map<String, Integer> amenities; // Map for amenities and their prices
    private int adults;   // Number of adults as int
    private int children; // Number of children as int

    // Default constructor required for calls to DataSnapshot.getValue(Room.class)
    public Room() {
        this.imageURLs = new ArrayList<>(); // Initialize to avoid NullPointerException
        this.amenities = new HashMap<>(); // Initialize amenities map
        this.adults = 0;   // Default value for adults
        this.children = 0; // Default value for children
    }

    public Room(String id, String name, List<String> imageURLs, String price, String description,
                Map<String, Integer> amenities, int adults, int children) {
        this.id = id;
        this.name = name;
        this.imageURLs = imageURLs;
        this.price = price; // Set as String
        this.description = description;
        this.amenities = amenities; // Initialize amenities
        this.adults = adults;       // Set number of adults as int
        this.children = children;   // Set number of children as int
    }

    // Getters and Setters
    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<String> getImageURLs() {
        return imageURLs; // Return the list of image URLs
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

    public Map<String, Integer> getAmenities() {
        return amenities; // Getter for amenities
    }

    public int getAdults() {
        return adults; // Getter for number of adults as int
    }

    public void setAdults(int adults) {
        this.adults = adults; // Setter for number of adults
    }

    public int getChildren() {
        return children; // Getter for number of children as int
    }

    public void setChildren(int children) {
        this.children = children; // Setter for number of children
    }
}
