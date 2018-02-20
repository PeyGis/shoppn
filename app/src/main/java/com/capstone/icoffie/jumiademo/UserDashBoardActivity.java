package com.capstone.icoffie.jumiademo;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.capstone.icoffie.jumiademo.model.API_ENDPOINT;
import com.capstone.icoffie.jumiademo.model.SharedPrefManager;
import com.capstone.icoffie.jumiademo.model.SingletonApi;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class UserDashBoardActivity extends AppCompatActivity implements View.OnClickListener {

    LocationManager locationManager;
    private static final int REQUEST_LOCATION = 10;
    String latitude, longitude;
    Handler handler;
    Timer timer;
    LinearLayout linearLayout1, linearLayout2;
    TextView txtName;
    Button btnSearch;
    EditText editText;
    private static final int DELAY = 120 * 1000;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_dash_board);

        //set view components
        linearLayout1 = (LinearLayout) findViewById(R.id.linearLayout);
        linearLayout2 = (LinearLayout) findViewById(R.id.linearLayout2);
        txtName = (TextView) findViewById(R.id.userNameTv);
        editText = (EditText) findViewById(R.id.phone_search);
        btnSearch = (Button) findViewById(R.id.search_phones_btn);

        linearLayout1.setOnClickListener(this);
        linearLayout2.setOnClickListener(this);
        btnSearch.setOnClickListener(this);

        //apppend username to textview
        txtName.append(SharedPrefManager.getClassinstance(this).getUserName());

        //check user session
        checkSessionToken();

        //settings for getting user location
        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            buildAlertMessageNoGps();

        }  else {
            handler = new Handler();
            timer = new Timer();
            TimerTask doAsynchronusTask = new TimerTask() {
                @Override
                public void run() {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                getLocation();
                                updateUserLocation(latitude, longitude);
                                //logoutUser();
                            } catch (Exception e) {
                                makeToast("Error in timertask");
                            }
                        }
                    });
                }
            };
            timer.schedule(doAsynchronusTask, DELAY, DELAY);
        }

    }

    public void makeToast(String message){
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    public void updateUserLocation(final String latitude, final String longitude) {

        makeToast(" Updating Location with Lat: " + latitude + " Lng: " + longitude);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, com.capstone.icoffie.jumiademo.model.API_ENDPOINT.LOGIN_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            makeToast(jsonObject.getString("message"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        makeToast("Error occured! Check internet connection");
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("user_id", String.valueOf(SharedPrefManager.getClassinstance(getApplicationContext()).getUserId()));
                params.put("token", SharedPrefManager.getClassinstance(getApplicationContext()).getUserToken());
                params.put("lat", latitude);
                params.put("lng", longitude);
                params.put("type", "update");
                return params;
            }
        };
        SingletonApi.getClassinstance(getApplicationContext()).addToRequest(stringRequest);
    }

    /**
     * A function to get user current location
     */
    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(UserDashBoardActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission
                (UserDashBoardActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(UserDashBoardActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);

        } else {
            Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            Location location1 = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            Location location2 = locationManager.getLastKnownLocation(LocationManager. PASSIVE_PROVIDER);

            if (location != null) {
                double lat = location2.getLatitude();
                double lng = location2.getLongitude();
                latitude = String.valueOf(lat);
                longitude = String.valueOf(lng);

            } else  if (location1 != null) {
                double lat = location2.getLatitude();
                double lng = location2.getLongitude();
                latitude = String.valueOf(lat);
                longitude = String.valueOf(lng);;

            } else  if (location2 != null) {
                double lat = location2.getLatitude();
                double lng = location2.getLongitude();
                latitude = String.valueOf(lat);
                longitude = String.valueOf(lng);


            }else{
                Toast.makeText(this,"Unble to Get location",Toast.LENGTH_SHORT).show();
            }
        }
    }
    // show alert daiglog to enable location
    protected void buildAlertMessageNoGps() {

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Please Turn ON your GPS Location")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case REQUEST_LOCATION:
                getLocation();
                break;
            default:
                Log.d("LOCATION", "Location service");
                break;
        }
    }

    @Override
    public void onClick(View v) {
        int id =v.getId();

        if(id == R.id.linearLayout || id == R.id.linearLayout2){
            makeToast("Feature not implemented");
        } else if(id == R.id.search_phones_btn){
            makeToast("Searching for Phones");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.nav_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.nav_logout:
                logoutUser();
                return  true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void logoutUser() {

        makeToast("Logging out user....");

        StringRequest stringRequest = new StringRequest(Request.Method.POST, com.capstone.icoffie.jumiademo.model.API_ENDPOINT.LOGIN_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            makeToast(jsonObject.getString("message"));
                            if(!jsonObject.getBoolean("error")){
                                SharedPrefManager.getClassinstance(getApplicationContext()).logout();
                                startActivity(new Intent(UserDashBoardActivity.this, LoginActivity.class));
                                finish();
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        makeToast("Error occured! Check internet connection");
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("user_token", SharedPrefManager.getClassinstance(getApplicationContext()).getUserToken());
                params.put("type", "logout");
                return params;
            }
        };
        SingletonApi.getClassinstance(getApplicationContext()).addToRequest(stringRequest);
    }

    public void checkSessionToken() {

        //makeToast("Checking Session....");
        StringRequest stringRequest = new StringRequest(Request.Method.POST, API_ENDPOINT.APP_FUNCTION,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            makeToast(jsonObject.getString("message"));
                            if(jsonObject.getBoolean("error")){
                                SharedPrefManager.getClassinstance(getApplicationContext()).logout();
                                startActivity(new Intent(UserDashBoardActivity.this, LoginActivity.class));
                                finish();
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        makeToast("Error occured! Check internet connection");
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                //params.put("user_id", String.valueOf(SharedPrefManager.getClassinstance(getApplicationContext()).getUserId()));
                params.put("user_token", SharedPrefManager.getClassinstance(getApplicationContext()).getUserToken());
                params.put("type", "check_session");
                return params;
            }
        };
        //requestQueue.add(stringRequest);
        SingletonApi.getClassinstance(getApplicationContext()).addToRequest(stringRequest);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onRestart() {
        checkSessionToken();
        super.onRestart();
    }

    @Override
    protected void onDestroy() {
        timer.cancel();
        super.onDestroy();
    }
}
