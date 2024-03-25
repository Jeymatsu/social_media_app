package com.company.scoolsocialmedia.admin;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;

import com.company.scoolsocialmedia.R;
import com.company.scoolsocialmedia.loginsignup.loginActivity;

public class AdminDashboardActivity extends AppCompatActivity {

    private RelativeLayout cardManageUsers;
    private RelativeLayout rlManagePosts;

    private RelativeLayout cardManageChatRooms;

    private RelativeLayout Logout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        cardManageUsers=findViewById(R.id.cardManageUsers);
        rlManagePosts=findViewById(R.id.rlManagePosts);
        cardManageChatRooms=findViewById(R.id.cardManageChatRooms);
        Logout=findViewById(R.id.Logout);



        cardManageUsers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AdminDashboardActivity.this,ManageUsersActivity.class));
            }
        });

        rlManagePosts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AdminDashboardActivity.this,ManagePostsActivity.class));
            }
        });

        cardManageChatRooms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AdminDashboardActivity.this,ManageChatRooms.class));
            }
        });

        Logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AdminDashboardActivity.this, loginActivity.class));
            }
        });


    }
}