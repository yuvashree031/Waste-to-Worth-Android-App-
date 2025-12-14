package com.example.wastetoworth;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

/**
 * MyFirebaseMessagingService - Handles Firebase Cloud Messaging notifications
 */
public class MyFirebaseMessagingService extends FirebaseMessagingService {
    
    private static final String TAG = "MyFirebaseMessaging";
    private static final String CHANNEL_ID = "waste_to_worth_notifications";
    private static final String CHANNEL_NAME = "Waste to Worth Notifications";
    private static final String CHANNEL_DESC = "Notifications for food donations and updates";
    
    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        Log.d(TAG, "Refreshed token: " + token);
        
        // Send token to your server or save locally
        getSharedPreferences("notification_prefs", MODE_PRIVATE)
                .edit()
                .putString("fcm_token", token)
                .apply();
    }
    
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        
        Log.d(TAG, "From: " + remoteMessage.getFrom());
        
        // Check if message contains a data payload
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
            
            // Handle the data message here
            String title = remoteMessage.getData().get("title");
            String body = remoteMessage.getData().get("body");
            String type = remoteMessage.getData().get("type");
            String donationId = remoteMessage.getData().get("donation_id");
            
            if (title != null && body != null) {
                sendNotification(title, body, type, donationId);
            }
        }
        
        // Check if message contains a notification payload
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
            
            String title = remoteMessage.getNotification().getTitle();
            String body = remoteMessage.getNotification().getBody();
            
            if (title != null && body != null) {
                sendNotification(title, body, null, null);
            }
        }
    }
    
    private void sendNotification(String title, String messageBody, String type, String donationId) {
        // Create notification channel
        createNotificationChannel();
        
        // Create intent based on notification type
        Intent intent;
        if ("donation_claimed".equals(type) && donationId != null) {
            // Open donation details or history
            intent = new Intent(this, DonationHistoryActivity.class);
            intent.putExtra("donation_id", donationId);
        } else if ("new_donation".equals(type)) {
            // Open donation feed
            intent = new Intent(this, DonationFeedActivity.class);
        } else {
            // Open main activity by default
            intent = new Intent(this, MainActivity.class);
        }
        
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);
        
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(messageBody));
        
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        
        // Generate unique notification ID
        int notificationId = (int) System.currentTimeMillis();
        
        try {
            notificationManager.notify(notificationId, notificationBuilder.build());
        } catch (SecurityException e) {
            Log.e(TAG, "Notification permission not granted", e);
        }
    }
    
    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance);
            channel.setDescription(CHANNEL_DESC);
            
            // Register the channel with the system
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}