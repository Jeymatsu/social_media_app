package com.company.scoolsocialmedia.adapters;

import static android.content.ContentValues.TAG;
import static android.content.Intent.getIntent;

import static androidx.core.content.ContextCompat.startActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.company.scoolsocialmedia.R;
import com.company.scoolsocialmedia.activities.ImageDetailActivity;
import com.company.scoolsocialmedia.activities.VideoPlayerActivity;
import com.company.scoolsocialmedia.fragements.CommentBottomSheetDialogFragment;
import com.company.scoolsocialmedia.listeners.OnPostClickListener;
import com.company.scoolsocialmedia.listeners.OnPostUserImageClickListener;
import com.company.scoolsocialmedia.model.BasicUser;
import com.company.scoolsocialmedia.model.NotificationManager;
import com.company.scoolsocialmedia.model.PostModel;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import org.apache.commons.text.WordUtils;

import java.util.List;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;

public class PostsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int POST_NO_IMAGE_TYPE = 1;
    public static final int POST_IMAGE_TYPE = 2;
    public static final int POST_VIDEO_TYPE = 3;
    public static final int POST_NO_MORE_POST_TYPE = 4;

    private List<PostModel> mPostsList;
    private Context mContex;
    private OnPostClickListener mOnPostClickListener;
    private OnPostUserImageClickListener listener;
    private Boolean isAdmin;

    private RefreshListener refreshListener;
    public interface RefreshListener {
        void onRefresh();
    }

    public void setRefreshListener(RefreshListener listener) {
        this.refreshListener = listener;
    }




    public PostsAdapter(List<PostModel> mPostsList, Context mContex, OnPostClickListener listener, OnPostUserImageClickListener listener2, Boolean isAdmin) {
        this.mPostsList = mPostsList;
        this.listener = listener2;
        this.mContex = mContex;
        this.mOnPostClickListener = listener;
        this.isAdmin=isAdmin;
    }

    @Override
    public int getItemViewType(int position) {
        PostModel post = mPostsList.get(position);
        if ((post.getPost_image() == null || post.getPost_image().equals("")) && !post.getPost_type().equals("NoMorePost")&& post.getPost_video()==null) {
            return POST_NO_IMAGE_TYPE;
        } else if (post.getPost_type().equals("NoMorePost")) {
            return POST_NO_MORE_POST_TYPE;
        } else if (post.getPost_image()==null) {
            return POST_VIDEO_TYPE;

        } else {
            return POST_IMAGE_TYPE;
        }
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == POST_NO_IMAGE_TYPE) {
            return new PostsWithNoImageViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.main_post_item_layout, parent, false));
        } else if (viewType == POST_NO_MORE_POST_TYPE) {
            return new PostNoMorePostViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.main_no_more_posts_layout, parent, false));
        } else if (viewType == POST_IMAGE_TYPE) {
            return new PostsWithImageViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.main_img_post_item__layout, parent, false));
        } else if (viewType == POST_VIDEO_TYPE) {
            return new PostsWithVideoViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.main_video_post_item_layout, parent, false));
        } else {
            return null;
        }
    }

    public List<PostModel> getDataSet() {
        return mPostsList;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        PostModel post = mPostsList.get(position);
        switch (holder.getItemViewType()) {
            case POST_NO_IMAGE_TYPE:
                handlePostWithNoImage(holder, post);
                break;
            case POST_IMAGE_TYPE:
                handlePostWithImage(holder, post);
                break;
            case POST_VIDEO_TYPE:
                handlePostWithVideo(holder, post);
                break;
        }
    }


    public void removePost(int position) {
        mPostsList.remove(position);
        notifyItemRemoved(position);
    }



    private void deletePostFromDB(String postID) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference().child("Posts_Table");
        usersRef.child(postID).removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // User data successfully deleted from Firebase
                        Toast.makeText(mContex, "POST DELETED SUCCESSFULLY", Toast.LENGTH_SHORT).show();
                        // Notify the activity to refresh
                        if (refreshListener != null) {
                            refreshListener.onRefresh();
                        }


                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Error occurred while deleting user data
                        Log.e(TAG, "Error deleting user data: " + e.getMessage());
                        // You may want to show a toast or handle the error in some way
                    }
                });


    }

    private void deletePost(String postId, Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Delete Post");
        builder.setMessage("Are you sure you want to delete this post?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                deletePostFromDB(postId);
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

    }

    private void handlePostWithNoImage(RecyclerView.ViewHolder holder, final PostModel post) {

        ((PostsWithNoImageViewHolder) holder).setIsRecyclable(false);


        if (isAdmin) {
            ((PostsWithNoImageViewHolder) holder).btnDelete.setVisibility(View.VISIBLE);
            ((PostsWithNoImageViewHolder) holder).btnDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                  deletePost(post.getPost_id(),mContex);

                }
            });


        }else {
            ((PostsWithNoImageViewHolder) holder).btnDelete.setVisibility(View.GONE);


        }

        ((PostsWithNoImageViewHolder) holder).postItemBody.setText(post.getPost_body());
        ((PostsWithNoImageViewHolder) holder).postItemTitle.setText(post.getPost_title());
        ((PostsWithNoImageViewHolder) holder).postItemType.setText(post.getPost_type());
        ((PostsWithNoImageViewHolder) holder).postItemDate.setText(post.getPost_date());
        ((PostsWithNoImageViewHolder) holder).postItemUserName.setText(WordUtils.capitalize(post.getPost_user_posted_name()));
        // Retrieve and display the like count for the post
        ((PostsWithNoImageViewHolder) holder).txtNumberLikes.setText(String.valueOf(post.getLikes())+ " Likes");

        ((PostsWithNoImageViewHolder) holder).postItemUserImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.showProfile(post.getUser_id());
            }
        });
        ((PostsWithNoImageViewHolder) holder).postItemUserName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.showProfile(post.getUser_id());
            }
        });
        if (post.getPost_user_posted_image() != null) {
            Glide.with(((PostsWithNoImageViewHolder) holder).itemView.getContext()).load(post.getPost_user_posted_image()).diskCacheStrategy(DiskCacheStrategy.ALL).placeholder(R.drawable.main_user_profile_avatar).into(((PostsWithNoImageViewHolder) holder).postItemUserImg);
        }
        ((PostsWithNoImageViewHolder) holder).titleAndBodyLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mOnPostClickListener.showPostDetail(post);
            }
        });

        ((PostsWithNoImageViewHolder) holder).like_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                DatabaseReference likesRef = FirebaseDatabase.getInstance().getReference("Likes_Table").child(post.getPost_id()).child(FirebaseAuth.getInstance().getUid());
                likesRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            // User has already liked the post, show a message indicating that
                            Toast.makeText(mContex, "You have already liked this post", Toast.LENGTH_SHORT).show();
                        } else {
                            // User has not liked the post yet, increment the like count
                            DatabaseReference postRef = FirebaseDatabase.getInstance().getReference("Posts_Table").child(post.getPost_id());
                            postRef.child("likes").runTransaction(new Transaction.Handler() {
                                @NonNull
                                @Override
                                public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                                    Integer currentLikes = mutableData.getValue(Integer.class);
                                    if (currentLikes == null) {
                                        // If likes node does not exist, set likes to 1
                                        mutableData.setValue(1);
                                    } else {
                                        // Increment likes count
                                        mutableData.setValue(currentLikes + 1);
                                    }
                                    return Transaction.success(mutableData);
                                }

                                @Override
                                public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {
                                    if (error != null) {
                                        // Handle error
                                    } else {
                                        // User has successfully liked the post, store the like in Likes_Table
                                        likesRef.setValue(true);
                                        // Fetch the username of the post author
                                        NotificationManager notificationManager = new NotificationManager();
                                        notificationManager.getUsernameFromFirebase(FirebaseAuth.getInstance().getUid(), new NotificationManager.UsernameCallback() {
                                            @Override
                                            public void onUsernameReceived(String postAuthorUsername) {
                                                // Construct the notification message using the post author's username
                                                String notificationMessage = "New Like on Your Post '" + post.getPost_title() + "' by " + postAuthorUsername;
                                                // Send the notification
                                                notificationManager.sendNotification(" Like " ,notificationMessage,post.getPost_id());
                                            }

                                            @Override
                                            public void onError(String errorMessage) {
                                                // Handle error while fetching username
                                            }
                                        });
                                        Toast.makeText(mContex, "Liked", Toast.LENGTH_SHORT).show();      }
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // Handle error
                    }
                });
            }






        });

        ((PostsWithNoImageViewHolder) holder).comment_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Pass the postId to the CommentBottomSheetDialogFragment constructor
                String postID=post.getPost_id();
                String postUserId=post.getUser_id();
                CommentBottomSheetDialogFragment bottomSheetDialogFragment = new CommentBottomSheetDialogFragment(postID,postUserId);
                bottomSheetDialogFragment.show(((AppCompatActivity) v.getContext()).getSupportFragmentManager(), bottomSheetDialogFragment.getTag());

            }
        });
