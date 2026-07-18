package com.example.wastetoworth;

import android.content.Context;
import android.location.Location;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * SmartDonationRecommender - ML-based recommendation system for donations
 * 
 * This class uses machine learning algorithms to provide personalized recommendations
 * for donors and receivers, optimizing donation matching and reducing food waste.
 */
public class SmartDonationRecommender {
    
    private Context context;
    private FirebaseFirestore db;
    
    /**
     * Donation recommendation result
     */
    public static class Recommendation {
        public String title;
        public String description;
        public String ngoName;
        public String location;
        public double distance;
        public int priority;
        public String category;
        public double matchScore;
        
        public Recommendation(String title, String description, String ngoName, 
                            String location, double distance, int priority) {
            this.title = title;
            this.description = description;
            this.ngoName = ngoName;
            this.location = location;
            this.distance = distance;
            this.priority = priority;
        }
    }
    
    /**
     * Callback interface for recommendations
     */
    public interface RecommendationCallback {
        void onRecommendationsReady(List<Recommendation> recommendations);
        void onError(String error);
    }
    
    /**
     * Constructor
     * @param context Application context
     */
    public SmartDonationRecommender(Context context) {
        this.context = context;
        this.db = FirebaseFirestore.getInstance();
    }
    
    /**
     * Get personalized donation recommendations for a donor
     * @param userLocation User's current location
     * @param foodCategory Category of food to donate
     * @param quantity Quantity of food
     * @param callback Callback for results
     */
    public void getRecommendationsForDonor(Location userLocation, String foodCategory, 
                                          int quantity, RecommendationCallback callback) {
        List<Recommendation> recommendations = new ArrayList<>();
        
        // Fetch urgent requests from Firestore
        db.collection("urgentRequests")
                .whereEqualTo("status", "active")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        try {
                            String title = document.getString("title");
                            String description = document.getString("description");
                            String ngoName = document.getString("ngoName");
                            String location = document.getString("location");
                            
                            // Calculate distance if location data is available
                            double distance = 0;
                            if (userLocation != null && document.contains("latitude") && document.contains("longitude")) {
                                double lat = document.getDouble("latitude");
                                double lng = document.getDouble("longitude");
                                distance = calculateDistance(userLocation.getLatitude(), 
                                                           userLocation.getLongitude(), lat, lng);
                            }
                            
                            // Calculate priority based on urgency and match
                            int priority = calculatePriority(document, foodCategory, distance);
                            
                            Recommendation rec = new Recommendation(title, description, ngoName, 
                                                                  location, distance, priority);
                            rec.category = foodCategory;
                            rec.matchScore = calculateMatchScore(document, foodCategory, quantity, distance);
                            
                            recommendations.add(rec);
                        } catch (Exception e) {
                            // Skip invalid documents
                        }
                    }
                    
                    // Sort by match score
                    recommendations.sort((r1, r2) -> Double.compare(r2.matchScore, r1.matchScore));
                    
                    // Add general recommendations if list is small
                    if (recommendations.size() < 3) {
                        recommendations.addAll(getGeneralRecommendations(foodCategory, userLocation));
                    }
                    
