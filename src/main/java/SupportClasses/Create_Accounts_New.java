package SupportClasses;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import Data_Structures.Account_Data;
import Data_Structures.Credit_Card_Data;
import Data_Structures.Tax_Data;

@Listeners(SupportClasses.TestNG_TestListener.class)

public class Create_Accounts_New{
	private static String ECAMuserid;
	private static String ECAMpassword;
	
	static String LevelsToTest = "3";
	static String CountryList[][]; 
	
	@BeforeClass
	public static void ECAM_Data(){
		ArrayList<String[]> PersonalData = new ArrayList<String[]>();
		PersonalData = Helper_Functions.getExcelData(Helper_Functions.DataDirectory + "\\Load_Your_UserIds.xls",  "Data");//create your own file with the specific data
		for(String s[]: PersonalData) {
			if (s[0].contentEquals("ECAM")) {
				ECAMuserid = s[1];
				ECAMpassword = s[2];
			}
		}
		
		Environment.SetLevelsToTest(LevelsToTest);
		CountryList = Environment.getCountryList("BR");
		//CountryList = new String[][]{{"JP", ""}, {"MY", ""}, {"PH", ""}, {"SG", ""}, {"KR", ""}, {"TW", ""}, {"TH", ""}};
	}
	
	@DataProvider //(parallel = true)
	public static Iterator<Object[]> dp(Method m) {
		List<Object[]> data = new ArrayList<Object[]>();
		
		for (int i=0; i < LevelsToTest.length(); i++) {
			String Level = String.valueOf(LevelsToTest.charAt(i));
			for (int j = 0; j < CountryList.length; j++) {
				Account_Data Account_Info = Environment.getAddressDetails(Level, CountryList[j][0]);
    			data.add( new Object[] {Level, Account_Info});
			}
		}
		return data.iterator();
	}

	@Test(dataProvider = "dp", enabled = true)
	public void Account_Creation(String Level, Account_Data Account_Info) {
		try {
			String Operating_Companies = "E";
			if ("US_CA_".contains(Account_Info.Billing_Address_Info.Country_Code)) {
				Operating_Companies += "G";
			}
			if ("US_CA_MX_".contains(Account_Info.Billing_Address_Info.Country_Code)) {
				Operating_Companies += "F";
			}
			Account_Data[] Accounts = null;
			Account_Data.Print_Account_Address(Account_Info);
			Account_Info.Email = Helper_Functions.MyFakeEmail;
			Account_Info.Level = Level;
			Account_Info.Company_Name = Helper_Functions.CurrentDateTime();
			Accounts = CreateAccountNumbers(Account_Info, Operating_Companies, 1);
			writeAccountsToExcel(Accounts, Operating_Companies);

		}catch (Exception e) {
			Assert.fail(e.getMessage());
		}
	}
	
