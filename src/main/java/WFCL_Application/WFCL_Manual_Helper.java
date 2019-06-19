package WFCL_Application;

import org.testng.annotations.Test;
import Data_Structures.Account_Data;
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

public class WFCL_Manual_Helper{
	static String LevelsToTest = "3";  
	static String CountryList[][]; 

	@BeforeClass
	public void beforeClass() {
		Environment.SetLevelsToTest(LevelsToTest);
		Helper_Functions.MyEmail = "accept@fedex.com";
	}
	 
	@DataProvider (parallel = true)
	public static Iterator<Object[]> dp(Method m) {
		List<Object[]> data = new ArrayList<Object[]>();
		
		for (int i=0; i < Environment.LevelsToTest.length(); i++) {
			String Level = String.valueOf(Environment.LevelsToTest.charAt(i));
			//Account_Data Account_Info = null;
			//int intLevel = Integer.parseInt(Level);
			switch (m.getName()) { //Based on the method that is being called the array list will be populated.
		    	case "AccountRegistration_INET_Test":
		    	case "AccountRegistration_Captcha_Test":
		    	case "AccountRegistration_Take_Account_Online":
		    		/*String Accounts[] = {"642454684", "642167308", "642455400", "797691380", "642454749", "642167405", "642455184", "797691500", "642454765", "642167529", "642455206", "797691640", "642454781", "642167626", "642455222", "797691860", "642454803", "642167723", "642455249", "797692280", "642454820", "642167820", "642455281", "797692700", "642454846", "642167928", "642455303", "797391700", "642454862", "642168142", "642455320"};
		    		for (String A: Accounts) {
		    			data.add( new Object[] {Level, A});
		    		}
		    		*/
		    		//
		    		

		    		data.add( new Object[] {Level, "785546440"}); 
		    		break;
		    	case "AccountRegistration_FDDT":
		    		/*
		    		String Accounts[] = {"670218872"};
		    		for (String A: Accounts) {
		    			data.add( new Object[] {Level, A});
		    		}
		    		break;
		    		
		    		
		    		User_Data User_Info_Array[] = User_Data.Get_UserIds(intLevel);
		    		for (int k = 0; k < User_Info_Array.length; k++) {
		    			if (User_Info_Array[k].ERROR.contentEquals("12")) {
		    				String Account = User_Info_Array[k].USER_ID.replace("L6Account", "");
		    				data.add( new Object[] {Level, Account});
		    			}
		    		}
		    		*/

		    		data.add( new Object[] {Level, "700029018"}); 
		    		break;

		    	case "Link_Account_To_Specific_User":
		    		User_Data User_Info = new User_Data();
					User_Info.USER_ID = "L6Acc700232794N032019T104345twxs";
					User_Info.PASSWORD = "Test1234";
					String AccountNumber = "700195279";
		    		data.add( new Object[] {Level, User_Info, AccountNumber});
		    		AccountNumber = "700194795";
		    		data.add( new Object[] {Level, User_Info, AccountNumber});
		    		break;	
		    	case "WFCL_Rewards_Registration":
		    		data.add( new Object[] {Level, "774045660"});
		    		break;
		    	case "AccountRegistration_MultiAccount_Reg":
		    		String Accounts_reg[] = {"641015784", "641015806", "641015822", "641015849", "642244400", "642244426", "642244965", "642244981", "641136107", "641136123", "641136140", "641136085", "641410349", "641410403", "641410365", "641410381", "642085344", "642085360", "642085301", "642085328", "641016802", "641016829", "641016845", "641016861", "643321203", "697292624", "697292764", "697293124", "699287180", "699287440", "641449482", "641449504", "643058022", "643058049", "643058081", "643058103", "643140900", "643140926", "643140942", "643140969"};
		    		for (String A: Accounts_reg) {
		    			data.add( new Object[] {Level, A});
		    		}
		    		break;
			}
		}	
		return data.iterator();
	}

	@Test(dataProvider = "dp", priority = 1, enabled = true)
	public void AccountRegistration_INET_Test(String Level, String Account){
		try {
			Account_Data Account_Info = Account_Lookup.Account_Details(Account, Level);
			Account_Data.Set_Dummy_Contact_Name(Account_Info);
			Account_Data.Set_UserId(Account_Info, "L" + Level + "Acc" + Account + "N");
			//Account_Info.User_Info.USER_ID = "L3WFCLUSERID01";
			//create user id and link to account number.
			Account_Info = WFCL_Functions_UsingData.Account_Linkage(Account_Info);
			//register the userid to INET
			WFCL_Functions_UsingData.INET_Registration(Account_Info);
			String Result[] = new String[] {Account_Info.User_Info.USER_ID, Account_Info.User_Info.PASSWORD, Account_Info.Account_Number, Account_Info.User_Info.UUID_NBR};
			Helper_Functions.PrintOut(Arrays.toString(Result), false);
		}catch (Exception e) {
			Assert.fail(e.getMessage());
		}
	}
	
