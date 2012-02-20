package caching;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import android.util.Log;

/*
 * Class: ObjectCacher
 * Generics:
 * 		K: the type that we are persisting to local memory
 * 
 * Allows us to store serializable objects to local memory and inflate them later
 */
public class ObjectCacher<K> extends AbstractCacher<K> {

	public ObjectCacher(String type) {
		super(type);
	}

	/*
	 * Method: doPersist
	 * Parameters:
	 * 		K data: the object to be written to local memory
	 * 
	 * Writes a serializable object of type K to local memory
	 */
	@Override
	public void doPersist(K data) {
		FileOutputStream fos = null;
		ObjectOutputStream out = null;
		try {
			fos = new FileOutputStream(_fName);
			out = new ObjectOutputStream(fos);
			out.writeObject(data);
			out.close();
			Log.v("DataCacher", "Persisted data to: " + _fName);
		} catch(IOException e) {
			Log.e("DataCacher", "IOException while writing: " + e);
		}
	}

	/*
	 * Method: readData
	 * 
	 * Reads an object of type K from local memory and returns it
	 */
	@SuppressWarnings("unchecked")
	@Override
	public K readData() {
		K obj = null;

		FileInputStream fis = null;
		ObjectInputStream in = null;
		try {
			fis = new FileInputStream(_fName);
			in = new ObjectInputStream(fis);
			obj = (K) in.readObject();
			in.close();
			Log.v("DataCacher", "Read data from: " + _fName);
		} catch(IOException e) {
			Log.e("DataCacher", "IOException while reading: " + e);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		return obj;
	}

}
