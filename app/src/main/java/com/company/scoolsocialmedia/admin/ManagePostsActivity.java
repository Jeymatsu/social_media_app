package com.company.scoolsocialmedia.admin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.company.scoolsocialmedia.Constants;
import com.company.scoolsocialmedia.R;
import com.company.scoolsocialmedia.adapters.PostsAdapter;
import com.company.scoolsocialmedia.listeners.OnPostClickListener;
import com.company.scoolsocialmedia.listeners.OnPostUserImageClickListener;
import com.company.scoolsocialmedia.model.Notification;
import com.company.scoolsocialmedia.model.PostModel;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.wang.avi.AVLoadingIndicatorView;

import java.util.ArrayList;
import java.util.List;

public class ManagePostsActivity extends AppCompatActivity implements OnPostClickListener, OnPostUserImageClickListener {

    RecyclerView recyclerView;

    Toolbar toolbar;
    List<PostModel> mPosts, mTempPosts, mAllPosts;
    List<Notification> notifications;

    PostsAdapter mAdapter;
    DatabaseReference postDataRef, userInfoRef, notifDataRef;

    AVLoadingIndicatorView aviLoadingView;
    int totalPostsLoaded = 0;

    LinearLayout emptyMsgLayout;
    CardView postSwitchBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_posts);

        toolbar = findViewById(R.id.toolbar);
        aviLoadingView = findViewById(R.id.avi);
        emptyMsgLayout = findViewById(R.id.empty_msg);
        postSwitchBtn = findViewById(R.id.postSelectorLayout);

        initRecyclerView();
        subscribeToPosts();
    }

    private void showProgressBar() {
        recyclerView.setVisibility(View.GONE);
        aviLoadingView.show();
    }

    private void hideProgressBar() {
        aviLoadingView.hide();
    }

    private void initRecyclerView() {
        recyclerView = findViewById(R.id.posts_recylerview);
        mPosts = new ArrayList<>();
        mTempPosts = new ArrayList<>();
        mAllPosts = new ArrayList<>();
        mAdapter = new PostsAdapter(mPosts, getApplicationContext(), this, this,true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
    }

    private void subscribeToPosts() {
        mPosts.clear();
        mAllPosts.clear();
        mTempPosts.clear();
        totalPostsLoaded = 0;
        showProgressBar();
        postDataRef = FirebaseDatabase.getInstance().getReference("Posts_Table");
        postDataRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    if (dataSnapshot.hasChildren()) {
                        long totalPosts = dataSnapshot.getChildrenCount();
                        for (DataSnapshot dsp : dataSnapshot.getChildren()) {
                            PostModel post = dsp.getValue(PostModel.class);
                            post.setPost_id(dsp.getKey());
                            fetchPostUserInfo(post, totalPosts);
                        }
                    } else {
                        prepareRecyclerView(true, false);
                    }
                } else {
                    prepareRecyclerView(true, false);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), "Error While Loading Posts", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void fetchPostUserInfo(final PostModel post, final long totalPosts) {
        userInfoRef = FirebaseDatabase.getInstance().getReference("User_Information");
        userInfoRef.child(post.getUser_id()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    if (dataSnapshot.hasChild("name")) {
                        post.setPost_user_posted_name(dataSnapshot.child("name").getValue().toString());
                    } else {
                        post.setPost_user_posted_name("(No Name)");
                    }
                    mPosts.add(post);
                    if (mPosts.size() == totalPosts) {
                        prepareRecyclerView(true, false);
                        loadPostsUserImages();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                prepareRecyclerView(true, false);
                Toast.makeText(getApplicationContext(), "Error While Loading Posts", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void prepareRecyclerView(boolean showOnlyPublicPosts, boolean showOnlyUserPosts) {
        hideProgressBar();
        if (!showOnlyUserPosts) {
            if (showOnlyPublicPosts) {
                if (mPosts.isEmpty()) {
                    recyclerView.setVisibility(View.GONE);
                    emptyMsgLayout.setVisibility(View.VISIBLE);
                    postSwitchBtn.setVisibility(View.VISIBLE);
                } else {
                    PostModel post = new PostModel(null, null, null, null, "NoMorePost", null, null, null, null, null, null, null,null);
                    mPosts.add(0, post);
                    recyclerView.setVisibility(View.VISIBLE);
                    emptyMsgLayout.setVisibility(View.GONE);
                    postSwitchBtn.setVisibility(View.VISIBLE);
                    changeDisplayedItems();
                }
                mTempPosts.clear();
            } else {
                if (!mPosts.isEmpty()) {
                    showProgressBar();
                    mTempPosts.addAll(mPosts);
                    mTempPosts.remove(0);
                    mPosts.clear();
                    for (PostModel post : mTempPosts) {
                        if (!post.getPost_type().equalsIgnoreCase("NoMorePost")) {
                            if (checkIfPostCommunityEqualsMyCommunity(post)) {
                                mPosts.add(post);
                            }
                        }
                    }
                    if (mPosts.isEmpty()) {
                        recyclerView.setVisibility(View.GONE);
                        emptyMsgLayout.setVisibility(View.VISIBLE);
                        postSwitchBtn.setVisibility(View.VISIBLE);
                    } else {
                        PostModel post = new PostModel(null, null, null, null, "NoMorePost", null, null, null, null, null, null, null,null);
                        mPosts.add(0,post);
                        recyclerView.setVisibility(View.VISIBLE);
                        emptyMsgLayout.setVisibility(View.GONE);
                        postSwitchBtn.setVisibility(View.VISIBLE);
                        changeDisplayedItems();
                    }
                    hideProgressBar();
                }
            }
        } else {
            if (showOnlyPublicPosts) {
                if (!mAllPosts.isEmpty()) {
                    mTempPosts.clear();
                    mTempPosts.addAll(mAllPosts);
                    mTempPosts.remove(0);
                    mPosts.clear();
                    for (PostModel post : mTempPosts) {
                        if (!post.getPost_type().equalsIgnoreCase("NoMorePost")) {
                            if (post.getUser_id().equalsIgnoreCase(Constants.getConstantUid())) {
                                mPosts.add(post);
                            }
                        }
                    }
                    if (mPosts.isEmpty()) {
                        recyclerView.setVisibility(View.GONE);
                        emptyMsgLayout.setVisibility(View.VISIBLE);
                        postSwitchBtn.setVisibility(View.VISIBLE);
                    } else {
                        PostModel post = new PostModel(null, null, null, null, "NoMorePost", null, null, null, null, null, null, null,null);
                        mPosts.add(0,post);
                        recyclerView.setVisibility(View.VISIBLE);
                        emptyMsgLayout.setVisibility(View.GONE);
                        postSwitchBtn.setVisibility(View.VISIBLE);
                        changeDisplayedItems();
                    }
                    mTempPosts.clear();
                }
            } else {
                if (!mAllPosts.isEmpty()) {
                    showProgressBar();
                    mTempPosts.addAll(mAllPosts);
                    mTempPosts.remove(0);
                    mPosts.clear();
                    for (PostModel post : mTempPosts) {
                        if (!post.getPost_type().equalsIgnoreCase("NoMorePost")) {
                            if (checkIfPostCommunityEqualsMyCommunity(post) && post.getUser_id().equalsIgnoreCase(Constants.getConstantUid())) {
                                mPosts.add(post);
                            }
                        }
                    }
                    if (mPosts.isEmpty()) {
                        recyclerView.setVisibility(View.GONE);
                        emptyMsgLayout.setVisibility(View.VISIBLE);
                        postSwitchBtn.setVisibility(View.VISIBLE);
                    } else {
                        PostModel post = new PostModel(null, null, null, null, "NoMorePost", null, null, null, null, null, null, null,null);
                        mPosts.add(0,post);
                        recyclerView.setVisibility(View.VISIBLE);
                        emptyMsgLayout.setVisibility(View.GONE);
                        postSwitchBtn.setVisibility(View.VISIBLE);
                        changeDisplayedItems();
                    }
                    mTempPosts.clear();
                    hideProgressBar();
                }
            }
        }
    }

    private void changeDisplayedItems(){
        recyclerView.setAdapter(null);
        recyclerView.setLayoutManager(null);
        recyclerView.setAdapter(mAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        mAdapter.notifyDataSetChanged();
    }

    private boolean checkIfPostCommunityEqualsMyCommunity(PostModel post) {
        for (String st : post.getTagged_communities()) {
            if (st.equalsIgnoreCase(Constants.uCommunity)) {
                return true;
            }
        }
        return false;
    }
    private void loadPostsUserImages() {
        if (!mPosts.isEmpty()) {
            for (int i = 0; i < mPosts.size(); i++) {
                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("profileImages/" + mPosts.get(i).getUser_id());
                final int finalI = i;
                storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        String imageURL = uri.toString();
                        mPosts.get(finalI).setPost_user_posted_image(imageURL);
                        mAdapter.notifyImageLoaded(finalI);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                    }
                });
            }
        }
    }


    @Override
    public void showPostDetail(PostModel post) {

    }

    @Override
    public void showProfile(String id) {

    }
}