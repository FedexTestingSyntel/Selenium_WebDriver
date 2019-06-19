package SupportClasses;

import java.lang.reflect.Method;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Pattern;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.junit.AfterClass;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import Data_Structures.Account_Data;

//import org.testng.annotations.Listeners;
//@Listeners(SupportClasses.TestNG_TestListener.class)

public class Account_Lookup extends Helper_Functions{
	static String LevelToTest = "3";
	static Account_Data AllAddresses[] = Environment.getAddressDetails();

	@BeforeClass
	public void beforeClass() {
		Environment.SetLevelsToTest(LevelToTest);
	}
	
	@AfterClass
	public void afterClass() {
		AllAddresses = null;
	}
	
	@DataProvider (parallel = true)
	public static Iterator<Object[]> dp(Method m) {
		List<Object[]> data = new ArrayList<Object[]>();
		
		for (int i=0; i < Environment.LevelsToTest.length(); i++) {
			String Level = String.valueOf(Environment.LevelsToTest.charAt(i));
			//int intLevel = Integer.parseInt(Level);
			Account_Data Existing_Account_Numbers[] = Environment.getAccountDetails(Level);
			switch (m.getName()) { //Based on the method that is being called the array list will be populated.
    			case "AccountCheck":
    				String AccountsNumbers[] = ParsedAcconts("600208691, 600208713, 600208730, 600208756, 600208772, 600208799, 600208810, 600208837, 600208853, 600208870");
    				String AccountsAlreadyPresent = "", PendingAdd = "{\"";
    				for (int j = 0; j < Existing_Account_Numbers.length - 1; j++) {
    					AccountsAlreadyPresent+= Existing_Account_Numbers[j].Account_Number + " ";
    				}
    				for (String S: AccountsNumbers) {
    					if (!AccountsAlreadyPresent.contains(S)) {
    						data.add( new Object[] {LevelToTest, S});
    						PendingAdd+= S + "\", \"";
    					}
    				}	
    				PrintOut(data.size() + "\n" + PendingAdd, false);
    				break;
    			case "Update_Account_Details":	
    				for (int j = 0; j < Existing_Account_Numbers.length - 1; j++) {
    					//if (Existing_Account_Numbers[j].Credit_Card_Number.contentEquals("")) {
    						data.add( new Object[] {Existing_Account_Numbers[j].Level, Existing_Account_Numbers[j].Account_Number});
    					//}
    				}
    				break;
			}
		}
		return data.iterator();
	}
	
	@Test(dataProvider = "dp", enabled = true)
	public static void AccountCheck(String Level, String Account_Number){
		try {
			Account_Data Account_Info[] = new Account_Data[] {Account_Details(Account_Number, Level)};
			Account_Data.Write_Accounts_To_Excel(Account_Info, true);
			
		} catch (Exception e) {
			Assert.fail("Cannot retrieve details");
		}
	}
	
	//this is a cleanup function. The goal is to go through all the account numbers from the test data sheet and make sure still present as well as update the values.
	@Test(dataProvider = "dp", enabled = false)
	public static void Update_Account_Details(String Level, String Account_Number){
		try {
			Account_Data Account_Info[] = new Account_Data[] {Account_Details(Account_Number, Level)};
			boolean updated = Account_Data.Write_Accounts_To_Excel(Account_Info, false);
			Assert.assertTrue(updated);
		} catch (Exception e) {
			Assert.fail("Cannot retrieve details");
		}
	}
	
	public static Account_Data Account_Details(String AccountNumber, String Level) throws Exception{
		Account_Data Account_Info = new Account_Data();
		Account_Info.Account_Number = AccountNumber;
		Account_Info.Level = Level;
		//get the address for the account number
		Account_Info = Account_Data_Address_Lookup(Account_Info);
		//get if the user is linked to credit card or invoice.
		Account_Info = Account_Data_CreditDetail_Lookup(Account_Info);
		Account_Data.Print_Account_Address(Account_Info);
		
		return Account_Info;
	}
	
	public static String[] ParsedAcconts(String AccountNumbers) {
		AccountNumbers = AccountNumbers.replaceAll(",", "");
		AccountNumbers = AccountNumbers + " ";
		AccountNumbers.replaceAll("  ", " ");
		StringTokenizer st1 = new StringTokenizer(AccountNumbers, " "); 
		List<String> list = new ArrayList<String>();
		while (st1.hasMoreTokens()) {
			list.add(st1.nextToken());
		}
		String[] stringArray = list.toArray(new String[0]);
		return stringArray;
	}
	
