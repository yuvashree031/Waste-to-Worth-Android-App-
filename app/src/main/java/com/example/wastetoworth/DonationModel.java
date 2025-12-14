package com.example.wastetoworth;

import com.google.firebase.Timestamp;

/**
 * Model class representing a donation in the Waste to Worth application.
 * This class maps to the donations collection in Firestore.
 */
public class DonationModel {

    private String documentId;
    private String name;
    private String description;
    private String type;
    private String quantity;
    private String donorName;
    private String donorId;
    private String location;  // Changed from GeoPoint to String
    private String address;
    private Timestamp timestamp;
    private boolean isReceived;
    private String receiverId;
    private String receiverName;
    private Timestamp receivedTimestamp;

    // Volunteer delivery fields
    private boolean volunteerAssigned;
    private String volunteerId;
    private String volunteerName;
    private String deliveryStatus; // "available", "accepted", "picked_up", "delivered"
    private String phone;
    private String imageUrl;
    private String category;
    private String condition;
    private String status;
    private String foodName;

    // Default constructor required for Firestore
    public DonationModel() {}

    public DonationModel(String documentId, String name, String description, String type, String foodName,
                         String quantity, String donorName, String donorId, String location,
                         String address, Timestamp timestamp) {
        this.documentId = documentId;
        this.name = name;
        this.description = description;
        this.type = type;
        this.foodName = foodName;
        this.quantity = quantity;
        this.donorName = donorName;
        this.donorId = donorId;
        this.location = location;
        this.address = address;
        this.timestamp = timestamp;
        this.isReceived = false;
    }

    // Getters and Setters
    public String getDocumentId() { return documentId; }
    public void setDocumentId(String documentId) { this.documentId = documentId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getQuantity() { return quantity; }
    public void setQuantity(String quantity) { this.quantity = quantity; }

    public String getDonorName() { return donorName; }
    public void setDonorName(String donorName) { this.donorName = donorName; }

    public String getDonorId() { return donorId; }
    public void setDonorId(String donorId) { this.donorId = donorId; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public Timestamp getTimestamp() { return timestamp; }
    public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }

    public boolean isReceived() { return isReceived; }
    public void setReceived(boolean received) { isReceived = received; }

    public String getReceiverId() { return receiverId; }
    public void setReceiverId(String receiverId) { this.receiverId = receiverId; }

    public String getReceiverName() { return receiverName; }
    public void setReceiverName(String receiverName) { this.receiverName = receiverName; }

    public Timestamp getReceivedTimestamp() { return receivedTimestamp; }
    public void setReceivedTimestamp(Timestamp receivedTimestamp) { this.receivedTimestamp = receivedTimestamp; }

    public boolean isVolunteerAssigned() { return volunteerAssigned; }
    public void setVolunteerAssigned(boolean volunteerAssigned) { this.volunteerAssigned = volunteerAssigned; }

    public String getVolunteerId() { return volunteerId; }
    public void setVolunteerId(String volunteerId) { this.volunteerId = volunteerId; }

    public String getVolunteerName() { return volunteerName; }
    public void setVolunteerName(String volunteerName) { this.volunteerName = volunteerName; }

    public String getDeliveryStatus() { return deliveryStatus; }
    public void setDeliveryStatus(String deliveryStatus) { this.deliveryStatus = deliveryStatus; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    // Alias for getPhone() to match adapter usage
    public String getDonorPhone() { return phone; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getCondition() { return condition; }
    public void setCondition(String condition) { this.condition = condition; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getFoodName() { return foodName; }
    public void setFoodName(String foodName) { this.foodName = foodName; }
    private String clothesType;
    private String clothesSize;
    
    public String getClothesType() { return clothesType; }
    public void setClothesType(String clothesType) { this.clothesType = clothesType; }
    
    public String getClothesSize() { return clothesSize; }
    public void setClothesSize(String clothesSize) { this.clothesSize = clothesSize; }
    
    private String clothesGender;
    public String getClothesGender() { return clothesGender; }
    public void setClothesGender(String clothesGender) { this.clothesGender = clothesGender; }
    
    private String foodType;
    public String getFoodType() { return foodType; }
    public void setFoodType(String foodType) { this.foodType = foodType; }
    
    private String foodWeight;
    public String getFoodWeight() { return foodWeight; }
    public void setFoodWeight(String foodWeight) { this.foodWeight = foodWeight; }
    
    private String foodExpiry;
    public String getFoodExpiry() { return foodExpiry; }
    public void setFoodExpiry(String foodExpiry) { this.foodExpiry = foodExpiry; }
    
    private String bookType;
    public String getBookType() { return bookType; }
    public void setBookType(String bookType) { this.bookType = bookType; }
    
    private String bookCount;
    public String getBookCount() { return bookCount; }
    public void setBookCount(String bookCount) { this.bookCount = bookCount; }
    
    private String furnitureType;
    public String getFurnitureType() { return furnitureType; }
    public void setFurnitureType(String furnitureType) { this.furnitureType = furnitureType; }
    
    private String furnitureCount;
    public String getFurnitureCount() { return furnitureCount; }
    public void setFurnitureCount(String furnitureCount) { this.furnitureCount = furnitureCount; }
    
    private String otherType;
    public String getOtherType() { return otherType; }
    public void setOtherType(String otherType) { this.otherType = otherType; }
    
    private String otherCount;
    public String getOtherCount() { return otherCount; }
    public void setOtherCount(String otherCount) { this.otherCount = otherCount; }
    
    private boolean urgent;
    public boolean isUrgent() { return urgent; }
    public void setUrgent(boolean urgent) { this.urgent = urgent; }
    

    // Alias for getDocumentId() for compatibility
    public String getDonationId() { return documentId; }
}
