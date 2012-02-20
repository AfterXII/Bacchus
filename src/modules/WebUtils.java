package modules;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import android.util.Log;

/*
 * Class: WebUtils
 * 
 * A class of static web utilities, used when scraping web APIs, etc.
 */
public class WebUtils {
	
	/*
	 * Method: buildURL
	 * Parameters:
	 * 		String baseURL: the URL, if any, to build the new URL on top of
	 * 		Map<String, String> attributes: an attribute list to write on the url
	 * 			e.g. ["radius", "5000"] would result in http://url.com?radius=5000
	 * 
	 * Builds a URL on top of a base URL (if any) using provided attributes
	 */
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
	
	/*
	 * Method: getHttpStream
	 * Parameters:
	 * 		String url: the URL to get a stream from - usually build by buildURL
	 * 
	 * Returns the resultant data from sending an HTTP request to the provided URL
	 */
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
			connection.disconnect();
			in.close();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		Log.v("TaxiLocator", "Got HTTP Stream: " + stream);

		return stream;
	}

}
