package com.example.anelasreservationsystem.CartFolder;

import com.example.anelasreservationsystem.Amenity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class CartItem implements Serializable {
    private String cartItemId;      // Unique ID for the cart item
    private String roomId;          // ID of the room
    private String roomType;        // Type of the room
    private String checkInDate;     // Check-in date
    private String checkOutDate;    // Check-out date
    private long numberOfNights;    // Number of nights for the reservation
    private String pricePerNight;   // Price per night
    private double totalPrice;      // Total price for the reservation
    private int quantity;           // Quantity of rooms (or items)
    private double amenitiesPrice;   // Total price of the amenities
    private List<String> imageUrls; // List of URLs for room images
    private List<Amenity> amenities; // List of amenities for the room
    private boolean isSelected;      // New field for selection

    // Default constructor (required for Firebase)
    public CartItem() {
        this.amenities = new ArrayList<>(); // Initialize with an empty list
        this.imageUrls = new ArrayList<>(); // Initialize with an empty list
        this.isSelected = false;             // Initialize selection state
    }

    // Constructor with all fields
    public CartItem(String cartItemId, String roomId, String roomType, String checkInDate,
                    String checkOutDate, long numberOfNights, String pricePerNight,
                    double totalPrice, int quantity, double amenitiesPrice,
                    List<String> imageUrls, List<Amenity> amenities) {
        this.cartItemId = cartItemId;
        this.roomId = roomId;
        this.roomType = roomType;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.numberOfNights = numberOfNights;
        this.pricePerNight = pricePerNight;
        this.totalPrice = totalPrice;
        this.quantity = quantity;
        this.amenitiesPrice = amenitiesPrice;
        this.imageUrls = imageUrls != null ? new ArrayList<>(imageUrls) : new ArrayList<>();
        this.amenities = amenities != null ? new ArrayList<>(amenities) : new ArrayList<>();
        this.isSelected = false; // Initialize selection state
    }

    // Getters and setters
    public String getCartItemId() {
        return cartItemId;
    }

    public void setCartItemId(String cartItemId) {
        this.cartItemId = cartItemId;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getRoomType() {
        return roomType;
    }

    public void setRoomType(String roomType) {
        this.roomType = roomType;
    }

    public String getCheckInDate() {
        return checkInDate;
    }

    public void setCheckInDate(String checkInDate) {
        this.checkInDate = checkInDate;
    }

    public String getCheckOutDate() {
        return checkOutDate;
    }

    public void setCheckOutDate(String checkOutDate) {
        this.checkOutDate = checkOutDate;
    }

    public long getNumberOfNights() {
        return numberOfNights;
    }

    public void setNumberOfNights(long numberOfNights) {
        this.numberOfNights = numberOfNights;
    }

    public String getPricePerNight() {
        return pricePerNight;
    }

    public void setPricePerNight(String pricePerNight) {
        this.pricePerNight = pricePerNight;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getAmenitiesPrice() {
        return amenitiesPrice;
    }

    public void setAmenitiesPrice(double amenitiesPrice) {
        this.amenitiesPrice = amenitiesPrice;
    }

    public List<String> getImageUrls() {
        return new ArrayList<>(imageUrls); // Return a copy to avoid external modification
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls != null ? new ArrayList<>(imageUrls) : new ArrayList<>();
    }

    public List<Amenity> getAmenities() {
        return new ArrayList<>(amenities); // Return a copy to avoid external modification
    }

    public void setAmenities(List<Amenity> amenities) {
        this.amenities = amenities != null ? new ArrayList<>(amenities) : new ArrayList<>();
    }

    // Getter and setter for selection state
    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}
