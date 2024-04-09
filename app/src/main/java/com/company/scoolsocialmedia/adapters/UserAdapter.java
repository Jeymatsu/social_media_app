package com.company.scoolsocialmedia.adapters;

import static android.content.ContentValues.TAG;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.company.scoolsocialmedia.Profile.ProfileActivity;
import com.company.scoolsocialmedia.R;
import com.company.scoolsocialmedia.model.BasicUser;
import com.company.scoolsocialmedia.model.Notification;
import com.company.scoolsocialmedia.model.NotificationManager;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {
    private List<BasicUser> userList;
    private List<BasicUser> selectedUsers;
    private boolean showCheckbox;
    private boolean isSearchActivity;
    private Context context;


    public UserAdapter(Context context,List<BasicUser> userList, boolean showCheckbox,boolean isSearchActivity) {
        this.userList = userList;
        this.selectedUsers = new ArrayList<>();
        this.showCheckbox = showCheckbox;
        this.isSearchActivity = isSearchActivity;
        this.context=context;

    }
    public void setFilteredList(List<BasicUser> filteredList) {
        this.userList = filteredList;
        notifyDataSetChanged();
    }
    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_item_layout, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        BasicUser user = userList.get(position);
        holder.bind(user,showCheckbox,isSearchActivity);
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public List<BasicUser> getSelectedUsers() {
        return selectedUsers;
    }

    class UserViewHolder extends RecyclerView.ViewHolder {
        private TextView userNameTextView;
        private CheckBox userCheckBox;
        private RelativeLayout rlDelete;
        private ImageView imgDelete;
        private ImageView imgWarning;
        private CardView chatRoomLayout;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            userNameTextView = itemView.findViewById(R.id.user_name_textview);
            userCheckBox = itemView.findViewById(R.id.user_checkbox);
            rlDelete=itemView.findViewById(R.id.rlDelete);
            imgDelete=itemView.findViewById(R.id.imgDelete);
            imgWarning=itemView.findViewById(R.id.imgWarning);
            chatRoomLayout=itemView.findViewById(R.id.chatRoomLayout);

            chatRoomLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    BasicUser user = userList.get(getAdapterPosition());
                    Intent intent = new Intent(context, ProfileActivity.class);
                    intent.putExtra("uid", user.getUserId());
                    context.startActivity(intent);
                }
            });

            userCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    BasicUser user = userList.get(getAdapterPosition());
                    if (isChecked) {
                        selectedUsers.add(user);
                    } else {
                        selectedUsers.remove(user);
                    }
                }
            });


            imgDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Get the user associated with this ViewHolder
                    BasicUser user = userList.get(getAdapterPosition());
                    // Show an alert dialog to confirm deletion
                    showDeleteConfirmationDialog(user);
                }
            });

            imgWarning.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Get the user associated with this ViewHolder
                    BasicUser user = userList.get(getAdapterPosition());
                    // Show an alert dialog to confirm deletion
                    showWarningConfirmationDialog(user);
                }
            });







        }

        private void showDeleteConfirmationDialog(final BasicUser user) {
            AlertDialog.Builder builder = new AlertDialog.Builder(itemView.getContext());
            builder.setTitle("Confirm Deletion");
            builder.setMessage("Are you sure you want to delete this user?");
            builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // User clicked Delete, proceed with deletion
                    deleteUser(user);
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // User clicked Cancel, do nothing
                    dialog.dismiss();
                }
            });
            builder.show();
        }

        private void showWarningConfirmationDialog(final BasicUser user) {
            AlertDialog.Builder builder = new AlertDialog.Builder(itemView.getContext());
            builder.setTitle("Confirm Warning");
            builder.setMessage("Are you sure you want to send warning this user?");
            builder.setPositiveButton("send", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // User clicked Delete, proceed with deletion
                    sendNotificationToDatabase(user);
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // User clicked Cancel, do nothing
                    dialog.dismiss();
                }
            });
            builder.show();
        }

        private void deleteUser(BasicUser user) {
            DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference().child("User_Information");
            usersRef.child(user.getUserId()).removeValue()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            // User data successfully deleted from Firebase
                            int position = userList.indexOf(user);
                            if (position != -1) {
                                userList.remove(position);
                                notifyItemRemoved(position);
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

            // Delete posts associated with the user
            DatabaseReference postsRef = FirebaseDatabase.getInstance().getReference().child("Posts_Table");
            Query userPostsQuery = postsRef.orderByChild("user_id").equalTo(user.getUserId());
            userPostsQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        postSnapshot.getRef().removeValue();
                        // Optionally, delete other related data associated with the post
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle error
                    Log.e(TAG, "Error deleting user posts: " + databaseError.getMessage());
                }
            });
            // Update the RecyclerView after deletion
            int position = getAdapterPosition();
            userList.remove(position);
            notifyItemRemoved(position);
        }


        private void sendNotificationToDatabase(BasicUser user) {

            // Check if the post author is not the same as the commenter
            NotificationManager notificationManager = new NotificationManager();
            notificationManager.sendNotification("Warning",   "You have violated community guidelines." , user.getUserId());

        }

        public void bind(BasicUser user,boolean showCheckbox,boolean isSearchActivity) {
            userNameTextView.setText(user.getName());
            userNameTextView.setText(user.getName());
            if (showCheckbox) {
                if(isSearchActivity){
                    userCheckBox.setVisibility(View.GONE);
                    rlDelete.setVisibility(View.GONE);
//                    userCheckBox.setChecked(selectedUsers.contains(user));
                }else {
                    userCheckBox.setVisibility(View.VISIBLE);
                    rlDelete.setVisibility(View.GONE);
                    userCheckBox.setChecked(selectedUsers.contains(user));
                }

            } else {
                userCheckBox.setVisibility(View.GONE);
                rlDelete.setVisibility(View.VISIBLE);

            }
        }
    }


}
