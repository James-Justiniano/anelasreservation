package com.example.anelasreservationsystem;

public class Cottage {
    private String name;
    private String imageURL; // Change this to imageURL
    private String price;

    public Cottage() {
        // Default constructor required for calls to DataSnapshot.getValue(Cottage.class)
    }

    public Cottage(String name, String imageURL, String price) {
        this.name = name;
        this.imageURL = imageURL; // Change this to imageURL
        this.price = price;
    }

    public String getName() {
        return name;
    }

    public String getImageURL() { // Change this to getImageURL
        return imageURL;
    }

    public String getPrice() {
        return price;
    }
}
