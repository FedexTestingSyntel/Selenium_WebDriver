package Data_Structures;

import java.util.ArrayList;

import SupportClasses.General_API_Calls;

public class USRC_Data {
	public String OAuth_Token_URL = "";
	public String OAuth_Token_Client_ID = "";
	public String OAuth_Token_Client_Secret = "";
	public String OAuth_Token = "";
	public String Level = "";
	
	public String EnrollmentURL = "";
	public String CreatePinURL = "";
	public String PendingAddressURL = "";
	public String VerifyPinURL = "";
	public String CancelEnrollmentURL = "";
	public String GenericUSRCURL = ""; 
	public String REGCCreateNewUserURL = "";
	public String ViewUserProfileWIDMURL = "";
	public String UpdateUserContactInformationWIDMURL ="";
	
	public String FDMSMS_PinType = "SMS";
	public String FDMPostcard_PinType = "POSTAL";
	
	public static ArrayList<String[]> ContactDetailsList = new ArrayList<String[]>();

	//Stores the data for each individual level
	private static USRC_Data DataClass[] = new USRC_Data[8];
	
	public static USRC_Data LoadVariables(String Level){
		int intLevel = Integer.parseInt(Level);
		//if the level details were already loaded then return detail.
		if (DataClass[intLevel] != null) {
			return DataClass[intLevel];
		}
		
		//since the level details have not been loaded load them.
		USRC_Data DC = new USRC_Data();
		DC.Level = Level;
		
		String LevelIdentifier[] = null;
  		switch (Level) {
  		case "1":		//expand to user direct endpoints later as needed
  			LevelIdentifier = new String[] {"", ""}; break;
  		case "2":
  			LevelIdentifier = new String[] {"https://apidev.idev.fedex.com:8443", ""}; break;
  		case "3":
  			LevelIdentifier = new String[] {"https://apidrt.idev.fedex.com:8443", ""}; break;
  		case "4":
  			LevelIdentifier = new String[] {"https://apistress.idev.fedex.com", ""}; break;
  		case "5":
  			LevelIdentifier = new String[] {"https://apibit.idev.fedex.com:8443", ""}; break;
  		case "6":
  			//L6 is not valid for direct URL
  			LevelIdentifier = new String[] {"https://apitest.fedex.com", ""}; break;
  		case "7":
  			//L7 is not valid for direct URL
  			LevelIdentifier = new String[] {"https://api.fedex.com", ""}; break;
		}
  		
  		DC.OAuth_Token_URL = LevelIdentifier[0] + "/auth/oauth/v2/token";
  		
  		//Load the API URLs
  		if (Level != "1") {//API not applicable to L1
  			DC.EnrollmentURL = LevelIdentifier[0] + "/deliverymanager/v1/enrollment";
  			DC.CreatePinURL = LevelIdentifier[0] + "/deliverymanager/v1/pin";
  			DC.PendingAddressURL = LevelIdentifier[0] + "/deliverymanager/v1/addresses/pending";
  			DC.VerifyPinURL = LevelIdentifier[0] + "/deliverymanager/v2/enrollment/pin";
  			DC.CancelEnrollmentURL = LevelIdentifier[0] + "/deliverymanager/v1/deliveryoptions/cancel";
  		}
  		
  		//currently uses a generic URL.
  		String GenericLevel = LevelUrlReturn(Level);
  		DC.GenericUSRCURL = GenericLevel + "/userCal/user";	
  		DC.REGCCreateNewUserURL = GenericLevel + "/regcal/registration/newfcluser";
  		DC.ViewUserProfileWIDMURL = GenericLevel + "/userCal/rest/v2/ViewUserProfileWIDM";
  		DC.UpdateUserContactInformationWIDMURL = GenericLevel + "/userCal/rest/v2/UpdateUserContactInformationWIDM";
  		
		switch (Level) { //Based on the method that is being called the array list will be populated. This will make the TestNG Pass/Fail results more relevant.
		case "1":
			DC.OAuth_Token_Client_ID = "l7xx1892f99a6f88470ba29abc141cd7bd8d";
			DC.OAuth_Token_Client_Secret ="a4325d011acf4876b3fe3206931b8f5a";
			break;
		case "2":
			DC.OAuth_Token_Client_ID = "l7xx1892f99a6f88470ba29abc141cd7bd8d";
			DC.OAuth_Token_Client_Secret ="a4325d011acf4876b3fe3206931b8f5a";
			break;
		case "3":
			DC.OAuth_Token_Client_ID = "l7xx1892f99a6f88470ba29abc141cd7bd8d";
			DC.OAuth_Token_Client_Secret ="a4325d011acf4876b3fe3206931b8f5a";
			break;
		case "4":
			DC.OAuth_Token_Client_ID = "l7xx4a86a91576b14d4bb7ba81f52470e48d";
			DC.OAuth_Token_Client_Secret ="bb0bc6e8fcba4813989ff50895590f30";
			break;
		case "5"://havn't used L5
			DC.OAuth_Token_Client_ID = "";
			DC.OAuth_Token_Client_Secret ="";
			break;
		case "6"://need to update these values manually, do not share
			DC.OAuth_Token_Client_ID = "l7xxde71559b0ad74896a6bf569b1c026c40";
			DC.OAuth_Token_Client_Secret = "8823413826a3482889f51fc6f5e30441";
			break;
		case "7"://need to update these values manually, do not share
			DC.OAuth_Token_Client_ID = "l7xxdd1a6d120ab84667b653bd99cd25a033";
			DC.OAuth_Token_Client_Secret ="b24b83e86c5640cf9f25baab30e99208";
			break;
		}

		DC.OAuth_Token = LoadBearerToken(DC);
		
		ArrayList<String[]> ContactList = new ArrayList<String[]>();
		String Phone = "9011111111", Email = "YouNeedToUpdateThisLater@fedex.com";
		if (Level.contentEquals("6")) {//need to user real data in L6
			//ContactList.add(new String[] {"PavanKumar", "", "Bindela", Phone, Email, "2717 W Royal Ln", "Apt 315", "Irving", "TX", "75063", "US", ""});
			ContactList.add(new String[] {"Suresh", "", "Dhandapani", Phone, Email, "880 Schilling Farm Road", "Apt 102", "Collierville", "TN", "38017", "US", ""});
		}else if (Level.contentEquals("7")) {//need to user real data in LP
			
		}else {
			//ContactList.add(new String[] {"Udaya", "", "Uriti", Phone, Email, "9427 Ruidosa Trl", "", "Irving", "TX", "75063", "US", "5g0d06szxzxyh5diyhyu4lnwi"});
			//ContactList.add(new String[] {"Resmi", "", "Raveendran", Phone, Email, "387 Main Street", "3760 QNFW", "WACO", "TX", "76712", "US", "38mynlmeqanwftczn0yyg7jlz"});
			ContactList.add(new String[] {"FIRSTNAME", "", "LASTNAME", Phone, Email, "2519 W ROYAL LN", "", "IRVING", "TX", "75063", "US", "c2cj08lvbmwkv5j1r5k38l1g"});
			ContactList.add(new String[] {"Udaya", "", "Uriti", Phone, Email, "9427 Ruidosa Trl", "", "IRVING", "TX", "75063", "US", "5g0d06szxzxyh5diyhyu4lnwi"});
			
			//These address are working from the WERL page as well         //Please note this data was setup for when using ACXIOM as the validator
			ContactList.add(new String[] {"TESTER", "", "ZCHUCKZ", Phone, Email, "32 MEADOW CREST DR", "", "SHERWOOD", "AR", "72120", "US", "6xmc5tpjhrtaymw7yfwshqfao"});//L3FDM061418T172631
			ContactList.add(new String[] {"TESTER", "", "ZTHREEZ", Phone, Email, "58 CABOT ST", "", "HARTFORD", "CT", "06112", "US", "247p0y08t50f3zed2exvaue09"});//L3FDM061418T172722
			ContactList.add(new String[] {"TESTER", "", "ZLILLYZ", Phone, Email, "9133 SUPERIOR DR","", "OLIVEBRANCH", "MS", "38654", "US", "2ojrqs935gpa1ypnicxcspwm5"}); //L3FDM061418T173123
			ContactList.add(new String[] {"TESTER", "", "ZNINEZ", Phone, Email, "75-681 LALII PL", "", "KAILUA KONA", "HI", "96740", "US", "464fn6icyb6e4iw9vetuyz5ir"}); //L3FDM061518T092155
			ContactList.add(new String[] {"TESTER", "", "ZZORANGEZ", Phone, Email, "95 HUTCHINS DR", "", "PORTLAND", "ME", "04102", "US", "3cvnkwdfotey3zldrko808dhf"}); //L3FDM061518T092633
			ContactList.add(new String[] {"TESTER", "", "ZPURPLEZ", Phone, Email, "3614 DELVERNE RD", "", "BALTIMORE", "MD", "21218", "US", "7i4ksltmhnqlxhzbs9j6356ix"}); //L3FDM061518T092659
			
			//These address will only work through direct call
			ContactList.add(new String[] {"TESTER", "", "ZFAMILYZ", Phone, Email, "891 FEDERAL RIDGE RD", "APT 202", "COLLIERVILLE", "TN", "38125", "US", "44k0o0ipf25thfcyl8svm65zz"});
			ContactList.add(new String[] {"TESTER", "", "ZTWOZ", Phone, Email, "329 MADISON ST", "", "DENVER", "CO", "80206", "US", "7hqf1rpftonlfthiifapqp45k"});
			ContactList.add(new String[] {"TESTER", "", "ZFOURZ", Phone, Email, "310 HAINES ST", "", "NEWARK", "DE", "19717", "US", "4cg4ekdc7zs9n40bhkgt3c7wj"});
			ContactList.add(new String[] {"TESTER", "", "ZFAMILYZ", Phone, Email, "3935 MISSION HILLS DR", "APT 203", "MEMPHIS", "TN", "38017", "US", "1lzav5yemhi3x440fnow7x44p"});
			ContactList.add(new String[] {"TESTER", "", "ZMAPLEZ", Phone, Email, "1203 BEARTOOTH DR","", "LAUREL", "MT", "59044", "US", "1gmjbut6uu5u6p8uy5f66u8r2"});
			ContactList.add(new String[] {"TESTER", "M", "ZRAZORBACKZ", Phone, Email, "6350 HEDGEWOOD DR", "", "ALLENTOWN", "PA", "18106", "US", "4amkkxogwfzill5yytaiun4ew"});
			ContactList.add(new String[] {"TESTER", "", "ZNINEZ", Phone, Email, "75-681 LALII PL", "", "KAILUA KONA", "HI", "96740", "US", "464fn6icyb6e4iw9vetuyz5ir"});
			ContactList.add(new String[] {"TESTER", "", "ZTENZ", Phone, Email, "1430 E 17TH ST", "", "IDAHO FALLS", "ID", "83404", "US", "6ty7pkr5m2ll10avo0jue5yop"});
			ContactList.add(new String[] {"TESTER", "", "ZELEVENZ", Phone, Email, "410 W WASHINGTON ST", "", "CASEYVILLE", "IL", "62232", "US", "3mg730s7lokdmn90hi44lgw2h"});
			ContactList.add(new String[] {"TESTER", "", "ZTHIRTEENZ", Phone, Email, "8527 UNIVERSITY BLVD", "STE 99", "DES MOINES", "IA", "50325", "US", ""});
			ContactList.add(new String[] {"TESTER", "", "ZBLUEZ", Phone, Email, "2206 URBANDALE ST", "", "SHREVEPORT", "LA", "71118", "US", "ki87l0p7fq4frpue8y45qlzv"});
			ContactList.add(new String[] {"TESTER", "", "ZZORANGEZ", Phone, Email, "95 HUTCHINS DR", "", "PORTLAND", "ME", "04102", "US", "3cvnkwdfotey3zldrko808dhf"});
			ContactList.add(new String[] {"TESTER", "", "ZPURPLEZ", Phone, Email, "3614 DELVERNE RD", "", "BALTIMORE", "MD", "21218", "US", "7i4ksltmhnqlxhzbs9j6356ix"});
		}
		ContactDetailsList = ContactList;
		
		DataClass[intLevel] = DC;
		
		return DC;
	}
	
