package com.company.scoolsocialmedia.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;

import com.company.scoolsocialmedia.R;
import com.company.scoolsocialmedia.adapters.NotificationAdapter;
import com.company.scoolsocialmedia.model.NotificationModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class NotiificationActivity extends AppCompatActivity {

    List<NotificationModel> list;
    NotificationAdapter adapter;
    RecyclerView rv;
    Toolbar tb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notiification);

        rv=findViewById(R.id.selectedrecycler);
        list=new ArrayList<>();
        adapter=new NotificationAdapter(this, list);
        LinearLayoutManager lm=new LinearLayoutManager(this);
        lm.setStackFromEnd(true);
        lm.setReverseLayout(true);
        rv.setHasFixedSize(true);
        rv.setLayoutManager(lm);

        fillView();
    }

    private void fillView() {

        FirebaseDatabase db=FirebaseDatabase.getInstance();
        db.getReference("notifications").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(snapshot.exists()){

//                    animationView.cancelAnimation();
//                    animationView.setVisibility(View.GONE);
                    list.clear();
                    for(DataSnapshot sn: snapshot.getChildren()){
                        NotificationModel nm=sn.getValue(NotificationModel.class);
                        list.add(nm);
                    }
                    rv.setAdapter(adapter);

                }else{
//                    animationView.cancelAnimation();
//                    animationView.setVisibility(View.GONE);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

}