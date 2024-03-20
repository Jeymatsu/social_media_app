package com.company.scoolsocialmedia.ChatRoom;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.company.scoolsocialmedia.MainActivity;
import com.company.scoolsocialmedia.R;
import com.company.scoolsocialmedia.adapters.UserAdapter;
import com.company.scoolsocialmedia.model.BasicUser;
import com.company.scoolsocialmedia.model.ChatRoom;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class CreateChatRoomActivity extends AppCompatActivity {

    private EditText chatroomNameEditText;
    private Button createChatroomButton;
    private DatabaseReference chatroomsRef;
    private DatabaseReference usersRef;
    private List<BasicUser> userList;
    private RecyclerView usersRecyclerView;
    private UserAdapter userAdapter;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_chat_room);

        chatroomNameEditText = findViewById(R.id.chatroom_name_edittext);
        createChatroomButton = findViewById(R.id.create_chatroom_button);
        usersRecyclerView = findViewById(R.id.users_recycler_view);

        chatroomsRef = FirebaseDatabase.getInstance().getReference().child("chatrooms");
        usersRef = FirebaseDatabase.getInstance().getReference().child("User_Information");

        userList = new ArrayList<>();
        userAdapter = new UserAdapter(userList);

        // Set up RecyclerView
        usersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        usersRecyclerView.setAdapter(userAdapter);

        createChatroomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String chatroomName = chatroomNameEditText.getText().toString();
                if (!TextUtils.isEmpty(chatroomName)) {
                    createChatRoom(chatroomName);
                } else {
                    Toast.makeText(CreateChatRoomActivity.this, "Please enter chat room name", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Load users from Firebase database
        loadUsers();
    }

    private void loadUsers() {
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    BasicUser user = snapshot.getValue(BasicUser.class);
                    userList.add(user);
                }
                userAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error
                Toast.makeText(CreateChatRoomActivity.this, "Failed to load users", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createChatRoom(String chatroomName) {
        String roomId = chatroomsRef.push().getKey();
        ChatRoom chatRoom = new ChatRoom(roomId, chatroomName, "", new ArrayList<>());
        chatroomsRef.child(roomId).setValue(chatRoom)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(CreateChatRoomActivity.this, "Chat room created successfully", Toast.LENGTH_SHORT).show();

                    // Logging statement to check selected users
                    List<BasicUser> selectedUsers = userAdapter.getSelectedUsers();
                    Log.d(TAG, "Selected users: " + selectedUsers.toString());

                    // Add selected users to the chat room
                    for (BasicUser user : selectedUsers) {
                        addUserToChatRoom(roomId, user.getUserId());
                    }
                    // Add the creator user after adding selected users
                    addUserToChatRoom(roomId, FirebaseAuth.getInstance().getCurrentUser().getUid());
                    startActivity(new Intent(CreateChatRoomActivity.this, MainActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(CreateChatRoomActivity.this, "Failed to create chat room", Toast.LENGTH_SHORT).show();
                });
    }


    private void addUserToChatRoom(String roomId, String userId) {
        Log.d(TAG, "Adding user " + userId + " to chat room " + roomId);
        chatroomsRef.child(roomId).child("participants").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<String> participants = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String participantId = snapshot.getValue(String.class);
                    participants.add(participantId);
                }
                Log.d(TAG, "Existing participants for chat room " + roomId + ": " + participants.toString());
                // Check if the user is already a participant
                if (!participants.contains(userId)) {
                    // Add the user as a participant
                    participants.add(userId);
                    chatroomsRef.child(roomId).child("participants").setValue(participants)
                            .addOnSuccessListener(aVoid -> {
                                Log.d(TAG, "User " + userId + " added to chat room " + roomId + " successfully");
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Failed to add user " + userId + " to chat room " + roomId + ": " + e.getMessage());
                            });
                } else {
                    Log.d(TAG, "User " + userId + " is already a participant of chat room " + roomId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error
                Log.e(TAG, "Failed to retrieve participants: " + databaseError.getMessage());
            }
        });
    }


}

