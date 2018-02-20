package com.capstone.icoffie.jumiademo;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.capstone.icoffie.jumiademo.model.SharedPrefManager;
import com.capstone.icoffie.jumiademo.model.SingletonApi;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity{

    LocationManager locationManager;
    private static final int REQUEST_LOCATION = 10;
    String latitude, longitude;
    private TextInputLayout emailwrapper;
    private TextInputLayout passwordwrapper;
    private Button loginBtn;
    private TextView signupBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        //if user has logged in, redirect to dashboard UI
        if(SharedPrefManager.getClassinstance(this).isLoggedIn()){
            finish();
            startActivity(new Intent(LoginActivity.this, UserDashBoardActivity.class));
            return;
        }

        //settings for getting user location
        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();

        }  else if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            //getLocation();
        }

        // get view by id
        emailwrapper = (TextInputLayout) findViewById(R.id.emailwrapper);
        passwordwrapper = (TextInputLayout) findViewById(R.id.passwordwrapper);
        loginBtn = (Button) findViewById(R.id.loginBtn);
        signupBtn = (TextView) findViewById(R.id.signupBtn);

        // open intent to sign up page
        signupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //startActivity(new Intent(LoginActivity.this, RegistrationActivity.class));
            }
        });

        emailwrapper.setHint("your email address");
        passwordwrapper.setHint("your password");

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getLocation();
                emailwrapper.setError(null);
                passwordwrapper.setError(null);

                String useremail = ""; String userpassword = "";
                if(emailwrapper.getEditText() != null) {
                    useremail = emailwrapper.getEditText().getText().toString();
                }

                if(passwordwrapper.getEditText() != null) {
                    userpassword = passwordwrapper.getEditText().getText().toString();
                }

                if(useremail.isEmpty() || !useremail.contains("@")){
                    emailwrapper.setError("Not a valid Email");
                } else if (userpassword.isEmpty() || (userpassword.length() < 6)){
                    passwordwrapper.setError("Password must be at least 6 chars");
                } else {
                    emailwrapper.setErrorEnabled(false);
                    passwordwrapper.setErrorEnabled(false);
                    callLoginAPI(useremail, userpassword, latitude, longitude);
                }
            }
        });

    }

    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(LoginActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission
                (LoginActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(LoginActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);

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

                Toast.makeText(this,"Unble to Get your location",Toast.LENGTH_SHORT).show();

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

    public void makeToast(String message){
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    public void callLoginAPI(final String email, final String password, final String latitude, final String longitude) {

        makeToast("Lat: " + latitude + " Lng: " + longitude);
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loging in.....");
        progressDialog.show();

        // RequestQueue requestQueue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, com.capstone.icoffie.jumiademo.model.API_ENDPOINT.LOGIN_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        progressDialog.dismiss();
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            if (!jsonObject.getBoolean("error")) {
                                Toast.makeText(getApplicationContext(), jsonObject.getString("message"), Toast.LENGTH_LONG).show();
                                JSONObject userObject = jsonObject.getJSONObject("user");

                                if(SharedPrefManager.getClassinstance(getApplicationContext()).saveUserDetails(
                                        userObject.getInt("User_Id"), userObject.getString("Name"),
                                        userObject.getString("Email"), userObject.getString("Token")
                                )){
                                    startActivity(new Intent(LoginActivity.this, UserDashBoardActivity.class));
                                    finish();
                                }
                            } else if (jsonObject.getBoolean("error")) {
                                Toast.makeText(getApplicationContext(), jsonObject.getString("message"), Toast.LENGTH_LONG).show();
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressDialog.dismiss();
                        error.printStackTrace();
                        Toast.makeText(getApplicationContext(),
                                "Error occured! Check internet connection", Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                String device_id = getDeviceIMEI();
                Map<String, String> params = new HashMap<>();
                params.put("email", email);
                params.put("password", password);
                params.put("lat", latitude);
                params.put("lng", longitude);
                params.put("device_ime", device_id);
                params.put("device_name", Build.MODEL);
                params.put("type", "login");

                return params;
            }
        };
        //requestQueue.add(stringRequest);
        SingletonApi.getClassinstance(getApplicationContext()).addToRequest(stringRequest);
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

    /**
     * Returns the unique identifier for the device
     *
     * @return unique identifier for the device
     */
    public String getDeviceIMEI() {
        String deviceUniqueIdentifier = null;
        TelephonyManager tm = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        if (null != tm) {
            deviceUniqueIdentifier = tm.getDeviceId();
        }
        if (null == deviceUniqueIdentifier || 0 == deviceUniqueIdentifier.length()) {
            deviceUniqueIdentifier = android.provider.Settings.System.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
        }
        return deviceUniqueIdentifier;
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
        super.onRestart();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
