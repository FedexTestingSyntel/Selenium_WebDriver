package USRC_Application;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import org.junit.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import Data_Structures.ADMC_Data;
import Data_Structures.PRDC_Data;
import Data_Structures.USRC_Data;
import Data_Structures.User_Data;
import PRDC_Application.PRDC_API_Endpoints;
import SupportClasses.Environment;
import SupportClasses.Helper_Functions;
import SupportClasses.General_API_Calls;
import org.testng.annotations.Test;
import ADMC_Application.ADMC_API_Endpoints;

public class USRC_TestData_Update {

	static String LevelsToTest = "3"; //Can but updated to test multiple levels at once if needed. Setting to "23" will test both level 2 and level 3.

	@BeforeClass
	public void beforeClass() {
		Environment.SetLevelsToTest(LevelsToTest);
	}
	
	@DataProvider (parallel = true)
	public Iterator<Object[]> dp(Method m) {
	    List<Object[]> data = new ArrayList<>();
	    
	    for (int i = 0; i < Environment.LevelsToTest.length(); i++) {
	    	String strLevel = "" + Environment.LevelsToTest.charAt(i);
	    	int intLevel = Integer.parseInt(strLevel);
	    	//loading the OAuth token and having all of the variables set.
	    	USRC_Data.LoadVariables(strLevel);
	    	PRDC_Data.LoadVariables(strLevel);
	    	
	    	//load user ids since both of the below use that value
	    	User_Data User_Info_Array[] = User_Data.Get_UserIds(intLevel);
			switch (m.getName()) { //Based on the method that is being called the array list will be populated.	
			case "CheckLogin":
				for (User_Data User_Info: User_Info_Array) {
    				if (User_Info.UUID_NBR.contentEquals("")) {
    					data.add(new Object[] {strLevel, User_Info});
    				}else if (!User_Info.ERROR.contentEquals("")) {
    					data.add(new Object[] {strLevel, User_Info});
    				}
    				//uncomment if need to run all
    				//else{data.add(new Object[] {strLevel, User_Info});}

    			}
				break;
			case "CheckMigration":
				for (User_Data User_Info: User_Info_Array){
    				if (User_Info.MIGRATION_STATUS.contentEquals("")) {
    					data.add(new Object[] {strLevel, User_Info.USER_ID, User_Info.PASSWORD});
    				}
    			}
				break;
			case "UpdateUser":
				for (User_Data User_Info: User_Info_Array) {
					if (User_Info.Address_Info.Country_Code.contentEquals("US")) {
    					data.add(new Object[] {strLevel, User_Info});
    					break;
    				}
    			}
				break;
			case "Check_WCRV_Status":
				for (User_Data User_Info: User_Info_Array) {
    				if (User_Info.WCRV_ENABLED.contentEquals("")) {
    					data.add(new Object[] {strLevel, User_Info});
    				}
    			}
			}//end switch MethodName
		}
	    System.out.println(data.size() + " scenarios.");
		return data.iterator();
	}

	@Test (dataProvider = "dp", enabled = false)
	public void UpdateUser(String Level, User_Data User_Info) {
		Environment.getInstance().setLevel(Level);
		USRC_Data USRC_Details = USRC_Data.LoadVariables(Level);
		User_Info.FIRST_NM = "Bob";
		
		String fdx_login_fcl_uuid[] = null;
		//get the cookies and the uuid of the user
		fdx_login_fcl_uuid = USRC_API_Endpoints.Login(USRC_Details.GenericUSRCURL, User_Info.USER_ID, User_Info.PASSWORD);
		
		String Test = USRC_API_Endpoints.UpdateUserContactInformationWIDM(USRC_Details.UpdateUserContactInformationWIDMURL, User_Info, fdx_login_fcl_uuid[0]);
		
		Helper_Functions.PrintOut(Test);
	}
	
	@Test (dataProvider = "dp", enabled = false )
	public void Check_WCRV_Status(String Level, User_Data User_Info) {
		boolean updatefile = false;
		
		Environment.getInstance().setLevel(Level);
		USRC_Data USRC_Details = USRC_Data.LoadVariables(Level);
		
		//get the cookies and the uuid of the user
		String fdx_login_fcl_uuid[] = null;
		fdx_login_fcl_uuid = USRC_API_Endpoints.Login(USRC_Details.GenericUSRCURL, User_Info.USER_ID, User_Info.PASSWORD);
		if (fdx_login_fcl_uuid != null) {
			String Details[][] = {{"UUID_NBR", User_Info.UUID_NBR},//index 0 and set below
					{"SSO_LOGIN_DESC", User_Info.USER_ID},
					{"USER_PASSWORD_DESC", User_Info.PASSWORD},
					};
			Details = WCRV_Access(Level, Details, fdx_login_fcl_uuid[0]);
			
			String FileName = Helper_Functions.DataDirectory + "\\TestingData.xls";
			updatefile = Helper_Functions.WriteToExcel(FileName, "L" + Level, Details, 0);
			Helper_Functions.PrintOut("Contact Details: " + Arrays.deepToString(Details), true);
		}else {
			Assert.fail("Not able to login.");
		}
		
		if (!updatefile) {
			Assert.fail("Not able to update file.");
		}
	}