//        if (post.getTagged_communities().size() > 0) {
//            int n = post.getTagged_communities().size();
//            if (n <= 2) {
//                ((PostsWithNoImageViewHolder) holder).secondCateogryHolder.setVisibility(View.GONE);
//                ((PostsWithNoImageViewHolder) holder).thirdCateogryHolder.setVisibility(View.GONE);
//                for (int i = 0; i < post.getTagged_communities().size(); i++) {
//                    TextView textView = new TextView(mContex);
//                    textView.setText(post.getTagged_communities().get(i));
//                    textView.setTextColor(Color.WHITE);
//                    textView.setTextSize(14);
//                    textView.setBackgroundResource(R.drawable.background_search);
//                    textView.getBackground().setColorFilter(randomizeColor(), PorterDuff.Mode.SRC_ATOP);
//                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//                    lp.setMarginStart(16);
//                    textView.setLayoutParams(lp);
//                    textView.setPadding(8, 4, 8, 4);
//                    ((PostsWithNoImageViewHolder) holder).firstCateogryHolder.addView(textView);
//                }
//            } else {
//                int dist = 0;
//                if (n <= 6) {
//                    ((PostsWithNoImageViewHolder) holder).secondCateogryHolder.setVisibility(View.VISIBLE);
//                    dist = 2;
//                } else {
//                    ((PostsWithNoImageViewHolder) holder).thirdCateogryHolder.setVisibility(View.VISIBLE);
//                    dist = 3;
//                }
//                int len = 0;
//                int layoutToAdd = 1;
//                for (int i = 0; i < post.getTagged_communities().size(); i++) {
//                    TextView textView = new TextView(mContex);
//                    textView.setText(post.getTagged_communities().get(i));
//                    textView.setTextColor(Color.WHITE);
//                    textView.setTextSize(14);
//                    textView.setBackgroundResource(R.drawable.background_search);
//                    textView.getBackground().setColorFilter(randomizeColor(), PorterDuff.Mode.SRC_ATOP);
//                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//                    lp.setMarginStart(16);
//                    textView.setLayoutParams(lp);
//                    textView.setPadding(8, 4, 8, 4);
//                    if (dist == 2) {
//                        if (layoutToAdd == 1) {
//                            ((PostsWithNoImageViewHolder) holder).firstCateogryHolder.addView(textView);
//                            len += post.getTagged_communities().get(i).length();
//                            if (len > 35) {
//                                layoutToAdd = 2;
//                                len = 0;
//                            } else {
//                                layoutToAdd = 1;
//                            }
//                        } else {
//                            ((PostsWithNoImageViewHolder) holder).secondCateogryHolder.addView(textView);
//                            len += post.getTagged_communities().get(i).length();
//                            if (len > 35) {
//                                layoutToAdd = 1;
//                                len = 0;
//                            } else {
//                                layoutToAdd = 2;
//                            }
//                        }
//                    } else {
//                        if (layoutToAdd == 1) {
//                            ((PostsWithNoImageViewHolder) holder).firstCateogryHolder.addView(textView);
//                            len += post.getTagged_communities().get(i).length();
//                            if (len > 35) {
//                                layoutToAdd = 2;
//                                len = 0;
//                            } else {
//                                layoutToAdd = 1;
//                            }
//                        } else if (layoutToAdd == 2) {
//                            ((PostsWithNoImageViewHolder) holder).secondCateogryHolder.addView(textView);
//                            len += post.getTagged_communities().get(i).length();
//                            if (len > 35) {
//                                layoutToAdd = 3;
//                                len = 0;
//                            } else {
//                                layoutToAdd = 2;
//                            }
//                        } else {
//                            ((PostsWithNoImageViewHolder) holder).thirdCateogryHolder.addView(textView);
//                            len += post.getTagged_communities().get(i).length();
//                            if (len > 35) {
//                                layoutToAdd = 2;
//                                len = 0;
//                            } else {
//                                layoutToAdd = 3;
//                            }
//                        }
//                    }
//                }
//            }
//        }
    }

    private void handlePostWithImage(final RecyclerView.ViewHolder holder, final PostModel post) {
        ((PostsWithImageViewHolder) holder).setIsRecyclable(false);

        if (isAdmin) {
            ((PostsWithImageViewHolder) holder).btnDelete.setVisibility(View.VISIBLE);
            ((PostsWithImageViewHolder) holder).btnDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    deletePost(post.getPost_id(),mContex);

                }
            });


        }else {
            ((PostsWithImageViewHolder) holder).btnDelete.setVisibility(View.GONE);


        }
        ((PostsWithImageViewHolder) holder).postImageItemImageInfo.setText(post.getPost_image_info());
        ((PostsWithImageViewHolder) holder).postImageItemType.setText(post.getPost_type());
        ((PostsWithImageViewHolder) holder).postImageItemDate.setText(post.getPost_date());
        ((PostsWithImageViewHolder) holder).postImageItemUserName.setText(WordUtils.capitalize(post.getPost_user_posted_name()));
        // Retrieve and display the like count for the post
        ((PostsWithImageViewHolder) holder).txtNumberLikes.setText(String.valueOf(post.getLikes())+ " Likes");
        ((PostsWithImageViewHolder) holder).postImageItemUserImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.showProfile(post.getUser_id());
            }
        });
        ((PostsWithImageViewHolder) holder).postImageItemUserName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.showProfile(post.getUser_id());
            }
        });
        Glide.with(((PostsWithImageViewHolder) holder).itemView.getContext()).load(post.getPost_image()).diskCacheStrategy(DiskCacheStrategy.ALL).placeholder(R.drawable.main_post_image_avatart).into(((PostsWithImageViewHolder) holder).postImageMainImage);
        if (post.getPost_user_posted_image() != null) {
            Glide.with(((PostsWithImageViewHolder) holder).itemView.getContext()).load(post.getPost_user_posted_image()).diskCacheStrategy(DiskCacheStrategy.ALL).placeholder(R.drawable.main_user_profile_avatar).into(((PostsWithImageViewHolder) holder).postImageItemUserImg);
        }
        ((PostsWithImageViewHolder) holder).postImageMainImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((PostsWithImageViewHolder) holder).itemView.getContext().startActivity(new Intent(((PostsWithImageViewHolder) holder).itemView.getContext(), ImageDetailActivity.class).putExtra("imageUrl", post.getPost_image()));
            }
        });
        ((PostsWithImageViewHolder) holder).postImageItemImageInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mOnPostClickListener.showPostDetail(post);
            }
        });

        ((PostsWithImageViewHolder) holder).like_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Toast.makeText(mContex, "Liked", Toast.LENGTH_SHORT).show();

            }
        });

        ((PostsWithImageViewHolder) holder).comment_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Pass the postId to the CommentBottomSheetDialogFragment constructor
                String postID=post.getPost_id();
                String postUserId=post.getUser_id();
                CommentBottomSheetDialogFragment bottomSheetDialogFragment = new CommentBottomSheetDialogFragment(postID,postUserId);
                bottomSheetDialogFragment.show(((AppCompatActivity) v.getContext()).getSupportFragmentManager(), bottomSheetDialogFragment.getTag());

            }
        });


        ((PostsWithImageViewHolder) holder).like_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                DatabaseReference likesRef = FirebaseDatabase.getInstance().getReference("Likes_Table").child(post.getPost_id()).child(FirebaseAuth.getInstance().getUid());
                likesRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            // User has already liked the post, show a message indicating that
                            Toast.makeText(mContex, "You have already liked this post", Toast.LENGTH_SHORT).show();
                        } else {
                            // User has not liked the post yet, increment the like count
                            DatabaseReference postRef = FirebaseDatabase.getInstance().getReference("Posts_Table").child(post.getPost_id());
                            postRef.child("likes").runTransaction(new Transaction.Handler() {
                                @NonNull
                                @Override
                                public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                                    Integer currentLikes = mutableData.getValue(Integer.class);
                                    if (currentLikes == null) {
                                        // If likes node does not exist, set likes to 1
                                        mutableData.setValue(1);
                                    } else {
                                        // Increment likes count
                                        mutableData.setValue(currentLikes + 1);
                                    }
                                    return Transaction.success(mutableData);
                                }

                                @Override
                                public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {
                                    if (error != null) {
                                        // Handle error
                                    } else {
                                        // User has successfully liked the post, store the like in Likes_Table
                                        likesRef.setValue(true);
                                        // Fetch the username of the post author
                                        NotificationManager notificationManager = new NotificationManager();
                                        notificationManager.getUsernameFromFirebase(FirebaseAuth.getInstance().getUid(), new NotificationManager.UsernameCallback() {
                                            @Override
                                            public void onUsernameReceived(String postAuthorUsername) {
                                                // Construct the notification message using the post author's username
                                                String notificationMessage = "New Like on Your Post '" + post.getPost_title() + "' by " + postAuthorUsername;
                                                // Send the notification
                                                notificationManager.sendNotification(" Like " ,notificationMessage,post.getPost_id());
                                            }

                                            @Override
                                            public void onError(String errorMessage) {
                                                // Handle error while fetching username
                                            }
                                        });
                                        Toast.makeText(mContex, "Liked", Toast.LENGTH_SHORT).show();      }
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // Handle error
                    }
                });
            }






        });


