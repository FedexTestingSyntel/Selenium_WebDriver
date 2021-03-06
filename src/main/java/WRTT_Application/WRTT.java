package WRTT_Application;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import SupportClasses.Environment;
import SupportClasses.Helper_Functions;

@Listeners(SupportClasses.TestNG_TestListener.class)

public class WRTT{
	static String LevelsToTest = "2";
	static String CountryList[][];
	
	@BeforeClass
	public void beforeClass() {
		Environment.SetLevelsToTest(LevelsToTest);
		CountryList = Environment.getCountryList("US");
		//CountryList = Environment.getCountryList("FULL");
	}
	
	@DataProvider (parallel = true)
	public static Iterator<Object[]> dp(Method m) {
		List<Object[]> data = new ArrayList<Object[]>();

		for (int i=0; i < Environment.LevelsToTest.length(); i++) {
			String Level = String.valueOf(Environment.LevelsToTest.charAt(i));
			//int intLevel = Integer.parseInt(Level);

			switch (m.getName()) { //Based on the method that is being called the array list will be populated.
		    	case "WRTT_Rate_Sheet":
		    		for (int j = 0; j < 18; j++) {
		    			int n = 3; //three is the numbers of booleans involved
		    			for (int k = 0; k < Math.pow(2, n); k++) {
		    				String bin = Integer.toBinaryString(k);
		    				while (bin.length() < n) {
		    					bin = "0" + bin;
		    				}
		    				char[] chars = bin.toCharArray();
		    				boolean[] boolArray = new boolean[n];
		    				for (int l = 0; l < chars.length; l++) {
		    					boolArray[l] = chars[l] == '0' ? true : false;
		    				}
		    				if (boolArray[0] == true && boolArray[1] == false) {//all listed as zone chart true
		    					data.add( new Object[] {Level, j, boolArray[0], boolArray[1], boolArray[2]});
		    				}
			    			data.add( new Object[] {Level, j, boolArray[0], boolArray[1], boolArray[2]});
			    		}
		    		}
		    		break;
		    	case "WRTT_SpalshPage_eCRV":
		    	case "WRTT_eCRV_WRTTLink":
		    		//data.add( new Object[] {Level, "US"});
		    		for (int j=0; j < CountryList.length; j++) {				
						data.add( new Object[] {Level, CountryList[j][0]});
					}
		    	break;
			}
		}	
		return data.iterator();
	}
	
	@Test(dataProvider = "dp", enabled = true)
	public static void WRTT_Rate_Sheet(String Level, int Service, boolean ZoneChart, boolean PDF, boolean List){
		Helper_Functions.PrintOut("Validate the rate sheet download through WRTT", false);
		try {
			String Result = WRTT_Functions.WRTT_Generate(Service, ZoneChart, PDF, List);
			Helper_Functions.PrintOut(Result, false);
		}catch (Exception e) {
			Assert.fail(e.getMessage());
		}
	}//end WRTT_Rate_Sheet
	
	@Test(dataProvider = "dp", enabled = true)
	public static void WRTT_eCRV_WRTTLink(String Level, String CountryCode){
		Helper_Functions.PrintOut("Validate the eCRV page for " + CountryCode, false);
		try {
			String Result = WRTT_Functions.WRTT_eCRV_WRTTLink(CountryCode);
			Helper_Functions.PrintOut(Result, false);
		}catch (Exception e) {
			Assert.fail(e.getMessage());
		}
	}//end WRTT_eCRV
	
	@Test(dataProvider = "dp", enabled = true)
	public static void WRTT_SpalshPage_eCRV(String Level, String CountryCode){
		Helper_Functions.PrintOut("Validate the RateSheets link is present in WGRT page for " + CountryCode, false);
		try {
			String Result = WRTT_Functions.eCRVNavigation(CountryCode);
			Helper_Functions.PrintOut(Result, false);
		}catch (Exception e) {
			Assert.fail(e.getMessage());
		}
	}//end WRTT_eCRV
 }