package com.company.scoolsocialmedia.activities;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.company.scoolsocialmedia.ChatRoom.ChatEntry;
import com.company.scoolsocialmedia.MainActivity;
import com.company.scoolsocialmedia.R;
import com.company.scoolsocialmedia.adapters.GroupMessageAdapter;
import com.company.scoolsocialmedia.listeners.OnMsgLayoutLongClick;
import com.company.scoolsocialmedia.model.ChatMessageModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class GroupChat extends AppCompatActivity {

    private EditText editTextMessage;
    private ImageView buttonSend;
    private RecyclerView recyclerViewMessages;
    private GroupMessageAdapter messageAdapter;
    private DatabaseReference messagesRef;
    private ValueEventListener messageListener;
    private String CHAT_ROOM_ID,CHAT_ROOM_NAME;

    Toolbar toolbar;
    private TextView chat_room_name;

    private DatabaseReference chatroomsRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        toolbar = findViewById(R.id.chatToolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        Intent intent = getIntent();
        CHAT_ROOM_ID = intent.getStringExtra("CHAT_ROOM_ID");
        CHAT_ROOM_NAME = intent.getStringExtra("CHAT_ROOM_NAME");

        chat_room_name=findViewById(R.id.chat_room_name);
        chat_room_name.setText(CHAT_ROOM_NAME);

        // Assuming you want to reference the "chatRooms" node in your Firebase Realtime Database
        chatroomsRef = FirebaseDatabase.getInstance().getReference("chatRooms");


        // Initialize views
        editTextMessage = findViewById(R.id.chatMsgEditText);
        buttonSend = findViewById(R.id.chatMsgSendBtn);
        recyclerViewMessages = findViewById(R.id.chatRecyclerView);

        // Initialize Firebase database reference
        messagesRef = FirebaseDatabase.getInstance().getReference("chatRooms").child(CHAT_ROOM_ID).child("messages");

        // Set up RecyclerView adapter
        List<ChatMessageModel> messagesList = new ArrayList<>();
        messageAdapter = new GroupMessageAdapter(messagesList, new OnMsgLayoutLongClick() {
            @Override
            public void showMsgInfo(ChatMessageModel message) {
                // Handle long click on message
            }
        });
        recyclerViewMessages.setAdapter(messageAdapter);
        recyclerViewMessages.setLayoutManager(new LinearLayoutManager(this));

        // Set up button click listener to send message
        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String messageText = editTextMessage.getText().toString().trim();
                if (!TextUtils.isEmpty(messageText)) {
                    String messageId = messagesRef.push().getKey();
                    String senderId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    String date = getCurrentDateTime();
                    ChatMessageModel message = new ChatMessageModel(messageId, date, messageText, senderId, "sent");
                    messagesRef.child(messageId).setValue(message);
                    editTextMessage.setText("");
                }
            }
        });

        // Set up listener to receive messages
        messageListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                messagesList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    ChatMessageModel message = snapshot.getValue(ChatMessageModel.class);
                    messagesList.add(message);
                }
                messageAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error
            }
        };
        messagesRef.addValueEventListener(messageListener);
    }

    private String getCurrentDateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return dateFormat.format(new Date());
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.leave_chat_room, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_chat_rooms) {
            // Handle leaving chatroom action
            removeUserFromChatRoom(CHAT_ROOM_ID, FirebaseAuth.getInstance().getCurrentUser().getUid());

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void removeUserFromChatRoom(String roomId, String userId) {
        DatabaseReference participantsRef = FirebaseDatabase.getInstance().getReference("chatrooms").child(roomId).child("participants");

        participantsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<String> participants = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String participantId = snapshot.getValue(String.class);
                    if (!participantId.equals(userId)) {
                        participants.add(participantId);
                    }
                }
                // Update the participants list in the database
                participantsRef.setValue(participants)
                        .addOnSuccessListener(aVoid -> {
                            Log.d(TAG, "User " + userId + " removed from chat room " + roomId + " successfully");
                                startActivity(new Intent(getApplicationContext(),ChatEntry.class));
                                finish();
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Failed to remove user " + userId + " from chat room " + roomId + ": " + e.getMessage());
                        });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error
                Log.e(TAG, "Failed to retrieve participants: " + databaseError.getMessage());
            }
        });
    }


    private void leaveChatRoom() {
        // Remove the current user from the chat room's participants list
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        chatroomsRef.child(CHAT_ROOM_ID).child("participants").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<String> participants = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String participantId = snapshot.getValue(String.class);
                    participants.add(participantId);
                }
                // Remove the current user's ID from the participants list
                participants.remove(currentUserId);
                // Update the participants list in the database
                chatroomsRef.child(CHAT_ROOM_ID).child("participants").setValue(participants)
                        .addOnSuccessListener(aVoid -> {
                            // Handle success
                            Toast.makeText(GroupChat.this, "Left the chat room", Toast.LENGTH_SHORT).show();
                            // Finish the activity or navigate back to the previous screen
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            // Handle failure
                            Toast.makeText(GroupChat.this, "Failed to leave the chat room", Toast.LENGTH_SHORT).show();
                        });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error
                Log.e(TAG, "Failed to retrieve participants: " + databaseError.getMessage());
            }
        });


    }


}
