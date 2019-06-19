package Mission_Critical;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import org.testng.annotations.Test;
import Data_Structures.Account_Data;
import Data_Structures.User_Data;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.openqa.selenium.By;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import SupportClasses.Account_Lookup;
import SupportClasses.Environment;
import SupportClasses.Helper_Functions;
import SupportClasses.WebDriver_Functions;
import WFCL_Application.WFCL_Functions_UsingData;
import WIDM_Application.WIDM_SOAPClient;

@Listeners(SupportClasses.TestNG_TestListener.class)

public class MC_PI_4{
	static String LevelsToTest = "6";
	static String CountryList[][];
	final boolean EnableCompleted = false;
	
	@BeforeClass
	public void beforeClass() {
		Environment.SetLevelsToTest(LevelsToTest);
		CountryList = Environment.getCountryList("smoke");
		Helper_Functions.MyEmail = "accept@fedex.com";
	}
	
	@DataProvider (parallel = false)
	public static Iterator<Object[]> dp(Method m) {
		List<Object[]> data = new ArrayList<Object[]>();

		for (int i=0; i < Environment.LevelsToTest.length(); i++) {
			String Level = String.valueOf(Environment.LevelsToTest.charAt(i));
			int intLevel = Integer.parseInt(Level);
			String Rewards_APAC_AND_LAC[] = new String[] {"au", "cn", "hk", "jp", "my", "nz", "ph", "sg", "kr", "tw", "th", "br", "mx"};
			String Rewards_APAC_AND_LAC_Lang[][] = new String[][] {{"au", "en"}, {"cn", "en"}, {"cn", "zh"}, {"hk", "en"}, {"hk", "zh"}, {"jp", "en"}, {"jp", "ja"}, {"my", "en"}, {"nz", "en"}, {"ph", "en"}, {"sg", "en"}, {"kr", "en"}, {"kr", "ko"}, {"tw", "en"}, {"tw", "zh"}, {"th", "en"}, {"th", "th"}, {"mx", "en"}, {"br", "en"}, {"mx", "es"}, {"br", "pt"}};
			
			Rewards_APAC_AND_LAC = new String[] {"nz"};
			switch (m.getName()) { //Based on the method that is being called the array list will be populated.
				case "WFCL_Rewards_Registration_APAC_AND_LAC":
					if (intLevel == 6 && Rewards_APAC_AND_LAC.length > 3) {
						//load a list with all possible options and shuffle and take first three options.
						List<Integer> range = IntStream.range(0, Rewards_APAC_AND_LAC.length - 1).boxed().collect(Collectors.toCollection(ArrayList::new));
						Collections.shuffle(range);
						//due to captcha in L6 only doing three of the countries randomly. Will need to make sure to enter the captcha manually.
						Rewards_APAC_AND_LAC = new String[] {Rewards_APAC_AND_LAC[range.get(0)], Rewards_APAC_AND_LAC[range.get(1)], Rewards_APAC_AND_LAC[range.get(2)]};
					}
					//no breakpoint as expected
				case "WFCL_Rewards_Registration_APAC_AND_LAC_Existing_User":
		    		for (int j = 0; j < Rewards_APAC_AND_LAC.length; j++) {
		    			Account_Data Account_Info = Helper_Functions.getFreshAccount(Level, Rewards_APAC_AND_LAC[j]);
		    			if (Account_Info != null) {
		    				data.add( new Object[] {Level, Account_Info});
		    			}else {
		    				Helper_Functions.PrintOut("Account is not available for country. " + Rewards_APAC_AND_LAC[j]);
		    			}
					}
					break;
				case "WFCL_Rewards_Registration_APAC_AND_LAC_Mismatch":
		    		for (int j = 0; j < Rewards_APAC_AND_LAC.length; j++) {
		    			Account_Data Account_Info = Helper_Functions.getFreshAccount(Level, Rewards_APAC_AND_LAC[j]);
		    			Account_Data.Print_Account_Address(Account_Info);
		    			String DifferentCountry = "JP";
		    			if (DifferentCountry.contentEquals(Rewards_APAC_AND_LAC[j].toUpperCase())) {
		    				DifferentCountry = "AU";
		    			}
		    			Account_Data Wrong_Account_Info = Environment.getAddressDetails(Level, DifferentCountry);
		    			Account_Data.Print_Account_Address(Wrong_Account_Info);
		    			if (Account_Info != null && Wrong_Account_Info != null) {
		    				//need to add more validation here, possible for the address to be same.
		    				Account_Info.Address_Overwrite(Wrong_Account_Info);
		    				data.add( new Object[] {Level, Account_Info});
		    			}else {
		    				Helper_Functions.PrintOut("Account is not available for country. " + Rewards_APAC_AND_LAC[j]);
		    			}
					}
					break;
				case "WFCL_Rewards_Login_APAC_AND_LAC":
					User_Data User_Info[] = User_Data.Get_UserIds(intLevel);
		    		for (int k = 0; k < User_Info.length; k++) {
		    			if (User_Info[k].PASSKEY.contentEquals("T")) {
		    				for (int j = 0; j < Rewards_APAC_AND_LAC.length; j++) {
		    					data.add( new Object[] {Level, Rewards_APAC_AND_LAC[j], User_Info[k]});
							}
							break;
		    			}
					}
					break;
				case "WFCL_Rewards_AEM_Link":
				case "WFCL_Rewards_APAC_AND_LAC_Country_Field":	
					for (String Instance[]: Rewards_APAC_AND_LAC_Lang) {
		    			data.add( new Object[] {Level, Instance[0], Instance[1]});
					}
					break;
				case "WFCL_Rewards_Logout":
					User_Info = User_Data.Get_UserIds(intLevel);
					for (String Instance[]: Rewards_APAC_AND_LAC_Lang) {
						for (int k = 0; k < User_Info.length; k++) {
							if (User_Info[k].PASSKEY.contentEquals("T")) {
				    			data.add( new Object[] {Level, Instance[0], Instance[1], User_Info[k]});
				    			User_Info[k].PASSKEY = "";//so user will not be used a second time
				    			break;
							}
		    			}
					}
					break;
			}
		}

		System.out.println("Starting " + m.getName() + " : There are " + data.size() + " scenarios.");
		return data.iterator();
	}
	