//        if (post.getTagged_communities().size() > 0) {
//            int n = post.getTagged_communities().size();
//            if (n <= 2) {
//                ((PostsWithImageViewHolder) holder).secondCateogryHolder.setVisibility(View.GONE);
//                ((PostsWithImageViewHolder) holder).thirdCateogryHolder.setVisibility(View.GONE);
//                for (int i = 0; i < post.getTagged_communities().size(); i++) {
//                    TextView textView = new TextView(mContex);
//                    textView.setText(post.getTagged_communities().get(i));
//                    textView.setTextColor(Color.WHITE);
//                    textView.setTextSize(14);
//                    textView.setBackgroundResource(R.drawable.background_search);
//                    textView.getBackground().setColorFilter(randomizeColor(), PorterDuff.Mode.SRC_ATOP);
//                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//                    lp.setMarginStart(16);
//                    textView.setLayoutParams(lp);
//                    textView.setPadding(8, 4, 8, 4);
//                    ((PostsWithImageViewHolder) holder).firstCateogryHolder.addView(textView);
//                }
//            } else {
//                int dist = 0;
//                if (n <= 6) {
//                    ((PostsWithImageViewHolder) holder).secondCateogryHolder.setVisibility(View.VISIBLE);
//                    dist = 2;
//                } else {
//                    ((PostsWithImageViewHolder) holder).thirdCateogryHolder.setVisibility(View.VISIBLE);
//                    dist = 3;
//                }
//                int len = 0;
//                int layoutToAdd = 1;
//                for (int i = 0; i < post.getTagged_communities().size(); i++) {
//                    TextView textView = new TextView(mContex);
//                    textView.setText(post.getTagged_communities().get(i));
//                    textView.setTextColor(Color.WHITE);
//                    textView.setTextSize(14);
//                    textView.setBackgroundResource(R.drawable.background_search);
//                    textView.getBackground().setColorFilter(randomizeColor(), PorterDuff.Mode.SRC_ATOP);
//                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//                    lp.setMarginStart(16);
//                    textView.setLayoutParams(lp);
//                    textView.setPadding(8, 4, 8, 4);
//                    if (dist == 2) {
//                        if (layoutToAdd == 1) {
//                            ((PostsWithImageViewHolder) holder).firstCateogryHolder.addView(textView);
//                            len += post.getTagged_communities().get(i).length();
//                            if (len > 35) {
//                                layoutToAdd = 2;
//                                len = 0;
//                            } else {
//                                layoutToAdd = 1;
//                            }
//                        } else {
//                            ((PostsWithImageViewHolder) holder).secondCateogryHolder.addView(textView);
//                            len += post.getTagged_communities().get(i).length();
//                            if (len > 35) {
//                                layoutToAdd = 1;
//                                len = 0;
//                            } else {
//                                layoutToAdd = 2;
//                            }
//                        }
//                    } else {
//                        if (layoutToAdd == 1) {
//                            ((PostsWithImageViewHolder) holder).firstCateogryHolder.addView(textView);
//                            len += post.getTagged_communities().get(i).length();
//                            if (len > 35) {
//                                layoutToAdd = 2;
//                                len = 0;
//                            } else {
//                                layoutToAdd = 1;
//                            }
//                        } else if (layoutToAdd == 2) {
//                            ((PostsWithImageViewHolder) holder).secondCateogryHolder.addView(textView);
//                            len += post.getTagged_communities().get(i).length();
//                            if (len > 35) {
//                                layoutToAdd = 3;
//                                len = 0;
//                            } else {
//                                layoutToAdd = 2;
//                            }
//                        } else {
//                            ((PostsWithImageViewHolder) holder).thirdCateogryHolder.addView(textView);
//                            len += post.getTagged_communities().get(i).length();
//                            if (len > 35) {
//                                layoutToAdd = 2;
//                                len = 0;
//                            } else {
//                                layoutToAdd = 3;
//                            }
//                        }
//                    }
//                }
//            }
//        }
    }


    private void handlePostWithVideo(final RecyclerView.ViewHolder holder, final PostModel post) {
        ((PostsWithVideoViewHolder) holder).setIsRecyclable(false);

        if (isAdmin) {
            ((PostsWithVideoViewHolder) holder).btnDelete.setVisibility(View.VISIBLE);
            ((PostsWithVideoViewHolder) holder).btnDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    deletePost(post.getPost_id(),mContex);

                }
            });


        }else {
            ((PostsWithVideoViewHolder) holder).btnDelete.setVisibility(View.GONE);


        }
        ((PostsWithVideoViewHolder) holder).postImageItemImageInfo.setText(post.getPost_image_info());
        ((PostsWithVideoViewHolder) holder).postImageItemType.setText("Video");
        ((PostsWithVideoViewHolder) holder).postImageItemDate.setText(post.getPost_date());
        ((PostsWithVideoViewHolder) holder).postImageItemUserName.setText(WordUtils.capitalize(post.getPost_user_posted_name()));
        // Retrieve and display the like count for the post
        ((PostsWithVideoViewHolder) holder).txtNumberLikes.setText(String.valueOf(post.getLikes()) + " Likes");
        ((PostsWithVideoViewHolder) holder).postImageItemUserImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.showProfile(post.getUser_id());
            }
        });
        ((PostsWithVideoViewHolder) holder).postImageItemUserName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.showProfile(post.getUser_id());
            }
        });

        Glide.with(holder.itemView.getContext())
                .load(post.getPost_video()) // Assuming getPost_video_thumbnail() returns the URL of the video thumbnail
                .placeholder(R.drawable.baseline_play_circle_outline_24) // Placeholder thumbnail until the actual thumbnail loads
                .into(((PostsWithVideoViewHolder) holder).main_image_post_image_imgview);

        ((PostsWithVideoViewHolder) holder).main_image_post_image_imgview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Implement logic to play the video
                // For example, you can start a new activity with a VideoView to play the video
                Intent intent = new Intent(view.getContext(), VideoPlayerActivity.class);
                intent.putExtra("videoUrl", post.getPost_video()); // Pass the URL of the video to the VideoPlayerActivity
                view.getContext().startActivity(intent);
            }
        });





        ((PostsWithVideoViewHolder) holder).like_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                DatabaseReference likesRef = FirebaseDatabase.getInstance().getReference("Likes_Table").child(post.getPost_id()).child(FirebaseAuth.getInstance().getUid());
                likesRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            // User has already liked the post, show a message indicating that
                            Toast.makeText(mContex, "You have already liked this post", Toast.LENGTH_SHORT).show();
                        } else {
                            // User has not liked the post yet, increment the like count
                            DatabaseReference postRef = FirebaseDatabase.getInstance().getReference("Posts_Table").child(post.getPost_id());
                            postRef.child("likes").runTransaction(new Transaction.Handler() {
                                @NonNull
                                @Override
                                public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                                    Integer currentLikes = mutableData.getValue(Integer.class);
                                    if (currentLikes == null) {
                                        // If likes node does not exist, set likes to 1
                                        mutableData.setValue(1);
                                    } else {
                                        // Increment likes count
                                        mutableData.setValue(currentLikes + 1);
                                    }
                                    return Transaction.success(mutableData);
                                }

                                @Override
                                public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {
                                    if (error != null) {
                                        // Handle error
                                    } else {
                                        // User has successfully liked the post, store the like in Likes_Table
                                        likesRef.setValue(true);
                                        // Fetch the username of the post author
                                        NotificationManager notificationManager = new NotificationManager();
                                        notificationManager.getUsernameFromFirebase(FirebaseAuth.getInstance().getUid(), new NotificationManager.UsernameCallback() {
                                            @Override
                                            public void onUsernameReceived(String postAuthorUsername) {
                                                // Construct the notification message using the post author's username
                                                String notificationMessage = "New Like on Your Post '" + post.getPost_body()+ "' by " + postAuthorUsername;
                                                // Send the notification
                                                notificationManager.sendNotification("Like",notificationMessage, post.getUser_id());
                                                Toast.makeText(mContex, "Liked", Toast.LENGTH_SHORT).show();
                                            }

                                            @Override
                                            public void onError(String errorMessage) {
                                                // Handle error while fetching username
                                            }
                                        });

                                    }
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // Handle error
                    }
                });
            }






        });

