package com.uttama.android.samples;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import org.apache.http.HttpResponse;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.GpsStatus;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ToggleButton;

public class GlobalPosition
extends Activity
implements SensorEventListener, GpsStatus.Listener, LocationListener {
	final static String LOG_TAG = "GlobalPosition";
	public static float feetPerMeter = 3.2808399f;
	private Model model;
	private ProvidersModel providersModel;
	
	public final static int REQUEST_CODE_MOCK_LOCATION = 1;
	public final static int REQUEST_CODE_PREFERENCES = 2;
	
	public final static String PREFERENCE_KEY_LINEAR_UNITS = "linearUnits";
	public final static String PREFERENCE_LINEAR_FEET = "feet";
	public final static String PREFERENCE_LINEAR_METERS = "meters";
	public final static String PREFERENCE_LINEAR_KILOMETERS = "kilometers";
	public final static String PREFERENCE_LINEAR_MILES = "miles";
	
	private CheckBox enableGps;
	private TextView altitude;
	private TextView accuracy;
	private TextView fixTime;
	private TextView fixDate;
	private TextView latitude;
	private TextView longitude;
	private TextView usgsAltitude;
	private TextView bearing;
	private TextView speed;
	private TextView sourceProvider;
	
	private LocationManager locationManager;
	private List<String> providers;
	
	private Location latestLocation;
	
	private SimpleDateFormat timestampDateFormat = new SimpleDateFormat("E MMM d yyyy");
	private SimpleDateFormat timestampTimeFormat = new SimpleDateFormat("kk:mm:ss z");
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(LOG_TAG, "oncreate");
        
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		providers = locationManager.getAllProviders();

        model = new Model();
        providersModel = new ProvidersModel();
        
        setContentView(R.layout.main);
        findViews();
        bindEvents();
/*
        // I don't know why I tried MVC here
        enableGps.setOnCheckedChangeListener(new OnCheckedChangeListener() {
        	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        		//model.setGpsEnabled(isChecked);
        		enableLocationUpdates(isChecked);
        	}
        });
        model.addObserver(new Observer() {
        	public void update(Observable model, Object obj) {
        		enableLocationUpdates(((Model) model).gpsEnabled());
        	}
        });
        */
    }
    @Override
    public void onResume() {
    	super.onResume();
        Log.i(LOG_TAG, "onresume");
		StringBuffer providerNames = new StringBuffer();
		for (String name : providers) {
			providerNames.append((providerNames.length() != 0 ? ", " : "") + name);
		}
		// FIXME do the toggle buttons instead
		//sensorName.setText(providerNames.toString());
		
		Spinner spinner = (Spinner) findViewById(R.id.spinner);
		spinner.setVisibility(View.GONE);
	    //ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
	    //        this, R.array.planets_array, android.R.layout.simple_spinner_item);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, providers);
		//ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.spinner_items, R.id.check_text_view, providers);
	    //adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    spinner.setAdapter(adapter);
	    
		try {
			latestLocation = getMostRecentLastKnownLocation(providers);
			updateViews(latestLocation);
		}
		catch (Exception e) {
			Log.e(LOG_TAG, e.toString());
		}
		//sensorManager.registerListener(this, sensor, rate);
		//locationManager.addGpsStatusListener(this);
		
		//enableLocationUpdates(true);
		//enableGps.setChecked(true);
    }
    @Override
    public void onPause() {
    	super.onPause();
        Log.i(LOG_TAG, "onpause");
		enableLocationUpdates(false);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	MenuInflater inflater = getMenuInflater();
    	inflater.inflate(R.menu.main_menu, menu);
    	return true;
    }
    @Override
	public boolean onOptionsItemSelected(MenuItem menu) {
    	Intent intent;
    	switch (menu.getItemId()) {
    	case R.id.mock:
    		intent = new Intent(this, MockLocation.class);
    		startActivityForResult(intent, REQUEST_CODE_MOCK_LOCATION);
    		return true;
    	case R.id.settings:
            intent = new Intent(this, GlobalPositionPreferences.class);
            startActivityForResult(intent, REQUEST_CODE_PREFERENCES);
    		return true;
    	}
    	return false;
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
    	if (requestCode == REQUEST_CODE_MOCK_LOCATION) {
    		// to add the mock provide to the update
    		enableLocationUpdates(true);
    	}
    	if (requestCode == REQUEST_CODE_PREFERENCES) {
    		updateViews(latestLocation);
    	}
    }
    private void bindEvents() {
	    ToggleButton networkToggleButton = (ToggleButton) findViewById(R.id.network_togglebutton);
	    networkToggleButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				providersModel.setProviderEnabled("network", isChecked);
				updateEnableLocationUpdates("network", isChecked);
			}
		});
	    ToggleButton gpsToggleButton = (ToggleButton) findViewById(R.id.gps_togglebutton);
	    gpsToggleButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
	        		//enableLocationUpdates(isChecked);
					providersModel.setProviderEnabled("gps", isChecked);
					updateEnableLocationUpdates("gps", isChecked);
				} else {
					buttonView.setChecked(false);
					showSettingsAlert();
				}
			}
		});
    }
    /**
     * Returns the most recent last known location from the given list of providers.
     * Returns null if there are no providers or if LocationManager.getLastKnownLocation()
     * return null for each provider.
     * @param providers the list of available provide names
     * @return the most recent location or null
     */
    private Location getMostRecentLastKnownLocation(List<String> providers) {
    	Location latest = null;
    	for (String provider : providers) {
    		Location candidate = locationManager.getLastKnownLocation(provider);
    		if (latest == null || candidate != null && candidate.getTime() < latest.getTime()) {
    			latest = candidate;
    		}
    	}
    	return latest;
    }
    public void findViews() {
        altitude = (TextView) findViewById(R.id.altitude);
        accuracy = (TextView) findViewById(R.id.accuracy);
        fixTime = (TextView) findViewById(R.id.fixTime);
        fixDate = (TextView) findViewById(R.id.fixDate);
        latitude = (TextView) findViewById(R.id.latitude);
        longitude = (TextView) findViewById(R.id.longitude);
        usgsAltitude = (TextView) findViewById(R.id.usgs_altitude);
        bearing = (TextView) findViewById(R.id.bearing);
        speed = (TextView) findViewById(R.id.speed);
        sourceProvider = (TextView) findViewById(R.id.source_provider);
    }
    /**
     * Register or deregister this activity as a location update listener. The updates
     * are delivered via the onLocationChanged callback.
     * 
     * @param onOff register or deregister
     */
    private void enableLocationUpdates(boolean onOff) {
		int min = 0; //1; // milliseconds
		float distance = 0.0f; //100.0f; // meters
		if (onOff) {
			for (String provider : providers) {
				//locationManager.requestLocationUpdates(provider, min, distance, this);
			}
		} else {
			//locationManager.removeUpdates(this);
		}
		Log.i(LOG_TAG, "location updates " + (onOff ? "enabled" : "disabled"));
    }
    /**
     * Enable or the disable the given provider. Note that LocationManager does not provide
     * a way to disable a single provider notification.
     * @param provider
     * @param enable
     */
    private void updateEnableLocationUpdates(String provider, boolean enable) {
		int min = 0; //1; // milliseconds
		float distance = 0.0f; //100.0f; // meters
    	if (enable) {
            Log.i(LOG_TAG, "enable location update for provider: " + provider);
            try {
    			locationManager.requestLocationUpdates(provider, min, distance, this);
            }
            catch (Exception e) {
            	Log.e(LOG_TAG, "error requesting location updates for: " + provider, e);
            }
    	} else {
			locationManager.removeUpdates(this);
			for (String provider2 : locationManager.getAllProviders()) {
				if (providersModel.isProviderEnabled(provider2)) {
					locationManager.requestLocationUpdates(provider2, min, distance, this);
				}
			}
    	}
    }
    protected void updateViews(Location location) {
		fixTime.setText(timestampTimeFormat.format(location.getTime()));
		fixDate.setText(timestampDateFormat.format(location.getTime()));
		sourceProvider.setText(String.format("%s", location.getProvider()));
		// location.hasAltitude() runtime exception
		setViewText(altitude, "", true, location.getAltitude());
		setViewText(accuracy, "", location.hasAccuracy(), location.getAccuracy());
		latitude.setText(String.format("%4.6f \u00B0", location.getLatitude()));
		longitude.setText(String.format("%4.6f \u00B0", location.getLongitude()));
		bearing.setText(String.format("%4.2f \u00B0", location.getBearing()));
		setViewText(speed, "", true, location.getSpeed());
		new UsgsTask().execute(location.getLatitude(), location.getLongitude());
    }
    private void setViewText(TextView view, String label, boolean hasValue, double value) {
    	String format = label.equals("accuracy") ? "\u00B1%2.1f %s" : "%+2.1f %s";
        boolean displayAsFeet = false;
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        String linearUnits = settings.getString(PREFERENCE_KEY_LINEAR_UNITS, PREFERENCE_LINEAR_METERS);
        if (linearUnits.equals(PREFERENCE_LINEAR_FEET)) displayAsFeet = true;
    	String units = displayAsFeet ? "ft" : "m";
    	if (hasValue) {
        	double unitValue = displayAsFeet ? value * feetPerMeter : value;
    		view.setText(String.format(label + format, unitValue, units));
    	} else {
    		view.setText(String.format(label + format, "--", units));
    	}
    }
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onSensorChanged(SensorEvent event) {
		//timestamp.setText("" + event.timestamp);
		
	}
	/* GpsStatus.Listener */
	@Override
	public void onGpsStatusChanged(int event) {
		GpsStatus status = locationManager.getGpsStatus(null);
		Location location = locationManager.getLastKnownLocation("gps");
		//timestamp.setText("a" + location.getTime());
	}
	
	/* LocationListener-ness */
	
	@Override
	public void onLocationChanged(Location location) {
		Log.i(LOG_TAG, location.toString());
		updateViews(location);
	}
	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		
	}
	/**
	 * Remind the user to enable GPS. Start location source settings if requested.
	 */
	private void showSettingsAlert() {
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
		alertDialog.setTitle("GPS");
		alertDialog.setMessage("GPS is not enabled. Do you want to go the settings menu?");
		alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
				startActivity(intent);
			}
		});
		alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});
		alertDialog.show();
	}
	private class UsgsTask
	extends AsyncTask<Double, Integer, Double> {
		@Override
		protected Double doInBackground(Double... coordinate) {
			UsgsGisParser parser = new UsgsGisParser();
			Double altitude = Double.NaN;
			try {
				altitude = parser.getUsgsAltitude(coordinate[0], coordinate[1]);
			}
			catch (Exception e) {
				Log.e(LOG_TAG, e.toString());
			}
			return altitude;
		}
		@Override
		protected void onPostExecute(Double altitude) {
			//usgsAltitude.setText(String.format("usgs altitude: %6.2f", altitude));
			setViewText(usgsAltitude, "", true, altitude);
		}
	}
	class Model
	extends Observable {
		private Observable stateObservable = new Observable();
		boolean gpsEnabled;
		public boolean gpsEnabled() {
			return this.gpsEnabled;
		}
		// deprecated
		public void setGpsEnabled(boolean gpsEnabled) {
			boolean changed = this.gpsEnabled != gpsEnabled;
			if (changed) {
				this.gpsEnabled = gpsEnabled;
				setChanged();
				notifyObservers();
			}
		}
		public void addObserver(Observer observer) {
			stateObservable.addObserver(observer);
		}
	}
}