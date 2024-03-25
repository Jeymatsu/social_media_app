package com.company.scoolsocialmedia.loginsignup;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.company.scoolsocialmedia.R;
import com.company.scoolsocialmedia.model.BasicUser;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.wang.avi.AVLoadingIndicatorView;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;

import java.util.Objects;

public class signupActivity extends AppCompatActivity {
    private EditText password_v, email_v,c_password;
    private CardView signUp;
    private TextView goToLogin;
    private AVLoadingIndicatorView progressBar;
    private FirebaseAuth mAuth;
    private EditText AcademicNumbertextInputEditText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        mAuth = FirebaseAuth.getInstance();
        progressBar = findViewById(R.id.signup_prog);
        password_v = findViewById(R.id.signUpPasswordtextInputEditText);
        c_password=findViewById(R.id.ConfirmsignUpPasswordtextInputEditText);
        email_v = findViewById(R.id.signUpEmailtextInputEditText);
        AcademicNumbertextInputEditText=findViewById(R.id.AcademicNumbertextInputEditText);
        signUp = findViewById(R.id.signUpButton);
        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signup();
            }
        });
        goToLogin = findViewById(R.id.goBackToLogin);
        goToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }
    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = activity.getCurrentFocus();
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
    public void signup() {
        final String email = email_v.getText().toString().trim();
        final String academicNumber =AcademicNumbertextInputEditText.getText().toString().trim();
        String password = password_v.getText().toString().trim();
        String cPassword=c_password.getText().toString().trim();

        if ( email.length() == 0
        ) {
            Toast.makeText(this, "Please enter email", Toast.LENGTH_SHORT).show();
        } else if (password.length() == 0) {
            Toast.makeText(this, "Please enter a password", Toast.LENGTH_SHORT).show();
        } else  {
            signUp.setVisibility(View.INVISIBLE);
            hideKeyboard(signupActivity.this);
            progressBar.show();
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                FirebaseUser user = mAuth.getCurrentUser();
                                // Create a BasicUser object with user information
                                BasicUser basicUser = new BasicUser("", "", "", "", "", email, "", user.getUid(), academicNumber);
                                // Store user information in Firebase Database
                                DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("User_Information");
                                usersRef.child(user.getUid()).setValue(basicUser).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

                                        if (task.isSuccessful()) {
                                            // Send verification email
                                            user.sendEmailVerification()
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (task.isSuccessful()) {
                                                                showEmailSentDialog();
                                                            }
                                                        }
                                                    });
                                        } else {
                                            progressBar.hide();
                                            Toast.makeText(signupActivity.this, "Registration failed.",
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    }


                                });

                            } else {
                                signUp.setVisibility(View.VISIBLE);
                                progressBar.hide();
                                Toast.makeText(signupActivity.this, Objects.requireNonNull(task.getException()).getLocalizedMessage(),
                                        Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        }
    }



    public void showEmailSentDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.verification_email_sent_layout, null, false);
        builder.setView(view);
        builder.setCancelable(false);
        final AlertDialog dialog = builder.show();
        CardView dismissBtn = view.findViewById(R.id.cancelDialog);
        dismissBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(signupActivity.this, loginActivity.class));
                finish();
            }
        });
    }
}