	@Test (dataProvider = "dp", enabled = true)
	public static void CheckLogin(String Level, User_Data User_Info) {
		Environment.getInstance().setLevel(Level);
		USRC_Data USRC_Details = USRC_Data.LoadVariables(Level);
		
		String Cookies = null, fdx_login_fcl_uuid[] = null;

		//in case cannot login will check with the generic other passwords
		String GenericPasswords[] = new String[] {User_Info.PASSWORD.replaceAll(" ", ""), "Test1234", "Test12345", "Test123456", "Password1", "Inet2010"};
		for (String TestPassword: GenericPasswords) {
			if (fdx_login_fcl_uuid == null && !TestPassword.contentEquals("")){
				User_Info.PASSWORD = TestPassword;
				fdx_login_fcl_uuid = USRC_API_Endpoints.Login(USRC_Details.GenericUSRCURL, User_Info.USER_ID, User_Info.PASSWORD);
			}
		}

		//this is the default key position to update the user table. Currently set to 1 for user id value.
		int keyPosition = 1;
		
		String Details[][] = {{"UUID_NBR", ""},//index 0 and set below
				{"SSO_LOGIN_DESC", User_Info.USER_ID}, //set as the key position for making updates above. int keyPosition
				{"USER_PASSWORD_DESC", User_Info.PASSWORD},
				{"SECRET_QUESTION_DESC", ""}, 
				{"SECRET_ANSWER_DESC", ""},
				{"FIRST_NM", ""}, 
				{"LAST_NM", ""}, 
				{"STREET_DESC", ""}, 
				{"CITY_NM", ""}, 
				{"STATE_CD", ""}, 
				{"POSTAL_CD", ""}, 
				{"COUNTRY_CD", ""}, 
				{"EMAIL_ADDRESS", ""},
				{"ERROR", ""}//will clear out old error time stamp if failed previously.
				};
		
		if (fdx_login_fcl_uuid != null){
			Cookies = fdx_login_fcl_uuid[0];
			Details[0][1] = fdx_login_fcl_uuid[1];//save the uuid

			Details = App_Role_Info_Check(Level, Details, Cookies);

			Details = FDM_Access(Level, Details, USRC_Details.GenericUSRCURL, Cookies);

			String ContactDetailsResponse = USRC_API_Endpoints.ViewUserProfileWIDM(USRC_Details.ViewUserProfileWIDMURL, Cookies);
			Details = USRC_API_Endpoints.Parse_ViewUserProfileWIDM(ContactDetailsResponse, Details);
			
			Details = WCRV_Access(Level, Details, Cookies);
			
			Details = Migration_And_Manage_Check(Level, Details, Cookies);
		}else {
			//will save the current time of the failure.
			Details = new String[][]{{"SSO_LOGIN_DESC", User_Info.USER_ID}, {"ERROR", Helper_Functions.CurrentDateTime(true)}};
			keyPosition = 0;
		}
		
		String FileName = Helper_Functions.DataDirectory + "\\TestingData.xls";
		boolean updatefile = Helper_Functions.WriteToExcel(FileName, "L" + Level, Details, keyPosition);
		Helper_Functions.PrintOut("Contact Details: " + Arrays.deepToString(Details), true);
		if (!updatefile) {
			Assert.fail("Not able to update file.");
		}else if (fdx_login_fcl_uuid == null) {
			Assert.fail("Not able to login");
		}
	}
	