	public static Account_Data Account_Data_Address_Lookup(Account_Data Account_Info) throws Exception{
  		try{
  			HttpPost httppost = new HttpPost("http://vjb00030.ute.fedex.com:7085/cfCDSTestApp/contact.jsp");

  			httppost.addHeader("Content-Type", "application/x-www-form-urlencoded");
  			httppost.addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3");

  			String OpCo[] = new String[] {"FX", "FDFR"};
  			String Level = "L" + Account_Info.Level;
  			
  			if (Level.contains("L6")){
  				PrintOut("Cannot see account numbers directly in L6, attempting in L3.", true);
  				Level = "L3";
  			}else if (Level.contains("L7")){
  				PrintOut("Cannot see account numbers directly in LP, attempting in L4.", true);
  				Level = "L4";
  			}
  			
  			
  			String SourceText = "";
  			for (int i = 0 ; i < 2; i++) {
  	  			List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
  	  			urlParameters.add(new BasicNameValuePair("contact", "accountContact"));
  	  			urlParameters.add(new BasicNameValuePair("contactAccountNumber", Account_Info.Account_Number));
  	  			urlParameters.add(new BasicNameValuePair("contactAccountOpCo", OpCo[i]));
  	  			urlParameters.add(new BasicNameValuePair("contactAccountContactType", ""));
  	  			urlParameters.add(new BasicNameValuePair("contactAccountSubmit", "submit"));
  	  			urlParameters.add(new BasicNameValuePair("contactLevel", Level));

  	  			httppost.setEntity(new UrlEncodedFormEntity(urlParameters));
  	  			
  	  			HttpEntity entity = httppost.getEntity();
  	  			String Request = EntityUtils.toString(entity, "UTF-8");
  	  			String Response = General_API_Calls.HTTPCall(httppost, Request);	
  	  			SourceText = Response;
  	  			
  	  			//if details not found try next OpCo
  	  			if (SourceText.contains("streetLine")) {
  	  				break;
  	  			}else if (i == OpCo.length - 1) {
  	  				Helper_Functions.PrintOut("Unable to find address details.");
  	  			}
  			}
  			
  			String start = "name = \"", end  = "\" value=\"";
  			int shareId = 0, streetline1 = 1, streetline2 = 2, city = 3, statecode = 4, postalcode = 5, countrycode = 6, areacode = 7, phoneNumber = 8, language = 9, firstName = 10, lastName = 11; 
			String AccountDetails[][] = {{"shareId", "shareId>\">", ""},
					{"streetline1", "streetLine>\">", ""}, 
					{"streetline2", "additionalLine1>\">", ""}, 
					{"city", "geoPoliticalSubdivision2>\">", ""}, 
					{"statecode", "geoPoliticalSubdivision3>\">", ""}, 
					{"postalcode", "postalCode>\">", ""}, 
					{"countrycode", "countryCode>\">", ""}, 
					{"areacode", "areaCode>\">", ""},
					{"phoneNumber", "phoneNumber>\">", ""}, 
					{"language", "<language>\">", ""}, 
					{"firstName", "<firstName>\">", ""}, 
					{"lastName", "<lastName>\">", ""}, 
					};
			

			for (int j = 0; j < 2; j++) {
	  			for (int k = 0 ; k < AccountDetails.length; k++) {
	  				String StartingPoint = AccountDetails[k][1];
	  				int intStartingPoint = SourceText.indexOf(StartingPoint) + StartingPoint.length();
	  				SourceText = SourceText.substring(intStartingPoint, SourceText.length());
	  				//make sure something was found.
	  				if (intStartingPoint > StartingPoint.length()){
	  					int s = SourceText.indexOf(start) + start.length(), e = SourceText.indexOf(end);
	  					AccountDetails[k][2] = SourceText.substring(s, e);
	  				}
	  			}

	  			//remove special characters
  				for (int i = 0; i < AccountDetails.length; i++){
  					String nfdNormalizedString = Normalizer.normalize(AccountDetails[i][2], Normalizer.Form.NFD); 
  					Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
  					AccountDetails[i][2] = pattern.matcher(nfdNormalizedString).replaceAll("");
  				}
  				
  				//If the zip is greater then 5 and this is for US address then only return first 5 characters
  				if (AccountDetails[postalcode][2].length() > 5 && AccountDetails[countrycode][2].contentEquals("US")){
  					AccountDetails[postalcode][2] = AccountDetails[postalcode][2].substring(0, 5);
				}
  				
  	  			if (j == 0) {//shipping address
  	  				Account_Info.Shipping_Address_Info.Share_Id = AccountDetails[shareId][2];
  					Account_Info.Shipping_Address_Info.Address_Line_1 = AccountDetails[streetline1][2];
  					Account_Info.Shipping_Address_Info.Address_Line_2 = AccountDetails[streetline2][2];
  					Account_Info.Shipping_Address_Info.City = AccountDetails[city][2];
  					Account_Info.Shipping_Address_Info.State_Code = AccountDetails[statecode][2];
  					Account_Info.Shipping_Address_Info.Zip = AccountDetails[postalcode][2];
  					Account_Info.Shipping_Address_Info.Country_Code = AccountDetails[countrycode][2];
  					Account_Info.Shipping_Address_Info.Phone_Number = AccountDetails[areacode][2] + AccountDetails[phoneNumber][2];		
  				}else {//billing address
  					Account_Info.Billing_Address_Info.Share_Id = AccountDetails[shareId][2];
  					Account_Info.Billing_Address_Info.Address_Line_1 = AccountDetails[streetline1][2];
  					Account_Info.Billing_Address_Info.Address_Line_2 = AccountDetails[streetline2][2];
  					Account_Info.Billing_Address_Info.City = AccountDetails[city][2];
  					Account_Info.Billing_Address_Info.State_Code = AccountDetails[statecode][2];
  					Account_Info.Billing_Address_Info.Zip = AccountDetails[postalcode][2];
  					Account_Info.Billing_Address_Info.Country_Code = AccountDetails[countrycode][2];
  					Account_Info.Billing_Address_Info.Phone_Number = AccountDetails[areacode][2] + AccountDetails[phoneNumber][2];
  				}

  	  			if (!AccountDetails[language][2].contentEquals("")) {
  	  				Account_Info.LanguageCode = AccountDetails[language][2];
  	  			}
  	  			if (!AccountDetails[firstName][2].contentEquals("")) {
	  				Account_Info.FirstName = AccountDetails[firstName][2];
	  			}
  	  			if (!AccountDetails[lastName][2].contentEquals("")) {
  	  				Account_Info.LastName = AccountDetails[lastName][2];
  	  			}
			}

  			//make sure country is valid
  			if (Account_Info.Billing_Address_Info.Country_Code.length() > 5 || Account_Info.Billing_Address_Info.Country_Code.length() == 0){
				return null;
			}else {
  				//check and add the region and country name
				if (AllAddresses == null) {
					AllAddresses = Environment.getAddressDetails();
				}
				for (Account_Data Address: AllAddresses) {
					boolean Billing = false, Shipping = false;
					if (!Billing && Address != null && Address.Billing_Address_Info.Country_Code.contentEquals(AccountDetails[countrycode][2])) {
						Account_Info.Billing_Address_Info.Region = Address.Billing_Address_Info.Region;
	  					Account_Info.Billing_Address_Info.Country = Address.Billing_Address_Info.Country;
	  					Billing = true;
					}
					if (!Shipping && Address != null && Address.Shipping_Address_Info.Country_Code.contentEquals(AccountDetails[countrycode][2])) {
						Account_Info.Shipping_Address_Info.Region = Address.Shipping_Address_Info.Region;
	  					Account_Info.Shipping_Address_Info.Country = Address.Shipping_Address_Info.Country;
	  					Shipping = true;
					}
					if (Billing && Shipping) {
						break;
					}
				}
			}
  			
  			Account_Info.Account_Nickname = Account_Info.Account_Number + "_" + Account_Info.Billing_Address_Info.Country_Code;
  			Account_Info.Masked_Account_Number = Account_Info.Account_Nickname + " - " + Account_Info.Account_Number.substring(Account_Info.Account_Number.length() - 3, Account_Info.Account_Number.length());
			
  			return Account_Info;
  		}catch (Exception e){
  			e.printStackTrace();
  			throw e;
  		}
  	}

