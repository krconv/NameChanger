package me.krconv.NameChanger;

public class TestingDataManager {

	public static void main(String[] args) {
		// Test methods relating to auto alt logic and data access
		NameChanger nameChanger = new NameChanger(); // Test plugin
		
		AutoAltManager autoAltManager = new AutoAltManager();
		autoAltManager.initialize(new NameChanger());
		
		//autoAltManager.da.testWriteHashMap();
		System.out.println("-----------");
		//autoAltManager.da.testWriteHashMap2();
		System.out.println("-----------");
		
		System.out.println(autoAltManager.removeAltNameForPlayer("MIp"));
		System.out.println("-----------");
		
		System.out.println(autoAltManager.setAltNameForPlayer("55EliteSandwich2", "55appyFla2nger"));
		System.out.println("-----------");
		
		//autoAltManager.da.testWriteHashMap();
		System.out.println("-----------");
		//autoAltManager.da.testWriteHashMap2();
		System.out.println("-----------");
		
		autoAltManager.setAltNameForPlayer("EliteSandwich5", "appyFlanger");
		
	
		//System.out.println(autoAltManager.removeAltNameForPlayer("benjaiSaS"));
		
		//System.out.println(autoAltManager.setAltNameForPlayer("nip", "3512"));
		
		//System.out.println(autoAltManager.getPlayerNameForAlt("3512"));
		System.out.println(autoAltManager.getAltNameForPlayer("55EliteSandwich2"));
		System.out.println(autoAltManager.getPlayerNameForAlt("55appyFla2nger"));
		//System.out.println(autoAltManager.getPlayerNameForAlt("nip"));
		//System.out.println(autoAltManager.doesPlayerHaveAlt("nip"));
		
		

	}

}
