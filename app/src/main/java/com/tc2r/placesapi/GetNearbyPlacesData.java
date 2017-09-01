package com.tc2r.placesapi;

import android.os.AsyncTask;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;
import java.util.List;

/**
 * Created by Tc2r on 8/31/2017.
 * <p>
 * Description:
 */

public class GetNearbyPlacesData extends AsyncTask<Object, String, String>{

	String googlePlacesData;
	GoogleMap mMap;
	String url;

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
	}

	@Override
	protected String doInBackground(Object... objects) {
		mMap = (GoogleMap) objects[0];
		url = (String)objects[1];

		DownloadUrl downloadUrl = new DownloadUrl();
		googlePlacesData = downloadUrl.readUrl(url);

		return googlePlacesData;
	}

	@Override
	protected void onPostExecute(String s) {
		List<HashMap<String, String>> nearbyPlaceList = null;
		DataParser parser = new DataParser();
		nearbyPlaceList = parser.parse(s);
		showNearbyPlaces(nearbyPlaceList);
	}

	private void showNearbyPlaces(List<HashMap<String, String>> nearbyPlaceList) {

		for (int i = 0; i < nearbyPlaceList.size(); i++) {
			MarkerOptions markerOptions = new MarkerOptions();
			HashMap<String, String> googlePlace = nearbyPlaceList.get(i);

			String placeName = googlePlace.get("place_name");
			String vicinity = googlePlace.get("vicinity");
			double lat = Double.parseDouble(googlePlace.get("lat"));
			double lng = Double.parseDouble(googlePlace.get("lng"));

			LatLng latLng = new LatLng(lat, lng);
			markerOptions.position(latLng);
			markerOptions.title(placeName + "; " + vicinity);
			markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN));

			mMap.addMarker(markerOptions);
			mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
			mMap.animateCamera(CameraUpdateFactory.zoomTo(10));

		}
	}


}
