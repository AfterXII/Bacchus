package modules;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import android.util.Log;

public class WebUtils {
	
	public static String buildURL(String baseURL, Map<String, String> attributes) {
		String url = baseURL;

		boolean appendAmp = false;

		for(Map.Entry<String, String> e : attributes.entrySet()) {
			if(appendAmp) {
				url += "&";
			}
			url += e.getKey() + "=" + e.getValue();

			appendAmp = true;
		}

		Log.v("TaxiLocator", "Built URL: " + url);

		return url;
	}
	
	public static String getHttpStream(String url) {
		String stream = "";

		URL urlObj;
		try {
			urlObj = new URL(url);
			HttpURLConnection connection = (HttpURLConnection) urlObj.openConnection();
			connection.setDoOutput(true);
			DataInputStream in = new DataInputStream(connection.getInputStream());
			String add = "";
			while(add != null) {
				add = in.readLine();
				stream += add;
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		Log.v("TaxiLocator", "Got HTTP Stream: " + stream);

		return stream;
	}

}
