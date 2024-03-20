package com.company.scoolsocialmedia.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.company.scoolsocialmedia.R;
import com.company.scoolsocialmedia.model.BasicUser;

import java.util.ArrayList;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {
    private List<BasicUser> userList;
    private List<BasicUser> selectedUsers;

    public UserAdapter(List<BasicUser> userList) {
        this.userList = userList;
        this.selectedUsers = new ArrayList<>();
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
        holder.bind(user);
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

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            userNameTextView = itemView.findViewById(R.id.user_name_textview);
            userCheckBox = itemView.findViewById(R.id.user_checkbox);

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
        }

        public void bind(BasicUser user) {
            userNameTextView.setText(user.getName());
            userCheckBox.setChecked(selectedUsers.contains(user));
        }
    }
}