	@Test(dataProvider = "dp", description = "518325", enabled = true) ///483863
	public void WFCL_Rewards_Registration_APAC_AND_LAC(String Level, Account_Data Account_Info) {
		try {
			//Account_Info = Account_Lookup.Account_Details(" 700378950", Level);
			Account_Data.Print_Account_Address(Account_Info);
			Account_Data.Set_UserId(Account_Info, "L" + Level + Account_Info.Billing_Address_Info.Country_Code + "Rewards");
			if (Account_Info.FirstName.contentEquals("")) {
				Account_Data.Set_Dummy_Contact_Name(Account_Info);
			}
			
			String Result[] = WFCL_Functions_UsingData.WFCL_RewardsRegistration(Account_Info);

			Helper_Functions.PrintOut(Arrays.toString(Result), false);
		}catch (Exception e) {
			Assert.fail(e.getMessage());
		}
	}
	
	//////////// Not completed
	@Test(dataProvider = "dp", description = "518325", enabled = true) ///483863
	public void WFCL_Rewards_Registration_APAC_AND_LAC_Existing_User(String Level, Account_Data Account_Info) {
		try {
			Account_Data.Print_Account_Address(Account_Info);
			Account_Data.Set_UserId(Account_Info, "L" + Level + Account_Info.Billing_Address_Info.Country_Code + "Rewards");
			Account_Data.Set_Dummy_Contact_Name(Account_Info);
			//Will create the user id.
			//WIDM_SOAPClient.AAAUserCreate(Level, Account_Info);
			Account_Info.User_Info.USER_ID = "L6JPRewards052019T105042tefq";
			
			String Result[] = WFCL_Functions_UsingData.WFCL_RewardsRegistration_Login(Account_Info);

			Helper_Functions.PrintOut(Arrays.toString(Result), false);
		}catch (Exception e) {
			Assert.fail(e.getMessage());
		}
	}
	
