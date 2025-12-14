package com.example.wastetoworth;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.label.ImageLabel;
import com.google.mlkit.vision.label.ImageLabeler;
import com.google.mlkit.vision.label.ImageLabeling;
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * FoodWasteClassifier - ML-based classifier for food waste detection and categorization
 * 
 * This class uses Google ML Kit to analyze images of food items and classify them
 * into categories, estimate freshness, and provide donation recommendations.
 */
public class FoodWasteClassifier {
    
    private static final String TAG = "FoodWasteClassifier";
    private Context context;
    private ImageLabeler imageLabeler;
    
    /**
     * Food classification result
     */
    public static class ClassificationResult {
        public String category;
        public float confidence;
        public String freshness;
        public boolean donatable;
        public String recommendation;
        public List<String> detectedLabels;
        
        public ClassificationResult() {
            detectedLabels = new ArrayList<>();
        }
    }
    
    /**
     * Callback interface for classification results
     */
    public interface ClassificationCallback {
        void onClassificationSuccess(ClassificationResult result);
        void onClassificationError(String error);
    }
    
    /**
     * Constructor
     * @param context Application context
     */
    public FoodWasteClassifier(Context context) {
        this.context = context;
        initializeLabeler();
    }
    
    /**
     * Initialize the ML Kit image labeler
     */
    private void initializeLabeler() {
        ImageLabelerOptions options = new ImageLabelerOptions.Builder()
                .setConfidenceThreshold(0.6f)
                .build();
        imageLabeler = ImageLabeling.getClient(options);
    }
    
    /**
     * Classify food waste from a bitmap image
     * @param bitmap The image to classify
     * @param callback Callback for results
     */
    public void classifyFoodWaste(Bitmap bitmap, ClassificationCallback callback) {
        if (bitmap == null) {
            callback.onClassificationError("Invalid image");
            return;
        }
        
        InputImage image = InputImage.fromBitmap(bitmap, 0);
        
        imageLabeler.process(image)
                .addOnSuccessListener(labels -> {
                    ClassificationResult result = processLabels(labels);
                    callback.onClassificationSuccess(result);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Classification failed", e);
                    callback.onClassificationError(e.getMessage());
                });
    }
    
    /**
     * Process detected labels and create classification result
     * @param labels List of detected labels from ML Kit
     * @return ClassificationResult with analysis
     */
    private ClassificationResult processLabels(List<ImageLabel> labels) {
        ClassificationResult result = new ClassificationResult();
        
        // Food categories
        Map<String, List<String>> foodCategories = getFoodCategories();
        
        float maxConfidence = 0f;
        String detectedCategory = "Other";
        
        // Analyze labels
        for (ImageLabel label : labels) {
            String labelText = label.getText().toLowerCase();
            float confidence = label.getConfidence();
            
            result.detectedLabels.add(labelText + " (" + String.format("%.1f%%", confidence * 100) + ")");
            
            // Find category
            for (Map.Entry<String, List<String>> entry : foodCategories.entrySet()) {
                if (entry.getValue().contains(labelText)) {
                    if (confidence > maxConfidence) {
                        maxConfidence = confidence;
                        detectedCategory = entry.getKey();
                    }
                }
            }
        }
        
        result.category = detectedCategory;
        result.confidence = maxConfidence;
        
        // Estimate freshness based on visual cues and confidence
        result.freshness = estimateFreshness(labels, maxConfidence);
        
        // Determine if donatable
        result.donatable = isDonatable(result.freshness, maxConfidence);
        
        // Generate recommendation
        result.recommendation = generateRecommendation(result);
        
        return result;
    }
    
