package com.example.wastetoworth.util;

import android.content.Context;

import com.example.wastetoworth.R;

/**
 * Utility class for form validation.
 */
public class FormValidator {
    
    // Minimum length for names
    private static final int MIN_NAME_LENGTH = 2;
    
    // Phone number pattern (10 digits starting with 6-9)
    private static final String PHONE_PATTERN = "^[6-9]\\d{9}$";
    
    /**
     * Validates a full name.
     * 
     * @param name The name to validate
     * @return Error message ID if invalid, 0 if valid
     */
    public static int validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return R.string.error_field_required;
        }
        
        if (name.trim().length() < MIN_NAME_LENGTH) {
            return R.string.error_invalid_name;
        }
        
        return 0; // Valid
    }
    
    /**
     * Validates a food item.
     * 
     * @param foodItem The food item to validate
     * @return Error message ID if invalid, 0 if valid
     */
    public static int validateFoodItem(String foodItem) {
        if (foodItem == null || foodItem.trim().isEmpty()) {
            return R.string.error_field_required;
        }
        
        return 0; // Valid
    }
    
    /**
     * Validates a phone number.
     * 
     * @param phone The phone number to validate
     * @return Error message ID if invalid, 0 if valid
     */
    public static int validatePhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return R.string.error_field_required;
        }
        
        if (!phone.matches(PHONE_PATTERN)) {
            return R.string.error_invalid_phone;
        }
        
        return 0; // Valid
    }
    
    /**
     * Validates a quantity.
     * 
     * @param quantity The quantity to validate
     * @return Error message ID if invalid, 0 if valid
     */
    public static int validateQuantity(String quantity) {
        if (quantity == null || quantity.trim().isEmpty()) {
            return R.string.error_field_required;
        }
        
        try {
            float qty = Float.parseFloat(quantity);
            if (qty <= 0) {
                return R.string.error_invalid_quantity;
            }
        } catch (NumberFormatException e) {
            return R.string.error_invalid_quantity;
        }
        
        return 0; // Valid
    }
    
    /**
     * Validates a location.
     * 
     * @param latitude The latitude to validate
     * @param longitude The longitude to validate
     * @return Error message ID if invalid, 0 if valid
     */
    public static int validateLocation(double latitude, double longitude) {
        if (latitude == 0.0 && longitude == 0.0) {
            return R.string.error_location_required;
        }
        
        return 0; // Valid
    }
}
