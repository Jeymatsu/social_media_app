package com.company.scoolsocialmedia.admin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.Toast;

import com.company.scoolsocialmedia.ChatRoom.ChatEntry;
import com.company.scoolsocialmedia.R;
import com.company.scoolsocialmedia.adapters.ChatRoomAdapter;
import com.company.scoolsocialmedia.model.ChatRoom;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ManageChatRooms extends AppCompatActivity {

    private RecyclerView chatRoomsRecyclerView;
    private ChatRoomAdapter chatRoomAdapter;
    private List<ChatRoom> chatRoomsList;
    private DatabaseReference chatRoomsRef;
    Toolbar toolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_chat_rooms);


        toolbar = findViewById(R.id.chatToolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        chatRoomsRecyclerView = findViewById(R.id.chat_rooms_recycler_view);
        chatRoomsList = new ArrayList<>();
        chatRoomAdapter = new ChatRoomAdapter(chatRoomsList);
        chatRoomsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatRoomsRecyclerView.setAdapter(chatRoomAdapter);

        chatRoomsRef = FirebaseDatabase.getInstance().getReference().child("chatrooms");
        fetchChatRooms();
    }

    private void fetchChatRooms() {
        chatRoomsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                chatRoomsList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    ChatRoom chatRoom = snapshot.getValue(ChatRoom.class);
                    chatRoomsList.add(chatRoom);
                }
                chatRoomAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ManageChatRooms.this, "Failed to load chat rooms", Toast.LENGTH_SHORT).show();
            }
        });
    }
}