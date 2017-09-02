package com.tc2r.placesapi;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Tc2r on 8/31/2017.
 * <p>
 * Description:
 * Parses the data from the Json String returned by the api.
 */

public class DataParser {


	// Parses the jsonData string into a list of placesin the form of HashMaps.
	public List<HashMap<String, String>> parse(String jsonData) {

		JSONArray jsonArray = null;
		JSONObject jsonObject;

		try {
			jsonObject = new JSONObject(jsonData);
			jsonArray = jsonObject.getJSONArray("results");
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return getPlaces(jsonArray);
	}

	//Takes jsonArray from Google Api and parses it into a List of HashMaps
	private List<HashMap<String, String>> getPlaces(JSONArray jsonArray) {


		List<HashMap<String, String>> placesList = new ArrayList<>();
		HashMap<String, String> placeMap = null;

		for (int i = 0; i < jsonArray.length(); i++) {
			try {
				// get each place from the array and add it to the
				// list
				placeMap = getPlace((JSONObject) jsonArray.get(i));
				placesList.add(placeMap);

			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return placesList;
	}

	// Takes the Google Places Json String and parses it.
	private HashMap<String, String> getPlace(JSONObject googlePlaceJson) {

		HashMap<String, String> googlePlacesMap = new HashMap<>();
		String placeName = "-NA-";
		String vicinity = "-NA-";
		String latitude = "";
		String longitude = "";
		String reference = "";


		// Parse data into String variables.
		try {
			if (!googlePlaceJson.isNull("name")) {

				placeName = googlePlaceJson.getString("name");

			}

			if (!googlePlaceJson.isNull("vicinity")) {
				vicinity = googlePlaceJson.getString("vicinity");
			}
			latitude = googlePlaceJson.getJSONObject("geometry").getJSONObject("location").getString("lat");
			longitude = googlePlaceJson.getJSONObject("geometry").getJSONObject("location").getString("lng");
			reference = googlePlaceJson.getString("reference");

			// Places parsed data into HashMap
			googlePlacesMap.put("place_name", placeName);
			googlePlacesMap.put("vicinity", vicinity);
			googlePlacesMap.put("lat", latitude);
			googlePlacesMap.put("lng", longitude);
			googlePlacesMap.put("reference", reference);


		} catch (JSONException e) {
			e.printStackTrace();
		}
		return googlePlacesMap;
	}

}