	@Test(dataProvider = "dp", priority = 1, enabled = false)
	public void AccountRegistration_Take_Account_Online(String Level, String Account){
		try {
			Account_Data Account_Info = Account_Lookup.Account_Details(Account, Level);
			Account_Data.Print_Account_Address(Account_Info);
			Account_Data.Set_Dummy_Contact_Name(Account_Info);
			Account_Data.Set_Account_Nickname(Account_Info, Account_Info.Account_Number + "_" + Account_Info.Billing_Address_Info.Country_Code);
			Account_Data.Set_UserId(Account_Info, "L" + Level + "A" + Account_Info.Account_Number + Account_Info.Billing_Address_Info.Country_Code);
			//create user id and link to account number.
			Account_Info = WFCL_Functions_UsingData.Account_Linkage(Account_Info);

			String Result[] = new String[] {Account_Info.User_Info.USER_ID, Account_Info.Account_Number, Account_Info.User_Info.UUID_NBR};
			Helper_Functions.PrintOut(Arrays.toString(Result), false);
		}catch (Exception e) {
			Assert.fail(e.getMessage());
		}
	}
	
	@Test(dataProvider = "dp", priority = 1, enabled = true)
	public void AccountRegistration_Captcha_Test(String Level, String Account){
		try {
			Account_Data Account_Info = Account_Lookup.Account_Details(Account, Level);
			Account_Data.Set_Dummy_Contact_Name(Account_Info);
			Account_Data.Set_UserId(Account_Info, "L" + Level + "Acc" + Account + "N");
			//start the test by entering captcha url and inputting data	
			int NumberOfAttempts = 3;
			WFCL_Functions_UsingData.WFCL_UserRegistration_Captcha(Account_Info, NumberOfAttempts);
			
			Helper_Functions.PrintOut("Completed Captcha", false);
		}catch (Exception e) {
			Assert.fail(e.getMessage());
		}
	}
	 
	@Test(dataProvider = "dp", priority = 1, enabled = true)
	public void AccountRegistration_FDDT(String Level, String Account){
		try {
			Account_Data Account_Info = Account_Lookup.Account_Details(Account, Level);
			Account_Data.Set_Dummy_Contact_Name(Account_Info);
			Account_Data.Set_UserId(Account_Info, "L" + Level + "FDDT" + Account + "N");
			Account_Info.User_Info.USER_ID = "L6Account" + Account;
			//create user id and link to account number. comment out the below if linking to existing user.
			//WFCL_Functions_UsingData.WFCL_UserRegistration(Account_Info); 
			
			WFCL_Functions_UsingData.Account_Linkage_FDDT(Account_Info);
			String Result[] = new String[] {Account_Info.User_Info.USER_ID, Account_Info.User_Info.PASSWORD, Account_Info.Account_Number, Account_Info.User_Info.UUID_NBR};
			Helper_Functions.PrintOut(Arrays.toString(Result), false);
		}catch (Exception e) {
			Assert.fail(e.getMessage());
		}
	}
	
	@Test(dataProvider = "dp", priority = 1, enabled = false)
	public void Link_Account_To_Specific_User(String Level, User_Data User_Info, String Account_Number){
		try {
			Account_Data Account_Info = Account_Lookup.Account_Details(Account_Number, Level);
			String Result[] = WFCL_Functions_UsingData.Account_Linkage(User_Info, Account_Info);
			Helper_Functions.PrintOut(Arrays.toString(Result), false);
		}catch (Exception e) {
			Assert.fail(e.getMessage());
		}
	}
	
	@Test(dataProvider = "dp", enabled = false)
	public void WFCL_Rewards_Registration(String Level, String Account) {
		try {
			Account_Data Account_Info = Account_Lookup.Account_Details(Account, Level);
			Account_Data.Set_Dummy_Contact_Name(Account_Info);
			Account_Data.Set_UserId(Account_Info, "L" + Level + "Rewards" + Account + "N");
			Account_Data.Print_Account_Address(Account_Info);
			
			String Result[] = WFCL_Functions_UsingData.WFCL_RewardsRegistration(Account_Info);

			Helper_Functions.PrintOut(Arrays.toString(Result), false);
		}catch (Exception e) {
			Assert.fail(e.getMessage());
		}
	}
	
	@Test(dataProvider = "dp", priority = 1, enabled = true)
	public void AccountRegistration_MultiAccount_Reg(String Level, String Account){
			try {
				Account_Data Account_Info = Account_Lookup.Account_Details(Account, Level);
				Account_Data.Set_Dummy_Contact_Name(Account_Info);
				Account_Data.Set_UserId(Account_Info, "L" + Level + "MAGIC" + Account_Info.Account_Number + Account_Info.Billing_Address_Info.Country_Code);
				//create user id and link to account number.
				Account_Info = WFCL_Functions_UsingData.Account_Linkage(Account_Info);
				//register the userid to INET, if not from us or CA then already registered.
				if ("US CA".contains(Account_Info.Billing_Address_Info.Country_Code)) {
					WFCL_Functions_UsingData.INET_Registration(Account_Info);
				}
				
				String Result[] = new String[] {Account_Info.User_Info.USER_ID, Account_Info.User_Info.PASSWORD, Account_Info.Account_Number, Account_Info.User_Info.UUID_NBR};
				Helper_Functions.PrintOut(Arrays.toString(Result), false);
			}catch (Exception e) {
				Assert.fail(e.getMessage());
			}
	}
}