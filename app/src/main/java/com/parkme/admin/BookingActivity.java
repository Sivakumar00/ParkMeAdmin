package com.parkme.admin;

import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.parkme.admin.Model.Common;
import com.parkme.admin.Model.MyView;
import com.parkme.admin.Model.SeatExample;

import java.lang.reflect.Array;

import by.anatoldeveloper.hallscheme.hall.HallScheme;
import by.anatoldeveloper.hallscheme.hall.Seat;
import by.anatoldeveloper.hallscheme.hall.SeatListener;
import by.anatoldeveloper.hallscheme.view.ZoomableImageView;

public class BookingActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    DatabaseReference mDatabase;
    String user_id;
    int row,column;
    int seat_id;
    DatabaseReference mRequestClientDatabase,mRequestAdminDatabase;
    int resultRow,resultCol;
    Button mSubmit;
    int selectRow,selectColumn;
    HallScheme scheme;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking);
        //init
        final Intent intent=getIntent();
        user_id=intent.getStringExtra("user_id");
        row=Integer.parseInt(Common.row);
        column=Integer.parseInt(Common.column);
        mSubmit=(Button)findViewById(R.id.btnBook);
        mAuth=FirebaseAuth.getInstance();
        mDatabase=FirebaseDatabase.getInstance().getReference().child("booking_lot");
        mRequestAdminDatabase= FirebaseDatabase.getInstance().getReference().child("parking_request_admin").child(mAuth.getCurrentUser().getUid());
        mRequestAdminDatabase.keepSynced(true);
        mRequestClientDatabase= FirebaseDatabase.getInstance().getReference().child("parking_request_client");
        mRequestClientDatabase.keepSynced(true);

        //grid
        ZoomableImageView imageView = (ZoomableImageView) findViewById(R.id.zoomable_image);
        final Seat seats[][] = new Seat[row][column];
        scheme = new HallScheme(imageView, basicScheme(row,column), this);
        scheme.setChosenSeatBackgroundColor(Color.RED);
        imageView.setZoomByDoubleTap(false);
        //listener
        scheme.setSeatListener(new SeatListener() {

            @Override
            public void selectSeat(int id) {
                resultCol=id%10;
                resultRow=id/10;
            }

            @Override
            public void unSelectSeat(int id) {

            }

        });
        mDatabase.child(mAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int count=(int) dataSnapshot.getChildrenCount();
                for (DataSnapshot dataSnapshot1:dataSnapshot.getChildren()){
                    String key=dataSnapshot1.getRef().getKey();

                    Log.w("dfkjahdfkjhasd",key);
                    selectRow= Integer.parseInt(dataSnapshot.child(key).child("row").getValue().toString());
                    selectColumn= Integer.parseInt(dataSnapshot.child(key).child("column").getValue().toString());
                    Log.w("row",selectRow+"");
                    Log.w("col",selectColumn+"");
                    scheme.clickSchemeProgrammatically(selectRow,selectColumn);
                    scheme.setMaxSelectedSeats(count+1);

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                    mRequestAdminDatabase.child(user_id).child("status").setValue("Accepted");
                    mRequestAdminDatabase.child(user_id).child("token").setValue("P-"+resultRow+""+resultCol);
                    mRequestClientDatabase.child(user_id).child(mAuth.getCurrentUser().getUid()).child("status").setValue("Accepted");
                    mRequestClientDatabase.child(user_id).child(mAuth.getCurrentUser().getUid()).child("token").setValue("P-"+resultRow+""+resultCol);

                    mDatabase.child(mAuth.getCurrentUser().getUid()).child(user_id).child("row").setValue(resultRow);
                    mDatabase.child(mAuth.getCurrentUser().getUid()).child(user_id).child("column").setValue(resultCol).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Intent intent1=new Intent(getApplicationContext(),MainActivity.class);
                                intent1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent1);
                                Toast.makeText(BookingActivity.this, "Successfully alloted -> P-"+resultRow+""+resultCol, Toast.LENGTH_SHORT).show();
                            }else{
                                Toast.makeText(BookingActivity.this, "Something wrong..!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });


            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();


    }

    private Seat[][] basicScheme(int row, int column) {
        Seat[][] seats=(Seat[][]) Array.newInstance(Seat.class,new int[]{row,column});
        for (int i=0;i<row;i++){
            for(int j=0;j<column;j++){
                SeatExample seat=new SeatExample();
                seat.id=(i*10)+(j);
                seat.selectedSeatMarker=String.valueOf((i*10)+j);
                seat.status=HallScheme.SeatStatus.FREE;
                seats[i][j]=seat;
            }
        }
        return seats;
    }
}
