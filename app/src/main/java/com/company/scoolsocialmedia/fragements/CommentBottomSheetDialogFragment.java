package com.company.scoolsocialmedia.fragements;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.company.scoolsocialmedia.R;
import com.company.scoolsocialmedia.adapters.CommentsAdapter;
import com.company.scoolsocialmedia.model.Comment;
import com.company.scoolsocialmedia.model.PostModel;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class CommentBottomSheetDialogFragment extends BottomSheetDialogFragment {
    private RecyclerView recyclerView;
    private CommentsAdapter adapter;
    private List<Comment> commentList;
    private DatabaseReference commentsRef;
    private String postId;

    private Button btn_post_comment;

    private EditText edit_comment;

    String username;



    public CommentBottomSheetDialogFragment() {
        // Required empty public constructor
    }

    public CommentBottomSheetDialogFragment(String postId) {
        this.postId = postId;
    }

    public static CommentBottomSheetDialogFragment newInstance(String param1, String param2) {
        CommentBottomSheetDialogFragment fragment = new CommentBottomSheetDialogFragment();

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_comment_bottom_sheet_dialog, container, false);

        btn_post_comment=view.findViewById(R.id.btn_post_comment);
        edit_comment=view.findViewById(R.id.edit_comment);


        btn_post_comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postComment(edit_comment.getText().toString());
            }
        });
        // Initialize RecyclerView
        recyclerView = view.findViewById(R.id.recycler_existing_comments);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        commentList = new ArrayList<>();
        adapter = new CommentsAdapter(commentList);
        recyclerView.setAdapter(adapter);

        // Get the post ID from the arguments
        Bundle bundle = getArguments();
        if (bundle != null) {
            postId = bundle.getString("postId");
        }

        // Set up Firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        commentsRef = database.getReference("comments").child(postId);

        // Load comments from Firebase
        loadComments(postId);
        getUsernameFromFirebase();

        return view;
    }

    private void loadComments(String postId) {
        Query query = commentsRef.orderByChild("postId").equalTo(postId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                commentList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Comment comment = snapshot.getValue(Comment.class);
                    commentList.add(comment);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("CommentsFragment", "Failed to read comments", databaseError.toException());
            }
        });
    }


    // Method to post a comment
    private void postComment(String commentText) {
        // Get current user ID and username


        // Generate unique key for the new comment
        String commentId = commentsRef.push().getKey();

        // Create Comment object
        Comment comment = new Comment(postId, getCurrentUserId(), username,commentText);

        // Write comment to Firebase
        commentsRef.child(commentId).setValue(comment);
    }

    // Method to get current user ID
    private String getCurrentUserId() {
        // Implement this method to get current user ID
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    // Method to get current username
    private void getUsernameFromFirebase() {
        // Get the reference to the current user's information in the Firebase database
        DatabaseReference userInfoRef = FirebaseDatabase.getInstance().getReference("User_Information")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        // Attach a listener to retrieve the username
        userInfoRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Check if the user's information exists
                if (dataSnapshot.exists()) {
                    // Check if the username exists
                    if (dataSnapshot.hasChild("name")) {
                        // Retrieve the username
                         username = dataSnapshot.child("name").getValue(String.class);
                        // Use the username as needed
                        Log.d("Username", "Username: " + username);
                    } else {
                        // Handle the case where the username does not exist
                        Log.d("Username", "No username found");
                    }
                } else {
                    // Handle the case where the user's information does not exist
                    Log.d("Username", "User information not found");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle any errors that occur during the data retrieval process
                Log.e("Username", "Error retrieving username: " + databaseError.getMessage());
            }
        });
    }



}