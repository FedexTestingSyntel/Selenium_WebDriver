package WFCL_Application;

import org.testng.annotations.Test;

import Data_Structures.User_Data;

import org.testng.annotations.BeforeClass;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import SupportClasses.*;

@Listeners(SupportClasses.TestNG_TestListener.class)

public class WFCL{
	static String LevelsToTest = "7";
	static String CountryList[][];

	@BeforeClass
	public void beforeClass() {
		Environment.SetLevelsToTest(LevelsToTest);
		CountryList = Environment.getCountryList("smoke");
		
		Environment.getInstance().setLevel("3");
		Helper_Functions.AccountDetails("641179302");

		//CountryList = Environment.getCountryList("BR");
		//CountryList = Environment.getCountryList("full");
		//need to fix this and make dynamic;
		//CountryList = new String[][]{{"US", "United States"}, {"CA", "Canada"}};
		//CountryList = new String[][]{{"CA", "Canada"}};
		//CountryList = new String[][]{{"US", "United States"}};
		//CountryList = new String[][]{{"JP", "Japan"}};
		//CountryList = new String[][]{{"BR", "Brazil"}};
		//CountryList = new String[][]{{"GB", "Great Brittan"}};
		//CountryList = new String[][]{{"SI", "Slovenia"}, {"RO", "Romania"}};
		//CountryList = new String[][]{{"JP", "Japan"}, {"MY", "Malaysia"}, {"SG", "Singapore"}, {"AU", "Australia"}, {"NZ", "New Zealand"}, {"HK", "Hong Kong"}, {"TW", "Taiwan"}, {"TH", "Thailand"}};
		//CountryList = new String[][]{{"AU", "Australia"}, {"NZ", "New Zealand"}, {"HK", "Hong Kong"}, {"TH", "Thailand"}};
	}
	
	@DataProvider (parallel = true)
	public static Iterator<Object[]> dp(Method m) {
		List<Object[]> data = new ArrayList<Object[]>();

		for (int i=0; i < Environment.LevelsToTest.length(); i++) {
			String Level = String.valueOf(Environment.LevelsToTest.charAt(i)), Account = null;
			int intLevel = Integer.parseInt(Level);
			//int intLevel = Integer.parseInt(Level);
			
			switch (m.getName()) { //Based on the method that is being called the array list will be populated.
		    	case "Address_Mismatch":
		    		if (Level == "7") {
		    			break;
		    		}
		    		Account = Helper_Functions.getExcelFreshAccount(Level, "us", true);
		    		data.add( new Object[] {Level, "US", Account, ""});
		    		data.add( new Object[] {Level, "US", Account, "Nickname"});
		    		break;
		    	case "AccountRegistration_GFBO":
		    		for (int j = 0; j < CountryList.length; j++) {
		    			Account = Helper_Functions.getExcelFreshAccount(Level, CountryList[j][0], true);
			    		data.add( new Object[] {Level, CountryList[j][0], Account});
					}
		    		break;
		    	case "AccountRegistration_Linkage":
		    		for (int j = 0; j < CountryList.length; j++) {
		    			Account = Helper_Functions.getExcelFreshAccount(Level, CountryList[j][0], true);
			    		data.add( new Object[] {Level, CountryList[j][0], Account, "Nickname"});
			    		data.add( new Object[] {Level, CountryList[j][0], Account, ""});
					}
		    		break;
		    	case "WADM_Invitaiton":
		    		User_Data User_Info[] = User_Data.Get_UserIds(intLevel);
		    		for (int k = 0; k < User_Info.length; k++) {
	    				if (User_Info[k].USER_ID.contains("CC")) {
	    					data.add( new Object[] {Level, User_Info[k].USER_ID, User_Info[k].PASSWORD, Helper_Functions.MyEmail});
	    					break;
	    				}
	    			}
		    		break;
			}
		}	
		return data.iterator();
	}

	@Test(dataProvider = "dp", enabled = false)
	public void WADM_Invitaiton(String Level, String AdminUser, String Password, String Email){     //check on this, make sure that testing correct
		try {
			String ContactName[] = Helper_Functions.LoadDummyName("WADMInvite", Level);
			String Result[] = WFCL_Functions.WFCL_WADM_Invitaiton(AdminUser, Password, ContactName, Email);
			Helper_Functions.PrintOut(Arrays.toString(Result), false);
		}catch (Exception e) {
			Assert.fail(e.getMessage());
		}
	};
	  
