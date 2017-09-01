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

	public static final int PERMISSION_REQUEST_LOCATION_CODE = 99;

	public static final int PROXIMITY_RADIUS = 2147;
	private static final String BASE_URL = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?";
	double latitude, longitude;

	private GoogleMap mMap;
	private GoogleApiClient client;
	private LocationRequest locationRequest;
	private Location lastKnownLocation;
	private Marker currentLocationMarker;
	private List<Address> addressList = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_maps);

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
					if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
						if (client == null) {
							buildGoogleApiClient();
						}
						mMap.setMyLocationEnabled(true);
					}
				} else {
					// permission is denied
					Toast.makeText(this, "PERMISSION DENIED", Toast.LENGTH_LONG).show();
				}
		}
	}


	/**
	 * Manipulates the map once available.
	 * This callback is triggered when the map is ready to be used.
	 * This is where we can add markers or lines, add listeners or move the camera. In this case,
	 * we just add a marker near Sydney, Australia.
	 * If Google Play services is not installed on the device, the user will be prompted to install
	 * it inside the SupportMapFragment. This method will only be triggered once the user has
	 * installed Google Play services and returned to the app.
	 */
	@Override
	public void onMapReady(GoogleMap googleMap) {
		mMap = googleMap;

		if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
			buildGoogleApiClient();
			mMap.setMyLocationEnabled(true);
		}
	}

	protected synchronized void buildGoogleApiClient() {
		client = new GoogleApiClient.Builder(this)
						.addConnectionCallbacks(this)
						.addOnConnectionFailedListener(this)
						.addApi(LocationServices.API)
						.build();

		client.connect();
	}

	@Override
	public void onLocationChanged(Location location) {
		latitude = location.getLatitude();
		longitude = location.getLongitude();
		lastKnownLocation = location;

		// if there is a marker already, remove it.
		if (currentLocationMarker != null) {

			currentLocationMarker.remove();
		}
		Log.d("lat = ", "" + latitude);

		LatLng latLng = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
		// Marker Options set properties to the marker.
		MarkerOptions markerOptions = new MarkerOptions();
		markerOptions.position(latLng);
		markerOptions.title("Current Location");
		markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
		currentLocationMarker = mMap.addMarker(markerOptions);
		mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
		mMap.animateCamera(CameraUpdateFactory.zoomBy(10));

		// Stop location updates
		if (client != null) {
			LocationServices.FusedLocationApi.removeLocationUpdates(client, this);
		}
	}

	public void onClick(View view) {

		Object dataTransfer[] = new Object[2];
		String url;
		GetNearbyPlacesData getNearbyPlacesData = new GetNearbyPlacesData();

		switch (view.getId()) {
			case R.id.B_search:
				EditText locationET = findViewById(R.id.TF_location);
				String location = locationET.getText().toString();
				if (!location.equals("")) {
					Geocoder geocoder = new Geocoder(this);

					try {
						addressList = geocoder.getFromLocationName(location, 5);

						if (addressList != null) {

							for (int i = 0; i < addressList.size(); i++) {

								Address myAddress = addressList.get(i);
								LatLng latLng = new LatLng(myAddress.getLatitude(), myAddress.getLongitude());
								MarkerOptions mo = new MarkerOptions();
								mo.position(latLng);
								mo.title("location");

								mMap.addMarker(mo);
								mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
								mMap.animateCamera(CameraUpdateFactory.zoomTo(10));
							}
						}

					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				break;
			case R.id.btn_hospitals:
				mMap.clear();
				String hospital = "hospital";
				url = getUrl(latitude, longitude, hospital);
				dataTransfer[0] = mMap;
				dataTransfer[1] = url;
				getNearbyPlacesData.execute(dataTransfer);
				Toast.makeText(this, "SHOWING NEARBY HOSPITALS", Toast.LENGTH_LONG).show();
				break;


			case R.id.B_restaurants:
				mMap.clear();
				String restaurant = "restaurant";
				url = getUrl(latitude, longitude, restaurant);
				dataTransfer[0] = mMap;
				dataTransfer[1] = url;
				getNearbyPlacesData.execute(dataTransfer);
				Toast.makeText(this, "SHOWING NEARBY RESTAURANTS", Toast.LENGTH_LONG).show();
				break;


			case R.id.B_schools:
				mMap.clear();
				String school = "school";
				url = getUrl(latitude, longitude, school);
				dataTransfer[0] = mMap;
				dataTransfer[1] = url;
				getNearbyPlacesData.execute(dataTransfer);

				Toast.makeText(this, "SHOWING NEARBY SCHOOLS", Toast.LENGTH_LONG).show();
				break;

			case R.id.B_to:
				break;
		}
	}

	private String getUrl(double latitude, double longitude, String nearbyPlace) {

		StringBuilder googlePlaceUrl = new StringBuilder(BASE_URL);
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

		// set the interval of time its called in milliseconds
		locationRequest.setInterval(100);
		locationRequest.setFastestInterval(1000);
		locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

		if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
			LocationServices.FusedLocationApi.requestLocationUpdates(client, locationRequest, this);
		}
	}

	public boolean checkLocationPermission() {

		if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

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
