package ADAT_Application;

import static org.junit.Assert.assertThat;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import org.hamcrest.CoreMatchers;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import Data_Structures.ADAT_Data;
import SupportClasses.Environment;
import SupportClasses.Helper_Functions;

//Listener needed for using overwrites for report generation and 
@Listeners(SupportClasses.TestNG_TestListener.class)

public class ADAT_SmokeTest {

	//will parse this string and run all the levels listed in the data provider.
	static String LevelsToTest = "23";	
	
	@BeforeClass
	public void beforeClass() {
		Environment.SetLevelsToTest(LevelsToTest);
	}
	
	@DataProvider (parallel = true)
	public static Iterator<Object[]> dp (Method m) {
		List<Object[]> data = new ArrayList<Object[]>();
		for (int i = 0; i < Environment.LevelsToTest.length(); i++) {
			String Level = String.valueOf(Environment.LevelsToTest.charAt(i));
			if (Level.contains("6")) {
				Helper_Functions.PrintOut("Need to make sure to use Tunnel for L6 execution due to firewall", false);
			}
			ADAT_Data DC = ADAT_Data.LoadVariables(Level);
			String Organizations[] = new String[] {
					DC.OrgPostcard, 
					DC.OrgPhone
					};
			for (String Org: Organizations) {
				switch (m.getName()) { //Based on the method that is being called the array list will be populated.
				case "ADAT_CreateUser":
				case "ADAT_CreatePin":
				case "ADAT_AddressVelocity":
				case "ADAT_PostcardVelocity":
				case "ADAT_PhoneVelocity":	
				case "ADAT_VerifyPin":
				case "ADAT_VerifyPin_Invalid":
					data.add( new Object[] {Level, DC, Org});
					break;
				}
			}
		}
		System.out.println("Starting " + m.getName() + " : There are " + data.size() + " scenarios.");
		return data.iterator();
	}
	
	@Test(dataProvider = "dp", priority = 1)
	public void ADAT_CreateUser(String Level, ADAT_Data ADAT_Info, String Organization) throws Exception {
		//Using the assumption the organization will be in the "FDM-??-PIN" format
		String UserName = Helper_Functions.LoadUserID("L" + Level + Organization.substring(4 , Organization.indexOf("-PIN"))) + "-" + Helper_Functions.CurrentDateTime();
		String Response = ADAT_Endpoints.ADAT_UserCreation(ADAT_Info.CreateUserUrl, Organization, UserName);
		
        String Response_Variables[] = new String[] {"udsTransactionID", "The operation was successful!"};
        for (String Variable: Response_Variables) {
			assertThat(Response, CoreMatchers.containsString(Variable));
		}
        String Result[] = new String[] {Level, Organization, UserName};
		Helper_Functions.PrintOut(Arrays.toString(Result), false);
	}
	
	@Test(dataProvider = "dp", priority = 2)
	public void ADAT_CreatePin(String Level, ADAT_Data ADAT_Info, String Organization) throws Exception {
		//create the user to do the pin validation. Using the assumption the organization will be in the "FDM-??-PIN" format
		String UserName = Helper_Functions.LoadUserID("L" + Level + Organization.substring(4 , Organization.indexOf("-PIN")));
		ADAT_Endpoints.ADAT_UserCreation(ADAT_Info.CreateUserUrl, Organization, UserName);
		
		String Response = ADAT_Endpoints.ADAT_PinCreation(ADAT_Info.CreatePinUrl, Organization, UserName);
		
        String Response_Variables[] = new String[] {Organization, "createTime", "validityStartTime", "validityEndTime", "otp"};
        for (String Variable: Response_Variables) {
			assertThat(Response, CoreMatchers.containsString(Variable));
		}
        String Result[] = new String[] {Level, Organization, UserName, ADAT_Endpoints.ParsePin(Response)};
		Helper_Functions.PrintOut(Arrays.toString(Result), false);
	}
	
	@Test(dataProvider = "dp", priority = 3)
	public void ADAT_AddressVelocity(String Level, ADAT_Data ADAT_Info, String Organization) throws Exception {
		String UserName = Helper_Functions.LoadUserID("L" + Level + "AddVel");
		ADAT_Endpoints.VelocityCheck(UserName, ADAT_Info.CreateUserUrl, ADAT_Info.VelocityUrl, Organization, ADAT_Info.OrgAddressVelocity, ADAT_Info.AddressVelocityThreshold);
		
		String Result[] = new String[] {Level, Organization, String.valueOf(ADAT_Info.AddressVelocityThreshold)};
		Helper_Functions.PrintOut(Arrays.toString(Result), false);
	}
	