    /**
     * Get food categories for classification
     * @return Map of categories and their keywords
     */
    private Map<String, List<String>> getFoodCategories() {
        Map<String, List<String>> categories = new HashMap<>();
        
        List<String> fruits = new ArrayList<>();
        fruits.add("fruit");
        fruits.add("apple");
        fruits.add("banana");
        fruits.add("orange");
        fruits.add("grape");
        fruits.add("berry");
        categories.put("Fruits", fruits);
        
        List<String> vegetables = new ArrayList<>();
        vegetables.add("vegetable");
        vegetables.add("carrot");
        vegetables.add("potato");
        vegetables.add("tomato");
        vegetables.add("lettuce");
        vegetables.add("broccoli");
        categories.put("Vegetables", vegetables);
        
        List<String> grains = new ArrayList<>();
        grains.add("bread");
        grains.add("rice");
        grains.add("pasta");
        grains.add("cereal");
        grains.add("grain");
        categories.put("Grains", grains);
        
        List<String> dairy = new ArrayList<>();
        dairy.add("milk");
        dairy.add("cheese");
        dairy.add("yogurt");
        dairy.add("dairy");
        categories.put("Dairy", dairy);
        
        List<String> protein = new ArrayList<>();
        protein.add("meat");
        protein.add("chicken");
        protein.add("fish");
        protein.add("egg");
        protein.add("bean");
        categories.put("Protein", protein);
        
        List<String> prepared = new ArrayList<>();
        prepared.add("meal");
        prepared.add("dish");
        prepared.add("food");
        prepared.add("cuisine");
        categories.put("Prepared Food", prepared);
        
        return categories;
    }
    
    /**
     * Estimate freshness based on ML analysis
     * @param labels Detected labels
     * @param confidence Confidence score
     * @return Freshness estimate (Fresh, Good, Fair, Poor)
     */
    private String estimateFreshness(List<ImageLabel> labels, float confidence) {
        // Check for spoilage indicators
        for (ImageLabel label : labels) {
            String text = label.getText().toLowerCase();
            if (text.contains("rotten") || text.contains("moldy") || text.contains("spoiled")) {
                return "Poor";
            }
        }
        
        // Estimate based on confidence
        if (confidence > 0.85f) {
            return "Fresh";
        } else if (confidence > 0.70f) {
            return "Good";
        } else if (confidence > 0.55f) {
            return "Fair";
        } else {
            return "Poor";
        }
    }
    
    /**
     * Determine if food is suitable for donation
     * @param freshness Freshness estimate
     * @param confidence Confidence score
     * @return true if donatable
     */
    private boolean isDonatable(String freshness, float confidence) {
        return (freshness.equals("Fresh") || freshness.equals("Good")) && confidence > 0.65f;
    }
    
    /**
     * Generate donation recommendation
     * @param result Classification result
     * @return Recommendation text
     */
    private String generateRecommendation(ClassificationResult result) {
        if (result.donatable) {
            return "This " + result.category.toLowerCase() + " appears to be in " + 
                   result.freshness.toLowerCase() + " condition and is suitable for donation. " +
                   "Please donate soon to help those in need!";
        } else {
            return "This item may not be suitable for donation due to its condition. " +
                   "Consider composting or proper disposal instead.";
        }
    }
    
    /**
     * Get smart donation recommendations based on inventory
     * @param items List of food items
     * @return List of recommendations
     */
    public List<String> getSmartRecommendations(List<String> items) {
        List<String> recommendations = new ArrayList<>();
        
        // Analyze patterns
        Map<String, Integer> categoryCount = new HashMap<>();
        for (String item : items) {
            String category = categorizeFoodItem(item);
            categoryCount.put(category, categoryCount.getOrDefault(category, 0) + 1);
        }
        
        // Generate recommendations
        if (categoryCount.getOrDefault("Fruits", 0) > 3) {
            recommendations.add("You have multiple fruits. Consider donating to a local food bank soon.");
        }
        if (categoryCount.getOrDefault("Prepared Food", 0) > 0) {
            recommendations.add("Prepared food should be donated within 2 hours for food safety.");
        }
        if (items.size() > 5) {
            recommendations.add("Large donation! Consider contacting an NGO for pickup service.");
        }
        
        return recommendations;
    }
    
    /**
     * Categorize a food item by name
     * @param item Food item name
     * @return Category name
     */
    private String categorizeFoodItem(String item) {
        String lower = item.toLowerCase();
        if (lower.contains("fruit") || lower.contains("apple") || lower.contains("banana")) {
            return "Fruits";
        } else if (lower.contains("vegetable") || lower.contains("carrot")) {
            return "Vegetables";
        } else if (lower.contains("bread") || lower.contains("rice")) {
            return "Grains";
        } else if (lower.contains("meal") || lower.contains("cooked")) {
            return "Prepared Food";
        }
        return "Other";
    }
    
    /**
     * Release resources
     */
    public void close() {
        if (imageLabeler != null) {
            imageLabeler.close();
        }
    }
}
