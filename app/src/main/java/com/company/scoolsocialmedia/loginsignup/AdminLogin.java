package com.company.scoolsocialmedia.loginsignup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.company.scoolsocialmedia.R;
import com.company.scoolsocialmedia.admin.AdminDashboardActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AdminLogin extends AppCompatActivity {

    private EditText ID;

    private EditText password;

    private CardView loginBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_login);

        ID=findViewById(R.id.ID);
        password=findViewById(R.id.password);
        loginBtn=findViewById(R.id.loginBtn);

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ID_ADMIN=ID.getText().toString();
                String Password=password.getText().toString();
                if(ID_ADMIN.equals("12345678") && Password.equals("Admin")){
                    signInAnonymously();
                }else {
                    Toast.makeText(AdminLogin.this, "ID or Password is wrong", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    private void signInAnonymously() {
        FirebaseAuth.getInstance().signInAnonymously()
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                            if (user != null) {
                                startActivity(new Intent(AdminLogin.this, AdminDashboardActivity.class));
                                finish();
                            }
                        } else {
                            // Handle unsuccessful sign-in
                            Toast.makeText(AdminLogin.this, "Authentication failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}