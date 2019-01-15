package com.uttama.android.samples;

import java.util.Date;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class MockLocation
extends Activity {
	private TextView latitude;
	private TextView longitude;
	private Button submitButton;
	private LocationManager locationManager;
	private final String mockProviderName = "elqusair";
	private boolean mockLocationsEnabled;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.mock_location);
        findViews();
        mockLocationsEnabled = query();
        if (mockLocationsEnabled) {
    		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    		try {
    			addMockProvider();
    		}
    		catch (Exception e) {
    			Log.e("GlobalPosition", e.toString());
    		}
            submitButton.setOnClickListener(new OnClickListener() {
    			@Override
    			public void onClick(View v) {
    				latitude.getText();
    				longitude.getText();
    				try {
    					setMockLocation(26.1, 34.25);
    				}
    				catch (Exception e) {
    					Log.e("GlobalPosition", e.toString()); }
    				finish();
    			}});
        } else {
        	submitButton.setEnabled(false);
        }
	}
	@Override
	public void onDestroy() {
		super.onDestroy();
		removeMockProvider();
	}
	private void findViews() {
		latitude = (TextView) findViewById(R.id.mock_latitude);
		longitude = (TextView) findViewById(R.id.mock_longitude);
		submitButton = (Button) findViewById(R.id.mock_submit_button);
	}
	/** check if the mock location settings */
	private boolean query() {
		ContentResolver cr = getContentResolver();
		String value = Settings.Secure.getString(cr, Settings.Secure.ALLOW_MOCK_LOCATION);
        return value.equals("1");
	}
	private void addMockProvider() {
		boolean requiresNetwork = false;
		boolean requiresSatellite = false;
		boolean requiresCell = false;
		boolean hasMonetaryCost = false;
		boolean supportsAltitude = false;
		boolean supportsSpeed = false;
		boolean supportsBearing = false;
		int powerRequirement = Criteria.POWER_LOW;
		int accuracy = Criteria.ACCURACY_FINE;
		locationManager.addTestProvider(mockProviderName, requiresNetwork, requiresSatellite, requiresCell, hasMonetaryCost, supportsAltitude, supportsSpeed, supportsBearing, powerRequirement, accuracy);
	}
	private void removeMockProvider() {
		locationManager.removeTestProvider(mockProviderName);
	}
	private void setMockLocation(double latitude, double longitude) {
		Location location = new Location(mockProviderName);
		location.setLatitude(latitude);
		location.setLongitude(longitude);
		location.setTime(new Date().getTime());
		locationManager.setTestProviderLocation(mockProviderName, location);
		locationManager.setTestProviderEnabled(mockProviderName, true);
	}
}
