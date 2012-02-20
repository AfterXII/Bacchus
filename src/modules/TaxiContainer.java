package modules;

import java.util.HashMap;
import java.util.Map;
import android.os.Parcel;
import android.os.Parcelable;

/*
 * Class: TaxiContainer
 * 
 * A parcelable implementation of a HashMap, used for storing name=>data pairs of nearby taxi services,
 * where data is usually an address and phone number. Used to pass data across threads, mainly back to UI threads
 * from a background service.
 */
public class TaxiContainer extends HashMap<String, String> implements Parcelable {
	
	private static final long serialVersionUID = 8792804276733547302L;
	
	public TaxiContainer() {}

	/*
	 * Constructor
	 * Parameters:
	 * 		Parcel pc: the parcel we are creating our structure from
	 * 
	 * Pulls data from a parcel object into our HashMap to re-create the structure
	 */
	public TaxiContainer(Parcel pc) {
		int size = pc.readInt();
		
		// Items are stored sequentially in the Parcel, so read them in order back into a HashMap
		//		e.g. [key, value, key, value, ... ]
		for(int i = 0; i < size; i++) {
			String key = pc.readString();
			String value = pc.readString();
			this.put(key, value);
		}
	}
	
	/*
	 * Method: writeToParcel
	 * Parameters:
	 * 		Parcel dest: the Parcel we are writing the HashMap's data to
	 * 		int flags: not used.
	 * 
	 * Write our key-value pairs to a Parcel so data can be passed across threads
	 * Items are stored sequentially in the Parcel, so write them in order to it
	 * 		e.g. [key, value, key, value, ... ]
	 */
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(this.size());
		for(Map.Entry<String, String> e : this.entrySet()) {
			dest.writeString(e.getKey());
			dest.writeString(e.getValue());
		}
	}

	public static final Creator<TaxiContainer> CREATOR = new Creator<TaxiContainer>() {
		public TaxiContainer createFromParcel(Parcel source) {
			return new TaxiContainer(source);
		}
		public TaxiContainer[] newArray(int size) {
			return new TaxiContainer[size];
		}
	};

	@Override
	public int describeContents() {
		return 0;
	}

}
