package modules;

import java.util.HashMap;
import java.util.Map;
import android.os.Parcel;
import android.os.Parcelable;

public class TaxiContainer<K, V> extends HashMap<String, String> implements Parcelable {

	private static final long serialVersionUID = 8792804276733547302L;
	
	public TaxiContainer() {}

	public TaxiContainer(Parcel pc) {
		int size = pc.readInt();
		
		for(int i = 0; i < size; i++) {
			String key = pc.readString();
			String value = pc.readString();
			this.put(key, value);
		}
	}

	public static final Creator<TaxiContainer<String, String>> CREATOR = new Creator<TaxiContainer<String, String>>() {
		public TaxiContainer<String, String> createFromParcel(Parcel source) {
			return new TaxiContainer<String, String>(source);
		}
		public TaxiContainer<String, String>[] newArray(int size) {
			return new TaxiContainer[size];
		}
	};

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(this.size());
		for(Map.Entry<String, String> e : this.entrySet()) {
			dest.writeString(e.getKey());
			dest.writeString(e.getValue());
		}
	}

}
