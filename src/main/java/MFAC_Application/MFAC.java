package MFAC_Application;

import org.testng.annotations.Test;
import Data_Structures.MFAC_Data;
import org.testng.annotations.BeforeClass;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.concurrent.CopyOnWriteArrayList;

import org.hamcrest.CoreMatchers;
import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.containsString;
import SupportClasses.Environment;
import SupportClasses.Helper_Functions;

@Listeners(SupportClasses.TestNG_TestListener.class)
//@Listeners(SupportClasses.TestNG_ReportListener.class)

public class MFAC{
	static String LevelsToTest = "23"; //Can but updated to test multiple levels at once if needed. Setting to "23" will test both level 2 and level 3.
	final boolean TestExpiration = true;//flag to determine if the expiration scenarios should be tested. When set to false those tests will not be executed.
	
	static CopyOnWriteArrayList<String[]> ExpirationData = new CopyOnWriteArrayList<String[]>(); 
	
	@BeforeClass
	public static void beforeClass() {
		Environment.SetLevelsToTest(LevelsToTest);
	}
	
	@DataProvider (parallel = true)
	public static Iterator<Object[]> dp(Method m) {
	    List<Object[]> data = new ArrayList<>();
	    
	    for (int i = 0; i < Environment.LevelsToTest.length(); i++) {
			String Level = String.valueOf(Environment.LevelsToTest.charAt(i));
			MFAC_Data c = MFAC_Data.LoadVariables(Level);
			switch (m.getName()) { //Based on the method that is being called the array list will be populated. This will make the TestNG Pass/Fail results more relevant.
			case "MFAC_AddressVelocity":
				if (!c.Level.contentEquals("1")){
					data.add(new Object[] {Level, c.OrgPostcard, c.OAuth_Token, c.AVelocityURL, c.AddressVelocityThreshold});
					data.add(new Object[] {Level, c.OrgPhone, c.OAuth_Token, c.AVelocityURL, c.AddressVelocityThreshold});
				}
    			if (!c.Level.contentEquals("6") && !c.Level.contentEquals("7")){
    				data.add(new Object[] {Level, c.OrgPostcard, c.OAuth_Token, c.DVelocityURL, c.AddressVelocityThreshold});
    				data.add(new Object[] {Level, c.OrgPhone, c.OAuth_Token, c.DVelocityURL, c.AddressVelocityThreshold});
    			}
    			break;
    		case "MFAC_IssuePin":
    			if (!c.Level.contentEquals("1")){
    				data.add(new Object[] {Level, c.OrgPostcard, c.OAuth_Token, c.AIssueURL});
    				data.add(new Object[] {Level, c.OrgPhone, c.OAuth_Token, c.AIssueURL});
    			}
    			if (!c.Level.contentEquals("6") && !c.Level.contentEquals("7")){
    				data.add(new Object[] {Level, c.OrgPostcard, c.OAuth_Token, c.DIssueURL});
    				data.add(new Object[] {Level, c.OrgPhone, c.OAuth_Token, c.DIssueURL});
    			}
    			break;
    		case "MFAC_DetermineLockoutTime"://only need to test API call as this is a helper test to determine current lockouts set.
    			data.add(new Object[] {Level, c.OrgPostcard, c.OAuth_Token, c.AIssueURL});
    			data.add(new Object[] {Level, c.OrgPhone, c.OAuth_Token, c.AIssueURL});
    			break;
    		case "MFAC_IssuePinVelocity":
    			if (!c.Level.contentEquals("1")){
    				data.add(new Object[] {Level, c.OrgPostcard, c.OAuth_Token, c.AIssueURL, c.PinVelocityThresholdPostcard});
    				data.add(new Object[] {Level, c.OrgPhone, c.OAuth_Token, c.AIssueURL, c.PinVelocityThresholdPhone});
    			}
    			if (!c.Level.contentEquals("6") && !c.Level.contentEquals("7")){
    				data.add(new Object[] {Level, c.OrgPostcard, c.OAuth_Token, c.DIssueURL, c.PinVelocityThresholdPostcard});
    				data.add(new Object[] {Level, c.OrgPhone, c.OAuth_Token, c.DIssueURL, c.PinVelocityThresholdPhone});
    			}
    			break;
    		case "MFAC_VerifyPinValid":
    		case "MFAC_VerifyPinNoLongerValid":
    		case "MFAC_IssuePinExpiration":
    			if (!c.Level.contentEquals("1")){
    				data.add(new Object[] {Level, c.OrgPostcard, c.OAuth_Token, c.AIssueURL, c.AVerifyURL});
    				data.add(new Object[] {Level, c.OrgPhone, c.OAuth_Token, c.AIssueURL, c.AVerifyURL});	
    			}
    			if (!c.Level.contentEquals("6") && !c.Level.contentEquals("7")){
    				data.add(new Object[] {Level, c.OrgPostcard, c.OAuth_Token, c.DIssueURL, c.DVerifyURL});
    				data.add(new Object[] {Level, c.OrgPhone, c.OAuth_Token, c.DIssueURL, c.DVerifyURL});
    			}
    			break;
    		case "MFAC_VerifyPinVelocity":
    			if (!c.Level.contentEquals("1")){
    				data.add(new Object[] {Level, c.OrgPostcard, c.OAuth_Token, c.AIssueURL, c.AVerifyURL, c.PinVelocityThresholdPostcard});
    				data.add(new Object[] {Level, c.OrgPhone, c.OAuth_Token, c.AIssueURL, c.AVerifyURL, c.PinVelocityThresholdPhone});
    			}
    			if (!c.Level.contentEquals("6") && !c.Level.contentEquals("7")){
    				data.add(new Object[] {Level, c.OrgPostcard, c.OAuth_Token, c.DIssueURL, c.DVerifyURL, c.PinVelocityThresholdPostcard});
    				data.add(new Object[] {Level, c.OrgPhone, c.OAuth_Token, c.DIssueURL, c.DVerifyURL, c.PinVelocityThresholdPhone});
    			}
    			break;
    		case "MFAC_AdditionalEnrollmentExpiration":
    			if (!c.Level.contentEquals("1")){
    				data.add(new Object[] {Level, c.OrgPostcard, c.OrgPhone, c.OAuth_Token, c.AIssueURL, c.AVerifyURL});
    				data.add(new Object[] {Level, c.OrgPhone, c.OrgPostcard, c.OAuth_Token, c.AIssueURL, c.AVerifyURL});
    			}
    			if (!c.Level.contentEquals("6") && !c.Level.contentEquals("7")){
    				data.add(new Object[] {Level, c.OrgPostcard, c.OrgPhone, c.OAuth_Token, c.DIssueURL, c.DVerifyURL});
    				data.add(new Object[] {Level, c.OrgPhone, c.OrgPostcard, c.OAuth_Token, c.DIssueURL, c.DVerifyURL});
    			}
    			break;
    		case "MFAC_IssuePinExpirationValidate":
    			if (ExpirationData.size() > 0) {
    				ExpirationData.sort((o1, o2) -> o1[1].compareTo(o2[1]));
    			}
    			
    			for (int j = 0 ; j < ExpirationData.size(); j++) {
    				if (ExpirationData.get(j)[0].contains("MFAC_IssuePinExpiration")) {
    					data.add(ExpirationData.get(j));
    					ExpirationData.remove(j);
    				}
    			}
    			break;	
    		case "MFAC_AdditionalEnrollmentExpirationValidate":
    			if (ExpirationData.size() > 0) {
    				ExpirationData.sort((o1, o2) -> o1[1].compareTo(o2[1]));
    			}
    			for (int j = 0 ; j < ExpirationData.size(); j++) {
    				if (ExpirationData.get(j)[0].contains("MFAC_AdditionalEnrollmentExpiration")) {
    					data.add(ExpirationData.get(j));
    					ExpirationData.remove(j);
    				}
    			}
    			break;	
			}//end switch MethodName
		}
		return data.iterator();
	}
	
