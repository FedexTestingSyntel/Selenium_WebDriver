package WDPA_Application;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import Data_Structures.User_Data;
import SupportClasses.Environment;
import SupportClasses.Helper_Functions;

@Listeners(SupportClasses.TestNG_TestListener.class)

public class WDPA_SmokeTest{
	static String LevelsToTest = "3";
	static String CountryList[][];

	@BeforeClass
	public void beforeClass() {
		Environment.SetLevelsToTest(LevelsToTest);
		CountryList = Environment.getCountryList("smoke");
	}
	
	@DataProvider (parallel = true)
	public static Iterator<Object[]> dp(Method m) {
		List<Object[]> data = new ArrayList<Object[]>();

		for (int i=0; i < Environment.LevelsToTest.length(); i++) {
			String Level = String.valueOf(Environment.LevelsToTest.charAt(i));
			int intLevel = Integer.parseInt(Level);

			switch (m.getName()) { //Based on the method that is being called the array list will be populated.
			case "WDPA_Ground":
	    		User_Data User_Info[] = User_Data.Get_UserIds(intLevel);
	    		for (int j = 0; j < CountryList.length; j++) {
	    			for (int k = 1; k < User_Info.length; k++) {
	    				if (User_Info[k].Address_Info.Country_Code.contentEquals(CountryList[j][0]) && User_Info[k].WDPA_ENABLED.contentEquals("T") && User_Info[k].GROUND_ENABLED.contentEquals("T")) {
	    					data.add( new Object[] {Level, CountryList[j][0], User_Info[k].USER_ID, User_Info[k].PASSWORD});
	    					break;
	    				}
	    			}
				}
	    		break;
	    	case "WDPA_Express":
	    	case "WDPA_ExpressFreight"://need to fix this later, not for all countries.
	    		User_Info = User_Data.Get_UserIds(intLevel);
	    		for (int j = 0; j < CountryList.length; j++) {
	    			for (int k = 1; k < User_Info.length; k++) {
	    				if (User_Info[k].Address_Info.Country_Code.contentEquals(CountryList[j][0]) && User_Info[k].WDPA_ENABLED.contentEquals("T") && User_Info[k].EXPRESS_ENABLED.contentEquals("T")) {
	    					data.add( new Object[] {Level, CountryList[j][0], User_Info[k].USER_ID, User_Info[k].PASSWORD});
	    					break;
	    				}
	    			}
				}
	    		break;
	    	case "WDPA_LTLFreight":
	    		User_Info = User_Data.Get_UserIds(intLevel);
	    		for (int j = 0; j < CountryList.length; j++) {
	    			for (int k = 0; k < User_Info.length; k++) {
	    				if (User_Info[k].Address_Info.Country_Code.contentEquals(CountryList[j][0]) && User_Info[k].WDPA_ENABLED.contentEquals("T") && User_Info[k].FREIGHT_ENABLED.contentEquals("T")) {
	    					data.add( new Object[] {Level, CountryList[j][0], User_Info[k].USER_ID, User_Info[k].PASSWORD});
	    					break;
	    				}
	    			}
				}
	    		break;
		    case "WDPA_LTLFreight_Anonymous":
		    	for (int j = 0; j < CountryList.length; j++) {
		    		if (CountryList[j][0].contentEquals("US") || CountryList[j][0].contentEquals("CA") || CountryList[j][0].contentEquals("MX")){
		    			data.add( new Object[] {Level, CountryList[j][0]});
		    		}	
				}
		    	break;
			}
		}
		return data.iterator();
	}
	
	@Test(dataProvider = "dp")
	public static void WDPA_Ground(String Level, String CountryCode, String UserID, String Password){
		try {
			String Address[] = Helper_Functions.LoadAddress(CountryCode);
			String ContactName = Helper_Functions.getRandomString(14);
			String Result = Arrays.toString(WDPA_Functions.WDPAPickupDetailed(CountryCode, UserID, Password, "ground", "CompanyNameHere", ContactName, "9011111111", Address, null, "INET"));
			Helper_Functions.PrintOut(Result, false);
		}catch (Exception e) {
			Assert.fail(e.getMessage());
		}
	}
	
	@Test(dataProvider = "dp")
	public static void WDPA_Express(String Level, String CountryCode, String UserID, String Password){
		try {
			String Address[] = Helper_Functions.LoadAddress(CountryCode);
			String ContactName = Helper_Functions.getRandomString(14);
			String Result = Arrays.toString(WDPA_Functions.WDPAPickupDetailed(CountryCode, UserID, Password, "express", "CompanyNameHere", ContactName, "9011111111", Address, null, "INET"));
			Helper_Functions.PrintOut(Result, false);
		}catch (Exception e) {
			Assert.fail(e.getMessage());
		}
	}
	
	@Test(dataProvider = "dp")
	public static void WDPA_ExpressFreight(String Level, String CountryCode, String UserID, String Password){
		try {
			String ContactName = Helper_Functions.getRandomString(14);
			String PackageDetails[] = {"1", "444", "L", "1400", "1800", "ExpLTL Attempt", "FedEx 1Day Freight", "ConfFiller", "side of barn", "5", "6", "7"};
			String Address[] = Helper_Functions.LoadAddress(CountryCode);
			String Result = Arrays.toString(WDPA_Functions.WDPAPickupDetailed("US", UserID, Password, "expFreight", "ExpressLTL Testing", ContactName, "9011111111", Address, PackageDetails, "INET"));
			Helper_Functions.PrintOut(Result, false);
		}catch (Exception e) {
			Assert.fail(e.getMessage());
		}
	}
	
	@Test(dataProvider = "dp")
	public static void WDPA_LTLFreight(String Level, String CountryCode, String UserID, String Password){
		try {
			String Address[] = Helper_Functions.LoadAddress(CountryCode);
			String ContactName = Helper_Functions.getRandomString(14);
			String Result = Arrays.toString(WDPA_Functions.WDPALTLPickup(Address, UserID, Password, "10", "400", ContactName));
			Helper_Functions.PrintOut(Result, false);
		}catch (Exception e) {
			Assert.fail(e.getMessage());
		}
	}
	
	@Test(dataProvider = "dp")
	public static void WDPA_LTLFreight_Anonymous(String Level, String CountryCode){
		try {
			String Address[] = Helper_Functions.LoadAddress(CountryCode);
			String ContactName = Helper_Functions.getRandomString(14);
			String Result = Arrays.toString(WDPA_Functions.WDPALTLPickup(Address, "", "", "10", "400", ContactName));
			Helper_Functions.PrintOut(Result, false);
		}catch (Exception e) {
			Assert.fail(e.getMessage());
		}
	}
}