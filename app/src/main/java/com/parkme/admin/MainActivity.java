package com.parkme.admin;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    private Button mAllot;
    private Button mRequests;
    private Button mSignout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //init
        mAllot=(Button)findViewById(R.id.btn_allot_park);
        mSignout=(Button)findViewById(R.id.btn_signout);
        mRequests=(Button)findViewById(R.id.btn_requests);

        //click listeners
        mAllot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(getApplicationContext(),AllotActivity.class);
                startActivity(intent);
            }
        });
        mRequests.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(getApplicationContext(),RequestListActivity.class);
                startActivity(intent);
            }
        });
        //signout
        mSignout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAuth.getInstance().signOut();
                sendToStart();
            }
        });

        mAuth=FirebaseAuth.getInstance();
        FirebaseUser currentUSer=FirebaseAuth.getInstance().getCurrentUser();
        if(currentUSer==null){
            sendToStart();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();


    }

    private void sendToStart() {
        Intent intent=new Intent(getApplicationContext(),StartActivity.class);
        startActivity(intent);
        finish();
    }
}
