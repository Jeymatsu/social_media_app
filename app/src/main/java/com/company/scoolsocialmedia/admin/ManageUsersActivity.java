package com.company.scoolsocialmedia.admin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.Toast;

import com.company.scoolsocialmedia.ChatRoom.CreateChatRoomActivity;
import com.company.scoolsocialmedia.R;
import com.company.scoolsocialmedia.adapters.UserAdapter;
import com.company.scoolsocialmedia.model.BasicUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ManageUsersActivity extends AppCompatActivity {

    Toolbar toolbar;
    private List<BasicUser> userList;

    private RecyclerView usersRecyclerView;
    private UserAdapter userAdapter;
    private DatabaseReference usersRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_users);

        toolbar = findViewById(R.id.chatToolbar);
        usersRecyclerView = findViewById(R.id.users_recycler_view);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        usersRef = FirebaseDatabase.getInstance().getReference().child("User_Information");
        userList = new ArrayList<>();
        userAdapter = new UserAdapter(userList,false);

        // Set up RecyclerView
        usersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        usersRecyclerView.setAdapter(userAdapter);
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
                Toast.makeText(ManageUsersActivity.this, "Failed to load users", Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    public void onBackPressed() {
        finish();
    }
}