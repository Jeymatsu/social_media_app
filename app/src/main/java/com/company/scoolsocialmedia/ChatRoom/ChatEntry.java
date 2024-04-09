package com.company.scoolsocialmedia.ChatRoom;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.company.scoolsocialmedia.R;
import com.company.scoolsocialmedia.adapters.ChatRoomAdapter;
import com.company.scoolsocialmedia.model.ChatRoom;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ChatEntry extends AppCompatActivity {
    private Button create_chatroom_button;

    private RecyclerView chatRoomsRecyclerView;
    private ChatRoomAdapter chatRoomAdapter;
    private List<ChatRoom> chatRoomsList;
    private DatabaseReference chatRoomsRef;
    Toolbar toolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_entry);

        toolbar = findViewById(R.id.chatToolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        create_chatroom_button = findViewById(R.id.create_chatroom_button);

        create_chatroom_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ChatEntry.this, CreateChatRoomActivity.class));
            }
        });

        chatRoomsRecyclerView = findViewById(R.id.chat_rooms_recycler_view);
        chatRoomsList = new ArrayList<>();
        chatRoomAdapter = new ChatRoomAdapter(chatRoomsList,false);
        chatRoomsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatRoomsRecyclerView.setAdapter(chatRoomAdapter);

        chatRoomsRef = FirebaseDatabase.getInstance().getReference().child("chatrooms");
        fetchChatRoomsForUser(FirebaseAuth.getInstance().getUid());

    }

    private void fetchChatRoomsForUser(String userId) {
        chatRoomsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                chatRoomsList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    ChatRoom chatRoom = snapshot.getValue(ChatRoom.class);
                    if (chatRoom != null && chatRoom.getParticipants() != null && chatRoom.getParticipants().contains(userId)) {
                        // Add the chat room to the list only if the user is a participant
                        chatRoomsList.add(chatRoom);
                    }
                }
                chatRoomAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error
            }
        });
    }





    @Override
    public void onBackPressed() {
        finish();
    }




}