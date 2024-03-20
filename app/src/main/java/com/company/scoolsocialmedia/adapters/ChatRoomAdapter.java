package com.company.scoolsocialmedia.adapters;

import static android.app.PendingIntent.getActivity;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.company.scoolsocialmedia.R;
import com.company.scoolsocialmedia.activities.GroupChat;
import com.company.scoolsocialmedia.model.ChatRoom;
import java.util.List;

public class ChatRoomAdapter extends RecyclerView.Adapter<ChatRoomAdapter.ChatRoomViewHolder> {

    private List<ChatRoom> chatRoomsList;
    private OnChatRoomClickListener onChatRoomClickListener;
    public ChatRoomAdapter(List<ChatRoom> chatRoomsList) {
        this.chatRoomsList = chatRoomsList;
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

        public ChatRoomViewHolder(@NonNull View itemView) {
            super(itemView);
            chatRoomNameTextView = itemView.findViewById(R.id.chat_room_name_textview);
            chatRoomLayout= itemView.findViewById(R.id.chatRoomLayout);

            itemView.setOnClickListener(this);
        }

        public void bind(ChatRoom chatRoom) {
            chatRoomNameTextView.setText(chatRoom.getName());
        }

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
