package me.krconv.NameChanger;

public class TestingDataManager {

	public static void main(String[] args) {
		// Test methods relating to auto alt logic and data access
		NameChanger nameChanger = new NameChanger(); // Test plugin
		
		AutoAltManager autoAltManager = new AutoAltManager();
		autoAltManager.initialize(new NameChanger());
		
		//System.out.println(autoAltManager.removeAltNameForPlayer("benjaiSaS"));
		
		//System.out.println(autoAltManager.setAltNameForPlayer("nip", "3512"));
		
		System.out.println(autoAltManager.getPlayerNameForAlt("3512"));
		System.out.println(autoAltManager.getAltNameForPlayer("NIP"));
		System.out.println(autoAltManager.doesPlayerHaveAlt("351*_SX;2"));

	}

}
