package WCRV_Application;

import org.testng.annotations.Test;
import Data_Structures.User_Data;
import SupportClasses.Environment;
import SupportClasses.Helper_Functions;
import SupportClasses.WebDriver_Functions;

import org.testng.annotations.BeforeClass;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

@Listeners(SupportClasses.TestNG_TestListener.class)

public class WCRV_SmokeTest{
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
			User_Data User_Info[];
			switch (m.getName()) { //Based on the method that is being called the array list will be populated.
		    	case "WCRV_Generate_RateSheet":
		    		User_Info = User_Data.Get_UserIds(intLevel);
		    		for (int j = 0; j < CountryList.length; j++) {
		    			for (int k = 0; k < User_Info.length; k++) {
		    				if (User_Info[k].WCRV_ENABLED.contains("T") && User_Info[k].Address_Info.Country_Code.contains(CountryList[j][0]) && !User_Info[k].EMAIL_ADDRESS.contentEquals(Helper_Functions.MyEmail) && User_Info[k].PASSKEY.contains("F")) {
		    					data.add( new Object[] {Level, CountryList[j][0], User_Info[k].USER_ID, User_Info[k].PASSWORD, "INTRA_COUNTRY", 1});
		    					data.add( new Object[] {Level, CountryList[j][0], User_Info[k].USER_ID, User_Info[k].PASSWORD, "EXPORT", 1});
		    					data.add( new Object[] {Level, CountryList[j][0], User_Info[k].USER_ID, User_Info[k].PASSWORD, "IMPORT", 1});
		    					data.add( new Object[] {Level, CountryList[j][0], User_Info[k].USER_ID, User_Info[k].PASSWORD, "THIRD_PARTY", 1});
		    					break;
		    				}
		    			}
		    		}
		    	break;
		    	case "WCRV_Generate_RateSheet_Passkey":
		    		User_Info = User_Data.Get_UserIds(intLevel);
		    		for (int j = 0; j < CountryList.length; j++) {
		    			for (int k = 0; k < User_Info.length; k++) {
		    				if (User_Info[k].WCRV_ENABLED.contains("T") && User_Info[k].Address_Info.Country_Code.contains(CountryList[j][0]) && !User_Info[k].EMAIL_ADDRESS.contentEquals(Helper_Functions.MyEmail) && User_Info[k].PASSKEY.contains("T")) {
		    					data.add( new Object[] {Level, CountryList[j][0], User_Info[k].USER_ID, User_Info[k].PASSWORD, "INTRA_COUNTRY", 1});
		    					data.add( new Object[] {Level, CountryList[j][0], User_Info[k].USER_ID, User_Info[k].PASSWORD, "EXPORT", 1});
		    					data.add( new Object[] {Level, CountryList[j][0], User_Info[k].USER_ID, User_Info[k].PASSWORD, "IMPORT", 1});
		    					data.add( new Object[] {Level, CountryList[j][0], User_Info[k].USER_ID, User_Info[k].PASSWORD, "THIRD_PARTY", 1});
		    					break;
		    				}
		    			}
		    		}
		    	break;
		    	case "WCRV_Help_Link":
		    		User_Info = User_Data.Get_UserIds(intLevel);
		    		for (int j = 0; j < CountryList.length; j++) {
		    			for (int k = 0; k < User_Info.length; k++) {
		    				if (User_Info[k].WCRV_ENABLED.contains("T")) {
		    					data.add( new Object[] {Level, User_Info[k], CountryList[j][0]});
		    					break;
		    				}
		    			}
		    		}
		    	break;
		    		
			}
		}	
		return data.iterator();
	}

	@Test(dataProvider = "dp")
	public void WCRV_Generate_RateSheet(String Level, String CountryCode, String UserId, String Password, String Service, int ServiceCount) {
		try {
			String Result[] = WCRV_Functions.WCRV_Generate(CountryCode, UserId, Password, Service, ServiceCount);
			Helper_Functions.PrintOut(Arrays.toString(Result), false);
		}catch (Exception e) {
			Assert.fail(e.getMessage());
		}
	}
	
	@Test(dataProvider = "dp")
	public void WCRV_Generate_RateSheet_Passkey(String Level, String CountryCode, String UserId, String Password, String Service, int ServiceCount) {
		try {
			String Result[] = WCRV_Functions.WCRV_Generate(CountryCode, UserId, Password, Service, ServiceCount);
			Helper_Functions.PrintOut(Arrays.toString(Result), false);
		}catch (Exception e) {
			Assert.fail(e.getMessage());
		}
	}
	
	@Test(dataProvider = "dp")
	public void WCRV_Help_Link(String Level, User_Data User_Info, String CountryCode) {
		try {
			String CurrentCookie = WebDriver_Functions.GetCookieUUID();
			if (CurrentCookie == null || !User_Info.UUID_NBR.contains(CurrentCookie)) {
				WebDriver_Functions.Login(User_Info);
			}
			
			String Result = WCRV_Functions.WCRV_Check_Help_Links(CountryCode);
			Helper_Functions.PrintOut(Result);
		}catch (Exception e) {
			Assert.fail(e.getMessage());
		}
	}
	
}