	@Test(dataProvider = "dp", priority = 2)
	public void MFAC_AddressVelocity(String Level, String OrgName, String OAuth_Token, String VelocityURL, int AddressVelocityThreshold) {//220496 Address Velocity
		Helper_Functions.PrintOut("Verify that the address velocity of " + AddressVelocityThreshold + " is reached and the correct error code is received. This is to replicate too many requests for pin at a given address.", false);
		String UserName = UserName(), Response;
		try {
			for (int i = 0; i < AddressVelocityThreshold; i++){
				Helper_Functions.PrintOut("  #" + (i + 1) + " Address Request.", false);
				Response = MFAC_API_Endpoints.AddressVelocityAPI(UserName, OrgName, VelocityURL, OAuth_Token);
				assertThat(Response, containsString("ALLOW"));
			}
			
			Helper_Functions.PrintOut("  #" + (AddressVelocityThreshold + 1) + " Address Request.", false);
			Response = MFAC_API_Endpoints.AddressVelocityAPI(UserName, OrgName, VelocityURL, OAuth_Token);
			assertThat(Response, CoreMatchers.allOf(containsString("DENY"), containsString("Unfortunately, too many failed attempts for registration have occurred. Please try again later.")));
		}catch (Exception e) {
			Assert.fail(e.getMessage());
		}
	}
	
