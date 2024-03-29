package com.company.scoolsocialmedia.fragements;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.viewmodel.CreationExtras;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.company.scoolsocialmedia.ChatRoom.CreateChatRoomActivity;
import com.company.scoolsocialmedia.R;
import com.company.scoolsocialmedia.activities.GroupChat;
import com.company.scoolsocialmedia.adapters.ChatRoomAdapter;
import com.company.scoolsocialmedia.adapters.ChatRoomsAdapter;
import com.company.scoolsocialmedia.model.ChatRoom;
import com.company.scoolsocialmedia.model.ChatRoomModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.wang.avi.AVLoadingIndicatorView;

import java.util.ArrayList;
import java.util.List;

public class BooksChatRoomsFragment extends Fragment implements ChatRoomAdapter.OnChatRoomClickListener {

    private AVLoadingIndicatorView avi;
    private ImageView chatIcon;
    private TextView emptyMsg;
    private RecyclerView recyclerView;

    private List<ChatRoomModel> mChatRooms;
    private ChatRoomsAdapter mAdapter;

    private DatabaseReference chatsRef;
    private StorageReference storageRef;
    private ValueEventListener eventListener;
    private Button create_chatroom_button;

    //New
    private RecyclerView chatRoomsRecyclerView;
    private ChatRoomAdapter chatRoomAdapter;
    private List<ChatRoom> chatRoomsList;
    private DatabaseReference chatRoomsRef;


    public BooksChatRoomsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_books_chat_rooms, container, false);
//        avi = view.findViewById(R.id.bookChatRoomAvi);
//        chatIcon = view.findViewById(R.id.bookChatRoomChatIcon);
        create_chatroom_button = view.findViewById(R.id.create_chatroom_button);

        create_chatroom_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), CreateChatRoomActivity.class));
            }
        });

        chatRoomsRecyclerView = view.findViewById(R.id.chat_rooms_recycler_view);
        chatRoomsList = new ArrayList<>();
        chatRoomAdapter = new ChatRoomAdapter(chatRoomsList,false);
        chatRoomsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        chatRoomsRecyclerView.setAdapter(chatRoomAdapter);

        chatRoomsRef = FirebaseDatabase.getInstance().getReference().child("chatrooms");
        fetchChatRooms();


//        emptyMsg = view.findViewById(R.id.bookChatRoomEmptyMsg);
//        recyclerView = view.findViewById(R.id.bookChatRoomRecyclerView);
//        initRecyclerView();
//        try {
//            fetchBooksChatRooms();
//        } catch (Exception e) {
//        }
        return view;
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
                Toast.makeText(getActivity(), "Failed to load chat rooms", Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    public void onChatRoomClick(ChatRoom chatRoom) {

    }

    @Override
    public void onChatRoomClick(int position) {

        ChatRoom clickedChatRoom = chatRoomsList.get(position);
        // Handle click event here
        Intent intent = new Intent(getActivity(), GroupChat.class);
        intent.putExtra("CHAT_ROOM_ID", clickedChatRoom.getRoomId());
        intent.putExtra("CHAT_ROOM_NAME", clickedChatRoom.getName());
        startActivity(intent);
        Log.d("BooksChatRoomsFragment", "Clicked on chat room: " + clickedChatRoom.getName());


    }


    @Override
    public void onDestroyView() {
        mChatRooms.clear();
        chatsRef.removeEventListener(eventListener);
        super.onDestroyView();
    }



    @Override
    public void onDestroy() {
        mChatRooms.clear();
        storageRef = null;
        chatsRef = null;
        super.onDestroy();
    }


    private void hideProgress() {
        avi.hide();
    }

    private void showEmptyMsg() {
        emptyMsg.setVisibility(View.VISIBLE);
        chatIcon.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
    }

    private void hideEmptyMsg() {
        emptyMsg.setVisibility(View.GONE);
        chatIcon.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
    }

    private void showProgress() {
        recyclerView.setVisibility(View.GONE);
        avi.show();
    }

//    @Override
//    public void initChat(ChatRoomModel room) {
//        startActivity(new Intent(getContext(), ChatActivity.class).putExtra("chatRoom", room).putExtra("type", "book").putExtra("action", "chat"));
//    }
//
//    @Override
//    public void deleteChatRoom(String id) {
//        showChatRoomDeleteConfirmation(id);
//    }



    @NonNull
    @Override
    public CreationExtras getDefaultViewModelCreationExtras() {
        return super.getDefaultViewModelCreationExtras();
    }
}