	@Test(dataProvider = "dp", enabled = false)
	public void AccountRegistration_GFBO(String Level, String CountryCode, String Account){     //check on this, make sure that testing correct
		try {
			String AddressDetails[] = Helper_Functions.AccountDetails(Account);
			String ContactName[] = Helper_Functions.LoadDummyName(CountryCode, Level);
			String UserID = Helper_Functions.LoadUserID("L" + Level + "GFBO" + CountryCode);
			
			String Result[] = WFCL_Functions.WFCL_AccountRegistration_INET(ContactName, UserID, Helper_Functions.MyEmail, Account, AddressDetails);
			Result = Arrays.copyOf(Result, Result.length + 1);
			Result[Result.length - 1] = "GFBO:" + WFCL_Functions.WFCL_AccountRegistration_GFBO(ContactName, UserID, Helper_Functions.MyEmail, Account, AddressDetails);
			Helper_Functions.PrintOut(Arrays.toString(Result), false);
		}catch (Exception e) {
			Assert.fail(e.getMessage());
		}
	};
	
	@Test(dataProvider = "dp", priority = 3, enabled = true)//since this method will consume an acocunt number run after others have completed
	public void Address_Mismatch(String Level, String CountryCode, String FreshAccount, String Nickname) {
		try {
			String UserName[] = Helper_Functions.LoadDummyName("INET", Level);
			String UserID = Helper_Functions.LoadUserID("L" + Level + FreshAccount + CountryCode + Nickname);
			String AddressDetails[] = Helper_Functions.AccountDetails(FreshAccount);
			String AddressMismatch[] = new String[AddressDetails.length];
			System.arraycopy( AddressDetails, 0, AddressMismatch, 0, AddressDetails.length );
			//update the address with different data
			AddressMismatch[2] = "MEMPHIS";
			AddressMismatch[4] = "TN";
			AddressMismatch[5] = "38119";
			String Result = WFCL_Functions.WFCL_AdminReg_WithMismatch(UserName, UserID, Helper_Functions.MyEmail, FreshAccount, AddressDetails, AddressMismatch, Nickname);
			Helper_Functions.PrintOut(Result, false);
		}catch (Exception e) {
			Assert.fail(e.getMessage());
		}
	}
	
	@Test(dataProvider = "dp", priority = 1)
	public void AccountRegistration_Linkage(String Level, String CountryCode, String Account, String AccountNickname){
		try {
			//Account = "762983940"; //to manually force account number
			//Helper_Functions.MyEmail = "suguna.kumaraswami.osv@fedex.com";
			String AddressDetails[] = Helper_Functions.AccountDetails(Account);
			String ContactName[] = Helper_Functions.LoadDummyName(CountryCode, Level);
			String UserID = Helper_Functions.LoadUserID("L" + Level + "Link" + CountryCode);
			String Result = WFCL_Functions.WFCL_AccountLinkage(ContactName, UserID, Helper_Functions.MyEmail, Account, AddressDetails, AccountNickname);
			Helper_Functions.PrintOut(Result, false);
		}catch (Exception e) {
			Assert.fail(e.getMessage());
		}
	}
	
	
	
