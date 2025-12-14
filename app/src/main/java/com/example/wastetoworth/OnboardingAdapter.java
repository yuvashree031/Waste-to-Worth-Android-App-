package com.example.wastetoworth;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

/**
 * OnboardingAdapter - Adapter for the onboarding ViewPager
 * Handles the creation of onboarding screens with different content for each page.
 */
public class OnboardingAdapter extends PagerAdapter {

    private Context context;

    // Onboarding screen data
    private int[] images = {
            R.drawable.ic_food_bank,
            R.drawable.ic_food,
            R.drawable.ic_map
    };

    private int[] titles = {
            R.string.onboarding_title_1,
            R.string.onboarding_title_2,
            R.string.onboarding_title_3
    };

    private int[] descriptions = {
            R.string.onboarding_desc_1,
            R.string.onboarding_desc_2,
            R.string.onboarding_desc_3
    };

    public OnboardingAdapter(Context context) {
        this.context = context;
    }

    @Override
    public int getCount() {
        return titles.length;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.item_onboarding, container, false);

        ImageView imageView = view.findViewById(R.id.imageOnboarding);
        TextView titleTextView = view.findViewById(R.id.textTitle);
        TextView descTextView = view.findViewById(R.id.textDescription);

        imageView.setImageResource(images[position]);
        titleTextView.setText(titles[position]);
        descTextView.setText(descriptions[position]);

        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }
}