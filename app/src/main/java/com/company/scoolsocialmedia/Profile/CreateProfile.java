package com.company.scoolsocialmedia.Profile;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.company.scoolsocialmedia.MainActivity;
import com.company.scoolsocialmedia.R;
import com.company.scoolsocialmedia.model.BasicUser;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.wang.avi.AVLoadingIndicatorView;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;

public class CreateProfile extends AppCompatActivity {

    private EditText fname_v, lname_v, city_v;
    private Spinner countrySpinner, communitySpinner;
    private CardView signUp;
    private AVLoadingIndicatorView progressBar;
    private RadioGroup rgroup;
    private FirebaseAuth mAuth;
    private String academicNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_profile);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        mAuth = FirebaseAuth.getInstance();

        Intent intent=new Intent();
        academicNumber=intent.getStringExtra("ACADEMIC_NUMBER");

        Toast.makeText(this, academicNumber, Toast.LENGTH_SHORT).show();

        fname_v = findViewById(R.id.signUpFNametextInputEditText);
        lname_v = findViewById(R.id.signUpLNametextInputEditText);
        city_v = findViewById(R.id.signUpCitySignup);
        progressBar = findViewById(R.id.signup_prog);
        countrySpinner = findViewById(R.id.country_spinner);
        communitySpinner = findViewById(R.id.community_spinner);
        rgroup = findViewById(R.id.signup_rg);
        signUp = findViewById(R.id.signUpButton);
        populateCommunity();
        populateCountry();

        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createProfile();
            }
        });
    }

    private void createProfile() {

        final String fname = WordUtils.capitalize(fname_v.getText().toString().trim());

        final String lname = WordUtils.capitalize(lname_v.getText().toString().trim());


        final String city = StringUtils.capitalize(city_v.getText().toString().trim());

        final String country = WordUtils.capitalize(countrySpinner.getSelectedItem().toString());
        final String community = WordUtils.capitalize(communitySpinner.getSelectedItem().toString());

        final String gender = getGender();


        if (fname.length() == 0 || lname.length() == 0 || city.length() == 0 || gender == null) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
        } else {
            try {
                FirebaseUser user = mAuth.getCurrentUser();
                BasicUser newUser = new BasicUser(fname + " " + lname, gender, country, city, community, user.getEmail(), "none",user.getUid(),academicNumber);
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference myRef = database.getReference("User_Information").child(user.getUid());
                myRef.setValue(newUser, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                        if (databaseError != null) {signUp.setVisibility(View.VISIBLE);
                            progressBar.hide();
                            Toast.makeText(CreateProfile.this, "Some Error occurred while creating profile", Toast.LENGTH_SHORT).show();
                        } else {

                            progressBar.hide();
                            Toast.makeText(CreateProfile.this, "Profile Created Successfully", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(CreateProfile.this, MainActivity.class));
                            finish();
                        }
                    }
                });
            } catch (Exception e) {
                signUp.setVisibility(View.VISIBLE);
                progressBar.hide();
                Toast.makeText(CreateProfile.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }

        }

    }

    private void populateCommunity() {
        String[] spinnerArray = getResources().getStringArray(R.array.community_list);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item, spinnerArray);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        communitySpinner.setAdapter(adapter);
    }

    private void populateCountry() {
        String[] spinnerArray = getResources().getStringArray(R.array.country_list);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item, spinnerArray);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        countrySpinner.setAdapter(adapter);
    }

    private String getGender() {
        try {
            int selectedId = rgroup.getCheckedRadioButtonId();
            RadioButton radioButton = findViewById(selectedId);
            return radioButton.getText().toString();
        } catch (Exception e) {
            return null;
        }
    }

}