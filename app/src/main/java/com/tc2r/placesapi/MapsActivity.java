package com.tc2r.placesapi;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback
				, GoogleApiClient.ConnectionCallbacks
				, LocationListener
				, GoogleApiClient.OnConnectionFailedListener {

	// Unique Permission Code for fine GPS locaiton
	public static final int PERMISSION_REQUEST_LOCATION_CODE = 99;

	// Distance from phone location we can find places.
	public static final int PROXIMITY_RADIUS = 2147;

	// Base Google API URLS
	private static final String BASE_NEARBY_URL = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?";
	private static final String BASE_DIRECTIONS_URL = "https://maps.googleapis.com/maps/api/directions/json?";


	private GoogleMap mMap;
	private GoogleApiClient client;
	private Marker currentLocationMarker;
	private List<Address> addressList = null;


	private LocationRequest locationRequest;
	private double latitude, longitude;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_maps);

		// If Build version is greater than 23
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			checkLocationPermission();
		}
		// Obtain the SupportMapFragment and get notified when the map is ready to be used.
		SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
						.findFragmentById(R.id.map);
		mapFragment.getMapAsync(this);
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		switch (requestCode) {
			case PERMISSION_REQUEST_LOCATION_CODE:
				// check if permission was granted or not
				if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					// permission is granted
					if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

						if (client == null) {

							// create api client
							buildGoogleApiClient();
						}

						// Enables the My Location Layer on the map.
						// Creates a button in the top right corner of the map. on click the camera centers
						// the map on the current location of the device.
						mMap.setMyLocationEnabled(true);
					}
				} else {
					// permission is denied
					Toast.makeText(this, R.string.permission_denied_toast, Toast.LENGTH_LONG).show();
				}
		}
	}


	/**
	 * Manipulates the map once available.
	 * This callback is triggered when the map is ready to be used.
	 * This is where we can add markers or lines, add listeners or move the camera.
	 * If Google Play services is not installed on the device, the user will be prompted to install
	 * it inside the SupportMapFragment. This method will only be triggered once the user has
	 * installed Google Play services and returned to the app.
	 */
	@Override
	public void onMapReady(GoogleMap googleMap) {
		mMap = googleMap;

		// If we are granted permissions, build Client
		if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
			buildGoogleApiClient();
			mMap.setMyLocationEnabled(true);
		}
	}

	protected synchronized void buildGoogleApiClient() {
		// Builds and connects to client
		client = new GoogleApiClient.Builder(this)
						.addConnectionCallbacks(this)
						.addOnConnectionFailedListener(this)
						.addApi(LocationServices.API)
						.build();

		client.connect();
	}

	@Override
	public void onLocationChanged(Location location) {

		// get lat and long from the Listener
		latitude = location.getLatitude();
		longitude = location.getLongitude();
		LatLng latLng = new LatLng(latitude, longitude);

		// if there is a marker already, remove it.
		if (currentLocationMarker != null) {

			currentLocationMarker.remove();
		}
		Log.d("lat = ", "" + latitude);

		// Marker Options set properties to the marker.
		MarkerOptions markerOptions = new MarkerOptions();
		markerOptions.position(latLng);
		markerOptions.title(getString(R.string.marker_my_location_title));
		markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));

		// set marker to location of markerOptions on map.
		currentLocationMarker = mMap.addMarker(markerOptions);


		// Set camera and map options.
		mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
		mMap.animateCamera(CameraUpdateFactory.zoomBy(15));
		mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

		// Stop location updates
		if (client != null) {
			LocationServices.FusedLocationApi.removeLocationUpdates(client, this);
		}
	}


	// When an Object is clicked this method will run.
	public void onClick(View view) {


		Object dataTransfer[] = new Object[2];
		String url, searchType;
		GetNearbyPlacesData getNearbyPlacesData = new GetNearbyPlacesData();

		switch (view.getId()) {
			case R.id.btn_search:
				// Take the input from the editText  and check for
				// addresses near the phone location that match input.
				EditText locationET = findViewById(R.id.et_location);
				String location = locationET.getText().toString();

				if (!location.equals("")) {

					Geocoder geocoder = new Geocoder(this);
					try {

						//Returns an array of Addresses that are known to describe the named location,
						addressList = geocoder.getFromLocationName(location, 5);

						if (addressList != null) {
							// set up map markers for each returned Address.
							for (int i = 0; i < addressList.size(); i++) {

								Address myAddress = addressList.get(i);


								// get lat and long from the Listener
								LatLng latLng = new LatLng(myAddress.getLatitude(), myAddress.getLongitude());

								// Marker Options set properties to the marker.
								MarkerOptions mo = new MarkerOptions();
								mo.position(latLng);
								mo.title(myAddress.getThoroughfare());
								mo.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));

								// Set camera and map options.
								mMap.addMarker(mo);
								mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
								mMap.animateCamera(CameraUpdateFactory.zoomTo(17));
							}
						}

					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				break;

			case R.id.btn_hospitals:

				// Clear map of markers
				mMap.clear();
				searchType = getString(R.string.search_type_hospital);
				url = getUrl(latitude, longitude, searchType);
				dataTransfer[0] = mMap;
				dataTransfer[1] = url;
				getNearbyPlacesData.execute(dataTransfer);
				Toast.makeText(this, R.string.search_text_toast_1, Toast.LENGTH_LONG).show();
				break;


			case R.id.btn_restaurants:
				mMap.clear();
				searchType = getString(R.string.search_type_restaurant);
				url = getUrl(latitude, longitude, searchType);
				dataTransfer[0] = mMap;
				dataTransfer[1] = url;
				getNearbyPlacesData.execute(dataTransfer);
				Toast.makeText(this, R.string.search_text_toast_2, Toast.LENGTH_LONG).show();
				break;


			case R.id.btn_schools:
				mMap.clear();
				searchType = getString(R.string.search_type_school);
				url = getUrl(latitude, longitude, searchType);
				dataTransfer[0] = mMap;
				dataTransfer[1] = url;
				getNearbyPlacesData.execute(dataTransfer);
				Toast.makeText(this, R.string.search_text_toast_3, Toast.LENGTH_LONG).show();
				break;


			case R.id.btn_to:
				break;
		}
	}

	private String getUrl(double latitude, double longitude, String nearbyPlace) {


		// Create the proper url to get a Json String from the GooglePlacesUrl.

		StringBuilder googlePlaceUrl = new StringBuilder(BASE_NEARBY_URL);
		googlePlaceUrl.append("location=" + latitude + "," + longitude);
		googlePlaceUrl.append("&radius=" + PROXIMITY_RADIUS);
		googlePlaceUrl.append("&type=" + nearbyPlace);
		googlePlaceUrl.append("&sensor=true");
		//googlePlaceUrl.append("&keyword=tacos");
		googlePlaceUrl.append("&key=" + getResources().getString(R.string.google_places_key));
		Log.wtf("URL IS", googlePlaceUrl.toString());
		return googlePlaceUrl.toString();
	}


	@Override
	public void onConnected(@Nullable Bundle bundle) {

		// create a location request
		locationRequest = new LocationRequest();

		// set the interval in which you want to get locations (milliseconds).
		locationRequest.setInterval(60 * 1000);
		// set highest rate of intervals to receive updates.
		locationRequest.setFastestInterval(20 * 1000);
		// set priority of location request.
		locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

		if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
			LocationServices.FusedLocationApi.requestLocationUpdates(client, locationRequest, this);
		}
	}

	public boolean checkLocationPermission() {

		//Determine whether you have been granted a particular permission.
		if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

			// Gets whether you should show UI with rationale for requesting a permission.
			if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
				// Returns true if app has requested this permission previously and request was denied

				ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_LOCATION_CODE);

			} else {

				ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_LOCATION_CODE);

			}
			// if user previously asked for permission it returns false
			return false;

		} else {
			return true;
		}
	}

	@Override
	public void onConnectionSuspended(int i) {

	}

	@Override
	public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

	}


}