	@Test (dataProvider = "dp", enabled = false)
	public void CheckMigration(String Level, String UserID, String Password) {
		Environment.getInstance().setLevel(Level);
		USRC_Data USRC_Details = USRC_Data.LoadVariables(Level);

		String Cookies = null, fdx_login_fcl_uuid[] = null;
		//get the cookies and the uuid of the user
		fdx_login_fcl_uuid = USRC_API_Endpoints.Login(USRC_Details.GenericUSRCURL, UserID.replaceAll(" ", ""), Password.replaceAll(" ", ""));
		
		//this is the default key position to update the user table. Currently set to 1 for user id value.
		int keyPosition = 1;
		
		String Details[][] = {{"UUID_NBR", ""},//index 0 and set below
				{"SSO_LOGIN_DESC", UserID},
				{"USER_PASSWORD_DESC", Password}
				};
		
		if (fdx_login_fcl_uuid != null){
			Cookies = fdx_login_fcl_uuid[0];
			Details[0][1] = fdx_login_fcl_uuid[1];//save the uuid
			Details = Migration_And_Manage_Check(Level, Details, Cookies);
		}else {
			//will save the current time of the failure.
			Details = new String[][]{{"SSO_LOGIN_DESC", UserID}, {"ERROR", Helper_Functions.CurrentDateTime(true)}};
			keyPosition = 0;
		}
		
		String FileName = Helper_Functions.DataDirectory + "\\TestingData.xls";
		boolean updatefile = Helper_Functions.WriteToExcel(FileName, "L" + Level, Details, keyPosition);
		//Helper_Functions.PrintOut("Contact Details: " + Arrays.deepToString(Details), true);//for debug
		if (!updatefile) {
			Assert.fail("Not able to update file.");
		}else if (fdx_login_fcl_uuid == null) {
			Assert.fail("Not able to login");
		}
	}
	
	public static String[][] FDM_Access(String Level, String Details[][], String USRCURL, String Cookies) {
		String Response = USRC_API_Endpoints.RecipientProfile(USRCURL, Cookies);
		Details = Arrays.copyOf(Details, Details.length + 1);
		
		if (Response.contains("recipientProfileEnrollmentStatus\":\"ENROLLED")) {
			Details[Details.length - 1] = new String[] {"FDM_STATUS", Response};//store all of the FDM details
		}else {
			Details[Details.length - 1] = new String[] {"FDM_STATUS", "F"};//not yet enrolled for FDM
		}
		return Details;
	}
	
	public static String[][] WCRV_Access(String Level, String Details[][], String Cookies) {
		PRDC_Data PRDC_D = PRDC_Data.LoadVariables(Level);
		String AccountDetails = PRDC_API_Endpoints.PRDC_Accounts_Call(PRDC_D.AccountsURL, Cookies);
		String WCRV_Access = "";
		if (AccountDetails.contains("displayRateSheetFlag\":true") && AccountDetails.contains("discountPricingFlag\":true") && AccountDetails.contains("accountCountryEnabledFlag\":true")){
			WCRV_Access = "T";
		}else if (!AccountDetails.contains("displayRateSheetFlag")){
			Helper_Functions.PrintOut("PRDC call did not return premission status.", false);
			WCRV_Access = "F";
		}else {
			if (AccountDetails.contains("displayRateSheetFlag\":false")){
				//the user does not have the view rate sheet privilege
				WCRV_Access += "displayRateSheet_False ";
			}
			if (AccountDetails.contains("discountPricingFlag\":false")){
				//no account numbers have a discount applied
				WCRV_Access += "discountPricingFlag_False ";
			}
			if (AccountDetails.contains("accountCountryEnabledFlag\":false")){
				//no account numbers are from a valid country
				WCRV_Access += "accountCountryEnabledFlag_False";
			}
		}
		Details = Arrays.copyOf(Details, Details.length + 1);
		Details[Details.length - 1] = new String[] {"WCRV_ENABLED", WCRV_Access};
		return Details;
	}

