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
import android.widget.SearchView;
import android.widget.Toast;

import com.company.scoolsocialmedia.MainActivity;
import com.company.scoolsocialmedia.R;
import com.company.scoolsocialmedia.activities.SearchUsersActivity;
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

    private SearchView searchView;



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
        userAdapter = new UserAdapter(this,userList,true,false);
        searchView = findViewById(R.id.userSearchView);
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
        loadUsers(FirebaseAuth.getInstance().getUid());
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterList(newText);
                return true;
            }
        });
    }

    private void loadUsers(String currentUserId) {
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    BasicUser user = snapshot.getValue(BasicUser.class);
                    if (user != null && !user.getUserId().equals(currentUserId)) {
                        userList.add(user);
                    }
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

        // Add the chat room to the database
        chatroomsRef.child(roomId).setValue(chatRoom)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(CreateChatRoomActivity.this, "Chat room created successfully", Toast.LENGTH_SHORT).show();

                    // Get selected users from the adapter
                    List<BasicUser> selectedUsers = userAdapter.getSelectedUsers();

                    // Add selected users to the chat room
                    addUserToChatRoom(roomId, selectedUsers);

                    // Finish activity after adding users
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(CreateChatRoomActivity.this, "Failed to create chat room", Toast.LENGTH_SHORT).show();
                });
    }


    private void addUserToChatRoom(String roomId, List<BasicUser> selectedUsers) {
        Log.d(TAG, "Adding users to chat room " + roomId);
        List<String> participantIds = new ArrayList<>();

        // Extract user IDs from selected users
        for (BasicUser user : selectedUsers) {
            participantIds.add(user.getUserId());
        }

        // Add the creator user (current user) if not already in the list
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if (!participantIds.contains(currentUserId)) {
            participantIds.add(currentUserId);
        }

        chatroomsRef.child(roomId).child("participants").setValue(participantIds)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Users added to chat room " + roomId + " successfully");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to add users to chat room " + roomId + ": " + e.getMessage());
                });
    }


    private void filterList(String text) {
        String query = text.toLowerCase().trim();
        List<BasicUser> filteredList = new ArrayList<>();

        for (BasicUser user : userList) {
            // Check if user name contains the search query
            if (user.getName().toLowerCase().contains(query)) {
                filteredList.add(user);
            }
        }

        // Update the adapter with the filtered list
        userAdapter.setFilteredList(filteredList);
    }



}

