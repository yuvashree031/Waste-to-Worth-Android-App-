package com.example.wastetoworth;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

/**
 * OnboardingActivity - Shows app introduction and feature highlights
 * This activity guides new users through the app's main features before they start using it.
 */
public class OnboardingActivity extends AppCompatActivity {

    private ViewPager viewPager;
    private LinearLayout dotsLayout;
    private Button btnNext;
    private TextView btnSkip;
    private TextView[] dots;
    private int currentPage;
    private OnboardingAdapter onboardingAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        initializeViews();
        setupOnboardingScreens();
        setupClickListeners();
        addDotsIndicator(0);
    }

    private void initializeViews() {
        viewPager = findViewById(R.id.viewPager);
        dotsLayout = findViewById(R.id.dotsLayout);
        btnNext = findViewById(R.id.btnNext);
        btnSkip = findViewById(R.id.btnSkip);
    }

    private void setupOnboardingScreens() {
        onboardingAdapter = new OnboardingAdapter(this);
        viewPager.setAdapter(onboardingAdapter);
        viewPager.addOnPageChangeListener(viewListener);
    }

    private void setupClickListeners() {
        btnNext.setOnClickListener(v -> {
            if (currentPage == dots.length - 1) {
                // Last page, go to role selection
                navigateToRoleSelection();
            } else {
                viewPager.setCurrentItem(currentPage + 1);
            }
        });

        btnSkip.setOnClickListener(v -> navigateToRoleSelection());
    }

    private void navigateToRoleSelection() {
        Intent intent = new Intent(OnboardingActivity.this, RoleSelectionActivity.class);
        startActivity(intent);
        finish();
    }

    private void addDotsIndicator(int position) {
        dots = new TextView[3]; // 3 onboarding screens
        dotsLayout.removeAllViews();

        for (int i = 0; i < dots.length; i++) {
            dots[i] = new TextView(this);
            dots[i].setText(Html.fromHtml("&#8226;"));
            dots[i].setTextSize(35);
            dots[i].setTextColor(getResources().getColor(R.color.soft_medium_text));
            dotsLayout.addView(dots[i]);
        }

        if (dots.length > 0) {
            dots[position].setTextColor(getResources().getColor(R.color.pastel_mint));
        }
    }

    ViewPager.OnPageChangeListener viewListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

        @Override
        public void onPageSelected(int position) {
            addDotsIndicator(position);
            currentPage = position;

            if (position == dots.length - 1) {
                btnNext.setText("Get Started");
                btnSkip.setVisibility(View.GONE);
            } else {
                btnNext.setText("Next");
                btnSkip.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {}
    };
}