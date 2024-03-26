package com.company.scoolsocialmedia.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.SearchView;
import android.widget.Toast;

import com.company.scoolsocialmedia.R;
import com.company.scoolsocialmedia.adapters.UserAdapter;
import com.company.scoolsocialmedia.admin.ManageUsersActivity;
import com.company.scoolsocialmedia.model.BasicUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class SearchUsersActivity extends AppCompatActivity {

    Toolbar toolbar;
    private List<BasicUser> userList;

    private RecyclerView usersRecyclerView;
    private UserAdapter userAdapter;
    private DatabaseReference usersRef;
    private SearchView searchView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_users);

        toolbar = findViewById(R.id.chatToolbar);
        usersRecyclerView = findViewById(R.id.users_recycler_view);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        searchView = findViewById(R.id.userSearchView);

        usersRef = FirebaseDatabase.getInstance().getReference().child("User_Information");
        userList = new ArrayList<>();
        userAdapter = new UserAdapter(this,userList,true);
        // Set up RecyclerView
        usersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        usersRecyclerView.setAdapter(userAdapter);
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
                Toast.makeText(SearchUsersActivity.this, "Failed to load users", Toast.LENGTH_SHORT).show();
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


    @Override
    public void onBackPressed() {
        finish();
    }
}