	//from the data provider incorrect address details provided
	@Test(dataProvider = "dp", description = "518325", enabled = false)
	public void WFCL_Rewards_Registration_APAC_AND_LAC_Mismatch(String Level, Account_Data Account_Info) {
		try {
			Account_Data.Print_Account_Address(Account_Info);
			Account_Data.Set_UserId(Account_Info, "L" + Level + Account_Info.Billing_Address_Info.Country_Code + "Rewards");
			Account_Data.Set_Dummy_Contact_Name(Account_Info);
			
			String Result[] = WFCL_Functions_UsingData.WFCL_RewardsRegistration(Account_Info);

			Helper_Functions.PrintOut(Arrays.toString(Result), false);
		}catch (Exception e) {
			Assert.fail(e.getMessage());
		}
	}
	
	@Test(dataProvider = "dp", description = "483861", enabled = false)
	public void WFCL_Rewards_Login_APAC_AND_LAC(String Level, String CountryCode, User_Data User_Info) {
		try {

			String Result[] = WFCL_Functions_UsingData.WFCL_RewardsLogin(CountryCode, User_Info);

			Helper_Functions.PrintOut(Arrays.toString(Result), false);
		}catch (Exception e) {
			Assert.fail(e.getMessage());
		}
	}
	
	@Test(dataProvider = "dp", description = "518318", enabled = EnableCompleted)
	public void WFCL_Rewards_AEM_Link(String Level, String CountryCode, String LanguageCode) {
		try {

			String Result[] = WFCL_Functions_UsingData.WFCL_Rewards_AEM_Link(CountryCode, LanguageCode);

			Helper_Functions.PrintOut(Arrays.toString(Result), false);
		}catch (Exception e) {
			Assert.fail(e.getMessage());
		}
	}
	
	@Test(dataProvider = "dp", description = "518584", enabled = EnableCompleted)
	public void WFCL_Rewards_Logout(String Level, String CountryCode, String LanguageCode, User_Data User_Info) {
		try {
			String Result[] = WFCL_Functions_UsingData.WFCL_Rewards_Logout(CountryCode, LanguageCode, User_Info);

			Helper_Functions.PrintOut(Arrays.toString(Result), false);
		}catch (Exception e) {
			Assert.fail(e.getMessage());
		}
	}
	
	@Test(dataProvider = "dp", description = "535433", enabled = EnableCompleted)
	public void WFCL_Rewards_APAC_AND_LAC_Country_Field(String Level, String CountryCode, String LanguageCode) {
		try {
			WebDriver_Functions.ChangeURL("WFCLREWARDS", CountryCode, LanguageCode, true);
 			
 			//click sign up now and begin registration
 			WebDriver_Functions.WaitPresent(By.name("signupnow"));
 			WebDriver_Functions.Click(By.name("signupnow"));

 			WebDriver_Functions.WaitPresent(By.id("country"));
 			String Value = WebDriver_Functions.GetValue(By.id("country"));
 			Assert.assertEquals(Value.toLowerCase(), CountryCode.toLowerCase(), "Country " + CountryCode);
 			boolean disabled = WebDriver_Functions.isEnabled(By.id("country"));
 			Assert.assertFalse(disabled);
 			WebDriver_Functions.takeSnapShot(CountryCode + "_" + LanguageCode + "_CountryList.png");
			Helper_Functions.PrintOut(CountryCode + "_" + LanguageCode + ": Country is selected and not editible.", false);
		}catch (Exception e) {
			Assert.fail(e.getMessage());
		}
	}
}