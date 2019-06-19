package MFAC_Application;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.TimeZone;

import org.testng.Assert;
import org.testng.annotations.Test;

import SupportClasses.Helper_Functions;

public class MFAC_Helper_Functions {
	
	@Test(dataProvider = "dp", enabled = false)
	public void DetermineLockoutTime(String OrgName, String OAuth_Token, String IssueURL){
		String Response = null, UserName = UserName();
		int ExpiraitonMinutes = -1, PinThreshold = -1;
		do {
			Response = MFAC_API_Endpoints.IssuePinAPI(UserName, OrgName, IssueURL, OAuth_Token);
			PinThreshold++;
		}while (Response.contains("pinExpirationDate"));
		Helper_Functions.PrintOut(OrgName + " " + IssueURL + "  Can request " + PinThreshold + " pins before being lockout out.", true);
			
		do {
			Response = MFAC_API_Endpoints.IssuePinAPI(UserName, OrgName, IssueURL, OAuth_Token);
			ExpiraitonMinutes++;
			Helper_Functions.PrintOut("Sleeping for a minute", true);
			Helper_Functions.Wait(60);
		}while (!Response.contains("pinExpirationDate"));
		Helper_Functions.PrintOut(OrgName + " " + IssueURL + "Can request additional pins after " + ExpiraitonMinutes + " minutes. ", true);
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

}