//        ((PostsWithVideoViewHolder) holder).postImageMainImage.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                ((PostsWithVideoViewHolder) holder).itemView.getContext().startActivity(new Intent(((PostsWithImageViewHolder) holder).itemView.getContext(), ImageDetailActivity.class).putExtra("imageUrl", post.getPost_image()));
//            }
//        });
        ((PostsWithVideoViewHolder) holder).postImageItemImageInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mOnPostClickListener.showPostDetail(post);
            }
        });

        ((PostsWithVideoViewHolder) holder).like_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Toast.makeText(mContex, "Liked", Toast.LENGTH_SHORT).show();

            }
        });

        ((PostsWithVideoViewHolder) holder).comment_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Pass the postId to the CommentBottomSheetDialogFragment constructor
                String postID=post.getPost_id();
                String postUserId=post.getUser_id();
                CommentBottomSheetDialogFragment bottomSheetDialogFragment = new CommentBottomSheetDialogFragment(postID,postUserId);
                bottomSheetDialogFragment.show(((AppCompatActivity) v.getContext()).getSupportFragmentManager(), bottomSheetDialogFragment.getTag());

            }
        });

//        if (post.getTagged_communities().size() > 0) {
//            int n = post.getTagged_communities().size();
//            if (n <= 2) {
//                ((PostsWithVideoViewHolder) holder).secondCateogryHolder.setVisibility(View.GONE);
//                ((PostsWithVideoViewHolder) holder).thirdCateogryHolder.setVisibility(View.GONE);
//                for (int i = 0; i < post.getTagged_communities().size(); i++) {
//                    TextView textView = new TextView(mContex);
//                    textView.setText(post.getTagged_communities().get(i));
//                    textView.setTextColor(Color.WHITE);
//                    textView.setTextSize(14);
//                    textView.setBackgroundResource(R.drawable.background_search);
//                    textView.getBackground().setColorFilter(randomizeColor(), PorterDuff.Mode.SRC_ATOP);
//                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//                    lp.setMarginStart(16);
//                    textView.setLayoutParams(lp);
//                    textView.setPadding(8, 4, 8, 4);
//                    ((PostsWithVideoViewHolder) holder).firstCateogryHolder.addView(textView);
//                }
//            } else {
//                int dist = 0;
//                if (n <= 6) {
//                    ((PostsWithVideoViewHolder) holder).secondCateogryHolder.setVisibility(View.VISIBLE);
//                    dist = 2;
//                } else {
//                    ((PostsWithVideoViewHolder) holder).thirdCateogryHolder.setVisibility(View.VISIBLE);
//                    dist = 3;
//                }
//                int len = 0;
//                int layoutToAdd = 1;
//                for (int i = 0; i < post.getTagged_communities().size(); i++) {
//                    TextView textView = new TextView(mContex);
//                    textView.setText(post.getTagged_communities().get(i));
//                    textView.setTextColor(Color.WHITE);
//                    textView.setTextSize(14);
//                    textView.setBackgroundResource(R.drawable.background_search);
//                    textView.getBackground().setColorFilter(randomizeColor(), PorterDuff.Mode.SRC_ATOP);
//                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//                    lp.setMarginStart(16);
//                    textView.setLayoutParams(lp);
//                    textView.setPadding(8, 4, 8, 4);
//                    if (dist == 2) {
//                        if (layoutToAdd == 1) {
//                            ((PostsWithVideoViewHolder) holder).firstCateogryHolder.addView(textView);
//                            len += post.getTagged_communities().get(i).length();
//                            if (len > 35) {
//                                layoutToAdd = 2;
//                                len = 0;
//                            } else {
//                                layoutToAdd = 1;
//                            }
//                        } else {
//                            ((PostsWithVideoViewHolder) holder).secondCateogryHolder.addView(textView);
//                            len += post.getTagged_communities().get(i).length();
//                            if (len > 35) {
//                                layoutToAdd = 1;
//                                len = 0;
//                            } else {
//                                layoutToAdd = 2;
//                            }
//                        }
//                    } else {
//                        if (layoutToAdd == 1) {
//                            ((PostsWithVideoViewHolder) holder).firstCateogryHolder.addView(textView);
//                            len += post.getTagged_communities().get(i).length();
//                            if (len > 35) {
//                                layoutToAdd = 2;
//                                len = 0;
//                            } else {
//                                layoutToAdd = 1;
//                            }
//                        } else if (layoutToAdd == 2) {
//                            ((PostsWithVideoViewHolder) holder).secondCateogryHolder.addView(textView);
//                            len += post.getTagged_communities().get(i).length();
//                            if (len > 35) {
//                                layoutToAdd = 3;
//                                len = 0;
//                            } else {
//                                layoutToAdd = 2;
//                            }
//                        } else {
//                            ((PostsWithVideoViewHolder) holder).thirdCateogryHolder.addView(textView);
//                            len += post.getTagged_communities().get(i).length();
//                            if (len > 35) {
//                                layoutToAdd = 2;
//                                len = 0;
//                            } else {
//                                layoutToAdd = 3;
//                            }
//                        }
//                    }
//                }
//            }
//        }
    }

    public void setDataSet(List<PostModel> posts) {
        this.mPostsList = posts;
        notifyDataSetChanged();
    }

    public void notifyImageLoaded(int pos) {
        notifyItemChanged(pos);
    }


    private int randomizeColor() {
        int[] colors = {R.color.category_bioSci, R.color.category_chem, R.color.category_chemEng, R.color.category_hum, R.color.category_met
                , R.color.category_civil, R.color.category_cs, R.color.category_devStd, R.color.category_math, R.color.category_pharm};
        return ContextCompat.getColor(this.mContex, colors[new Random().nextInt(colors.length)]);
    }


    @Override
    public int getItemCount() {
        return mPostsList.size();
    }

    public static class PostNoMorePostViewHolder extends RecyclerView.ViewHolder {

        public PostNoMorePostViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    public static class PostsWithNoImageViewHolder extends RecyclerView.ViewHolder {

        private TextView postItemBody;
        private TextView postItemDate;
        private TextView postItemTitle;
        private TextView postItemType;
        private CircleImageView postItemUserImg;
        private TextView postItemUserName;
        private LinearLayout titleAndBodyLayout;
//        private LinearLayout firstCateogryHolder;
//        private LinearLayout secondCateogryHolder;
//        private LinearLayout thirdCateogryHolder;

        private LinearLayout rlLike;

        private ImageView like_icon;
        private ImageView comment_icon;
        private TextView txtNumberLikes;

        private Button btnDelete;



        public PostsWithNoImageViewHolder(@NonNull View itemView) {
            super(itemView);
            postItemBody = itemView.findViewById(R.id.post_item_postBody_txtview);
            postItemDate = itemView.findViewById(R.id.post_item_postTime_txtview);
            postItemTitle = itemView.findViewById(R.id.post_item_postTitle_txtview);
            postItemType = itemView.findViewById(R.id.post_item_postType_txtview);
            postItemUserImg = itemView.findViewById(R.id.post_item_postedUserImg_imgView);
            postItemUserName = itemView.findViewById(R.id.post_item_postedUserName_txtview);
            titleAndBodyLayout = itemView.findViewById(R.id.titleAndBodyLayout);
            rlLike= itemView.findViewById(R.id.rlLike);
            like_icon = itemView.findViewById(R.id.like_icon);
            comment_icon = itemView.findViewById(R.id.comment_icon);
            txtNumberLikes=itemView.findViewById(R.id.txtNumberLikes);
            btnDelete=itemView.findViewById(R.id.btnDelete);

//            firstCateogryHolder = itemView.findViewById(R.id.postItemFirstCategoryLayout);
//            secondCateogryHolder = itemView.findViewById(R.id.postItemSecondCategoryLayout);
//            thirdCateogryHolder = itemView.findViewById(R.id.postItemThirdCategoryLayout);






        }




    }

    public static class PostsWithImageViewHolder extends RecyclerView.ViewHolder {

        private TextView postImageItemImageInfo;
        private TextView postImageItemDate;
        private TextView postImageItemType;
        private CircleImageView postImageItemUserImg;
        private ImageView postImageMainImage;
        private TextView postImageItemUserName;

        private ImageView like_icon;
        private ImageView comment_icon;

//        private LinearLayout firstCateogryHolder;
//        private LinearLayout secondCateogryHolder;
//        private LinearLayout thirdCateogryHolder;
          private TextView txtNumberLikes;

        private Button btnDelete;

        public PostsWithImageViewHolder(@NonNull View itemView) {
            super(itemView);
            postImageItemImageInfo = itemView.findViewById(R.id.post_image_item_postBody_txtview);
            postImageItemDate = itemView.findViewById(R.id.post_image_item_postTime_txtview);
            postImageItemType = itemView.findViewById(R.id.post_image_item_postType_txtview);
            postImageItemUserImg = itemView.findViewById(R.id.post_image_item_postedUserImg_imgView);
            postImageItemUserName = itemView.findViewById(R.id.post_image_item_postedUserName_txtview);
            postImageMainImage = itemView.findViewById(R.id.main_image_post_image_imgview);
            like_icon = itemView.findViewById(R.id.like_icon);
            comment_icon = itemView.findViewById(R.id.comment_icon);
            txtNumberLikes=itemView.findViewById(R.id.txtNumberLikes);
            btnDelete=itemView.findViewById(R.id.btnDelete);
//            firstCateogryHolder = itemView.findViewById(R.id.postImgItemFirstCategoryLayout);
//            secondCateogryHolder = itemView.findViewById(R.id.postImgItemSecondCategoryLayout);
//            thirdCateogryHolder = itemView.findViewById(R.id.postImgItemThirdCategoryLayout);
        }
    }



    public static class PostsWithVideoViewHolder extends RecyclerView.ViewHolder {

        private TextView postImageItemImageInfo;
        private TextView postImageItemDate;
        private TextView postImageItemType;
        private CircleImageView postImageItemUserImg;
        private ImageView main_image_post_image_imgview;
        private TextView postImageItemUserName;
//        private LinearLayout firstCateogryHolder;
//        private LinearLayout secondCateogryHolder;
//        private LinearLayout thirdCateogryHolder;
        private ImageView like_icon;
        private ImageView comment_icon;
        private TextView txtNumberLikes;
        private Button btnDelete;

        public PostsWithVideoViewHolder(@NonNull View itemView) {
            super(itemView);
            postImageItemImageInfo = itemView.findViewById(R.id.post_image_item_postBody_txtview);
            postImageItemDate = itemView.findViewById(R.id.post_image_item_postTime_txtview);
            postImageItemType = itemView.findViewById(R.id.post_image_item_postType_txtview);
            postImageItemUserImg = itemView.findViewById(R.id.post_image_item_postedUserImg_imgView);
            postImageItemUserName = itemView.findViewById(R.id.post_image_item_postedUserName_txtview);
            main_image_post_image_imgview = itemView.findViewById(R.id.main_image_post_image_imgview);
            like_icon = itemView.findViewById(R.id.like_icon);
            comment_icon = itemView.findViewById(R.id.comment_icon);
            txtNumberLikes=itemView.findViewById(R.id.txtNumberLikes);
            btnDelete=itemView.findViewById(R.id.btnDelete);
//            firstCateogryHolder = itemView.findViewById(R.id.postImgItemFirstCategoryLayout);
//            secondCateogryHolder = itemView.findViewById(R.id.postImgItemSecondCategoryLayout);
//            thirdCateogryHolder = itemView.findViewById(R.id.postImgItemThirdCategoryLayout);
        }
    }


}