	@Test(dataProvider = "dp", priority = 3)
	public void ADAT_PostcardVelocity(String Level, ADAT_Data ADAT_Info, String Organization) throws Exception {
		String UserName = Helper_Functions.LoadUserID("L" + Level + "PostVel");
		ADAT_Endpoints.VelocityCheck(UserName, ADAT_Info.CreateUserUrl, ADAT_Info.VelocityUrl, Organization, ADAT_Info.OrgPostcardVelocity, ADAT_Info.PostcardPinVelocityThreshold);
	
		String Result[] = new String[] {Level, Organization, String.valueOf(ADAT_Info.PostcardPinVelocityThreshold)};
		Helper_Functions.PrintOut(Arrays.toString(Result), false);
	}
	
	@Test(dataProvider = "dp", priority = 3)
	public void ADAT_PhoneVelocity(String Level, ADAT_Data ADAT_Info, String Organization) throws Exception {
		String UserName = Helper_Functions.LoadUserID("L" + Level + "PhoneVel");
		ADAT_Endpoints.VelocityCheck(UserName, ADAT_Info.CreateUserUrl, ADAT_Info.VelocityUrl, Organization, ADAT_Info.OrgPhoneVelocity, ADAT_Info.PhonePinVelocityThreshold);
	
		String Result[] = new String[] {Level, Organization, String.valueOf(ADAT_Info.PhonePinVelocityThreshold)};
		Helper_Functions.PrintOut(Arrays.toString(Result), false);
	}
	
	@Test(dataProvider = "dp", priority = 4)
	public void ADAT_VerifyPin(String Level, ADAT_Data ADAT_Info, String Organization) throws Exception {
		//create the user to do the pin validation. Using the assumption the organization will be in the "FDM-??-PIN" format
		String UserName = Helper_Functions.LoadUserID("L" + Level + Organization.substring(4 , Organization.indexOf("-PIN")));
		ADAT_Endpoints.ADAT_UserCreation(ADAT_Info.CreateUserUrl, Organization, UserName);

		String Response = ADAT_Endpoints.ADAT_PinCreation(ADAT_Info.CreatePinUrl, Organization, UserName);
		String Pin = ADAT_Endpoints.ParsePin(Response);
		
		Response = ADAT_Endpoints.ADAT_VerifyPin(ADAT_Info.VerifyPinUrl, Organization, UserName, ADAT_Endpoints.ParsePin(Response));

        String Response_Variables[] = new String[] {"transactionID", "message", "<cx:message>The operation was successful.</cx:message>"};
        for (String Variable: Response_Variables) {
			assertThat(Response, CoreMatchers.containsString(Variable));
		}
        
        String Result[] = new String[] {Level, Organization, UserName, Organization, Pin};
		Helper_Functions.PrintOut(Arrays.toString(Result), false);
	}
	
	@Test(dataProvider = "dp", priority = 4)
	public void ADAT_VerifyPin_Invalid(String Level, ADAT_Data ADAT_Info, String Organization) throws Exception {
		//create the user to do the pin validation. Using the assumption the organization will be in the "FDM-??-PIN" format
		String UserName = Helper_Functions.LoadUserID("L" + Level + Organization.substring(4 , Organization.indexOf("-PIN")));
		ADAT_Endpoints.ADAT_UserCreation(ADAT_Info.CreateUserUrl, Organization, UserName);

		String Response = ADAT_Endpoints.ADAT_PinCreation(ADAT_Info.CreatePinUrl, Organization, UserName);
		
		Response = ADAT_Endpoints.ADAT_VerifyPin(ADAT_Info.VerifyPinUrl, Organization, UserName, ADAT_Endpoints.ParsePin(Response) + "1");
		assertThat(Response, CoreMatchers.containsString("400Bad"));
		
		String Result[] = new String[] {Level, Organization, UserName, "400Bad as expected"};
		Helper_Functions.PrintOut(Arrays.toString(Result), false);
	}
	
	
}