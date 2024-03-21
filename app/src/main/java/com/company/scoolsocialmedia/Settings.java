package com.company.scoolsocialmedia;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.company.scoolsocialmedia.loginsignup.loginActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Settings extends AppCompatActivity {
    private LinearLayout disable;
    private MaterialTextView userLblName, userLblPhone, userLblEmail, userLblLogout,userLblChangepass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Settings");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setElevation(5);
        }

        // Initialize Firebase
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference().child("User_Information");


        // Initialize TextViews
        userLblName = findViewById(R.id.userLblName);
        userLblEmail = findViewById(R.id.userLblEmail);
        userLblLogout =findViewById(R.id.userLblLogout);
        userLblChangepass =findViewById(R.id.userLblChangepass);

        // Retrieve user data and display in TextViews
        if (currentUser != null) {
            String userId = currentUser.getUid();
            usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        String email = dataSnapshot.child("email").getValue(String.class);
                        String username = dataSnapshot.child("name").getValue(String.class);

                        userLblEmail.setText(email);
                        userLblName.setText(username);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    //handle error
                }

            });
        }

        userLblChangepass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                perfromForgotPassAction();
            }
        });
        //click listener for userLblLogout
        userLblLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                userLblLogout.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.bottomNavigationBackground));
                // Create a confirmation dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(Settings.this);
                builder.setTitle(Html.fromHtml("<b>Logout Confirmation</b>"));
                builder.setMessage("Are you sure you want to logout?");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Yes"
                        // Sign out the user
                        FirebaseAuth.getInstance().signOut();
                        Toast.makeText(Settings.this, "Log Out Successful!", Toast.LENGTH_SHORT).show();
                        // Redirect to your login or home activity
                        Intent intent = new Intent(getApplicationContext(), loginActivity.class);
                        startActivity(intent);
                        finish();
                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "No"
                        // Dismiss the dialog
                        dialogInterface.dismiss();
                        userLblLogout.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.darkGray));
                    }
                });
                // Set custom icon
                builder.setIcon(R.drawable.ic_logout);

                // Show the dialog
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });





    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void perfromForgotPassAction() {
        final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.forgot_password_layout, null, false);
        builder.setView(view);
        builder.setCancelable(true);
        final android.app.AlertDialog dialog = builder.show();
        final EditText recoverEmail = view.findViewById(R.id.recoverEmail);
        final TextView title = view.findViewById(R.id.title);
        title.setText("Change Password");
        TextView submit = view.findViewById(R.id.submitEmail);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String m_Text = recoverEmail.getText().toString().trim();
                if (!m_Text.equals("")) {
                    FirebaseAuth.getInstance().sendPasswordResetEmail(m_Text)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(Settings.this, "Password Recovery Email Sent", Toast.LENGTH_SHORT).show();
                                        dialog.cancel();
                                    } else {
                                        Toast.makeText(Settings.this, "Error Sending Password Recovery Email", Toast.LENGTH_SHORT).show();
                                        dialog.cancel();
                                    }
                                }
                            });
                } else {
                    Toast.makeText(Settings.this, "Please Enter an email", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

}
