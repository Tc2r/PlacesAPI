package com.tc2r.placesapi;

// Recieve data from url using httpURLconnection.

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class DownloadUrl {

	public String readUrl(String myUrl) {
		String data = "";
		InputStream inputStream = null;
		HttpURLConnection urlConnection = null;

		try {
			URL url = new URL(myUrl);
			urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.connect();

			inputStream = urlConnection.getInputStream();

			BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
			StringBuffer sb = new StringBuffer();

			// read each line
			String line = "";
			while((line = br.readLine()) != null){
				sb.append(line);
			}

			data = sb.toString();
			br.close();



		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {

			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			urlConnection.disconnect();
		}
		return  data;
	}
}
