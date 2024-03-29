package com.company.scoolsocialmedia.model;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class NotificationManager {
    private DatabaseReference notificationsRef;

    public NotificationManager() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        notificationsRef = database.getReference("notifications");
    }
    public void sendNotification(String title, String message, String userID) {

        // Create a new notification object
        NotificationModel notification = new NotificationModel(title, message, userID);

        // Save the notification under the user's ID in the database
        notificationsRef.child(userID).push().setValue(notification);
    }

    public interface UsernameCallback {
        void onUsernameReceived(String username);

        void onError(String errorMessage);
    }

    public void getUsernameFromFirebase(String userID, UsernameCallback callback) {
        // Get the reference to the current user's information in the Firebase database
        DatabaseReference userInfoRef = FirebaseDatabase.getInstance().getReference("User_Information")
                .child(userID);

        // Attach a listener to retrieve the username
        userInfoRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Check if the user's information exists
                if (dataSnapshot.exists()) {
                    // Check if the username exists
                    if (dataSnapshot.hasChild("name")) {
                        // Retrieve the username
                        String username = dataSnapshot.child("name").getValue(String.class);
                        // Use the username as needed
                        callback.onUsernameReceived(username);
                    } else {
                        // Handle the case where the username does not exist
                        callback.onError("No username found");
                    }
                } else {
                    // Handle the case where the user's information does not exist
                    callback.onError("User information not found");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle any errors that occur during the data retrieval process
                callback.onError("Error retrieving username: " + databaseError.getMessage());
            }
        });
    }
}