	  /*

	
	//Non smoke test scenarios

	//@Test
	public void WFCL_AccountRegistration_Rewards(){
		if (Environment == 7) { //Set to not work in production due to limited test data
			Assert.fail();
		}
		
		String Results[] = null;
		try {
			String LocAccountNumber = NonAdminAccounts[0];//us account
		String AccountAddress[] = Helper_Functions.AccountDetails(LocAccountNumber);
			Results = WFCL_RewardsRegistration(Helper_Functions.LoadDummyName(AccountAddress[6]), Helper_Functions.LoadUserID(strLevel + "Rewards"), LocAccountNumber, AccountAddress);
		}catch (Exception e) {
			Assert.fail();
		}
		PassOrFail = true;
		ResultsList.set(TestNumber, UpdateArrayList(ResultsList.get(TestNumber), 1, Arrays.toString(Results)));
	}
	
	
	
	//@Test
	public void WFCL_GFBO_Login(){
		try {
			WFCL_GFBO_Login(strGFBO, strPassword, "US");
		}catch (Exception e) {
			Assert.fail();
		}
		PassOrFail = true;
		ResultsList.set(TestNumber, UpdateArrayList(ResultsList.get(TestNumber), 1, strGFBO));
	};	
	
	//@Test
	public void WFCL_AccountRegistration_ISGT(){     //NEED to test
		String LocAccountNumber = NonAdminAccounts[0];//us account
		String Results[] = null;
		try {
			String Address[] = null;
			
			if (Environment == 7) {
				Address = Helper_Functions.LoadAddress("US", "TN", "10 FedEx Parkway");
			}else {
				Address = Helper_Functions.AccountDetails(LocAccountNumber);
			}
			
			Results = WFCL_AccountRegistration_ISGT(Helper_Functions.LoadDummyName(Address[6]), Helper_Functions.LoadUserID(strLevel + "ISGT"), LocAccountNumber, Address);
		}catch (Exception e) {
			Assert.fail();
		}
		PassOrFail = true;
		ResultsList.set(TestNumber, UpdateArrayList(ResultsList.get(TestNumber), 1, Arrays.toString(Results)));
	}

	//@Test
	public void WFCL_WADM_Invitaiton(){     //check on this, make sure that testing correct
		String Results[] = null;
		try {
			Results = WFCL_WADM_Invitaiton(strDummyWADMAdmin, strPassword, Helper_Functions.LoadDummyName("FBO"), strEmail);
		}catch (Exception e) {
			Assert.fail();
		}
		PassOrFail = true;
		ResultsList.set(TestNumber, UpdateArrayList(ResultsList.get(TestNumber), 1, Arrays.toString(Results)));
	};
	
	//@Test
	public void WFCL_GFBO_Invitaiton(){     //check on this, not sure how to get this working, GFBO does not reference elements and ids correctly
		String Results[] = null;
		try {
			Results = WFCL_GFBO_Invitaiton(strGFBO, strPassword, Helper_Functions.LoadDummyName("FBO"), strEmail, "2");
		}catch (Exception e) {
			Assert.fail();
		}
		PassOrFail = true;
		ResultsList.set(TestNumber, UpdateArrayList(ResultsList.get(TestNumber), 1, Arrays.toString(Results)));
	};	
	
	//@Test
	public void WFCL_CreditCardRegistration_US_ErrorMessage() {   //PPM#45905- Change Verbiage of CC Failure in OADR
		for (int i = 0; i < EnrollmentIDs.length; i++){
			if (EnrollmentIDs[i][1].contentEquals("US")){
				String EnrollmentId = EnrollmentIDs[i][0];
				try {
					String ErrorMessage = "FedEx Online Account Registration is not able to process your request at this time. Please call 1.800.463.3339 and ask for \"new account setup\" to connect with a new account customer service representative."; 
					WFCL_CreditCardRegistration_Error(Helper_Functions.LoadCreditCard("I"), EnrollmentId, Helper_Functions.LoadAddress("US"), Helper_Functions.LoadDummyName(), Helper_Functions.LoadUserID(strLevel), ErrorMessage);
				} catch (Exception e) {
					Assert.fail();
				}
				PassOrFail = true;
				ResultsList.set(TestNumber, UpdateArrayList(ResultsList.get(TestNumber), 1, "Correct"));
				break;
			}
		}
	}
	
	//@Test
	public void WFCL_UserRegistration_Captcha() {
		
		boolean Results = false;
		try {
			Results = WFCL_Captcha(strTodaysDate, Helper_Functions.LoadDummyName(), Helper_Functions.LoadAddress("US"));
		} catch (Exception e) {
			Assert.fail();
		}
		PassOrFail = true;
		ResultsList.set(TestNumber, UpdateArrayList(ResultsList.get(TestNumber), 1, Results + ""));
	}
	
	//@Test
	public void WFCL_UserRegistration_Captcha_Legacy() {
		boolean Results = false;
		try {
			Results = WFCL_Captcha_Legacy(strTodaysDate, Helper_Functions.LoadDummyName(), Helper_Functions.LoadAddress("US"));
		} catch (Exception e) {
			Assert.fail();
		}
		PassOrFail = true;
		ResultsList.set(TestNumber, UpdateArrayList(ResultsList.get(TestNumber), 1, Results + ""));
	}

	 */
	
    
}
