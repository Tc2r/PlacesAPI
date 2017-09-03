package com.tc2r.placesapi;

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;

/**
 * Created by Tc2r on 9/2/2017.
 * <p>
 * Description:
 */

public class GetDirectionsData extends AsyncTask<Object, String, String> {

	String googleDirectionsData;
	GoogleMap mMap;
	String url, duration, distance;
	LatLng latLng;

	@Override
	protected String doInBackground(Object... objects) {

		mMap = (GoogleMap) objects[0];
		url = (String) objects[1];
		latLng = (LatLng) objects[2];

		Log.d("URL", url);
		DownloadUrl downloadUrl = new DownloadUrl();

		googleDirectionsData = downloadUrl.readUrl(url);

		return googleDirectionsData;
	}

	@Override
	protected void onPostExecute(String s) {

		// convert Json string into Hashmap of places.
		HashMap<String, String> directionsList = null;
		DataParser parser = new DataParser();
		directionsList = parser.parseDirections(s);
		duration = directionsList.get("duration");
		distance = directionsList.get("distance");

		// Clear map
		mMap.clear();

		// Set marker on saved destination.
		MarkerOptions markerOptions = new MarkerOptions();
		markerOptions.position(latLng);
		markerOptions.draggable(true);
		markerOptions.title("Duration: " + duration);
		markerOptions.snippet("Distance: " + distance);
		markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));


		// set marker to map, zoom in on marker.
		mMap.addMarker(markerOptions).showInfoWindow();
		mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
		mMap.animateCamera(CameraUpdateFactory.zoomTo(16));

	}
}
