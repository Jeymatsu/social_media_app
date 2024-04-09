package com.company.scoolsocialmedia.activities;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.company.scoolsocialmedia.ChatRoom.ChatEntry;
import com.company.scoolsocialmedia.Constants;
import com.company.scoolsocialmedia.MainActivity;
import com.company.scoolsocialmedia.R;
import com.company.scoolsocialmedia.adapters.GroupMessageAdapter;
import com.company.scoolsocialmedia.adapters.UserAdapter;
import com.company.scoolsocialmedia.listeners.OnMsgLayoutLongClick;
import com.company.scoolsocialmedia.loginsignup.loginActivity;
import com.company.scoolsocialmedia.loginsignup.signupActivity;
import com.company.scoolsocialmedia.model.BasicUser;
import com.company.scoolsocialmedia.model.ChatMessageModel;
import com.company.scoolsocialmedia.model.ChatRoom;
import com.company.scoolsocialmedia.utils.DBOperations;
import com.company.scoolsocialmedia.utils.DateTimeUtils;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class GroupChat extends AppCompatActivity {

    private EditText editTextMessage;
    private ImageView buttonSend;
    private Uri uri;
    private RecyclerView recyclerViewMessages;
    StorageReference storageRef;

    private GroupMessageAdapter messageAdapter;
    private DatabaseReference messagesRef;
    public static final int CHOOSE_FROM_GALLERY = 99;
    private ValueEventListener messageListener;
    private String CHAT_ROOM_ID,CHAT_ROOM_NAME;
    Toolbar toolbar;
    private TextView chat_room_name;
    private DatabaseReference chatroomsRef,chatRooms,usersRef;
    private List<BasicUser> userList;
    private UserAdapter userAdapter;
    private ImageView btnMedia;
    private boolean isImageLoaded = false;
    private boolean isVideoLoaded = false;
    private Boolean isAllMembers=false;
    private ProgressDialog progressDialog;

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
        progressDialog = new ProgressDialog(this);
        Intent intent = getIntent();
        CHAT_ROOM_ID = intent.getStringExtra("CHAT_ROOM_ID");
        CHAT_ROOM_NAME = intent.getStringExtra("CHAT_ROOM_NAME");

        chat_room_name=findViewById(R.id.chat_room_name);
        chat_room_name.setText(CHAT_ROOM_NAME);

        // Assuming you want to reference the "chatRooms" node in your Firebase Realtime Database
        chatroomsRef = FirebaseDatabase.getInstance().getReference("chatRooms");
        chatRooms = FirebaseDatabase.getInstance().getReference().child("chatrooms");
        usersRef = FirebaseDatabase.getInstance().getReference().child("User_Information");


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
                    ChatMessageModel message = new ChatMessageModel(messageId, date, messageText, senderId, "sent","","","");
                    messagesRef.child(messageId).setValue(message);
                    editTextMessage.setText("");
                }
            }
        });

        btnMedia=findViewById(R.id.btnMedia);
        btnMedia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
             chooseImageOrVideoToUpload();
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

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Leave Chat Room");
            builder.setMessage("Are you sure you want to leave the chat room?");
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    removeUserFromChatRoom(CHAT_ROOM_ID, FirebaseAuth.getInstance().getCurrentUser().getUid());

                    // Remove the post from the RecyclerView

                }
            });
            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    // User clicked No, do nothing
                }
            });
            AlertDialog alertDialog = builder.create();
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    alertDialog.show();
                }
            });

            return true;
        } else if (id == R.id.action_edit_name) {
            showEditChatRoom();

        }else if (id == R.id.action_group_members) {
            showChatRoomMembers();

        }else if (id == R.id.action_add_members) {
            AddRoomMembers();

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




    // Method to edit chat room name
    private void editChatRoomName(String roomId, String newName) {
        chatRooms.child(roomId).child("name").setValue(newName)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Chat Room Changed Succesfully", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(GroupChat.this,ChatEntry.class));
                    finish();
                    // Handle success
                })
                .addOnFailureListener(e -> {
                    // Handle failure
                });
    }

    // Method to add a member to the chat room
    private void addMemberToChatRoom(String roomId, String userId) {
        chatroomsRef.child(roomId).child("participants").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<String> participants = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String participantId = snapshot.getValue(String.class);
                    participants.add(participantId);
                }
                // Add the new member to the participants list
                participants.add(userId);
                // Update the participants list in the database
                chatroomsRef.child(roomId).child("participants").setValue(participants)
                        .addOnSuccessListener(aVoid -> {
                            // Handle success
                        })
                        .addOnFailureListener(e -> {
                            // Handle failure
                        });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error
            }
        });
    }

    // Method to remove a member from the chat room
    private void removeMemberFromChatRoom(String roomId, String userId) {
        chatroomsRef.child(roomId).child("participants").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<String> participants = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String participantId = snapshot.getValue(String.class);
                    participants.add(participantId);
                }
                // Remove the member from the participants list
                participants.remove(userId);
                // Update the participants list in the database
                chatroomsRef.child(roomId).child("participants").setValue(participants)
                        .addOnSuccessListener(aVoid -> {
                            // Handle success
                        })
                        .addOnFailureListener(e -> {
                            // Handle failure
                        });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error
            }
        });
    }



    private void chooseImageOrVideoToUpload() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                if (shouldShowRequestPermissionRationale(
                        Manifest.permission.READ_EXTERNAL_STORAGE)) {
                }
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        1);
            } else {
                // Create an intent to pick either image or video
                Intent pickIntent = new Intent(Intent.ACTION_GET_CONTENT);
                pickIntent.setType("*/*");
                pickIntent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"image/*", "video/*"});
                startActivityForResult(Intent.createChooser(pickIntent, "Select Picture or Video"), CHOOSE_FROM_GALLERY);
            }
        } else {
            // Create an intent to pick either image or video
            Intent pickIntent = new Intent(Intent.ACTION_GET_CONTENT);
            pickIntent.setType("*/*");
            pickIntent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"image/*", "video/*"});
            startActivityForResult(Intent.createChooser(pickIntent, "Select Picture or Video"), CHOOSE_FROM_GALLERY);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CHOOSE_FROM_GALLERY && resultCode == RESULT_OK && data != null) {
            uri = data.getData();
            String mimeType = getContentResolver().getType(uri);
            if (mimeType != null) {
                if (mimeType.startsWith("image/")) {
                    // If the selected file is an image
//                    loadImageIntoView(uri);
                    uploadImage();
                } else if (mimeType.startsWith("video/")) {
                    // If the selected file is a video
//                    loadVideoIntoView(uri);
                    uploadVideo();
                } else {
                    // Show error message for unsupported file types
                    Toast.makeText(this, "Unsupported file type", Toast.LENGTH_SHORT).show();
                }
            } else {
                // Show error message if MIME type is null
                Toast.makeText(this, "Cannot determine file type", Toast.LENGTH_SHORT).show();
            }

        }
    }

    private void uploadImage(){
        progressDialog.setMessage("Sending Image...");
        progressDialog.setCancelable(false); // Set whether the dialog can be canceled by pressing the back key
        storageRef = FirebaseStorage.getInstance().getReference().child("chat-images/").child(Constants.getConstantUid() + "/" + Constants.getConstantUid() + "_image" + DateTimeUtils.getCurrentDateTime());
        storageRef.putFile(uri, DBOperations.getmetaData()).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    Toast.makeText(getApplicationContext(), "Error while uploading image: " + task.getException().getLocalizedMessage(), Toast.LENGTH_LONG).show();
                }
                return storageRef.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                final Uri downUri = task.getResult();
                String messageId = messagesRef.push().getKey();
                String senderId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                String date = getCurrentDateTime();
                ChatMessageModel message = new ChatMessageModel(messageId, date, "", senderId, "sent","image",downUri.toString(),"");
                messagesRef.child(messageId).setValue(message);

                progressDialog.dismiss();

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(GroupChat.this, "Failed to send Iamge", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void uploadVideo(){
        progressDialog.setMessage("Sending Video...");
        progressDialog.setCancelable(false); // Set whether the dialog can be canceled by pressing the back key
        storageRef = FirebaseStorage.getInstance().getReference().child("chat-videos/").child(Constants.getConstantUid() + "/" + Constants.getConstantUid() + "_video" + DateTimeUtils.getCurrentDateTime());
        // Set content type explicitly for video files
        StorageMetadata metadata = new StorageMetadata.Builder()
                .setContentType("video/*")
                .build();
        storageRef.putFile(uri,metadata).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    Toast.makeText(getApplicationContext(), "Error while uploading video: " + task.getException().getLocalizedMessage(), Toast.LENGTH_LONG).show();
                }
                return storageRef.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {

                final Uri downUri = task.getResult();
                String messageId = messagesRef.push().getKey();
                String senderId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                String date = getCurrentDateTime();
                ChatMessageModel message = new ChatMessageModel(messageId, date, "", senderId, "sent","video","",downUri.toString());
                messagesRef.child(messageId).setValue(message);
                progressDialog.dismiss();

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(GroupChat.this, "FAILED TO SEND VIDEO", Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }
        });
    }


    public void showEditChatRoom() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.edit_chatroom_layout, null, false);
        builder.setView(view);
        builder.setCancelable(true);
        EditText chatRoomName=view.findViewById(R.id.chatRoomName);
        String newChatRoomName=chatRoomName.getText().toString();
        final AlertDialog dialog = builder.show();
        CardView dismissBtn = view.findViewById(R.id.cancelDialog);
        dismissBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               editChatRoomName(CHAT_ROOM_ID,chatRoomName.getText().toString());
            }
        });
    }

    public void showChatRoomMembers() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.chat_room_members, null, false);
        builder.setView(view);
        builder.setCancelable(true);

        userList = new ArrayList<>();
        userAdapter = new UserAdapter(this, userList,true,false);
        RecyclerView usersRecyclerView = view.findViewById(R.id.users_recycler_view);
        // Set up RecyclerView
        usersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        usersRecyclerView.setAdapter(userAdapter);




        SearchView searchView=view.findViewById(R.id.userSearchView);
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
        // Fetch chat room details to get participants
        chatRooms.child(CHAT_ROOM_ID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    ChatRoom chatRoom = dataSnapshot.getValue(ChatRoom.class);
                    if (chatRoom != null && chatRoom.getParticipants() != null) {
                        // Fetch user details for each participant
                        for (String userId : chatRoom.getParticipants()) {
                            if (userId != null) {
                                fetchUserDetails(userId);
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error
            }
        });


        final AlertDialog dialog = builder.show();
        Button dismissBtn = view.findViewById(R.id.create_chatroom_button);
        dismissBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<BasicUser> selectedUsers = userAdapter.getSelectedUsers();
                for (BasicUser user : selectedUsers) {
                    removeUserFromChatRoom(CHAT_ROOM_ID, user.getUserId());
                }
                Toast.makeText(GroupChat.this, "USER REMOVED", Toast.LENGTH_SHORT).show();

            }
        });


    }

    private void fetchUserDetails(String userId) {
        usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    BasicUser user = dataSnapshot.getValue(BasicUser.class);
                    if (user != null) {
                        userList.add(user);
                        userAdapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error
            }
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


    private void fetchAllUsersExceptChatRoomMembers(final List<String> chatRoomMembers) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference().child("User_Information");

        // Query users who are not in the chat room
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userList.clear(); // Clear the userList before populating with new users
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    // Check if the user is not in the chat room
                    String userId = snapshot.getKey();
                    if (!chatRoomMembers.contains(userId)) {
                        // User is not in the chat room, add to the userList
                        BasicUser user = snapshot.getValue(BasicUser.class);
                        userList.add(user);
                    }
                }
                userAdapter.notifyDataSetChanged(); // Notify adapter after populating the userList
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error
            }
        });
    }


    @SuppressLint("ResourceAsColor")
    public void AddRoomMembers() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.chat_room_members, null, false);
        builder.setView(view);
        builder.setCancelable(true);

        userList = new ArrayList<>();
        userAdapter = new UserAdapter(this, userList, true,false);
        RecyclerView usersRecyclerView = view.findViewById(R.id.users_recycler_view);
        // Set up RecyclerView
        usersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        usersRecyclerView.setAdapter(userAdapter);




        SearchView searchView=view.findViewById(R.id.userSearchView);
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
        // Fetch chat room details to get participants
        chatRooms.child(CHAT_ROOM_ID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    ChatRoom chatRoom = dataSnapshot.getValue(ChatRoom.class);
                    if (chatRoom != null && chatRoom.getParticipants() != null) {
                        // Fetch all users except chat room members
                        fetchAllUsersExceptChatRoomMembers(chatRoom.getParticipants());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error
            }
        });


        final AlertDialog dialog = builder.show();
        Button dismissBtn = view.findViewById(R.id.create_chatroom_button);
        dismissBtn.setText("Add");
        dismissBtn.setBackgroundColor(R.color.bottomNavigationBackground);
        dismissBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                List<BasicUser> selectedUsers = userAdapter.getSelectedUsers();
                for (BasicUser user : selectedUsers) {
                    // Add selected user to the chat room
                    addUserToChatRoom(CHAT_ROOM_ID, user.getUserId());
                }
                Toast.makeText(GroupChat.this, "Users added to the chat room", Toast.LENGTH_SHORT).show();
                dialog.dismiss(); // Close the dialog after adding users

            }
        });


    }

    private void addUserToChatRoom(String roomId, String userId) {
        DatabaseReference chatRoomRef = chatRooms.child(roomId).child("participants");

        // Check if the user is already a participant
        chatRoomRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<String> participants = new ArrayList<>();
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        String participantId = snapshot.getValue(String.class);
                        participants.add(participantId);
                    }
                }
                // Add the user as a participant if not already present
                if (!participants.contains(userId)) {
                    participants.add(userId);
                    chatRoomRef.setValue(participants)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d(TAG, "User " + userId + " added to chat room " + roomId + " successfully");
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.e(TAG, "Failed to add user " + userId + " to chat room " + roomId + ": " + e.getMessage());
                                }
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
