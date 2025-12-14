package com.example.wastetoworth.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.wastetoworth.data.DonationRepository;

/**
 * Factory for creating DonateViewModel with required dependencies.
 */
public class DonateViewModelFactory implements ViewModelProvider.Factory {
    private final Application application;
    private final DonationRepository repository;

    public DonateViewModelFactory(Application application, DonationRepository repository) {
        this.application = application;
        this.repository = repository;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(DonateViewModel.class)) {
            //noinspection unchecked
            return (T) new DonateViewModel(application, repository);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
