package constants;

public class Constants {
	
	/*
	 * API-related constants: keys, etc.
	 */
	public class API {
		public static final String PLACES_API_KEY = "AIzaSyBdXAPJl6qkgF1BMAL9NPOszpG16P1E8vQ";
	}
	
	/*
	 * Caching constants, usually relative filenames
	 */
	public class Caching {
		public static final String LOCATION = "location";
		public static final String TAXI_SERVICES = "taxis";
	}
	
	/*
	 * Strings representing what kind of message is being passed
	 */
	public class Handlers {
		public static final String JSON_STREAM = "NewJsonStream";
	}
	
	/*
	 * Integers, doubles, etc.
	 */
	public class Numbers {
		public static final double LOCATION_TOLERANCE = 5;
	}

}