	public static Account_Data[] CreateAccountNumbers(Account_Data Account_Info, String OperatingCompanies, int NumAccounts) throws Exception{
		try {
			WebDriver_Functions.ChangeURL("ECAM", "", false);
			if (WebDriver_Functions.isPresent(By.id("username"))) {
				WebDriver_Functions.Type(By.id("username"), ECAMuserid);
				WebDriver_Functions.Type(By.id("password"), ECAMpassword);
				WebDriver_Functions.Click(By.id("submit"));
			}

			//ECAM Page
			for(int i = 0; i < 30; i++) {
				try {
					WebDriver_Functions.WaitPresent(By.id("new_act_info"));
		 			WebDriver_Functions.WaitClickable(By.id("new_act_info"));
					WebDriver_Functions.Click(By.id("new_act_info"));
					break;
				}catch (Exception e) {
					Helper_Functions.Wait(1);
				}
			}

			WebDriver_Functions.WaitPresent(By.id("acct_info_ship_countrylist"));
			String ShippingCountrCode = Account_Info.Shipping_Address_Info.Country_Code, BillingCountryCode = Account_Info.Billing_Address_Info.Country_Code;
			WebDriver_Functions.Select(By.id("acct_info_countrylist"), BillingCountryCode, "v");//billing country
			WebDriver_Functions.Select(By.id("acct_info_ship_countrylist"), ShippingCountrCode, "v");//shipping country
			WebDriver_Functions.Select(By.id("acct_info_source_grp"), "ALLIANCES","v");//the Source group of the account numbers
			
			WebDriver_Functions.WaitPresent(By.id("acctinfo_customertype"));
			String Account_Type = "BUSINESS";
			WebDriver_Functions.Select(By.id("acctinfo_customertype") , Account_Type,"v");//Customer WebDriver_Functions.Type
			
			//This next section will populate based on the country and the customer type
			if (OperatingCompanies.contains("E")) {
				WebDriver_Functions.Click(By.id("check_exp"));
				WebDriver_Functions.Select(By.id("acct_type_exp") , Account_Type,"v");//Express WebDriver_Functions.Type
			}
			if (OperatingCompanies.contains("G")) {
				WebDriver_Functions.Click(By.id("check_gnd"));
				WebDriver_Functions.Select(By.id("acct_type_exp") , Account_Type,"v");//Express WebDriver_Functions.Type
			}
			if (OperatingCompanies.contains("F")) {
				WebDriver_Functions.Click(By.id("check_fht"));
				WebDriver_Functions.Select(By.id("acct_type_fht") , "SHIPPER","v");//Freight WebDriver_Functions.Type
				WebDriver_Functions.Select(By.id("acct_sub_type_fht") , "DOCK","v");//Freight Sub WebDriver_Functions.Type
			}
			
			Account_Type += OperatingCompanies;
			
			WebDriver_Functions.Type(By.id("acctinfo_no_acct"), Integer.toString(NumAccounts)); //number of account numbers that should be created
			WebDriver_Functions.Click(By.id("next_contact"));
			
			//Account Contact Information
			WebDriver_Functions.Type(By.id("first_name"), Account_Info.FirstName);
			WebDriver_Functions.Type(By.id("last_name"), Account_Info.LastName);
			try {
				WebDriver_Functions.Select(By.id("contact_language") , "EN","v");//set the language as English
			}catch (Exception e){}
			
			WebDriver_Functions.Type(By.id("contact_phn_one"), Account_Info.Shipping_Address_Info.Phone_Number);
			WebDriver_Functions.Click(By.name("ship_radio"));
			WebDriver_Functions.Click(By.id("next_address"));
			
			//Account Address
			WebDriver_Functions.Type(By.id("acctinfo_postal_input_info"), Account_Info.Billing_Address_Info.Zip);
			WebDriver_Functions.Type(By.id("add_info_company"), Account_Info.Company_Name);
			WebDriver_Functions.Type(By.id("address_phn_number"), Account_Info.Billing_Address_Info.Phone_Number);
			WebDriver_Functions.Type(By.id("acctinfo_addr_one"), Account_Info.Billing_Address_Info.Address_Line_1);
			WebDriver_Functions.Type(By.id("acctinfo_addr_two"), Account_Info.Billing_Address_Info.Address_Line_2);
			
			//try and select the city, may be only the single city or multiple based on zip code.
			if (WebDriver_Functions.isVisable(By.id("acctinfo_city_input_info_list"))) {
				WebDriver_Functions.WaitClickable(By.id("acctinfo_city_input_info_list"));
				WebDriver_Functions.Select(By.id("acctinfo_city_input_info_list"), Account_Info.Billing_Address_Info.City.toUpperCase(), "v");
			}else if (WebDriver_Functions.isVisable(By.id("acctinfo_city_input_info_box"))) {
				WebDriver_Functions.Type(By.id("acctinfo_city_input_info_box"), Account_Info.Billing_Address_Info.City.toUpperCase());
			}
			
			//enter the state
			if (WebDriver_Functions.isVisable(By.id("acctinfo_state_input_info"))) {
				WebDriver_Functions.Select(By.id("acctinfo_state_input_info"), Account_Info.Billing_Address_Info.State_Code.toUpperCase(), "v");
			}
			
			if (WebDriver_Functions.isVisable(By.id("nomatch"))) {
				WebDriver_Functions.Click(By.id("nomatch"));
			}
			
			Helper_Functions.Wait(4); //need to remove later, issue is page not passing the regulatory section.
			if (WebDriver_Functions.isVisable(By.id("next_payment"))) {
				WebDriver_Functions.Click(By.id("next_payment"));
			}
			
			//if the address validation page is displayed.
			boolean ModifiedAddress = false;
			if(WebDriver_Functions.isVisable(By.id("adrs_val_modified"))){
				WebDriver_Functions.Click(By.id("adrs_val_modified"));
				ModifiedAddress = true;
			}else if (WebDriver_Functions.isVisable(By.id("nomatch"))){
				WebDriver_Functions.Click(By.id("nomatch"));
			}
			
			//Regulator Information page
			String StateTax = "", CountryTax = "";
			if (WebDriver_Functions.isVisable(By.id("next_reg"))) {
				if (WebDriver_Functions.isVisable(By.id("dyn_vat_lbl"))){//Vat Number
					WebDriver_Functions.Type(By.id("dyn_vat_lbl"), Account_Info.Billing_Address_Info.Country_Code + "000000000000000000"); //generic no vat number
				}
				
				Tax_Data Tax_Info = Environment.getTaxDetails(BillingCountryCode);
				if (Tax_Info != null) {
					StateTax = Tax_Info.STATE_TAX_ID;
					CountryTax = Tax_Info.TAX_ID;	
					if (WebDriver_Functions.isVisable(By.xpath("//*[@id='mand']"))){//Tax id 1
						WebDriver_Functions.Type(By.id("reg_tax_id_one"), StateTax);
					}
					if (WebDriver_Functions.isVisable(By.xpath("//*[@id='mand1']"))){//Tax id 2
						WebDriver_Functions.Type(By.id("reg_tax_id_two"), CountryTax);
					}
				}
				WebDriver_Functions.Click(By.id("next_reg"));
			}
			
			Helper_Functions.Wait(4); //need to remove later
			
			if (WebDriver_Functions.isVisable(By.id("next_payment"))) {
				WebDriver_Functions.Click(By.id("next_payment"));
			}
			
			if (WebDriver_Functions.isVisable(By.id("nomatch"))) {
				WebDriver_Functions.Click(By.id("nomatch"));
			}
			
			//Added due to L5 issue, there could be a {Alert text : Address could not be validated because of internal error.} ignore it
			Helper_Functions.Wait(4); //need to remove later
			WebDriver_Functions.CloseAlertPopUp();
			
			if (WebDriver_Functions.isVisable(By.id("next_reg"))) {
				WebDriver_Functions.Click(By.id("next_reg"));
			}
			
			//Payment Information
			Credit_Card_Data CC_Info = Environment.getCreditCardDetails(Account_Info.Level, "V");
			String Payment = "Invoice";
			try {
				WebDriver_Functions.WaitPresent(By.id("acct_pay_info_list"));
				WebDriver_Functions.Select(By.id("acct_pay_info_list"), "Credit_Card","v");
				
				//Example  "Visa", "4005554444444460", "460", "12", "20"
				WebDriver_Functions.Type(By.id("acct_payment_info_number_details"), CC_Info.CARD_NUMBER);
				WebDriver_Functions.Type(By.id("name_on_card"), Account_Info.FirstName + " " + Account_Info.LastName);
				WebDriver_Functions.Type(By.id("acct_pay_expiry"), CC_Info.EXPIRATION_MONTH + "/20" + CC_Info.EXPIRATION_YEAR);
				WebDriver_Functions.Type(By.id("acct_pay_cvv_code"), CC_Info.CVV);
				WebDriver_Functions.Select(By.id("acct_payment_info_card_type") , CC_Info.TYPE.toUpperCase(),"v");   //Auto populated
				WebDriver_Functions.Click(By.id("next_comments"));
				Payment = CC_Info.TYPE;
				Account_Data.Set_Credit_Card(Account_Info, CC_Info);
			}catch (Exception e){
				WebDriver_Functions.Select(By.id("acct_pay_info_list") , "Invoice","v");	
				WebDriver_Functions.Click(By.id("next_comments"));
			}
			
			//confirmation page final submission step
			WebDriver_Functions.Click(By.id("comments_form_save"));	
			//*[@id="dialog-confirm"]/center/text()
			WebDriver_Functions.WaitPresent(By.id("dialog-confirm"));
			WebDriver_Functions.WaitForTextPresentIn(By.id("dialog-confirm"), "Account has been created");
	        String AccountNumbers = DriverFactory.getInstance().getDriver().findElement(By.id("dialog-confirm")).getText();
	        Helper_Functions.PrintOut("AccountNumbers:   " + AccountNumbers + "     -- " + Payment, false);
			AccountNumbers = AccountNumbers.replace("Account has been created successfully and Account Numbers are ", "");
			AccountNumbers = AccountNumbers.replace("Account has been created successfully and Account Number is ", "");
			AccountNumbers = AccountNumbers.replaceAll(" ", "");
			
			Account_Data Account_Details[] = new Account_Data[NumAccounts];
			StringTokenizer tok = new StringTokenizer(AccountNumbers, ",", true);
			

			
			for (int i = 0 ; i < NumAccounts; i++) {
				Account_Details[i] = new Account_Data(Account_Info);
				String token = tok.nextToken();
				if (ModifiedAddress) {
					Account_Info = Account_Lookup.Account_Details(token, Account_Info.Level);
					Account_Details[i] = new Account_Data(Account_Info);
					ModifiedAddress = false;
				}
				Account_Details[i].Account_Number = token;
			    Account_Details[i].Account_Type = Account_Type;
			    Account_Details[i].Tax_ID_One = StateTax;
			    Account_Details[i].Tax_ID_Two = CountryTax;
			    
			    if (Payment.contentEquals("Invoice")) {
			    	//load the dummy invoice numbers.
			    	Account_Details[i].Invoice_Number_A = Environment.DummyInvoiceOne;
			    	Account_Details[i].Invoice_Number_B = Environment.DummyInvoiceTwo;
			    }

			    if(tok.hasMoreTokens()) {
			    	tok.nextToken();
			    }
			}
			return Account_Details;
		} catch (Exception e) {
			throw e;
		}
	}
	
