package com.example.anelasreservationsystem;

import com.example.anelasreservationsystem.CartFolder.CartItem;

import java.util.List;

public class Reservation {
    private String userId;
    private String email;
    private String phoneNumber;
    private List<CartItem> items;
    private String status;
    private String proofOfPaymentImage;

    public Reservation(String userId, String email, String phoneNumber, List<CartItem> items, String status, String proofOfPaymentImage) {
        this.userId = userId;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.items = items;
        this.status = status;
        this.proofOfPaymentImage = proofOfPaymentImage;
    }

    // Getters and setters
    public String getUserId() { return userId; }
    public String getEmail() { return email; }
    public String getPhoneNumber() { return phoneNumber; }
    public List<CartItem> getItems() { return items; }
    public String getStatus() { return status; }
    public String getProofOfPaymentImage() { return proofOfPaymentImage; }
}
