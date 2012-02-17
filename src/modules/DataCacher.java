package modules;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import android.util.Log;

public class DataCacher {
	
	public static final String LOCATION = "location";
	public static final String TAXI_SERVICES = "taxis";
	
	private String _type;
	
	public DataCacher(String type) {
		_type = type;
	}
	
	public void doPersist(Serializable data) {
		String fName = "sdcard/" + _type + ".ser";
		
		FileOutputStream fos = null;
		ObjectOutputStream out = null;
		try {
			fos = new FileOutputStream(fName);
			out = new ObjectOutputStream(fos);
			out.writeObject(data);
			out.close();
			Log.v("DataCacher", "Persisted data to: " + fName);
		} catch(IOException e) {
			Log.e("DataCacher", "IOException while writing: " + e);
		}
	}
	
	public Serializable getObject() {
		String fName = "sdcard/" + _type + ".ser";
		
		Serializable obj = null;
		FileInputStream fis = null;
		ObjectInputStream in = null;
		try {
			fis = new FileInputStream(fName);
			in = new ObjectInputStream(fis);
			obj = (Serializable) in.readObject();
			in.close();
			Log.v("DataCacher", "Read data from: " + fName);
		} catch(IOException e) {
			Log.e("DataCacher", "IOException while reading: " + e);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		return obj;
	}
	
	public void writeLine(String line) {
		String fName = "sdcard/" + _type + ".ser";
		
		FileOutputStream fos = null;
		BufferedOutputStream out = null;
		try {
			fos = new FileOutputStream(fName);
			out = new BufferedOutputStream(fos);
			byte[] bytes = line.getBytes();
			out.write(bytes);
			out.close();
			Log.v("DataCacher", "Persisted line to : " + fName);
		} catch(IOException e) {
			Log.e("DataCacher", "IOException while writing line: " + e);
		}
	}
	
	public String getLine() {
		String fName = "sdcard/" + _type + ".ser";
		
		Log.v("DataCacher", "Retrieving line from file: " + fName);
		
		String line = null;
		FileInputStream fis = null;
		BufferedReader in = null;
		try {
			fis = new FileInputStream(fName);
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
