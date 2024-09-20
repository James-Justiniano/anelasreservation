package com.example.anelasreservationsystem;

import java.io.Serializable;

public class Amenity implements Serializable {
    private String name; // Name of the amenity
    private String price; // Price of the amenity

    // Default constructor (required for Firebase)
    public Amenity() {}

    // Constructor with parameters
    public Amenity(String name, String price) {
        this.name = name;
        this.price = price;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }
}