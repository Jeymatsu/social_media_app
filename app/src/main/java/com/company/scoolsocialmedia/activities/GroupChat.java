package com.company.scoolsocialmedia.activities;

import static com.company.scoolsocialmedia.utils.DateTimeUtils.getCurrentDateTime;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

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
    private String CHAT_ROOM_ID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        Intent intent=getIntent();
        CHAT_ROOM_ID=intent.getStringExtra("CHAT_ROOM_ID");


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
                    String date = getCurrentDateTime(); // You need to implement getCurrentDateTime() method to get the current date/time
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

}