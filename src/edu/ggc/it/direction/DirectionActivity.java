package edu.ggc.it.direction;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import edu.ggc.it.R;

/**
 * This class is an activity class
 * 
 * @author Thai Pham
 * @version 0.1
 * 
 */
public class DirectionActivity extends Activity {

	// Create a imageview for the GGC map
	private TouchImageView img;
	// Create new context for activity
	private Context myContext;
	// Create a textview to display instructions to users
	private TextView instructionText;
	// Create location manager
	private LocationManager lm;
	// Create a location
	UserLocationListener myLocationListener;
	// This will get the user's latitude
	private double latitude;
	// This will get the user's longitude
	private double longitude;
	// This will get the destination's latitude
	private double latitudeDes;
	// This will get the destination's longitude
	private double longitudeDes;
	// Create a location list that holds list of places in GGC
	private LocationArray myLocationList;
	// Create a new spinner to display list of places in GGC
	private Spinner spin;
	// This will get the item that users choose from place's list of spinner
	private String spin_val;
	private ArrayAdapter<String> spin_adapter;

	/**
	 * @Override
	 */
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_direction);
		myContext = this;

		//Check if user's device GPS is enable or not. If not, let user to enable it
		checkGPS();
		// Init locationlist
		myLocationList = new LocationArray();
		// Create new listenner for spinner
		MyItemSelectedListenner mySelectedListenner = new MyItemSelectedListenner();
		spin = (Spinner) findViewById(R.id.spinnerText);
		spin.setOnItemSelectedListener(mySelectedListenner);

		instructionText = (TextView) findViewById(R.id.instruction_text);
		img = (TouchImageView) findViewById(R.id.imageMap);
		img.setMaxZoom(4f);
		// Create ArrayAdapter for spinner
		spin_adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_dropdown_item,
				myLocationList.getNameList());
		// setting adapter to spinner
		spin.setAdapter(spin_adapter);
	}

	/**
	 * This function aims to release all objects to NULL in order to save memory
	 */
	public void onBackPressed() {
		super.onBackPressed();
		Drawable d = img.getDrawable();
		if (d != null)
			d.setCallback(null);
		img.setImageDrawable(null);
		img = null;

		// Set Location manager to null to turn GPS off
		if (myLocationListener != null)
			lm.removeUpdates(myLocationListener);
		lm = null;
		spin_adapter = null;
		spin = null;
	}

	/**
	 * This function aims to update user's location when it changes, to correct
	 * location of users on the map
	 */
	public void updateLocation() {
		img.setUserCoordinator(latitude, longitude);
		img.invalidate();
	}

	/**
	 * This method is to check if the GPS on users' device is on or off. If it
	 * is off, allow users turn on
	 */
	public void checkGPS() {
		// Create a new location manager
		lm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		// Run these codes when testing on a real device has GPS
		if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			buildAlertMessageNoGps();
		} else {
			lm = (LocationManager) this
					.getSystemService(Context.LOCATION_SERVICE);
			lm.getProvider(LocationManager.GPS_PROVIDER);
			myLocationListener = new UserLocationListener();
			lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10,
					myLocationListener);
		}
	}

	/**
	 * This class to override LocationListener to get current location of users.
	 * 
	 */
	private class UserLocationListener implements LocationListener {

		@Override
		public void onLocationChanged(Location location) {
			longitude = location.getLongitude();
			latitude = location.getLatitude();
			updateLocation();
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
	}

	/**
	 * This method is used to warn users when they click on Direction button if
	 * the GPS on their device is not enable and allow them to enable it
	 */
	private void buildAlertMessageNoGps() {
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(
				"Your GPS seems to be disabled, do you want to enable it?")
				.setCancelable(false)
				.setPositiveButton("Yes",
						new DialogInterface.OnClickListener() {
							public void onClick(final DialogInterface dialog,
									final int id) {
								startActivity(new Intent(
										android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
							}
						})
				.setNegativeButton("No", new DialogInterface.OnClickListener() {
					public void onClick(final DialogInterface dialog,
							final int id) {
						dialog.cancel();
					}
				});
		final AlertDialog alert = builder.create();
		alert.show();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.weather, menu);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		Geocoder geocoder = new Geocoder(this, Locale.ENGLISH);
		List<Address> addresses = null;
		try {
			addresses = geocoder.getFromLocation(latitude, longitude, 3);
		} catch (IOException ex) {
			// Handle IOException
		}
		String zipCode = "";
		for (int i = 0; i < addresses.size(); i++) {
			Address address = addresses.get(i);
			if (address.getPostalCode() != null)
				zipCode = address.getPostalCode();
		}
		String url = "http://www.weather.com/weather/today/" + zipCode;
		if (item.getItemId() == R.id.weather) {
			try {
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setData(Uri.parse(url));
				startActivity(intent);
			} catch (Exception e) {
				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
			}
		}
		return true;
	}

	/**
	 * This method is a listener for the spinner in order to get the position
	 * of item which is selected
	 * 
	 */
	public class MyItemSelectedListenner implements OnItemSelectedListener {

		public void onItemSelected(AdapterView<?> arg0, View arg1,
				int position, long id) {
			spin_val = myLocationList.getInstruction(position);
			String bld = myLocationList.getBuilding(position);

			if (bld.length() == 0) {
				instructionText.setText(spin_val);
				img.setImageResource(R.drawable.thai_ggc_map);
			} else {// Run these lines when users click on any item on the list
					// of spinner
				if (bld.length() == 1) {
					instructionText
							.setText("It is located on building "
									+ bld
									+ "\nInstruction: Your location is Yellow dot, Destination is Red dot\n"
									+ "    " + spin_val);
				} else {
					instructionText
							.setText("\nInstruction: Your location is Yellow dot, Destination is Red dot\n"
									+ "    " + spin_val);
				}
			}
			// Depend on building where the instruction points to, these lines
			// will show the appropriate map
			latitudeDes = myLocationList.getLatitude(position);
			longitudeDes = myLocationList.getLongitude(position);
			img.setDesCoordinator(latitudeDes, longitudeDes);
			img.setOriginalSize();
		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
			// TODO Auto-generated method stub
		}
	}
}