	public static boolean writeAccountsToExcel(Account_Data Account_Info[],String Operating_Companies) throws Exception{
		String fileName = Helper_Functions.DataDirectory + "\\AddressDetails.xls", sheetName = "Account_Numbers"; 
		
		writeAccountsToExcel(Account_Info, Operating_Companies, fileName, sheetName);
		//write the same accounts to the backup sheet, just for reference later
		writeAccountsToExcel(Account_Info, Operating_Companies, fileName, sheetName + "_Backup");
		return true;
	}
	
	public static boolean writeAccountsToExcel(Account_Data Account_Info[], String Operating_Companies, String fileName, String sheetName) throws Exception{
		for(Account_Data AD: Account_Info) {
			String ContactDetailsParsed[][] = {{"Level", AD.Level},
					{"Shipping_Address_Line_1", AD.Shipping_Address_Info.Address_Line_1},
					{"Shipping_Address_Line_2", AD.Shipping_Address_Info.Address_Line_2},
					{"Shipping_City", AD.Shipping_Address_Info.City},
					{"Shipping_State", AD.Shipping_Address_Info.State},
					{"Shipping_State_Code", AD.Shipping_Address_Info.State_Code},
					{"Shipping_Phone_Number", AD.Shipping_Address_Info.Phone_Number},
					{"Shipping_Zip", AD.Shipping_Address_Info.Zip},
					{"Shipping_Country_Code", AD.Shipping_Address_Info.Country_Code},
					{"Shipping_Region", AD.Shipping_Address_Info.Region},
					{"Shipping_Country", AD.Shipping_Address_Info.Country},
					{"Billing_Address_Line_1", AD.Billing_Address_Info.Address_Line_1},
					{"Billing_Address_Line_2", AD.Billing_Address_Info.Address_Line_2},
					{"Billing_City", AD.Billing_Address_Info.City},
					{"Billing_State", AD.Billing_Address_Info.State},
					{"Billing_State_Code", AD.Billing_Address_Info.State_Code},
					{"Billing_Phone_Number", AD.Billing_Address_Info.Phone_Number},
					{"Billing_Zip", AD.Billing_Address_Info.Zip},
					{"Billing_Country_Code", AD.Billing_Address_Info.Country_Code},
					{"Billing_Region", AD.Billing_Address_Info.Region},
					{"Billing_Country", AD.Billing_Address_Info.Country},
					{"Account_Number", AD.Account_Number},
					{"Credit_Card_Type", AD.Credit_Card_Type},
					{"Credit_Card_Number", AD.Credit_Card_Number},
					{"Credit_Card_CVV", AD.Credit_Card_CVV},
					{"Credit_Card_Expiration_Month", AD.Credit_Card_Expiration_Month},
					{"Credit_Card_Expiration_Year", AD.Credit_Card_Expiration_Year},
					{"Invoice_Number_A", AD.Invoice_Number_A},
					{"Invoice_Number_B", AD.Invoice_Number_B},
					{"Account_Type", AD.Account_Type},
					{"Tax_ID_One", AD.Tax_ID_One },
					{"Tax_ID_Two", AD.Tax_ID_Two}, 
					{"Operating_Companies", Operating_Companies}};
			Helper_Functions.WriteToExcel(fileName, sheetName, ContactDetailsParsed, -1);
		}
		return true;
	}

	
	@Test (enabled = true)
	public void Debug_Account_Creation() {
		Account_Data Account_Info = Environment.getAddressDetails("2", "US");
		Account_Info.Company_Name = "Company" + Helper_Functions.CurrentDateTime();
		Credit_Card_Data CC_Info = Environment.getCreditCardDetails(Account_Info.Level, "V");
		Account_Data.Set_Credit_Card(Account_Info, CC_Info);
		
		try {
			Environment.getInstance().setLevel("2");
			WebDriver_Functions.ChangeURL("ECAM", "", false);
			if (WebDriver_Functions.isPresent(By.id("username"))) {
				WebDriver_Functions.Type(By.id("username"), ECAMuserid);
				WebDriver_Functions.Type(By.id("password"), ECAMpassword);
				WebDriver_Functions.Click(By.id("submit"));
			}

			Set<Cookie> cookies = DriverFactory.getInstance().getDriver().manage().getCookies();
	        Iterator<Cookie> itr = cookies.iterator();
	        String Name = "OAMAuth";
	        while (itr.hasNext()) {
	            Cookie cookie = itr.next();
	            Helper_Functions.PrintOut(cookie.toString(), true);
	            if (cookie.getName().contains(Name)){
	            	Helper_Functions.PrintOut(cookie.getName() + " value is " + cookie.getValue(), true);
	            }
	        }
	        Helper_Functions.PrintOut("Here", true);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		//String Cookie = Loggin();
		Create_Account_Numbers(Account_Info, "1");
		Account_Data.Print_Account_Address(Account_Info);
	}
	
	public static String Loggin() {
		HttpPost httppost = new HttpPost("https://devoam.secure.fedex.com/oam/server/auth_cred_submit");
		
		JSONObject main = new JSONObject()
				.put("username", ECAMuserid)
				.put("password", ECAMpassword)
				.put("lang", "en_US")
				.put("request_id", "5255617725010824253");
		
		String json = main.toString();
		
		httppost.addHeader("Accept", "application/json, text/javascript, */*; q=0.01");
		httppost.addHeader("Content-Type", "application/json; charset=UTF-8");
		httppost.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/74.0.3729.131 Safari/537.36");
		httppost.addHeader("X-clientid", "ECAM");
		httppost.addHeader("X-locale", "en_US");
		//httppost.addHeader("X-loggedin", "LOGGEDIN");
		httppost.addHeader("X-Requested-With", "XMLHttpRequest");
		httppost.addHeader("X-version", "1.0");

		StringEntity params;
		try {
			params = new StringEntity(json.toString());
			httppost.setEntity(params);
			HttpClient httpclient = HttpClients.createDefault();
			HttpResponse Response = httpclient.execute(httppost);
			Header[] Headers = Response.getAllHeaders();
			//takes apart the headers of the response and returns the fdx_login cookie if present
			
			for (Header Header : Headers) {
				if (Header.getName().contentEquals("Set-Cookie") && Header.getValue().contains("OAMAuth")) {
					String Header_String = Header.toString();
					String Cookie = Header_String.substring(Header_String.indexOf(" ") + 1, Header_String.indexOf(";"));
					Helper_Functions.PrintOut(Cookie, false);
					return Cookie;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static Account_Data[] Create_Account_Numbers(Account_Data Account_Info, String NumAccounts) {
		String AppUrl = "";
		if (Account_Info.Level.contentEquals("2")) {
			AppUrl = "https://devedcsso.secure.fedex.com/account/v1/newEnterpriseAccount";
		}else if (Account_Info.Level.contentEquals("3")) {
			AppUrl = "";
		}else if (Account_Info.Level.contentEquals("5")) {
			AppUrl = "";
		}
			
		HttpPost httppost = new HttpPost(AppUrl);

		JSONObject Empty_element = new JSONObject();
		
//start of freight section
		JSONObject createdBy = new JSONObject()
			.put("employeeId", ECAMuserid)
			.put("operatingCompany", "FEDEX_SERVICES");
		
		JSONObject freight_comments_elements = new JSONObject()
			.put("createdBy", createdBy )
			.put("type", "GENERAL_INFORMATION")
			.put("commentDescription", "New account requested by " + Account_Info.FirstName + " " + Account_Info.LastName)
			.put("operatingCompany", "FEDEX_FREIGHT");
		JSONObject freight_comments_array[] = new JSONObject[] {freight_comments_elements};
		
		JSONObject creditStatusDetail = new JSONObject()
			.put("status", "NO_CREDIT")
			.put("creditReasonCode", "CAS09")
			.put("cashReasonCode", "Customer Request");
		
		JSONObject localization = new JSONObject()
			.put("languageCode", Account_Info.LanguageCode)
			.put("localeCode", Account_Info.Billing_Address_Info.Country_Code);  //assumed country code
				
		String marketingCorrespondenceTypes[] = new String[] {};//not sure what this is atm
		JSONObject communicationDetail = new JSONObject()
			.put("marketingCorrespondenceTypes", marketingCorrespondenceTypes)
			.put("localization", localization);
	
		String streetLines[];
		if (Account_Info.Billing_Address_Info.Address_Line_2 == null || Account_Info.Billing_Address_Info.Address_Line_2.contentEquals("")) {
			streetLines = new String[] {Account_Info.Billing_Address_Info.Address_Line_1};
		}else {
			streetLines = new String[] {Account_Info.Billing_Address_Info.Address_Line_1, Account_Info.Billing_Address_Info.Address_Line_2};
		}
		JSONObject address = new JSONObject()
			.put("shareId", Account_Info.Billing_Address_Info.Share_Id)
			.put("addressClassification", "UNKNOWN")
			.put("streetLines", streetLines)
			.put("city", Account_Info.Billing_Address_Info.City)
			.put("stateOrProvinceCode", Account_Info.Billing_Address_Info.State_Code)
			.put("postalCode", Account_Info.Billing_Address_Info.Zip)
			.put("countryCode", Account_Info.Billing_Address_Info.Country_Code)
			.put("residential", false); //default to false currently
		
		JSONObject companyName = new JSONObject()
			.put("name", Account_Info.Company_Name);

		JSONObject permissions = new JSONObject()
			.put("CALL", "GRANT")
			.put("TEXT", "DENY");
		
		String area_code = Account_Info.Billing_Address_Info.Phone_Number.substring(0, 3);//assumption that first 3 digits are the area code.
		String localNumber = Account_Info.Billing_Address_Info.Phone_Number.substring(3, Account_Info.Billing_Address_Info.Phone_Number.length());
		String countryCode = "1";
		JSONObject number = new JSONObject()
			.put("areaCode", area_code)
			.put("localNumber", localNumber)
			.put("countryCode", countryCode);
		
		JSONObject phoneNumberDetails_elements = new JSONObject()
			.put("number", number)
			.put("usage", "PRIMARY")
			.put("permissions", permissions);
		JSONObject phoneNumberDetails_array[] = new JSONObject[] {phoneNumberDetails_elements};
		
		JSONObject contactAncillaryDetail = new JSONObject()
			.put("phoneNumberDetails", phoneNumberDetails_array)
			.put("companyName", companyName);
		
		JSONObject personName = new JSONObject()
			.put("firstName", Account_Info.FirstName)
			.put("lastName", Account_Info.LastName);
		
		JSONObject contact = new JSONObject()
			.put("personName", personName)
			.put("companyName", Account_Info.Company_Name);
		
		JSONObject contactAndAddress = new JSONObject()
			.put("contact", contact)
			.put("contactAncillaryDetail", contactAncillaryDetail)
			.put("address", address);
		
		JSONObject contact_no_person = new JSONObject()
			.put("companyName", Account_Info.Company_Name);
		
		JSONObject contactAndAddress_no_person = new JSONObject()
			.put("contact", contact_no_person)
			.put("contactAncillaryDetail", contactAncillaryDetail)
			.put("address", address);
		
		JSONObject PRIMARY_ACCOUNT_contact_element = new JSONObject()
			.put("type", "PRIMARY_ACCOUNT")
			.put("contactAndAddress", contactAndAddress_no_person)
			.put("communicationDetail", communicationDetail);
		
		JSONObject PRIMARY_SHIPPER_CONTACT_contact_element = new JSONObject()
			.put("type", "PRIMARY_SHIPPER_CONTACT") ///////////////////////asdf
			.put("contactAndAddress", contactAndAddress)
			.put("communicationDetail", communicationDetail);
		
		JSONObject PRIMARY_BILLING_ACCOUNT_contact_element = new JSONObject()
			.put("type", "PRIMARY_BILLING_ACCOUNT")                //////////////asdf
			.put("contactAndAddress", contactAndAddress)
			.put("communicationDetail", communicationDetail);
		
		JSONObject PRIMARY_BILLING_CONTACT_contact_element = new JSONObject()
			.put("type", "PRIMARY_BILLING_CONTACT")
			.put("contactAndAddress", contactAndAddress)
			.put("communicationDetail", communicationDetail);
		
		JSONObject contacts_array[] = new JSONObject[] {PRIMARY_ACCOUNT_contact_element, PRIMARY_SHIPPER_CONTACT_contact_element, PRIMARY_BILLING_ACCOUNT_contact_element, PRIMARY_BILLING_CONTACT_contact_element};
		JSONObject accountGroups_array[] = new JSONObject[] {Empty_element, Empty_element, Empty_element}; //come back later and figure out what this does
		String attributes_array[] = new String[] {"DOCK"};
		
		JSONObject freightProfileSpecification = new JSONObject()
			.put("contacts", contacts_array)
			.put("accountGroups", accountGroups_array)
			.put("accountType", "SHIPPER")
			.put("attributes", attributes_array)
			.put("creditStatusDetail", creditStatusDetail)
			.put("comments", freight_comments_array);
//end of freight section
		
//start of express section
		JSONObject revenueDetail = new JSONObject()
			.put("preferredCurrencyType", "USD");
		
		JSONObject express_comments_elements = new JSONObject()
			.put("createdBy", createdBy )
			.put("type", "GENERAL_INFORMATION")
			.put("commentDescription", "New account requested by " + Account_Info.FirstName + " " + Account_Info.LastName)
			.put("operatingCompany", "EXPRESS");
		JSONObject express_comments_array[] = new JSONObject[] {express_comments_elements};
		
		JSONObject address_Element = new JSONObject()
				.put("address", address);
		
		JSONObject address_array[] = new JSONObject[] {address_Element};
		
		JSONObject address_credit_card = new JSONObject()
				.put("streetLines", streetLines)
				.put("city", Account_Info.Billing_Address_Info.City)
				.put("stateOrProvinceCode", Account_Info.Billing_Address_Info.State_Code)
				.put("postalCode", Account_Info.Billing_Address_Info.Zip)
				.put("countryCode", Account_Info.Billing_Address_Info.Country_Code)
				.put("residential", false); //default to false currently
		
		JSONObject holder = new JSONObject()
			.put("contact", contact)
			.put("contactAncillaryDetail", contactAncillaryDetail)
			.put("address", address_credit_card);
		
		JSONObject creditCard = new JSONObject()
			.put("number", Account_Info.Credit_Card_Number)
			.put("creditCardType", Account_Info.Credit_Card_Type.toUpperCase())
			.put("expirationDate", Account_Info.Credit_Card_Expiration_Month + "/20" + Account_Info.Credit_Card_Expiration_Year)  //y2K 2?
			.put("verificationCode", Account_Info.Credit_Card_CVV)
			.put("holder", holder);
		
		String express_attributes_array[] = new String[] {"FEDEX_CAN_CALL_FOR_MARKETING"};
		
		JSONObject expressProfileSpecification = new JSONObject()
			.put("contacts", contacts_array)
			.put("accountType", "BUSINESS")
			.put("attributes", express_attributes_array)
			.put("creditCard", creditCard)
			.put("bankDetails", address_array)
			.put("comments", express_comments_array)
			.put("revenueDetail", revenueDetail);
//end of express section
		
//start of enterprise section
		JSONObject enterprise_comments_elements = new JSONObject()
			.put("createdBy", createdBy )
			.put("type", "GENERAL_INFORMATION")
			.put("commentDescription", "New account requested by " + Account_Info.FirstName + " " + Account_Info.LastName)
			.put("operatingCompany", "FEDEX_EXPRESS");
		JSONObject enterprise_comments_array[] = new JSONObject[] {enterprise_comments_elements, freight_comments_elements};
		
		JSONObject CONTACT_contact_element = new JSONObject()
			.put("type", "CONTACT")
			.put("contactAndAddress", contactAndAddress)
			.put("communicationDetail", communicationDetail);
			
		JSONObject enterprise_contacts_array[] = new JSONObject[] {PRIMARY_ACCOUNT_contact_element, CONTACT_contact_element};

		JSONObject enterpriseProfileSpecification = new JSONObject()
			.put("contacts", enterprise_contacts_array)
			.put("accountType", "BUSINESS")
			.put("attributes", express_attributes_array)
			.put("comments", enterprise_comments_array);
//end of enterprise section
		
		Object token = null; //not happy with the below but getting error when trying to insert null
		JSONObject accountCreationDetail = new JSONObject()
			.put("creationUserGroup", "ALLIANCES")
			.put("numberOfAccounts", NumAccounts)
			.put("entityType", "BUSINESS")
			.put("comments", token  == null ? JSONObject.NULL : token);;
		
		String requestedProfiles_array[] = new String[] {"ENTERPRISE", "EXPRESS", "FREIGHT", "GROUND"};   //will need to changes this based on the region.
		
		JSONObject enterpriseCustomerSpecification = new JSONObject()
			.put("processingOption", "IGNORE_DUPLICATE_ACCOUNT_ADDRESSES")
			.put("requestedProfiles", requestedProfiles_array)
			.put("accountCreationDetail", accountCreationDetail)
			.put("enterpriseProfileSpecification", enterpriseProfileSpecification)
			.put("expressProfileSpecification", expressProfileSpecification)
			.put("freightProfileSpecification", freightProfileSpecification);
		
		JSONObject processingParameters = new JSONObject()
			.put("returnDetailedErrors", false)
			.put("anonymousTransaction", false)
			.put("returnLocalizedDateTime", false);
		
		JSONObject main = new JSONObject()
			.put("enterpriseCustomerSpecification", enterpriseCustomerSpecification)
			.put("processingParameters", processingParameters);
					
		String json = main.toString();
		
		String Domain = AppUrl.substring(0, AppUrl.indexOf("fedex.com") + "fedex.com".length());
		httppost.addHeader("Accept", "application/json, text/javascript, */*; q=0.01");
		httppost.addHeader("Content-Type", "application/json; charset=UTF-8");
		httppost.addHeader("Origin", Domain);
		httppost.addHeader("Referer", Domain + "/ecam/");
		httppost.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/74.0.3729.131 Safari/537.36");
		httppost.addHeader("X-clientid", "ECAM");
		httppost.addHeader("X-locale", "en_US");
		httppost.addHeader("X-loggedin", "NLOGGEDIN");
		httppost.addHeader("X-Requested-With", "XMLHttpRequest");
		httppost.addHeader("X-version", "1.0");
		httppost.addHeader("Cookie", "OAMAuthnHintCookie=0@1558043872");

		StringEntity params;
		String Response = null;
		try {
			params = new StringEntity(json.toString());
			httppost.setEntity(params);
			Response = General_API_Calls.HTTPCall(httppost, json);
		} catch (Exception e) {
			e.printStackTrace();
		}
					
		return null;
	}
}
