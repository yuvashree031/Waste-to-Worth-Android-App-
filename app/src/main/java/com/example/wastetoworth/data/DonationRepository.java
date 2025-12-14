package com.example.wastetoworth.data;

import com.example.wastetoworth.data.model.Donation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;

/**
 * Repository for handling donation data operations.
 */
public class DonationRepository {
    private static final String COLLECTION_DONATIONS = "donations";
    private final FirebaseFirestore firestore;
    
    public DonationRepository() {
        this.firestore = FirebaseFirestore.getInstance();
    }
    
    /**
     * Interface for callbacks from donation operations.
     */
    public interface DonationCallback {
        void onSuccess(String donationId);
        void onError(String error);
    }
    
    /**
     * Submits a new donation to Firestore.
     * @param donation The donation to submit
     * @param callback Callback for handling the result
     */
    public void submitDonation(Donation donation, DonationCallback callback) {
        CollectionReference donationsRef = firestore.collection(COLLECTION_DONATIONS);
        
        // Convert donation to map
        Map<String, Object> donationMap = donation.toMap();
        
        // Add the donation to Firestore
        donationsRef.add(donationMap)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentReference document = task.getResult();
                        if (document != null) {
                            // Update the donation with the generated ID
                            donation.setId(document.getId());
                            document.set(donation.toMap())
                                    .addOnSuccessListener(aVoid -> callback.onSuccess(document.getId()))
                                    .addOnFailureListener(e -> callback.onError(e.getMessage()));
                        } else {
                            callback.onError("Failed to create donation document");
                        }
                    } else {
                        callback.onError(task.getException() != null ? 
                                task.getException().getMessage() : "Unknown error occurred");
                    }
                });
    }
    
    /**
     * Updates an existing donation in Firestore.
     * @param donation The donation with updated fields
     * @param callback Callback for handling the result
     */
    public void updateDonation(Donation donation, DonationCallback callback) {
        if (donation.getId() == null || donation.getId().isEmpty()) {
            callback.onError("Cannot update donation without an ID");
            return;
        }
        
        firestore.collection(COLLECTION_DONATIONS)
                .document(donation.getId())
                .set(donation.toMap())
                .addOnSuccessListener(aVoid -> callback.onSuccess(donation.getId()))
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }
    
    /**
     * Deletes a donation from Firestore.
     * @param donationId The ID of the donation to delete
     * @param callback Callback for handling the result
     */
    public void deleteDonation(String donationId, DonationCallback callback) {
        firestore.collection(COLLECTION_DONATIONS)
                .document(donationId)
                .delete()
                .addOnSuccessListener(aVoid -> callback.onSuccess(donationId))
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }
}