	public static String[][] App_Role_Info_Check(String Level, String Details[][], String Cookies){
		USRC_Data USRC_Details = USRC_Data.LoadVariables(Level);
		
		String AccountRetrievalRequest = USRC_API_Endpoints.AccountRetrievalRequest(USRC_Details.GenericUSRCURL, Cookies);
		
		String Parse[][] = {{"GFBO_ENABLED", "appName\":\"fclgfbo\",\"roleCode\":\""},
							{"WGRT_ENABLED", "appName\":\"fclrates\",\"roleCode\":\""}, 
							{"WDPA_ENABLED", "appName\":\"fclpickup\",\"roleCode\":\""}, 
							{"PASSKEY", "appName\":\"fclpasskey\",\"roleCode\":\""}
							};

		for (int j = 0; j < Parse.length; j++) {
			Details = Arrays.copyOf(Details, Details.length + 1);
			if (AccountRetrievalRequest.contains(Parse[j][1])) {
				Details[Details.length - 1] = new String[] {Parse[j][0], "T"};
			}else {
				Details[Details.length - 1] = new String[] {Parse[j][0], "F"};
			}
		}
		Details = Arrays.copyOf(Details, Details.length + 1);
		Details[Details.length - 1] = new String[] {"ACCOUNT_NUMBER", USRC_API_Endpoints.Parse_AccountRetrievalRequest_AccountNumber(AccountRetrievalRequest)};

		//This will check if the user is the owner of the account
		String Account_Value = General_API_Calls.Parse_API_For_Value(AccountRetrievalRequest, "value");
		String Account_Key = General_API_Calls.Parse_API_For_Value(AccountRetrievalRequest, "key");
		//Attempt in case the user was created with account nickname in the <Account>_<Details> format
		if (Account_Value != null && Account_Value.contentEquals("")) {
			String Account_Nickname = General_API_Calls.Parse_API_For_Value(AccountRetrievalRequest, "accountNickname");
			if (Account_Nickname.contains("\\u005f")) {
				Account_Nickname = Account_Nickname.substring(0, Account_Nickname.indexOf("\\u005f"));
			}
			Account_Value = Account_Nickname;
		}
		String Linage_Indicator = "NOTKNOWN";
		if (Account_Key != null && !Account_Key.contentEquals("") && Account_Value != null && !Account_Value.contentEquals("") ) {
			String EnterpriseCustomerRequest = USRC_API_Endpoints.EnterpriseCustomerRequest(USRC_Details.GenericUSRCURL, Cookies, Account_Value, Account_Key);
			Linage_Indicator = General_API_Calls.Parse_API_For_Value(EnterpriseCustomerRequest, "linkageIndicator");
		}
		Details = Arrays.copyOf(Details, Details.length + 1);
		Details[Details.length - 1] = new String[] {"LINKAGE_INDICATOR", Linage_Indicator};
	
		return Details;
	}
	
	public static String[][] Migration_And_Manage_Check(String Level, String Details[][], String Cookies){
		ADMC_Data ADMC_Details = ADMC_Data.LoadVariables(Level);
		
		String AccountRetrievalRequest = ADMC_API_Endpoints.RoleAndStatus(ADMC_Details.RoleAndStatusURL, Cookies);
		
		//Example: {"successful": true,"output": {"migrationStatus": true,"userType": "COMPANY_ADMIN"}}
		//         {"output":{"migrationStatus":false,"userType":"NON_MANAGED"},"successful":true}
		
		String Parse[][] = {{"MIGRATION_STATUS", "migrationStatus\":", ","},
							{"USER_TYPE", "\"userType\":\"", "\""}, 
							};

		for (int j = 0; j < Parse.length; j++) {
			Details = Arrays.copyOf(Details, Details.length + 1);
			if (AccountRetrievalRequest.contains(Parse[j][1])) {
				//substring staring with the variable in question
				String temp = AccountRetrievalRequest.substring(AccountRetrievalRequest.indexOf(Parse[j][1]) + Parse[j][1].length(), AccountRetrievalRequest.length());
				String value_needed = temp.substring(0, temp.indexOf(Parse[j][2]));
				Details[Details.length - 1] = new String[] {Parse[j][0], value_needed};
			}else {
				Details[Details.length - 1] = new String[] {Parse[j][0], "NA"};
			}
		}
		
		if (Details[Details.length - 1][1].contains("NON_MANAGED") && Details[Details.length - 2][1].contains("false") ) {
			Details[Details.length - 2][1] = "F";
		}else if (!Details[Details.length - 1][1].contains("NON_MANAGED") && Details[Details.length - 2][1].contains("true") ) {
			Details[Details.length - 2][1] = "WADM";
		}else if (!Details[Details.length - 1][1].contains("NON_MANAGED") && Details[Details.length - 2][1].contains("false") ) {
			Details[Details.length - 2][1] = "IPAS";
		}
	
		return Details;
	}
	
	public static String[][] Linkage_Indicator(String Level, String Details[][], String USRCURL, String Cookies, String AccountNumber, String AccountKey) {
		String Response = USRC_API_Endpoints.RecipientProfile(USRCURL, Cookies);
		Details = Arrays.copyOf(Details, Details.length + 1);
		
		if (Response.contains("recipientProfileEnrollmentStatus\":\"ENROLLED")) {
			Details[Details.length - 1] = new String[] {"FDM_STATUS", Response};//store all of the FDM details
		}else {
			Details[Details.length - 1] = new String[] {"FDM_STATUS", "F"};//not yet enrolled for FDM
		}
		return Details;
	}
}
