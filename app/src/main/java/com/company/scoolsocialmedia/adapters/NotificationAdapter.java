package com.company.scoolsocialmedia.adapters;

import static android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.company.scoolsocialmedia.R;
import com.company.scoolsocialmedia.model.NotificationModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {
        Context context;
        List<NotificationModel> userList;

        public NotificationAdapter(Context context, List<NotificationModel> userList) {
            this.context = context;
            this.userList = userList;
        }

        @NonNull
        @Override
        public NotificationAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v= LayoutInflater.from(context).inflate(R.layout.show_notification_list, parent, false );
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

            NotificationModel nm=userList.get(position);
            holder.notification.setText(nm.getMessage());
            holder.notification_title.setText(nm.getTitle());



        }

        @Override
        public int getItemCount() {
            return userList.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView notification;
            TextView notification_title;
            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                notification=itemView.findViewById(R.id.notification);
                notification_title=itemView.findViewById(R.id.notification_title);




            }
        }
}
