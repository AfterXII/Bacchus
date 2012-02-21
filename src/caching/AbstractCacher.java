package caching;

/*
 * Class: AbstractCacher
 * 
 * A class which can be extended to persist and inflate various types from local memory
 */
public abstract class AbstractCacher<K> {
	
	protected String _fName;
	
	public AbstractCacher(String type) {
		_fName = "sdcard/" + type + ".ser";
	}
	
	public abstract void doPersist(K data);
	
	public abstract K readData();
	
}
