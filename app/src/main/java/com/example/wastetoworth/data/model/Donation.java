package com.example.wastetoworth.data.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.GeoPoint;

import java.util.HashMap;
import java.util.Map;

/**
 * Model class representing a donation.
 */
public class Donation {
    private String id;
    private String userId;
    private String fullName;
    private String foodItem;
    private String phone;
    private float quantity;
    private String description;
    private GeoPoint location;
    private Timestamp timestamp;
    private String status; // e.g., "pending", "accepted", "completed"
    
    // Required empty constructor for Firestore
    public Donation() {
    }
    
    public Donation(String userId, String fullName, String foodItem, String phone, 
                   float quantity, String description, GeoPoint location) {
        this.userId = userId;
        this.fullName = fullName;
        this.foodItem = foodItem;
        this.phone = phone;
        this.quantity = quantity;
        this.description = description;
        this.location = location;
        this.timestamp = Timestamp.now();
        this.status = "pending";
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public String getFullName() {
        return fullName;
    }
    
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
    
    public String getFoodItem() {
        return foodItem;
    }
    
    public void setFoodItem(String foodItem) {
        this.foodItem = foodItem;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public float getQuantity() {
        return quantity;
    }
    
    public void setQuantity(float quantity) {
        this.quantity = quantity;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public GeoPoint getLocation() {
        return location;
    }
    
    public void setLocation(GeoPoint location) {
        this.location = location;
    }
    
    public Timestamp getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    /**
     * Converts the donation to a Map for Firestore.
     * @return A Map representation of the donation
     */
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("userId", userId);
        map.put("fullName", fullName);
        map.put("foodItem", foodItem);
        map.put("phone", phone);
        map.put("quantity", quantity);
        map.put("description", description);
        map.put("location", location);
        map.put("timestamp", timestamp);
        map.put("status", status);
        return map;
    }
}
