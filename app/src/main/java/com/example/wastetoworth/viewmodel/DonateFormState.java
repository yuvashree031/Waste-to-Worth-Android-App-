package com.example.wastetoworth.viewmodel;

/**
 * Data validation state of the donation form.
 */
public class DonateFormState {
    private String fullNameError;
    private String foodItemError;
    private String phoneError;
    private String quantityError;
    private String locationError;
    
    public DonateFormState() {
        // Default constructor
    }
    
    public String getFullNameError() {
        return fullNameError;
    }
    
    public void setFullNameError(String fullNameError) {
        this.fullNameError = fullNameError;
    }
    
    public String getFoodItemError() {
        return foodItemError;
    }
    
    public void setFoodItemError(String foodItemError) {
        this.foodItemError = foodItemError;
    }
    
    public String getPhoneError() {
        return phoneError;
    }
    
    public void setPhoneError(String phoneError) {
        this.phoneError = phoneError;
    }
    
    public String getQuantityError() {
        return quantityError;
    }
    
    public void setQuantityError(String quantityError) {
        this.quantityError = quantityError;
    }
    
    public String getLocationError() {
        return locationError;
    }
    
    public void setLocationError(String locationError) {
        this.locationError = locationError;
    }
    
    /**
     * Checks if the form data is valid.
     * @return true if all fields are valid, false otherwise
     */
    public boolean isDataValid() {
        return fullNameError == null && 
               foodItemError == null && 
               phoneError == null && 
               quantityError == null && 
               locationError == null;
    }
}
