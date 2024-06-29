package com.example.weatherforecastapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.weatherforecastapp.databinding.ActivityMainBinding;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int PERMISSION_CODE = 1;

    private ActivityMainBinding binding;
    private ArrayList<WeatherModel> weatherModelArrayList;
    private WeatherAdapter weatherAdapter;
    private LocationManager locationManager;
    private String cityName;

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        setContentView(binding.getRoot());

        weatherModelArrayList = new ArrayList<>();
        weatherAdapter = new WeatherAdapter(this, weatherModelArrayList);
        binding.idRVWeather.setAdapter(weatherAdapter);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (checkLocationPermission()) {
            getLocationAndUpdateWeather();
        } else {
            requestLocationPermission();
        }

        binding.idIVSearch.setOnClickListener(v -> {
            String city = binding.idEdtCity.getText().toString().trim();
            if (city.isEmpty()) {
                binding.idEdtCity.setError("Please enter city name");
                binding.idEdtCity.requestFocus();
            } else {
                cityName = city;
                getWeatherInfo(cityName);
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    private void getLocationAndUpdateWeather() {
        if (checkLocationPermission()) {
            @SuppressLint("MissingPermission") Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if (location != null) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                cityName = getCityName(latitude, longitude);
                getWeatherInfo(cityName);
            } else {
                Toast.makeText(this, "Unable to retrieve location", Toast.LENGTH_SHORT).show();
            }
        } else {
            requestLocationPermission();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    private boolean checkLocationPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) ==
                PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.  ACCESS_BACKGROUND_LOCATION},
                PERMISSION_CODE);
    }

    private String getCityName(double latitude, double longitude) {
        String cityName = "Not Found";
        Geocoder gcd = new Geocoder(getBaseContext(), Locale.getDefault());
        try {
            List<Address> addressList = gcd.getFromLocation(latitude, longitude, 1);
            if (!addressList.isEmpty()) {
                Address address = addressList.get(0);
                cityName = address.getLocality();
            } else {
                Toast.makeText(this, "User city not found", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return cityName;
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)

    public void getWeatherInfo(String cityName) {
        String apiKey = "8b1aab5e6058419db03112531240703";
        String url = null;
        try {
            url = "https://api.weatherapi.com/v1/forecast.json?key=" + apiKey + "&q=" + URLEncoder.encode(cityName, "UTF-8") + "&days=1&aqi=no&alerts=no";

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error encoding URL", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.idTVCityName.setText(cityName);
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        @SuppressLint({"SetTextI18n", "NotifyDataSetChanged"}) JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, response -> {
            Log.e(TAG, "JSON Response: " + response.toString()); // Log the JSON response for debugging purposes

            binding.idPBLoading.setVisibility(View.GONE);
          //  binding.idRLHome.setVisibility(View.VISIBLE);
            weatherModelArrayList.clear();
            try {

                JSONObject currentObj = response.getJSONObject("current");
                String temperature = currentObj.getString("temp_c");
                binding.idTVTemperature.setText(temperature + "°C");
                int isDay = currentObj.getInt("is_day");
                JSONObject conditionObj = currentObj.getJSONObject("condition");
                String condition = conditionObj.getString("text");
                String conditionIcon = conditionObj.getString("icon");
                Picasso.get().load("https://" + conditionIcon).into(binding.idIVIcon);
                binding.idTVCondition.setText(condition);
                if (isDay == 1) {
                    //morning
                    Picasso.get().load("https://unsplash.com/photos/blue-sky-and-white-clouds-lbmrrNgq2lo").into(binding.idIVBack);
                } else {
                    Picasso.get().load("https://unsplash.com/photos/galaxy-wallpaper-svbDI1Pq30s").into(binding.idIVBack);
                }
                JSONObject forecastObj = response.getJSONObject("forecast");
                JSONArray forecastDayArray = forecastObj.getJSONArray("forecastday");
                if (forecastDayArray.length() > 0) {
                    JSONObject forecastDayObj = forecastDayArray.getJSONObject(0);
                    JSONArray hourArray = forecastDayObj.getJSONArray("hour");
                    for (int i = 0; i < hourArray.length(); i++) {
                        JSONObject hourObj = hourArray.getJSONObject(i);
                        String time = hourObj.getString("time");
                        String temper = hourObj.getString("temp_c");
                        String img = hourObj.getJSONObject("condition").getString("icon");
                        String wind = hourObj.getString("wind_kph");
                        weatherModelArrayList.add(new WeatherModel(time, temper, img, wind));
                    }
                    binding.idRLHome.setVisibility(View.VISIBLE);
                    weatherAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(MainActivity.this, "No forecast available", Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(MainActivity.this, "Error parsing weather data", Toast.LENGTH_SHORT).show();
            }
        }, error -> {
            Toast.makeText(MainActivity.this, "Failed to fetch weather data", Toast.LENGTH_SHORT).show();
            binding.idPBLoading.setVisibility(View.GONE);
        });
        requestQueue.add(jsonObjectRequest);
        //binding.idRLHome.setVisibility(View.VISIBLE);
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
                getLocationAndUpdateWeather();
            } else {
                Toast.makeText(this, "Please provide the permissions", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
}