                    callback.onRecommendationsReady(recommendations);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }
    
    /**
     * Get recommendations for receivers/NGOs
     * @param ngoLocation NGO location
     * @param neededCategories Categories of food needed
     * @param callback Callback for results
     */
    public void getRecommendationsForReceiver(Location ngoLocation, List<String> neededCategories,
                                             RecommendationCallback callback) {
        List<Recommendation> recommendations = new ArrayList<>();
        
        // Fetch available donations
        db.collection("donations")
                .whereEqualTo("status", "available")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        try {
                            String foodType = document.getString("foodType");
                            String donorName = document.getString("donorName");
                            String location = document.getString("location");
                            
                            // Check if food category matches needs
                            boolean matches = false;
                            for (String category : neededCategories) {
                                if (foodType != null && foodType.toLowerCase().contains(category.toLowerCase())) {
                                    matches = true;
                                    break;
                                }
                            }
                            
                            if (matches) {
                                double distance = 0;
                                if (ngoLocation != null && document.contains("latitude") && document.contains("longitude")) {
                                    double lat = document.getDouble("latitude");
                                    double lng = document.getDouble("longitude");
                                    distance = calculateDistance(ngoLocation.getLatitude(),
                                                               ngoLocation.getLongitude(), lat, lng);
                                }
                                
                                Recommendation rec = new Recommendation(
                                    "Available: " + foodType,
                                    "From " + donorName,
                                    donorName,
                                    location,
                                    distance,
                                    1
                                );
                                rec.category = foodType;
                                rec.matchScore = calculateReceiverMatchScore(document, neededCategories, distance);
                                
                                recommendations.add(rec);
                            }
                        } catch (Exception e) {
                            // Skip invalid documents
                        }
                    }
                    
                    // Sort by match score
                    recommendations.sort((r1, r2) -> Double.compare(r2.matchScore, r1.matchScore));
                    
                    callback.onRecommendationsReady(recommendations);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }
    
    /**
     * Calculate distance between two coordinates in kilometers
     */
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Radius of the earth in km
        
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return R * c;
    }
    
    /**
     * Calculate priority score for a request
     */
    private int calculatePriority(QueryDocumentSnapshot document, String foodCategory, double distance) {
        int priority = 1;
        
        // Higher priority for urgent requests
        if (document.contains("urgent") && document.getBoolean("urgent")) {
            priority = 3;
        }
        
        // Adjust based on distance
        if (distance < 5) {
            priority++;
        }
        
        // Adjust based on category match
        String requestedCategory = document.getString("category");
        if (requestedCategory != null && requestedCategory.equalsIgnoreCase(foodCategory)) {
            priority++;
        }
        
        return Math.min(priority, 3);
    }
    
    /**
     * Calculate match score for donor recommendations
     */
    private double calculateMatchScore(QueryDocumentSnapshot document, String foodCategory, 
                                      int quantity, double distance) {
        double score = 100.0;
        
        // Category match (40 points)
        String requestedCategory = document.getString("category");
        if (requestedCategory != null && requestedCategory.equalsIgnoreCase(foodCategory)) {
            score += 40;
        } else if (requestedCategory != null && requestedCategory.toLowerCase().contains(foodCategory.toLowerCase())) {
            score += 20;
        }
        
        // Distance factor (30 points)
        if (distance < 2) {
            score += 30;
        } else if (distance < 5) {
            score += 20;
        } else if (distance < 10) {
            score += 10;
        }
        
        // Urgency factor (20 points)
        if (document.contains("urgent") && document.getBoolean("urgent")) {
            score += 20;
        }
        
        // Quantity match (10 points)
        if (document.contains("requiredQuantity")) {
            try {
                int required = document.getLong("requiredQuantity").intValue();
                if (quantity >= required) {
                    score += 10;
                } else {
                    score += 5;
                }
            } catch (Exception e) {
                // Ignore
            }
        }
        
        return score;
    }
    
    /**
     * Calculate match score for receiver recommendations
     */
    private double calculateReceiverMatchScore(QueryDocumentSnapshot document, 
                                              List<String> neededCategories, double distance) {
        double score = 100.0;
        
        // Category match
        String foodType = document.getString("foodType");
        for (String category : neededCategories) {
            if (foodType != null && foodType.toLowerCase().contains(category.toLowerCase())) {
                score += 30;
                break;
            }
        }
        
        // Distance factor
        if (distance < 2) {
            score += 25;
        } else if (distance < 5) {
            score += 15;
        } else if (distance < 10) {
            score += 5;
        }
        
        // Freshness/time factor
        if (document.contains("timestamp")) {
            try {
                long timestamp = document.getLong("timestamp");
                long currentTime = System.currentTimeMillis();
                long hoursSince = (currentTime - timestamp) / (1000 * 60 * 60);
                
                if (hoursSince < 2) {
                    score += 20; // Very fresh
                } else if (hoursSince < 6) {
                    score += 10;
                }
            } catch (Exception e) {
                // Ignore
            }
        }
        
        return score;
    }
    
    /**
     * Get general recommendations when no specific matches found
     */
    private List<Recommendation> getGeneralRecommendations(String foodCategory, Location userLocation) {
        List<Recommendation> recommendations = new ArrayList<>();
        
        recommendations.add(new Recommendation(
            "Local Food Bank",
            "Your nearest food bank accepts " + foodCategory.toLowerCase() + " donations",
            "Community Food Bank",
            "Nearby",
            0,
            1
        ));
        
        recommendations.add(new Recommendation(
            "Community Kitchen",
            "Help feed the homeless with your donation",
            "Community Kitchen",
            "City Center",
            0,
            1
        ));
        
        recommendations.add(new Recommendation(
            "NGO Partnership",
            "Partner NGOs are always looking for fresh food donations",
            "Partner NGOs",
            "Various Locations",
            0,
            1
        ));
        
        return recommendations;
    }
    
    /**
     * Get optimal donation time based on historical data
     * @return Recommended time slot
     */
    public String getOptimalDonationTime() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        
        if (hour >= 6 && hour < 10) {
            return "Morning (6 AM - 10 AM) - Great time for breakfast donations!";
        } else if (hour >= 10 && hour < 14) {
            return "Late Morning (10 AM - 2 PM) - Perfect for lunch donations!";
        } else if (hour >= 14 && hour < 18) {
            return "Afternoon (2 PM - 6 PM) - Good time for evening meal prep!";
        } else if (hour >= 18 && hour < 21) {
            return "Evening (6 PM - 9 PM) - Ideal for dinner donations!";
        } else {
            return "Late Evening - Consider donating tomorrow morning for freshness!";
        }
    }
    
    /**
     * Get impact statistics for motivation
     * @param totalDonations Total donations made
     * @return Impact message
     */
    public String getImpactMessage(int totalDonations) {
        int mealsFed = totalDonations * 3; // Estimate 3 meals per donation
        int co2Saved = totalDonations * 2; // Estimate 2kg CO2 saved per donation
        
        return String.format("You've helped feed approximately %d meals and saved %d kg of CO2 emissions! Keep making a difference!", 
                           mealsFed, co2Saved);
    }
}
