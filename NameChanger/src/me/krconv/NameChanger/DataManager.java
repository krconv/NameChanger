package me.krconv.NameChanger;

public final class DataManager {

	// Singleton Stuff
	private static class SingletonHolder { 
	     public static final DataManager INSTANCE = new DataManager();
	}
	public static DataManager getInstance() {
	     return SingletonHolder.INSTANCE;
	}
	// End Singleton Stuff
	
	
	
}
