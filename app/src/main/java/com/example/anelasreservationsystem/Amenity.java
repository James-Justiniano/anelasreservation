package com.example.anelasreservationsystem;

import java.io.Serializable;

public class Amenity implements Serializable {
    private String name;  // Name of the amenity
    private int price;    // Price of the amenity (changed to int for numeric handling)
    private int quantity; // Quantity of the amenity

    // Default constructor (required for Firebase)
    public Amenity() {}

    // Constructor with parameters
    public Amenity(String name, int price, int quantity) {
        this.name = name;
        this.price = price;
        this.quantity = quantity;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
