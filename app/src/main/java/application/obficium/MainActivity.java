package application.obficium;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends Activity {
    LocationManager locationManager;
    LocationListener locationListener;
    TextView latitudeView;
    TextView longitudeView;
    TextView getRequestView;
    Uri mapURI;
    Intent mapIntent;
    Button viewMapButton;
    Button updateLocBtn;
    Button startPostRequestBtn;
    Button stopPostRequestBtn;
    CheckBox getRequestCheckBox;
    double dbLat;
    double dbLong;
    Location location;
    RequestQueue mRequestQueue;
    String urlLocation;
    String sLatitude;
    String sLongitude;
    HashMap<String, String> params;
    Timer postTimer;
    Timer getTimer;
    Toast startToast;
    Toast stopToast;
    Toast getToast;
    int duration = Toast.LENGTH_SHORT;
    int executionInterval = 15000;
    CharSequence startText;
    CharSequence stopText;
    CharSequence getText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startText = "Sending Requests to server every " + executionInterval/1000 +
                " seconds";
        stopText = "Stopped sending requests";
        getText = "Receiving requests every "+ executionInterval/1000+" seconds";
        startToast = Toast.makeText(this,startText,duration);
        stopToast = Toast.makeText(this,stopText,duration);
        getToast = Toast.makeText(this,getText,duration);
        latitudeView = (TextView) findViewById(R.id.latitudeView);
        longitudeView = (TextView) findViewById(R.id.longitudeView);
        viewMapButton = (Button) findViewById(R.id.button_view_map);
        updateLocBtn = (Button) findViewById(R.id.update_location_btn);
        startPostRequestBtn = (Button) findViewById(R.id.button_start);
        stopPostRequestBtn = (Button) findViewById(R.id.button_stop);
        getRequestView = (TextView) findViewById(R.id.get_req_view);
        getRequestCheckBox = (CheckBox) findViewById(R.id.get_req_check_box);
        mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        params = new HashMap<String, String>();
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        urlLocation = "http://10.0.0.11:3000/location";

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        dbLat = location.getLatitude();
        dbLong = location.getLongitude();
        sLatitude = String.valueOf(dbLat);
        sLongitude = String.valueOf(dbLong);
        latitudeView.setText(sLatitude);
        longitudeView.setText(sLongitude);
        updateLocBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateLocation();
            }
        });
        mapURI = Uri.parse("geo:" + sLatitude + "," + sLongitude);
        mapIntent = new Intent(Intent.ACTION_VIEW, mapURI);
        mapIntent.setPackage("com.google.android.apps.maps");
        viewMapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mapIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(mapIntent);
                }
            }
        });
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                latitudeView.setText(sLatitude);
                longitudeView.setText(sLongitude);
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {
            }

            @Override
            public void onProviderEnabled(String s) {
            }

            @Override
            public void onProviderDisabled(String s) {
            }
        };
        return;
    }

    @Override
    public void onResume() {
        getRequestCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getTimer = new Timer();
                if (getRequestCheckBox.isChecked()) {
                    getToast.show();
                    getRequests();
                } else {
                    getTimer.cancel();
                }
            }
        });


        startPostRequestBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                postTimer = new Timer();
                sendRequests();
                startToast.show();
            }
        });
        stopPostRequestBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                postTimer.cancel();
                stopToast.show();
            }
        });

        super.onResume();
    }


    public void updateLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }


        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0,
                locationListener);
        location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        dbLat = location.getLatitude();
        dbLong = location.getLongitude();
        latitudeView.setText(String.valueOf(dbLat));
        longitudeView.setText(String.valueOf(dbLong));
    }

    public void postLocationRequests() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        dbLat = location.getLatitude();
        dbLong = location.getLongitude();
        sLatitude = String.valueOf(dbLat);
        sLongitude = String.valueOf(dbLong);
        params.put("latitude", sLatitude);
        params.put("longitude", sLongitude);
        JsonObjectRequest postRequest = new JsonObjectRequest(Request.Method.POST, urlLocation, new
                JSONObject(params),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Log.v("Response:%n %s", response.toString(4));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Error: ", error.getMessage());
            }
        });
        mRequestQueue.add(postRequest);
    }

    public void sendRequests() {
        final Handler handler = new Handler();
        TimerTask doAsynchronousTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @SuppressWarnings("unchecked")
                    public void run() {
                        try {
                            postLocationRequests();
                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                        }
                    }
                });
            }
        };
        postTimer.schedule(doAsynchronousTask, 0, executionInterval);
    }

    public void getLocationRequests() {
        JsonArrayRequest getRequest = new JsonArrayRequest(Request.Method.GET, urlLocation, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        String latitude = null;
                        String longitude = null;
                        try {
                            JSONObject jresponse = response.getJSONObject(0);
                            latitude = jresponse.getString("latitude");
                            longitude = jresponse.getString("longitude");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        getRequestView.setText(latitude + ", "+longitude);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                });

        /*
        final StringRequest getRequest = new StringRequest(Request.Method.GET, urlLocation,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        getRequestView.setText(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Get Request Response: ", error.toString());
                    }
                });
                */
        mRequestQueue.add(getRequest);
    }

    public void getRequests() {
        final Handler handler = new Handler();
        TimerTask doAsynchronousTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @SuppressWarnings("unchecked")
                    public void run() {
                        try {
                            getLocationRequests();
                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                        }
                    }
                });
            }
        };
        getTimer.schedule(doAsynchronousTask, 0, executionInterval);
    }

}
