package com.example.wastetoworth.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.wastetoworth.data.DonationRepository;

import com.example.wastetoworth.data.model.Donation;
import com.example.wastetoworth.util.Event;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.GeoPoint;

public class DonateViewModel extends AndroidViewModel {
    private final DonationRepository donationRepository;
    private final FirebaseAuth firebaseAuth;
    
    private final MutableLiveData<DonateFormState> formState = new MutableLiveData<>();
    private final MutableLiveData<DonationSubmissionState> submissionState = new MutableLiveData<>();
    private final MutableLiveData<Event<String>> showMessage = new MutableLiveData<>();
    
    // Form data
    private String fullName = "";
    private String foodItem = "";
    private String phone = "";
    private String quantity = "";
    private String description = "";
    private double latitude = 0.0;
    private double longitude = 0.0;
    
    // Constants
    private static final int MIN_NAME_LENGTH = 2;
    private static final String PHONE_REGEX = "^[0-9]{10}$";
    
    public DonateViewModel(@NonNull Application application, DonationRepository repository) {
        super(application);
        this.donationRepository = repository;
        this.firebaseAuth = FirebaseAuth.getInstance();
        this.formState.setValue(new DonateFormState());
    }
    
    public LiveData<DonateFormState> getFormState() {
        return formState;
    }
    
    public LiveData<DonationSubmissionState> getSubmissionState() {
        return submissionState;
    }
    
    public LiveData<Event<String>> getShowMessage() {
        return showMessage;
    }
    
    // Setters for form data
    public void setFullName(String fullName) {
        this.fullName = fullName != null ? fullName : "";
        validateForm();
    }
    
    public void setFoodItem(String foodItem) {
        this.foodItem = foodItem != null ? foodItem : "";
        validateForm();
    }
    
    public void setPhone(String phone) {
        this.phone = phone != null ? phone : "";
        validateForm();
    }
    
    public void setQuantity(String quantity) {
        this.quantity = quantity != null ? quantity : "";
        validateForm();
    }
    
    public void setDescription(String description) {
        this.description = description != null ? description : "";
    }
    
    public void setLocation(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
        validateForm();
    }
    
    /**
     * Validates the donation form and updates the form state.
     */
    private void validateForm() {
        DonateFormState newFormState = new DonateFormState();
        
        // Validate full name
        if (fullName.trim().isEmpty() || fullName.trim().length() < MIN_NAME_LENGTH) {
            newFormState.setFullNameError("Name must be at least " + MIN_NAME_LENGTH + " characters");
        }
        
        // Validate food item
        if (foodItem.trim().isEmpty()) {
            newFormState.setFoodItemError("Food item is required");
        }
        
        // Validate phone number
        if (phone.trim().isEmpty() || !phone.matches(PHONE_REGEX)) {
            newFormState.setPhoneError("Please enter a valid 10-digit phone number");
        }
        
        // Validate quantity
        try {
            float qty = Float.parseFloat(quantity);
            if (qty <= 0) {
                newFormState.setQuantityError("Quantity must be greater than 0");
            }
        } catch (NumberFormatException e) {
            newFormState.setQuantityError("Please enter a valid number");
        }
        
        // Check if location is set
        if (latitude == 0.0 && longitude == 0.0) {
            newFormState.setLocationError("Please select a location on the map");
        }
        
        formState.setValue(newFormState);
    }
    
    /**
     * Submits the donation to the repository.
     */
    public void submitDonation() {
        if (!formState.getValue().isDataValid()) {
            showMessage.setValue(new Event<>("Please fix the form errors before submitting."));
            return;
        }
        
        if (firebaseAuth.getCurrentUser() == null) {
            showMessage.setValue(new Event<>("You must be logged in to make a donation."));
            return;
        }
        
        submissionState.setValue(new DonationSubmissionState(true, false, null));
        
        try {
            Donation donation = new Donation(
                firebaseAuth.getCurrentUser().getUid(),
                fullName.trim(),
                foodItem.trim(),
                phone.trim(),
                Float.parseFloat(quantity),
                description.trim(),
                new GeoPoint(latitude, longitude)
            );
            
            donationRepository.submitDonation(donation, new DonationRepository.DonationCallback() {
                @Override
                public void onSuccess(String donationId) {
                    submissionState.postValue(new DonationSubmissionState(false, true, null));
                    showMessage.postValue(new Event<>("Donation submitted successfully!"));
                }
                
                @Override
                public void onError(String error) {
                    submissionState.postValue(new DonationSubmissionState(false, false, error));
                    showMessage.postValue(new Event<>("Failed to submit donation: " + error));
                }
            });
            
        } catch (Exception e) {
            submissionState.setValue(new DonationSubmissionState(false, false, e.getMessage()));
            showMessage.setValue(new Event<>("An error occurred: " + e.getMessage()));
        }
    }
}