	public static Account_Data Account_Data_CreditDetail_Lookup(Account_Data Account_Info) throws Exception{
  		try{
  			HttpPost httppost = new HttpPost("http://vjb00030.ute.fedex.com:7085/cfCDSTestApp/express.jsp");

  			httppost.addHeader("Content-Type", "application/x-www-form-urlencoded");
  			httppost.addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3");

  			String Level = "L" + Account_Info.Level;
  			if (Level.contains("L6")){
  				PrintOut("Cannot see account numbers directly in L6, attempting in L3.", true);
  				Level = "L3";
  			}else if (Level.contains("L7")){
  				PrintOut("Cannot see account numbers directly in LP, attempting in L4.", true);
  				Level = "L4";
  			}
  			
  			String SourceText = "";
  	  		List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
  	  		urlParameters.add(new BasicNameValuePair("express", "expressCreditCard"));
  	  		urlParameters.add(new BasicNameValuePair("expressAccountNumber", Account_Info.Account_Number));
  	  		urlParameters.add(new BasicNameValuePair("expressSubmit", "submit"));
  	  		urlParameters.add(new BasicNameValuePair("expressLevel", Level));
  	  			
  	  		httppost.setEntity(new UrlEncodedFormEntity(urlParameters));
  	  			
  	  		HttpEntity entity = httppost.getEntity();
  	  		String Request = EntityUtils.toString(entity, "UTF-8");
  	  		String Response = General_API_Calls.HTTPCall(httppost, Request);	
  	  		SourceText = Response;
  	  		
  			String start = "name = \"", end  = "\" value=\"";
			String AccountDetails[][] = {{"CreditCardType", "<type>", ""}, 
					{"creditCardId", "<creditCardId>", ""}, 
					{"expDateMonth", "<expDateMonth>", ""}, 
					{"expDateYear", "<expDateYear>", ""},  
					};
			

	  		for (int k = 0 ; k < AccountDetails.length; k++) {
	  			String StartingPoint = AccountDetails[k][1];
	  			int intStartingPoint = SourceText.indexOf(StartingPoint) + StartingPoint.length();
	  			SourceText = SourceText.substring(intStartingPoint, SourceText.length());
	  			if (intStartingPoint > StartingPoint.length()){
	  				int s = SourceText.indexOf(start) + start.length(), e = SourceText.indexOf(end);
	  				AccountDetails[k][2] = SourceText.substring(s, e);
	  			}
	  		}
	  			
	  		if (!AccountDetails[0][2].contentEquals("")) {
	  			//since found the account is linked to a credit card reference the excel sheet and find the full credit card details.
	  			String Last_Four_Digits = AccountDetails[1][2].substring(AccountDetails[1][2].length() - 4, AccountDetails[1][2].length());
				String Credit_Lookup[] = Helper_Functions.LoadCreditCard(Last_Four_Digits);
				if (Credit_Lookup != null && Credit_Lookup[1].substring(Credit_Lookup[1].length() - 4, Credit_Lookup[1].length()).contains(Last_Four_Digits)) {
					Account_Info.Credit_Card_Type = Credit_Lookup[0];
					Account_Info.Credit_Card_Number = Credit_Lookup[1];
					Account_Info.Credit_Card_CVV = Credit_Lookup[2];
				}else {
					//when the CC linked to the account is now known from the test data sheet.
					Account_Info.Credit_Card_Type = AccountDetails[0][2];
					Account_Info.Credit_Card_Number = AccountDetails[1][2];
					Account_Info.Credit_Card_CVV = "???";
				}
				
				Account_Info.Credit_Card_Expiration_Month = AccountDetails[2][2];
				//last 2 digits of the year.
				Account_Info.Credit_Card_Expiration_Year = AccountDetails[3][2].substring(AccountDetails[3][2].length() - 2, AccountDetails[3][2].length());
	  		}else {
				//load dummy invoice numbers
	  			Account_Info.Invoice_Number_A = "750000000";
	  			Account_Info.Invoice_Number_B = "750000001";
			}
			
  			return Account_Info;
  		}catch (Exception e){
  			e.printStackTrace();
  			throw e;
  		}
  	}
	
}
