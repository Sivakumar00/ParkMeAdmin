package com.parkme.admin;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.provider.ContactsContract;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.parkme.admin.Model.Common;
import com.parkme.admin.Model.Request;
import com.roger.catloadinglibrary.CatLoadingView;

import org.w3c.dom.Text;

public class RequestListActivity extends AppCompatActivity {
    DatabaseReference mRequestAdminDatabase,mUserDatabase,mRequestClientDatabase;
    FirebaseAuth mAuth;
    RecyclerView mRecyclerView;
    Toolbar mToolbar;
    String address,status,row,column;

    ProgressDialog mProgress;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_list);
        mAuth=FirebaseAuth.getInstance();
        mRequestAdminDatabase= FirebaseDatabase.getInstance().getReference().child("parking_request_admin").child(mAuth.getCurrentUser().getUid());
        mRequestAdminDatabase.keepSynced(true);
        mRequestClientDatabase= FirebaseDatabase.getInstance().getReference().child("parking_request_client");
        mRequestClientDatabase.keepSynced(true);
        mUserDatabase=FirebaseDatabase.getInstance().getReference().child("users").child("client");
        mUserDatabase.keepSynced(true);
        mToolbar=(Toolbar)findViewById(R.id.request_list_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Requests");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mProgress=new ProgressDialog(this);
        mProgress.setTitle("Loading");
        mProgress.setMessage("Please wait..");
        mProgress.setCanceledOnTouchOutside(false);
        mProgress.show();


        //recyclerview
        mRecyclerView=(RecyclerView)findViewById(R.id.request_list);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseRecyclerAdapter<Request,RequestViewHolder> firebaseRecyclerAdapter=new FirebaseRecyclerAdapter<Request, RequestViewHolder>(
                Request.class,
                R.layout.single_request_layout,
                RequestViewHolder.class,
                mRequestAdminDatabase
        ) {
            @Override
            protected void populateViewHolder(final RequestViewHolder viewHolder, Request model, int position) {

                final String key=getRef(position).getKey();
                mUserDatabase.child(key).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String name=dataSnapshot.child("name").getValue().toString();
                        viewHolder.setName(name);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                mRequestAdminDatabase.child(key).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        String car_no=dataSnapshot.child("car_no").getValue().toString();
                        String date=dataSnapshot.child("date").getValue().toString();
                        String time=dataSnapshot.child("time").getValue().toString();
                        String status=dataSnapshot.child("status").getValue().toString();

                        viewHolder.setCarNumber(car_no);
                        viewHolder.setDate(date);
                        viewHolder.setTime(time);
                        viewHolder.setStatus(status);

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        DatabaseReference mParking=FirebaseDatabase.getInstance().getReference().child("parking_available").child(mAuth.getCurrentUser().getUid());
                        mParking.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                Common.row =dataSnapshot.child("row").getValue().toString();
                                Common.column =dataSnapshot.child("column").getValue().toString();
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                        AlertDialog.Builder builder = new AlertDialog.Builder(RequestListActivity.this);
                        builder.setTitle(R.string.app_name);
                        builder.setIcon(R.mipmap.ic_launcher);
                        builder.setMessage("Accept the booking or not?")
                                .setCancelable(false)
                                .setPositiveButton("Accept", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        acceptBooking(key);

                                        Intent intent=new Intent(getApplicationContext(),BookingActivity.class);
                                        intent.putExtra("user_id",key);
                                        startActivity(intent);
                                    }
                                })
                                .setNegativeButton("Reject", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        rejectBooking(key);
                                        dialog.cancel();
                                    }
                                });
                        AlertDialog alert = builder.create();
                        alert.show();
                    }
                });
            }
        };
        mRecyclerView.setAdapter(firebaseRecyclerAdapter);
        mProgress.dismiss();
    }

    private void rejectBooking(String key) {
        mRequestAdminDatabase.child(key).child("status").setValue("Rejected");
        mRequestClientDatabase.child(key).child(mAuth.getCurrentUser().getUid()).child("status").setValue("Rejected");

    }

    private void acceptBooking(String key) {


    }

    public static class RequestViewHolder extends RecyclerView.ViewHolder
    {
        View mView;
        public RequestViewHolder(View itemView) {
            super(itemView);
            mView=itemView;
        }
        public void setName(String text){
            TextView txtAddress=(TextView)mView.findViewById(R.id.request_name);
            txtAddress.setText(text);

        }
        public void setCarNumber(String text){
            TextView txtAddress=(TextView)mView.findViewById(R.id.request_car_no);
            txtAddress.setText(text);
        }
        public void setDate(String text){
            TextView txtAddress=(TextView)mView.findViewById(R.id.book_date);
            txtAddress.setText(text);
        }
        public void setTime(String text){
            TextView txtAddress=(TextView)mView.findViewById(R.id.book_time);
            txtAddress.setText(text);
        }
        public void setStatus(String text){
            TextView txtAddress=(TextView)mView.findViewById(R.id.request_status);
            if(text.equals("Requested")){
                txtAddress.setTextColor(mView.getResources().getColor(R.color.orange));
            }else if(text.equals("Accepted")){
                txtAddress.setTextColor(mView.getResources().getColor(R.color.green));
            }else if(text.equals("Rejected")){
                txtAddress.setTextColor(mView.getResources().getColor(R.color.red));
            }
            txtAddress.setText(text);
        }

    }
}