	static String BearerTokens[] = new String[] {"", "", "", "", "", "", ""};
	public static String LoadBearerToken(USRC_Data DC){
		if (!DC.Level.contentEquals("") && !BearerTokens[Integer.parseInt(DC.Level)].contentEquals("")) {
			return BearerTokens[Integer.parseInt(DC.Level)];
		}
		//generate the OAuthToken, please note that this is not valid on L1 as API calls cannot be used on that level
		if (!DC.Level.contentEquals("1")) {
			DC.OAuth_Token = General_API_Calls.getAuthToken(DC.OAuth_Token_URL, DC.OAuth_Token_Client_ID , DC.OAuth_Token_Client_Secret);
			System.out.println("OAuth_Token : " + DC.OAuth_Token);
		}
		return DC.OAuth_Token;
	}
	
	public static String LevelUrlReturn(String level) {
  		String LevelURL = null;
  		switch (level) {
      		case "1":
      			LevelURL = "https://wwwbase.idev.fedex.com"; break;
      		case "2":
      			LevelURL = "https://wwwdev.idev.fedex.com";  break;
      		case "3":
      			LevelURL = "https://wwwdrt.idev.fedex.com"; break;
      		case "4":
      			LevelURL = "https://wwwstress.dmz.idev.fedex.com"; break;
      		case "5":
      			LevelURL = "https://wwwbit.idev.fedex.com"; break;
      		case "6":
      			LevelURL = "https://wwwtest.fedex.com"; break;
      		case "7":
      			LevelURL = "https://www.fedex.com"; break;
  		}
  		return LevelURL;
  	}

	public static String[] getContactDetails(int con) {
		if (con <= ContactDetailsList.size()){
			return ContactDetailsList.get(con);
		}else {
			return ContactDetailsList.get((con % ContactDetailsList.size()));
		}
	}
}