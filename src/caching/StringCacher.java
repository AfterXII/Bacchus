package caching;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import android.util.Log;

/*
 * Class: StringCacher
 * 
 * A strict implementation of AbstractCacher, which allows us to persist String objects to local memory
 */
public class StringCacher extends AbstractCacher<String> {

	public StringCacher(String type) {
		super(type);
	}

	/*
	 * Method: doPersist
	 * Parameters:
	 * 		String data: the string to be written to local memory
	 * 
	 * Writes a string to local memory
	 */
	@Override
	public void doPersist(String data) {
		FileOutputStream fos = null;
		BufferedOutputStream out = null;
		try {
			fos = new FileOutputStream(_fName);
			out = new BufferedOutputStream(fos);
			byte[] bytes = data.getBytes();
			out.write(bytes);
			out.close();
			Log.v("DataCacher", "Persisted line to : " + _fName);
		} catch(IOException e) {
			Log.e("DataCacher", "IOException while writing line: " + e);
		}
	}

	/*
	 * Method: readData
	 * 
	 * Reads an string from local memory and returns it
	 */
	@Override
	public String readData() {
		Log.v("DataCacher", "Retrieving line from file: " + _fName);
		
		String line = null;
		FileInputStream fis = null;
		BufferedReader in = null;
		try {
			fis = new FileInputStream(_fName);
			in = new BufferedReader(new InputStreamReader(fis));
			line = in.readLine();
			in.close();
			Log.v("DataCacher", "Read in line: " + line);
		} catch(IOException e) {
			Log.e("DataCacher", "IOException while reading line: " + e);
		}
		
		return line;
	}

}
