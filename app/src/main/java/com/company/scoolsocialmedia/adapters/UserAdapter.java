package com.company.scoolsocialmedia.adapters;

import static android.content.ContentValues.TAG;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import androidx.recyclerview.widget.RecyclerView;

import com.company.scoolsocialmedia.R;
import com.company.scoolsocialmedia.model.BasicUser;
import com.company.scoolsocialmedia.model.Notification;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {
    private List<BasicUser> userList;
    private List<BasicUser> selectedUsers;
    private boolean showCheckbox;


    public UserAdapter(List<BasicUser> userList, boolean showCheckbox) {
        this.userList = userList;
        this.selectedUsers = new ArrayList<>();
        this.showCheckbox = showCheckbox;

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
        holder.bind(user,showCheckbox);
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

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            userNameTextView = itemView.findViewById(R.id.user_name_textview);
            userCheckBox = itemView.findViewById(R.id.user_checkbox);
            rlDelete=itemView.findViewById(R.id.rlDelete);
            imgDelete=itemView.findViewById(R.id.imgDelete);
            imgWarning=itemView.findViewById(R.id.imgWarning);

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
            // Update the RecyclerView after deletion
            int position = getAdapterPosition();
            userList.remove(position);
            notifyItemRemoved(position);
        }


        private void sendNotificationToDatabase(BasicUser user) {
            DatabaseReference notificationsRef=FirebaseDatabase.getInstance().getReference().child("notifications");

            // Get current timestamp
            String timestamp = String.valueOf(System.currentTimeMillis());

            // Create a Notification object with relevant data
            Notification notification = new Notification(timestamp, "", "", user.getUserId());

            // Store the notification in the database
            notificationsRef.child(timestamp).setValue(notification)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            // Notification data successfully stored in the database
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Failed to store notification data in the database
                            Log.e("WarningActivity", "Failed to send notification", e);
                        }
                    });
        }

        public void bind(BasicUser user,boolean showCheckbox) {
            userNameTextView.setText(user.getName());
            userNameTextView.setText(user.getName());
            if (showCheckbox) {
                userCheckBox.setVisibility(View.VISIBLE);
                rlDelete.setVisibility(View.GONE);
                userCheckBox.setChecked(selectedUsers.contains(user));
            } else {
                userCheckBox.setVisibility(View.GONE);
                rlDelete.setVisibility(View.VISIBLE);

            }
        }
    }


}
