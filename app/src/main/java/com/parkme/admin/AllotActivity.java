package com.parkme.admin;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AllotActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    FirebaseAuth mAuth;
    Spinner mFloor,mRow,mColumn;
    private TextView mLocation;
    private Button mAllotBtn;
    Place place;
    String lat,lng;
    String address;
    private GoogleApiClient mGoogleApiClient;
    DatabaseReference mDatabase,mGeoLocation;
    String floor,row,column;
    private final static int PLACE_PICKER_REQUEST = 1;
    public static final String TAG = "PlacePickerActivity";
    private static final int LOC_REQ_CODE = 1;
    private static final int PLACE_PICKER_REQ_CODE = 2;
    GeoFire geoFire;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_allot);

        //init
        mAuth = FirebaseAuth.getInstance();
        mFloor = (Spinner) findViewById(R.id.spinner_floor);
        mColumn = (Spinner) findViewById(R.id.spinner_column);
        mRow = (Spinner) findViewById(R.id.spinner_row);
        mLocation = (TextView) findViewById(R.id.txt_set_location);
        mAllotBtn = (Button) findViewById(R.id.btn_allot);
        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .build();
        mGeoLocation= FirebaseDatabase.getInstance().getReference().child("parking_location");
        mDatabase=FirebaseDatabase.getInstance().getReference().child("parking_available");

        geoFire=new GeoFire(mGeoLocation);
        mFloor.setOnItemSelectedListener(this);
        mRow.setOnItemSelectedListener(this);
        mColumn.setOnItemSelectedListener(this);
        //click listeners
        mAllotBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //code
              if(!TextUtils.isEmpty(floor)&&
                      !TextUtils.isEmpty(row)&&
                      !TextUtils.isEmpty(column)&&
                      !TextUtils.isEmpty(lat)&&
                      !TextUtils.isEmpty(lng)){
                  final double latitude=Double.parseDouble(lat);
                  final double longitude=Double.parseDouble(lng);
                  Map<String,String > map=new HashMap<>();
                  map.put("row",row);
                  map.put("column",column);
                  map.put("floor",floor);
                  map.put("address",address);

                  mDatabase.child(mAuth.getCurrentUser().getUid()).setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                      @Override
                      public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            geoFire.setLocation(mAuth.getCurrentUser().getUid(), new GeoLocation(latitude, longitude), new GeoFire.CompletionListener() {
                                @Override
                                public void onComplete(String key, DatabaseError error) {
                                    if (error != null) {
                                        System.err.println("There was an error saving the location to GeoFire: " + error);
                                    } else {
                                        Intent intent1=new Intent(getApplicationContext(),MainActivity.class);
                                        intent1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        startActivity(intent1);
                                        Toast.makeText(getApplicationContext(),"Registered successfully",Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }else{
                            Toast.makeText(AllotActivity.this, "Unsuccessfull", Toast.LENGTH_SHORT).show();
                        }
                      }
                  });

              }else {
                  Toast.makeText(AllotActivity.this, "Enter all fields..!", Toast.LENGTH_SHORT).show();
              }

            }
        });
        mLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                try {
                    startActivityForResult(builder.build(AllotActivity.this), PLACE_PICKER_REQUEST);
                } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }
            }
        });
        //array adapter for spinner
        List<String> floor =new ArrayList<>();
        floor.add("1");
        floor.add("2");
        floor.add("3");
        floor.add("4");
        floor.add("5");
        floor.add("6");

        List<String> row=new ArrayList<>();
        row.add("1");
        row.add("2");
        row.add("3");
        row.add("4");
        row.add("5");
        row.add("6");
        row.add("7");
        row.add("8");
        row.add("9");
        row.add("10");

        List<String> column=new ArrayList<>();
        column.add("1");
        column.add("2");
        column.add("3");
        column.add("4");
        column.add("5");
        column.add("6");
        column.add("7");
        column.add("8");
        column.add("9");
        column.add("10");
    //adapter for floor
        ArrayAdapter<String> floorAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, floor);
        floorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mFloor.setAdapter(floorAdapter);

        //adapter for row
        ArrayAdapter<String> rowAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, row);
        rowAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mRow.setAdapter(rowAdapter);

        //adapter for column
        ArrayAdapter<String> columnAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, column);
        columnAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mColumn.setAdapter(columnAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(data, this);
                StringBuilder stBuilder = new StringBuilder();
                String placename = String.format("%s", place.getName());
                lat = String.valueOf(place.getLatLng().latitude);
                lng = String.valueOf(place.getLatLng().longitude);
                 address = String.format("%s", place.getAddress());
                mLocation.setText(address);

            }
        }
    }
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        // On selecting a spinner item
        Spinner spinner=(Spinner)parent;
      if(spinner.getId()==R.id.spinner_floor){
           floor=parent.getItemAtPosition(position).toString();
          Toast.makeText(this, ""+floor, Toast.LENGTH_SHORT).show();
      }
        if(spinner.getId()==R.id.spinner_row){
            row=parent.getItemAtPosition(position).toString();
        }
        if(spinner.getId()==R.id.spinner_column){
            column=parent.getItemAtPosition(position).toString();
        }
    }


    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}
