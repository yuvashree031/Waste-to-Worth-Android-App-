package com.example.wastetoworth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class DonationTypeActivity extends AppCompatActivity {
    
    private CardView cardFoodDonation, cardClothesDonation, cardBooksDonation, cardElectronicsDonation, cardOtherDonation;
    private Button btnCancel;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donation_type);
        
        initializeViews();
        setupClickListeners();
    }
    
    private void initializeViews() {
        cardFoodDonation = findViewById(R.id.cardFoodDonation);
        cardClothesDonation = findViewById(R.id.cardClothesDonation);
        cardBooksDonation = findViewById(R.id.cardBooksDonation);
        cardElectronicsDonation = findViewById(R.id.cardElectronicsDonation);
        cardOtherDonation = findViewById(R.id.cardOtherDonation);
        btnCancel = findViewById(R.id.btnCancel);
    }
    
    private void setupClickListeners() {
        cardFoodDonation.setOnClickListener(v -> openDonationForm("food"));
        cardClothesDonation.setOnClickListener(v -> openDonationForm("clothes"));
        cardBooksDonation.setOnClickListener(v -> openDonationForm("books"));
        cardElectronicsDonation.setOnClickListener(v -> openDonationForm("electronics"));
        cardOtherDonation.setOnClickListener(v -> openDonationForm("other"));
        btnCancel.setOnClickListener(v -> finish());
    }
    
    private void openDonationForm(String donationType) {
        Intent intent = new Intent(this, DonateActivity.class);
        intent.putExtra("donationType", donationType);
        startActivity(intent);
        finish();
    }
}