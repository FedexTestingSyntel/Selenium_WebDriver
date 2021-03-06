package WPRL_Application;

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

public class WPRL { 

	static String LevelsToTest = "3";
	static String CountryList[][];

	@BeforeClass
	public void beforeClass() {
		Environment.SetLevelsToTest(LevelsToTest);
		Helper_Functions.MyEmail = Helper_Functions.MyFakeEmail;
		CountryList = Environment.getCountryList("smoke");
		//CountryList = new String[][]{{"US", "United States"}};
	}
	
	@DataProvider (parallel = true)
	public static Iterator<Object[]> dp(Method m) {
		List<Object[]> data = new ArrayList<Object[]>();

		for (int i=0; i < Environment.LevelsToTest.length(); i++) {
			String Level = String.valueOf(Environment.LevelsToTest.charAt(i));
			int intLevel = Integer.parseInt(Level);
			//Based on the method that is being called the array list will be populated.
			switch (m.getName()) { 
		    	case "WPRL_Contact_Admin":
		    	case "WPRL_AccountManagement_Passkey":
		    		User_Data User_Info[] = User_Data.Get_UserIds(intLevel);
		    		for (int j = 0; j < CountryList.length; j++) {
		    			for (int k = 0; k < User_Info.length; k++) {
		    				if (User_Info[k].USER_ID.contains("WADM")) {
		    					data.add( new Object[] {Level, User_Info[k].USER_ID, User_Info[k].PASSWORD, CountryList[j][0], Helper_Functions.MyEmail});
		    					break;
		    				}
		    			}
					}
		    	break;
		    	case "WPRL_FDM":
		    		User_Info = User_Data.Get_UserIds(intLevel);
		    		for (int j = 0; j < CountryList.length; j++) {
		    			for (int k = 0; k < User_Info.length; k++) {
		    				if (User_Info[k].USER_ID.contains("FDM")) {
		    					data.add( new Object[] {Level, User_Info[k].USER_ID, User_Info[k].PASSWORD, CountryList[j][0], Helper_Functions.MyEmail});
		    					break;
		    				}
		    			}
					}
		    	break;
		    	case "WPRL_AccountManagement_NonPasskey":
		    		User_Info = User_Data.Get_UserIds(intLevel);
		    		for (int j = 0; j < CountryList.length; j++) {
		    			for (int k = 0; k < User_Info.length; k++) {
		    				if (User_Info[k].USER_ID.contains("INET")) {
		    					data.add( new Object[] {Level, User_Info[k].USER_ID, User_Info[k].PASSWORD, CountryList[j][0], Helper_Functions.MyEmail});
		    					break;
		    				}
		    			}
					}
		    		break;
		    	case "WPRL_Contact_NonAdmin":
		    		User_Info = User_Data.Get_UserIds(intLevel);
		    		for (int j = 0; j < CountryList.length; j++) {
		    			for (int k = 0; k < User_Info.length; k++) {
		    				if (User_Info[k].USER_ID.contains("Create")) {
		    					data.add( new Object[] {Level, User_Info[k].USER_ID, User_Info[k].PASSWORD, CountryList[j][0], Helper_Functions.MyEmail});
		    					break;
		    				}
		    			}
					}
		    	break;
		    	case "WPRL_AccountManagement_BillingInvoice":
		    			///need to update on this, needs specific data
		    	break;
		    	
		    	case "WPRL_FDM_Enroll":
	    			for (int j = 22; j < 29; j++) {
	    				data.add( new Object[] {Level, j});
	    				//break;
	    			}
		    		break;
			}
		}	
		return data.iterator();
	}

	@Test(dataProvider = "dp")
	public void WPRL_Contact_Admin(String Level, String UserID, String Password, String CountryCode, String Email) {
		try {
			String Address[] = Helper_Functions.LoadAddress(CountryCode);
			String ContactName[] = Helper_Functions.LoadDummyName(CountryCode, Level);
			String ContactDetails[][] = Helper_Functions.LoadPhone_Mobile_Fax_Email(CountryCode);
			String Result[] =  WPRL_Functions.WPRL_Contact(UserID, Password, Address, ContactName, ContactDetails, Email);
			Helper_Functions.PrintOut(Arrays.toString(Result), false);
		}catch (Exception e) {
			Assert.fail(e.getMessage());
		}
	}
	
	@Test(dataProvider = "dp")
	public void WPRL_Contact_NonAdmin(String Level, String UserID, String Password, String CountryCode, String Email) {
		try {
			String Address[] = Helper_Functions.LoadAddress(CountryCode);
			String ContactName[] = Helper_Functions.LoadDummyName(CountryCode, Level);
			String ContactDetails[][] = Helper_Functions.LoadPhone_Mobile_Fax_Email(CountryCode);
			String Result[] =  WPRL_Functions.WPRL_Contact(UserID, Password, Address, ContactName, ContactDetails, Email);
			Helper_Functions.PrintOut(Arrays.toString(Result), false);
		}catch (Exception e) {
			Assert.fail(e.getMessage());
		}
	}
	