	@Test(dataProvider = "dp", priority = 2)
	public void MFAC_IssuePin(String Level, String OrgName, String OAuth_Token, String IssueURL){//220459 IssuePin
		Helper_Functions.PrintOut("Verify that the user is able to request a pin.", false);
		String Response = null, UserName = UserName();
		
		try {
			Response = MFAC_API_Endpoints.IssuePinAPI(UserName, OrgName, IssueURL, OAuth_Token);

			assertThat(Response, CoreMatchers.allOf(containsString("pinOTP"), containsString("pinExpirationDate")));//pin should be generated//pin expiration time should be present.
			
			String Pin = ParsePIN(Response);
			Integer.parseInt(Pin);//checking to see if an integer was returned.
		}catch (Exception e) {
			Assert.fail(e.getMessage());
		}
	}
	
	@Test(dataProvider = "dp", priority = 2)
	public void MFAC_IssuePinVelocity(String Level, String OrgName, String OAuth_Token, String IssueURL, int PinVelocityThreshold){//220459 IssuePin
		Helper_Functions.PrintOut("Verify that the user can request up to " + PinVelocityThreshold + " pin numbers before unable to request more.", false);
		String Response = null, UserName = UserName();
		
		try {
			for (int i = 0; i < PinVelocityThreshold; i++){
				Helper_Functions.PrintOut("  #" + (i + 1) + " Pin Request.", false);
				Response = MFAC_API_Endpoints.IssuePinAPI(UserName, OrgName, IssueURL, OAuth_Token);
				assertThat(Response, CoreMatchers.allOf(containsString("pinOTP"), containsString("pinExpirationDate")));
			}
			
			Helper_Functions.PrintOut("  #" + (PinVelocityThreshold + 1) + " Pin Request.", false);
			Response = MFAC_API_Endpoints.IssuePinAPI(UserName, OrgName, IssueURL, OAuth_Token);
			//033018 - updated value from DENY to 5700, updated to match with what USRC uses.
			assertThat(Response, CoreMatchers.allOf(containsString("5700"), containsString("Unfortunately, you have exceeded your attempts for verification. Please try again later.")));
		}catch (Exception e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test(dataProvider = "dp", priority = 2)
	public void MFAC_VerifyPinValid(String Level, String OrgName, String OAuth_Token, String IssueURL, String VerifyURL){//220462 Verify Pin
		Helper_Functions.PrintOut("VerifyPinValid: Verify that user is able to request a pin and then verify that can recieve success when using the generated pin.", false);
		String Response = null, UserName = UserName();
		
		try {
			Response =  MFAC_API_Endpoints.IssuePinAPI(UserName, OrgName, IssueURL, OAuth_Token);
			assertThat(Response, containsString("pinExpirationDate"));
			String Pin = ParsePIN(Response);
			
			//Test verify pin on valid request
			Response = MFAC_API_Endpoints.VerifyPinAPI(UserName, OrgName, Pin, VerifyURL, OAuth_Token);
			assertThat(Response, containsString("Success"));
		}catch (Exception e) {
			Assert.fail(e.getMessage());
		}
	}
	
	@Test(dataProvider = "dp", priority = 2)
	public void MFAC_VerifyPinVelocity(String Level, String OrgName, String OAuth_Token, String IssueURL, String VerifyURL, int PinVelocityThreshold){//220462 Verify Pin
		Helper_Functions.PrintOut("VerifyPinThreshold: When an invalid pin is entered the pin failure message should be returned passed the velocity threshold of " + PinVelocityThreshold + ".", false);
		String Response = null, UserName = UserName();
		
		try {
			Response = MFAC_API_Endpoints.IssuePinAPI(UserName, OrgName, IssueURL, OAuth_Token);
			assertThat(Response, containsString("pinExpirationDate"));
			//Test verify pin on valid request
			for (int i = 0; i < PinVelocityThreshold + 2; i++) {
				Response = MFAC_API_Endpoints.VerifyPinAPI(UserName, OrgName, "1111", VerifyURL, OAuth_Token);
				assertThat(Response, containsString("PIN.FAILURE"));
			}
		}catch (Exception e) {
			Assert.fail(e.getMessage());
		}
	}
	
	@Test(dataProvider = "dp", priority = 2)
	public void MFAC_VerifyPinNoLongerValid(String Level, String OrgName, String OAuth_Token, String IssueURL, String VerifyURL){//220462 Verify Pin
		Helper_Functions.PrintOut("VerifyPinNoLongerValid: Verify that when user requests a second pin that the first is no longer valid.", false);
		String Response = null, UserName = UserName();
		
		try {
			Response = MFAC_API_Endpoints.IssuePinAPI(UserName, OrgName, IssueURL, OAuth_Token);
			assertThat(Response, containsString("pinOTP"));//just to make sure valid response
			String Pin = ParsePIN(Response);
			Integer.parseInt(Pin);
			Response = MFAC_API_Endpoints.IssuePinAPI(UserName, OrgName, IssueURL, OAuth_Token);
			assertThat(Response, containsString("pinOTP"));//just to make sure valid response
			String PinTwo = ParsePIN(Response);
			Integer.parseInt(PinTwo);
			//Test that the first pin is no longer valid
			Response = MFAC_API_Endpoints.VerifyPinAPI(UserName, OrgName, Pin, VerifyURL, OAuth_Token);
			assertThat(Response, containsString("PIN.FAILURE"));
			//Test verify pin on valid request
			Response = MFAC_API_Endpoints.VerifyPinAPI(UserName, OrgName, PinTwo, VerifyURL, OAuth_Token);
			assertThat(Response, containsString("Success"));
		}catch (Exception e) {
			Assert.fail(e.getMessage());
		}
	}

	@SuppressWarnings("deprecation")  //add due to the date comparison 
	@Test(dataProvider = "dp", enabled = TestExpiration, priority = 1)
	public void MFAC_IssuePinExpiration(String Level, String OrgName, String OAuth_Token, String IssueURL, String VerifyURL){
		Helper_Functions.PrintOut("Verify that after a pin is expired it can no longer be used to complete registration.", false);
		String Response = null, UserName = UserName();
		
		try {
			Response = MFAC_API_Endpoints.IssuePinAPI(UserName, OrgName, IssueURL, OAuth_Token);
			assertThat(Response, containsString("pinExpirationDate")); 
			
			Date CurrrentTime = new Date();
			SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm a zzz");
			dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
			dateFormat.format(CurrrentTime);
			Date ExpirationTime = GetExpiration(Response);
		
			if (ExpirationTime.getDate() == CurrrentTime.getDate() && ExpirationTime.getMonth() == CurrrentTime.getMonth()) {
				String Expiration[] = new String[] {Level, "MFAC_IssuePinExpiration:  " + Response, dateFormat.format(ExpirationTime).toString(), UserName, OrgName, ParsePIN(Response), VerifyURL, OAuth_Token, "PIN.FAILURE"};
				ExpirationData.add(Expiration);
				Helper_Functions.PrintOut("Will be validated after expiration in later test. --IssuePinExpirationValidate--", false);
			}else {
				String LongExpirationMessage = "Not Validing the Expiraiton at this time, need to verify seperatly once the expiration has passed. Here is the CST time it will expire. " + ExpirationTime;
				Helper_Functions.PrintOut(LongExpirationMessage, true);
			}
		}catch (Exception e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test(dataProvider = "dp",enabled = TestExpiration,dependsOnMethods = "MFAC_IssuePinExpiration", priority = 3)
	public void MFAC_IssuePinExpirationValidate(String Level, String Result, String ExpirationResponse, String UserName, String OrgName, String Pin, String VerifyURL, String OAuth_Token, String Expected){
		String Response = null;
		Helper_Functions.PrintOut("Check that once the expiration time is over the user can no longer complete registration with expired pin number.", false);
		try {
			Date CurrrentTime = new Date();
			SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm a zzz");
			dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
			dateFormat.format(CurrrentTime);
			Date ExpirationTime = null;
			try {
				ExpirationTime = dateFormat.parse(ExpirationResponse);
			} catch (Exception e1) {
				e1.printStackTrace();
			};
			
			Thread currentThread = Thread.currentThread();
			long ThreadID = currentThread.getId();
			SimpleDateFormat TF = new SimpleDateFormat("HH:mm:ss");
			while (ExpirationTime.compareTo(CurrrentTime) == 1){
				Helper_Functions.Wait(60);
				CurrrentTime = new Date();
				System.out.println("(" + ThreadID + " C:" + TF.format(CurrrentTime) + "->E:" + TF.format(ExpirationTime) + ") Waiting for Expiration " + OrgName );   //added to keep watch from gui and see progress. Will not update into file
			};
			Helper_Functions.Wait(60);
			dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
			dateFormat.format(CurrrentTime);
			Helper_Functions.PrintOut("Attempting to validate after expiraiton time has passed.\nCurrent Time: " + CurrrentTime, true);
			//Test verify pin on valid request from the different org
			Response = MFAC_API_Endpoints.VerifyPinAPI(UserName, OrgName, Pin, VerifyURL, OAuth_Token);
			assertThat(Response, containsString(Expected));//expected will either be success of pin failure based on scenario.
		}catch (Exception e) {
			Assert.fail(e.getMessage());
		}
	}

	@SuppressWarnings("deprecation")  //add due to the date comparison 
	@Test(dataProvider = "dp", enabled = TestExpiration, priority = 1)
	public void MFAC_AdditionalEnrollmentExpiration(String Level, String OrgName, String SecondOrg, String OAuth_Token, String IssueURL, String VerifyURL){
		Helper_Functions.PrintOut("Verify that the user recieves the updated expiration time when changing enrollment method.", false);
		
		String Response = null, UserName = UserName(), Pin = null;
		
		try {
			Response = MFAC_API_Endpoints.IssuePinAPI(UserName, OrgName, IssueURL, OAuth_Token);
			assertThat(Response, containsString("pinExpirationDate"));
			
			Date CurrrentTime = new Date();
			SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm a zzz");
			dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
			dateFormat.format(CurrrentTime);
			Date ExpirationTime = GetExpiration(Response);
			Date SecondExpirationTime = null;
			if (ExpirationTime.getDate() == CurrrentTime.getDate() && ExpirationTime.getMonth() == CurrrentTime.getMonth()) {
				Response =  MFAC_API_Endpoints.IssuePinAPI(UserName, SecondOrg, IssueURL, OAuth_Token);
				assertThat(Response, containsString("pinExpirationDate"));
				Pin = ParsePIN(Response);
				SecondExpirationTime = GetExpiration(Response);
			}

			if (SecondExpirationTime == null) {
				String LongExpirationMessage = "Not Validing the Expiraiton at this time, need to verify seperatly once the expiration has passed. Here is the CST time it will expire. " + ExpirationTime;
				Helper_Functions.PrintOut(LongExpirationMessage, true);
			}else if (SecondExpirationTime.compareTo(ExpirationTime) == 1) { //make sure second expiration is after initial would expire
				String Expire[] = new String[] {Level, "MFAC_AdditionalEnrollmentExpiration: " + Response, dateFormat.format(ExpirationTime).toString(), UserName, SecondOrg, Pin, VerifyURL, OAuth_Token, "Success"};
				ExpirationData.add(Expire);
			}else {
				//Test verify pin on valid request from the different org, this will not wait for the old one to have expired.
				Response = MFAC_API_Endpoints.VerifyPinAPI(UserName, SecondOrg, Pin, VerifyURL, OAuth_Token);
				assertThat(Response, containsString("Success"));
			}
		}catch (Exception e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test(dataProvider = "dp",enabled = TestExpiration, dependsOnMethods = "MFAC_AdditionalEnrollmentExpiration", priority = 3)
	public void MFAC_AdditionalEnrollmentExpirationValidate(String Level, String Result, String ExpirationResponse, String UserName, String OrgName, String Pin, String VerifyURL, String OAuth_Token, String Expected){
		String Response = null;
		Helper_Functions.PrintOut("Verify that the user can switch enrollment methods mid process and still complete registration.", false);
		try {
			Date CurrrentTime = new Date();
			SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm a zzz");
			dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
			dateFormat.format(CurrrentTime);
			Date ExpirationTime = null;
			try {
				ExpirationTime = dateFormat.parse(ExpirationResponse);
			} catch (Exception e1) {
				e1.printStackTrace();
			};
			
			Thread currentThread = Thread.currentThread();
			long ThreadID = currentThread.getId();
			SimpleDateFormat TF = new SimpleDateFormat("HH:mm:ss");
			while (ExpirationTime.compareTo(CurrrentTime) == 1){
				Helper_Functions.Wait(60);
				CurrrentTime = new Date();
				System.out.println("(" + ThreadID + " C:" + TF.format(CurrrentTime) + "->E:" + TF.format(ExpirationTime) + ") Waiting for Expiration " + OrgName );   //added to keep watch from gui and see progress. Will not update into file
			};
			Helper_Functions.Wait(60);
			dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
			dateFormat.format(CurrrentTime);
			Helper_Functions.PrintOut("Attempting to validate after expiraiton time has passed.\nCurrent Time: " + CurrrentTime, true);
			//Test verify pin on valid request from the different org
			Response = MFAC_API_Endpoints.VerifyPinAPI(UserName, OrgName, Pin, VerifyURL, OAuth_Token);
			assertThat(Response, containsString(Expected));//expected will either be success of pin failure based on scenario.
		}catch (Exception e) {
			Assert.fail(e.getMessage());
		}
	}
	
	///////////////////////////////////METHODS//////////////////////////////////
	public static String ParsePIN(String s) {
		if(s.contains("pinOTP") && s.contains("pinExpirationDate")) {
			return s.substring(s.indexOf("pinOTP\":") + 9, s.indexOf("\",\"pinExpirationDate"));
		}
		return null;
	}
	
	public static Date GetExpiration(String s) {
		StringTokenizer st = new StringTokenizer(s,"\"");  
		while (st.hasMoreElements()) {
			if (st.nextToken().contentEquals("pinExpirationDate")) { //pinExpirationDate":"04/03/2018 02:50 PM GMT"
				st.nextToken();//Move past the ":" token
				break;
			}
		}
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm a zzz");
		dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		Date ExpirationTime = null; 
		String ExpirationString = null;
		try {
			ExpirationString = st.nextToken();
			ExpirationTime = dateFormat.parse(ExpirationString);
		}catch (Exception e) {
			Assert.fail("Expiration time is not in valid format." + ExpirationString);
		}//		4/03/2018 02:50 PM GMT
		return ExpirationTime;
	}
	
	///////Helper Functions///////////////	
	public static String UserName() {
		return Helper_Functions.getRandomString(10) + "-" + Helper_Functions.getRandomString(24);
	}
	
	public static int IssuePinExternal(String UserName, String OrgName){
		if (OrgName.contentEquals("SMS")) {
			OrgName = "FDM-PHONE-PIN";
		}else if (OrgName.contentEquals("POSTAL")) {
			OrgName = "FDM-POSTCARD-PIN";
		}
		String Level = Environment.getInstance().getLevel();
		MFAC_Data c = MFAC_Data.LoadVariables(Level);
		String IssueURL = c.AIssueURL;
		String OAuth_Token = c.OAuth_Token;
		String Response = MFAC_API_Endpoints.IssuePinAPI(UserName, OrgName, IssueURL, OAuth_Token);
		//pin should be generated//pin expiration time should be present.
		assertThat(Response, CoreMatchers.allOf(containsString("pinOTP"), containsString("pinExpirationDate")));
		String Pin = ParsePIN(Response);
		
		return Integer.parseInt(Pin);
	}
	
}