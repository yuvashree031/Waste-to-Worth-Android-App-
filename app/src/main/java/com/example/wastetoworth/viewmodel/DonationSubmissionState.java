package com.example.wastetoworth.viewmodel;

/**
 * Data class that captures submission state for the donation form.
 */
public class DonationSubmissionState {
    private boolean isLoading;
    private boolean isSuccess;
    private String error;

    public DonationSubmissionState(boolean isLoading, boolean isSuccess, String error) {
        this.isLoading = isLoading;
        this.isSuccess = isSuccess;
        this.error = error;
    }

    public boolean isLoading() {
        return isLoading;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public String getError() {
        return error;
    }
}