	@Test(dataProvider = "dp")
	public void WPRL_AccountManagement_Passkey(String Level, String UserID, String Password, String CountryCode, String Email) {
		try {
			String Address[] = Helper_Functions.LoadAddress(CountryCode);
			String CreditCard[] = Helper_Functions.LoadCreditCard("V");
			String ContactName[] = Helper_Functions.LoadDummyName(CountryCode, Level);
			String Result[] =  WPRL_Functions.WPRL_AccountManagement(UserID, Password, Address, CreditCard, ContactName);
			Helper_Functions.PrintOut(Arrays.toString(Result), false);
		}catch (Exception e) {
			Assert.fail(e.getMessage());
		}
	}
	
	@Test(dataProvider = "dp")
	public void WPRL_AccountManagement_NonPasskey(String Level, String UserID, String Password, String CountryCode, String Email) {
		try {
			String Address[] = Helper_Functions.LoadAddress(CountryCode);
			String CreditCard[] = Helper_Functions.LoadCreditCard("V");
			String ContactName[] = Helper_Functions.LoadDummyName(CountryCode, Level);
			String Result[] =  WPRL_Functions.WPRL_AccountManagement(UserID, Password, Address, CreditCard, ContactName);
			Helper_Functions.PrintOut(Arrays.toString(Result), false);
		}catch (Exception e) {
			Assert.fail(e.getMessage());
		}
	}
	
	@Test(dataProvider = "dp")
	public void WPRL_AccountManagement_BillingInvoice(String Level, String UserID, String Password, String CountryCode, String Email) {
		try {
			String Address[] = Helper_Functions.LoadAddress(CountryCode);
			String CreditCard[] = Helper_Functions.LoadCreditCard("V");
			String ContactName[] = Helper_Functions.LoadDummyName(CountryCode, Level);
			String Result[] =  WPRL_Functions.WPRL_AccountManagement(UserID, Password, Address, CreditCard, ContactName);
			Helper_Functions.PrintOut(Arrays.toString(Result), false);
		}catch (Exception e) {
			Assert.fail(e.getMessage());
		}
	}
	
	@Test(dataProvider = "dp")
	public void WPRL_FDM(String Level, String UserID, String Password, String CountryCode, String Email) {
		try {
			String Address[] = Helper_Functions.LoadAddress(CountryCode);
			String CreditCard[] = Helper_Functions.LoadCreditCard("V");
			String ContactName[] = Helper_Functions.LoadDummyName(CountryCode, Level);
			String Result[] =  WPRL_Functions.WPRL_FDM(CountryCode, UserID, Password, Address, CreditCard, ContactName);
			Helper_Functions.PrintOut(Arrays.toString(Result), false);
		}catch (Exception e) {
			Assert.fail(e.getMessage());
		}
	}
	
	@Test(dataProvider = "dp")
	public void WPRL_FDM_Enroll(String Level, int ContactDetails) {
		try {
			String UserId = Helper_Functions.LoadUserID("L" + Level + "FDM");
			String Result[] =  WERL_Functions.WERL_Postal_FDM(ContactDetails, UserId, Helper_Functions.myPassword);
			Helper_Functions.PrintOut(Arrays.toString(Result), false);
		}catch (Exception e) {
			Assert.fail(e.getMessage());
		}
	}
}


/*
	
	//@Test
	public void WPRL_FDM_Enrollemnt(){
		String Result[] = null;
		try {
			String FDM_Address[] = LoadAddress("US");
			String UserId = strLevel + FDM_Address[6] + "FDM" + strTodaysDate;
			//String NewUser = CreateNewUser(FDM_Address, Environment + "", strPassword);
			String[] NewUser = WFCL_JUnit.WFCL_UserRegistration(UserId, LoadDummyName(), FDM_Address);
			Result = WPRL_FDM_Enrollemnt(NewUser[0], NewUser[1], FDM_Address, "postcard");
		}catch (Exception e) {
			Assert.fail();
		}
		PassOrFail = true;
		ResultsList.set(TestNumber, UpdateArrayList(ResultsList.get(TestNumber), 1, Arrays.toString(Result)));
	}
	
	@Test
	public void WPRL_FDM_Enrollemnt_Add(){
		String Result[] = null;
		try {
			Result = WPRL_FDM("US", strWPRLsingleFDM, strPassword, LoadAddress("US"), LoadCreditCard("V"), LoadDummyName());
		}catch (Exception e) {
			Assert.fail();
		}
		PassOrFail = true;
		ResultsList.set(TestNumber, UpdateArrayList(ResultsList.get(TestNumber), 1, Arrays.toString(Result)));
	}
*/