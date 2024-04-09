package com.company.scoolsocialmedia.adapters;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import com.bumptech.glide.Glide;
import com.company.scoolsocialmedia.Constants;
import com.company.scoolsocialmedia.R;
import com.company.scoolsocialmedia.activities.ImageDetailActivity;
import com.company.scoolsocialmedia.activities.VideoPlayerActivity;
import com.company.scoolsocialmedia.listeners.OnMsgLayoutLongClick;
import com.company.scoolsocialmedia.model.ChatMessageModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.apache.commons.text.WordUtils;

import java.util.List;

public class GroupMessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    List<ChatMessageModel> mMessages;
    OnMsgLayoutLongClick listener;

    public GroupMessageAdapter(List<ChatMessageModel> mMessages, OnMsgLayoutLongClick listener) {
        this.mMessages = mMessages;
        this.listener = listener;
    }

    // Message types
    private static final int SENT_MESSAGE_TYPE = 1;
    private static final int RECEIVED_MESSAGE_TYPE = 2;
    private static final int WELCOME_MESSAGE_TYPE = 3;
    private static final int SENT_IMAGE_MESSAGE_TYPE = 4;
    private static final int RECEIVED_IMAGE_MESSAGE_TYPE = 5;
    private static final int SENT_VIDEO_MESSAGE_TYPE = 6;
    private static final int RECEIVED_VIDEO_MESSAGE_TYPE = 7;

    @Override
    public int getItemViewType(int position) {

        ChatMessageModel message = mMessages.get(position);
        if (message.getDate().equalsIgnoreCase("welcome")) {
            return WELCOME_MESSAGE_TYPE;
        } else if (message.getSender_id().equalsIgnoreCase(Constants.getConstantUid())) {
            if (message.getType().equalsIgnoreCase("image")) {
                return SENT_IMAGE_MESSAGE_TYPE;
            } else if (message.getType().equalsIgnoreCase("video")) {
                return SENT_VIDEO_MESSAGE_TYPE;
            } else {
                return SENT_MESSAGE_TYPE;
            }
        } else {
            if (message.getType().equalsIgnoreCase("image")) {
                return RECEIVED_IMAGE_MESSAGE_TYPE;
            } else if (message.getType().equalsIgnoreCase("video")) {
                return RECEIVED_VIDEO_MESSAGE_TYPE;
            } else {
                return RECEIVED_MESSAGE_TYPE;
            }
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view;
        switch (viewType) {
            case SENT_MESSAGE_TYPE:
                view = inflater.inflate(R.layout.chat_sent_message_layout, parent, false);
                return new SentMessageViewHolder(view);
            case RECEIVED_MESSAGE_TYPE:
                view = inflater.inflate(R.layout.chat_received_msg_layout, parent, false);
                return new ReceivedMessageViewHolder(view);
            case WELCOME_MESSAGE_TYPE:
                view = inflater.inflate(R.layout.chat_welcome_msg_layout, parent, false);
                return new WelcomeMessageViewHolder(view);
            case SENT_IMAGE_MESSAGE_TYPE:
                view = inflater.inflate(R.layout.chat_sent_image_layout, parent, false);
                return new SentImageMessageViewHolder(view);
            case RECEIVED_IMAGE_MESSAGE_TYPE:
                view = inflater.inflate(R.layout.chat_received_image_layout, parent, false);
                return new ReceivedImageMessageViewHolder(view);
            case SENT_VIDEO_MESSAGE_TYPE:
                view = inflater.inflate(R.layout.chat_sent_video_layout, parent, false);
                return new SentVideoMessageViewHolder(view);
            case RECEIVED_VIDEO_MESSAGE_TYPE:
                view = inflater.inflate(R.layout.chat_received_video_layout, parent, false);
                return new ReceivedVideoMessageViewHolder(view);
            default:
                return null;
        }
    }

    public int getItemPosition(ChatMessageModel msg) {
        for (int i = 0; i < mMessages.size(); i++) {
            if (mMessages.get(i).getMsgKey().equalsIgnoreCase(msg.getMsgKey())) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChatMessageModel message = mMessages.get(position);
        switch (holder.getItemViewType()) {
            case SENT_MESSAGE_TYPE:
                handleSentMessage(holder, message);
                break;
            case RECEIVED_MESSAGE_TYPE:
                handleReceivedMessage(holder, message);
                break;
            case WELCOME_MESSAGE_TYPE:
                handleWelcomeMessage(holder, message);
                break;
            case SENT_IMAGE_MESSAGE_TYPE:
                handleSentImageMessage(holder, message);
                break;
            case RECEIVED_IMAGE_MESSAGE_TYPE:
                handleReceivedImageMessage(holder, message);
                break;
            case SENT_VIDEO_MESSAGE_TYPE:
                handleSentVideoMessage(holder, message);
                break;
            case RECEIVED_VIDEO_MESSAGE_TYPE:
                handleReceivedVideoMessage(holder, message);
                break;
        }
    }

    private void handleWelcomeMessage(ViewHolder holder, ChatMessageModel message) {
        String msg = "";
        String[] names = message.getMessage().split("_");
        if (message.getSender_id().equalsIgnoreCase(Constants.getConstantUid())) {
            msg = "You Accepted " + WordUtils.capitalize(names[0]) + "\'s proposal. Send a message to start conversation";
        } else {
            msg = WordUtils.capitalize(names[1]) + " accepted your proposal. Send a message to start conversation";
        }
        ((WelcomeMessageViewHolder) holder).mainWelcomeMsg.setText(msg);
    }

    private void handleSentMessage(ViewHolder holder, final ChatMessageModel message) {
        ((SentMessageViewHolder) holder).mainSentMsg.setText(message.getMessage());
        ((SentMessageViewHolder) holder).mainLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                listener.showMsgInfo(message);
                return false;
            }
        });
    }

    private void handleReceivedMessage(ViewHolder holder, final ChatMessageModel message) {
        ReceivedMessageViewHolder receivedViewHolder = (ReceivedMessageViewHolder) holder;
        ((ReceivedMessageViewHolder) holder).mainReceivedMsg.setText(message.getMessage());
        // Fetch and set sender's name
        fetchUserName(message.getSender_id(), receivedViewHolder.text_view_sender_name);
        ((ReceivedMessageViewHolder) holder).mainLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                listener.showMsgInfo(message);
                return false;
            }
        });
    }


    private void handleSentImageMessage(RecyclerView.ViewHolder holder, ChatMessageModel message) {
        SentImageMessageViewHolder viewHolder = (SentImageMessageViewHolder) holder;
        // Load and display the sent image using Glide or any other image loading library
        Glide.with(holder.itemView.getContext())
                .load(message.getImageUrl()) // Replace message.getImageUrl() with the actual URL of the sent image
                .placeholder(R.drawable.default_image) // Placeholder image until the actual image loads
                .into(viewHolder.imageView);
        viewHolder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), ImageDetailActivity.class);
                intent.putExtra("imageUrl", message.getImageUrl()); // Pass the URL of the video to the VideoPlayerActivity
                v.getContext().startActivity(intent);
            }
        });
    }

    private void handleReceivedImageMessage(RecyclerView.ViewHolder holder, ChatMessageModel message) {
        ReceivedImageMessageViewHolder viewHolder = (ReceivedImageMessageViewHolder) holder;
        // Load and display the received image using Glide or any other image loading library
        fetchUserName(message.getSender_id(), viewHolder.text_view_sender_name);
        Glide.with(holder.itemView.getContext())
                .load(message.getImageUrl()) // Replace message.getImageUrl() with the actual URL of the received image
                .placeholder(R.drawable.default_image) // Placeholder image until the actual image loads
                .into(viewHolder.imageView);
        viewHolder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), ImageDetailActivity.class);
                intent.putExtra("imageUrl", message.getImageUrl()); // Pass the URL of the video to the VideoPlayerActivity
                v.getContext().startActivity(intent);
            }
        });
    }

    private void handleSentVideoMessage(RecyclerView.ViewHolder holder, ChatMessageModel message) {
        SentVideoMessageViewHolder viewHolder = (SentVideoMessageViewHolder) holder;
        // Load and display the video thumbnail using Glide or any other image loading library
        Glide.with(holder.itemView.getContext())
                .load(message.getVideoThumbnailUrl()) // Replace message.getVideoThumbnailUrl() with the actual URL of the video thumbnail
                .placeholder(R.drawable.baseline_play_circle_outline_24) // Placeholder thumbnail until the actual thumbnail loads
                .into(viewHolder.sentVideoThumbnail);

        // Set click listener to play the video
        viewHolder.sentVideoThumbnail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Implement logic to play the video
                // For example, you can start a new activity with a VideoView to play the video
                // Or you can use a video player library to handle video playback
                Intent intent = new Intent(v.getContext(), VideoPlayerActivity.class);
                intent.putExtra("videoUrl", message.getVideoThumbnailUrl()); // Pass the URL of the video to the VideoPlayerActivity
                v.getContext().startActivity(intent);
            }
        });
    }

    private void handleReceivedVideoMessage(RecyclerView.ViewHolder holder, ChatMessageModel message) {
        ReceivedVideoMessageViewHolder viewHolder = (ReceivedVideoMessageViewHolder) holder;
        fetchUserName(message.getSender_id(), viewHolder.text_view_sender_name);
        // Load and display the video thumbnail using Glide or any other image loading library
        Glide.with(holder.itemView.getContext())
                .load(message.getVideoThumbnailUrl()) // Replace message.getVideoThumbnailUrl() with the actual URL of the video thumbnail
                .placeholder(R.drawable.baseline_play_circle_outline_24) // Placeholder thumbnail until the actual thumbnail loads
                .into(viewHolder.receivedVideoThumbnail);

        // Set click listener to play the video
        viewHolder.receivedVideoThumbnail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Implement logic to play the video
                // For example, you can start a new activity with a VideoView to play the video
                Intent intent = new Intent(v.getContext(), VideoPlayerActivity.class);
                intent.putExtra("videoUrl", message.getVideoThumbnailUrl()); // Pass the URL of the video to the VideoPlayerActivity
                v.getContext().startActivity(intent);
            }
        });
    }


    @Override
    public int getItemCount() {
        return mMessages.size();
    }

    public List<ChatMessageModel> getDataSet() {
        return this.mMessages;
    }

    public void setDataSet(List<ChatMessageModel> msgs) {
        this.mMessages = msgs;
        notifyDataSetChanged();
    }


    public static class SentMessageViewHolder extends ViewHolder {

        private TextView mainSentMsg;
        private RelativeLayout mainLayout;

        public SentMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            mainSentMsg = itemView.findViewById(R.id.sent_msg_text_body_textview);
            mainLayout = itemView.findViewById(R.id.chatSentMsgMainLayout);
        }
    }

    public static class ReceivedMessageViewHolder extends ViewHolder {

        private TextView mainReceivedMsg;
        private TextView text_view_sender_name;
        private RelativeLayout mainLayout;

        public ReceivedMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            mainReceivedMsg = itemView.findViewById(R.id.received_msg_text_body_textview);
            text_view_sender_name = itemView.findViewById(R.id.text_view_sender_name);
            mainLayout = itemView.findViewById(R.id.chatReceivedMsgLayout);
        }
    }


    private static class SentImageMessageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        SentImageMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.sent_image_view);
        }
    }

    private static class ReceivedImageMessageViewHolder extends RecyclerView.ViewHolder {

        private TextView text_view_sender_name;
        ImageView imageView;

        ReceivedImageMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.received_image_view);
            text_view_sender_name = itemView.findViewById(R.id.text_view_sender_name);

        }
    }

    private static class SentVideoMessageViewHolder extends RecyclerView.ViewHolder {
        ImageView sentVideoThumbnail;

        SentVideoMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            sentVideoThumbnail = itemView.findViewById(R.id.sent_video_thumbnail);
            // Add any additional initialization or click listeners here
        }
    }

    private static class ReceivedVideoMessageViewHolder extends RecyclerView.ViewHolder {
        private TextView text_view_sender_name;
        ImageView receivedVideoThumbnail;

        ReceivedVideoMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            text_view_sender_name = itemView.findViewById(R.id.text_view_sender_name);
            receivedVideoThumbnail = itemView.findViewById(R.id.received_video_thumbnail);
            // Add any additional initialization or click listeners here
        }
    }


    public static class WelcomeMessageViewHolder extends ViewHolder {

        private TextView mainWelcomeMsg;

        public WelcomeMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            mainWelcomeMsg = itemView.findViewById(R.id.welcome_msg_text_body_textview);
        }
    }

    private void fetchUserName(String userId, final TextView textViewSenderName) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference().child("User_Information").child(userId).child("name");
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String userName = dataSnapshot.getValue(String.class);
                    textViewSenderName.setText(userName);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error
            }
        });
    }
}
