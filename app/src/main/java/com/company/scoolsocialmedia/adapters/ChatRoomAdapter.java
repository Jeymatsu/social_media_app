package com.company.scoolsocialmedia.adapters;

import static android.app.PendingIntent.getActivity;
import static android.content.ContentValues.TAG;

import static androidx.core.content.ContextCompat.startActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.company.scoolsocialmedia.R;
import com.company.scoolsocialmedia.activities.GroupChat;
import com.company.scoolsocialmedia.model.BasicUser;
import com.company.scoolsocialmedia.model.ChatRoom;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class ChatRoomAdapter extends RecyclerView.Adapter<ChatRoomAdapter.ChatRoomViewHolder> {

    private List<ChatRoom> chatRoomsList;
    private OnChatRoomClickListener onChatRoomClickListener;

    private Boolean isAdmin;

    public ChatRoomAdapter(List<ChatRoom> chatRoomsList, Boolean isAdmin) {
        this.chatRoomsList = chatRoomsList;
        this.isAdmin = isAdmin;
    }

    @NonNull
    @Override
    public ChatRoomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_room_item_layout, parent, false);
        return new ChatRoomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatRoomViewHolder holder, int position) {
        ChatRoom chatRoom = chatRoomsList.get(position);
        holder.bind(chatRoom);
        holder.chatRoomLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), GroupChat.class);
                // Use the clicked chat room from the adapter
                intent.putExtra("CHAT_ROOM_ID", chatRoom.getRoomId());
                intent.putExtra("CHAT_ROOM_NAME", chatRoom.getName());
                v.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return chatRoomsList.size();
    }

    public void setOnChatRoomClickListener(OnChatRoomClickListener listener) {
        this.onChatRoomClickListener = listener;
    }

    public interface OnChatRoomClickListener {
        void onChatRoomClick(ChatRoom chatRoom);

        void onChatRoomClick(int position);
    }

    class ChatRoomViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView chatRoomNameTextView;
        private CardView chatRoomLayout;
        private Button btnDelete;

        public ChatRoomViewHolder(@NonNull View itemView) {
            super(itemView);
            chatRoomNameTextView = itemView.findViewById(R.id.chat_room_name_textview);
            chatRoomLayout= itemView.findViewById(R.id.chatRoomLayout);
            btnDelete=itemView.findViewById(R.id.btnDelete);


            btnDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ChatRoom chatRoom = chatRoomsList.get(getAdapterPosition());
                    showDeleteConfirmationDialog(chatRoom);
                }
            });



            itemView.setOnClickListener(this);


        }

        private void showDeleteConfirmationDialog(final ChatRoom chatRoom) {
            AlertDialog.Builder builder = new AlertDialog.Builder(itemView.getContext());
            builder.setTitle("Confirm Deletion");
            builder.setMessage("Are you sure you want to delete this chat room?");
            builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // User clicked Delete, proceed with deletion
                    deleteUser(chatRoom);
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

        private void deleteUser(ChatRoom chatRoom) {
            DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference().child("chatrooms");
            usersRef.child(chatRoom.getRoomId()).removeValue()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            // User data successfully deleted from Firebase
                            int position = chatRoomsList.indexOf(chatRoom);
                            if (position != -1) {
                                chatRoomsList.remove(position);
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
            chatRoomsList.remove(position);
            notifyItemRemoved(position);
        }

        public void bind(ChatRoom chatRoom) {
            chatRoomNameTextView.setText(chatRoom.getName());
            if(isAdmin){
                btnDelete.setVisibility(View.VISIBLE);
            }else {
                btnDelete.setVisibility(View.GONE);

            }        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION && onChatRoomClickListener != null) {
                ChatRoom clickedChatRoom = chatRoomsList.get(position);
                onChatRoomClickListener.onChatRoomClick(clickedChatRoom);
            }
        }
    }
}
