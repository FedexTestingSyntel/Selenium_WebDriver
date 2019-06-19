package SupportClasses;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.openqa.selenium.By;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import Data_Structures.Account_Data;
import Data_Structures.Tax_Data;

@Listeners(SupportClasses.TestNG_TestListener.class)

public class Create_Accounts{
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
	}
	
	@DataProvider //(parallel = true)
	public static Iterator<Object[]> dp(Method m) {
		Helper_Functions.MyEmail = "accept@fedex.com";
		String CountriesToCreate = "BR,";//end with a comma after each
		
		List<Object[]> data = new ArrayList<Object[]>();
		ArrayList<String[]> AddressDetails = new ArrayList<String[]>();
		AddressDetails = Helper_Functions.getExcelData(Helper_Functions.DataDirectory + "\\AddressDetails.xls",  "Countries");//load the relevant information from excel file.
		
		for (int i=0; i < LevelsToTest.length(); i++) {
			String Level = String.valueOf(LevelsToTest.charAt(i));
			String TempCountriesToCreate = CountriesToCreate;
			for (String CountryAddress[]: AddressDetails) {
				while (TempCountriesToCreate.contains(CountryAddress[6])) {
					data.add( new Object[] {Level, CountryAddress});
					TempCountriesToCreate = TempCountriesToCreate.replaceFirst(CountryAddress[6] + ",", "");
				}
			}
		}
		return data.iterator();
	}

	@Test(dataProvider = "dp")
	public void Account_Creation(String Level, String CountryDetails[]) {
		try {
			String CountryCode = CountryDetails[6].toLowerCase();
			String OperatingCompanies = "E";
			if (CountryCode.contentEquals("us") || CountryCode.contentEquals("ca")) {
				OperatingCompanies += "G";
			}
			if (CountryCode.contentEquals("us") || CountryCode.contentEquals("ca") || CountryCode.contentEquals("mx")) {
				OperatingCompanies += "F";
			}
			//OperatingCompanies = "F";
			Account_Data Account_Details = new Account_Data();
			Account_Details.Level = Level;
			Account_Details.Shipping_Address_Info.Address_Line_1 = CountryDetails[0];
			Account_Details.Shipping_Address_Info.Address_Line_2 = CountryDetails[1];
			Account_Details.Shipping_Address_Info.City = CountryDetails[2];
			Account_Details.Shipping_Address_Info.State = CountryDetails[3];
			Account_Details.Shipping_Address_Info.State_Code = CountryDetails[4];
			Account_Details.Shipping_Address_Info.Zip = CountryDetails[5];
			Account_Details.Shipping_Address_Info.Country_Code = CountryDetails[6];
			Account_Details.Shipping_Address_Info.Region = CountryDetails[7];
			Account_Details.Shipping_Address_Info.Country = CountryDetails[8];
			Account_Details.Shipping_Address_Info.Phone_Number = Helper_Functions.ValidPhoneNumber(CountryCode);
			Account_Details.Billing_Address_Info.Address_Line_1 = CountryDetails[0];
			Account_Details.Billing_Address_Info.Address_Line_2 = CountryDetails[1];
			Account_Details.Billing_Address_Info.City = CountryDetails[2];
			Account_Details.Billing_Address_Info.State = CountryDetails[3];
			Account_Details.Billing_Address_Info.State_Code = CountryDetails[4];
			Account_Details.Billing_Address_Info.Zip = CountryDetails[5];
			Account_Details.Billing_Address_Info.Country_Code = CountryDetails[6];
			Account_Details.Billing_Address_Info.Region = CountryDetails[7];
			Account_Details.Billing_Address_Info.Country = CountryDetails[8];
			Account_Details.Billing_Address_Info.Phone_Number = Helper_Functions.ValidPhoneNumber(CountryCode);

			Account_Data[] Accounts = null;
			Helper_Functions.PrintOut("Attempting to create account number for " + Arrays.toString(CountryDetails), false);
			Accounts = CreateAccountNumbers(Account_Details, OperatingCompanies, 10);
			Account_Data.Write_Accounts_To_Excel(Accounts, true);

		}catch (Exception e) {
			Assert.fail(e.getMessage());
		}
	}
	
	public static Account_Data[] CreateAccountNumbers(Account_Data Account_Info, String OperatingCompanies, int NumAccounts) throws Exception{
		try {
			// AccountDetails Example = 
			//ShippingCountryCode, BillingCountryCode, OperatingCompanies (E = Express, G = Ground, F = Freight so "EDF" is all three), NumberOfAccounts
			// AddressDetails Example =  {"10 FEDEX PKWY 2nd FL", "", "COLLIERVILLE", "Tennessee", "TN", "38017", "US"});
			//Address line 1, address line 2, City, StateName, StateCode, ZipCode, CountryCode

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
			
			WebDriver_Functions.Type(By.id("acctinfo_no_acct"), Integer.toString(NumAccounts)); //number of account numbers that should be created
			WebDriver_Functions.Click(By.id("next_contact"));
			
			//Account Contact Information
			WebDriver_Functions.Type(By.id("first_name"), "First" + Account_Info.Billing_Address_Info.Country_Code);
			WebDriver_Functions.Type(By.id("last_name"), "Last" + Account_Type);
			try {
				WebDriver_Functions.Select(By.id("contact_language") , "EN","v");//set the language as English
			}catch (Exception e){}
			
			WebDriver_Functions.Type(By.id("contact_phn_one"), Account_Info.Shipping_Address_Info.Phone_Number);
			WebDriver_Functions.Click(By.name("ship_radio"));
			WebDriver_Functions.Click(By.id("next_address"));
			
			//Account Address
			WebDriver_Functions.Type(By.id("acctinfo_postal_input_info"), Account_Info.Billing_Address_Info.Zip);
			WebDriver_Functions.Type(By.id("add_info_company"), Helper_Functions.CurrentDateTime());
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
			
			if (WebDriver_Functions.isVisable(By.id("adrs_val_modified"))) {
				WebDriver_Functions.Click(By.id("adrs_val_modified"));
			}
			
			
			Helper_Functions.Wait(4); //need to remove later, issue is page not passing the regulatory section.
			if (WebDriver_Functions.isVisable(By.id("next_payment"))) {
				WebDriver_Functions.Click(By.id("next_payment"));
			}
			
			//if the address validation page is displayed.
			if(WebDriver_Functions.isVisable(By.id("adrs_val_non_modified"))){
				WebDriver_Functions.Select(By.id("addr_validation_override_input_info"), "CUSTOMER_PROVIDED_PROOF", "v");
				WebDriver_Functions.Click(By.id("adrs_val_non_modified"));
			}else if (WebDriver_Functions.isVisable(By.id("adrs_val_modified"))){
				WebDriver_Functions.Select(By.id("addr_validation_override_input_info"), "CUSTOMER_PROVIDED_PROOF", "v");
				WebDriver_Functions.Click(By.id("adrs_val_modified"));
			}else if (WebDriver_Functions.isVisable(By.id("nomatch"))){
				WebDriver_Functions.Click(By.id("nomatch"));
			}
			
			//Regulator Information page
			String StateTax = "", CountryTax = "";
			if (WebDriver_Functions.isVisable(By.id("next_reg"))) {
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
			String CreditCard[] = Helper_Functions.LoadCreditCard("V");
			String Payment = "Invoice";
			try {
				WebDriver_Functions.WaitPresent(By.id("acct_pay_info_list"));
				WebDriver_Functions.Select(By.id("acct_pay_info_list"), "Credit_Card","v");
				
				//Example  "Visa", "4005554444444460", "460", "12", "20"
				WebDriver_Functions.Type(By.id("acct_payment_info_number_details"), CreditCard[1]);
				WebDriver_Functions.Type(By.id("name_on_card"), "Test Account");
				WebDriver_Functions.Type(By.id("acct_pay_expiry"), CreditCard[3] + "/20" + CreditCard[4]);
				WebDriver_Functions.Type(By.id("acct_pay_cvv_code"), CreditCard[2]);
				WebDriver_Functions.Select(By.id("acct_payment_info_card_type") , CreditCard[0].toUpperCase(),"v");   //Auto populated
				WebDriver_Functions.Click(By.id("next_comments"));
				Payment = CreditCard[1];

			}catch (Exception e){
				WebDriver_Functions.Select(By.id("acct_pay_info_list") , "Invoice","v");	
				WebDriver_Functions.Click(By.id("next_comments"));
			}
			
			//Comment/confirmation page
			WebDriver_Functions.Click(By.id("comments_form_save"));	
			//*[@id="dialog-confirm"]/center/text()
			try {
				WebDriver_Functions.WaitPresent(By.id("dialog-confirm"));
			}catch (Exception InternalError) {
				if (WebDriver_Functions.isPresent(By.id("dialog_div")) && WebDriver_Functions.GetText(By.id("dialog_div")).contains("Interal")) {
					for (int i = 0 ; i < 10; i++) {
						//some error occurred in ECAM. Try submitting 10 more times to see if just ECAM issue.
						//close the error dialog and try submitting again
						WebDriver_Functions.Click(By.cssSelector("body > div:nth-child(3) > div.ui-dialog-buttonpane.ui-widget-content.ui-helper-clearfix > div > button > span"));
						WebDriver_Functions.Click(By.id("comments_form_save"));
						Helper_Functions.Wait(5);
						if (WebDriver_Functions.isPresent(By.id("dialog-confirm"))) {
							break;//get out of loop
						}
					}
				}
			}
			
			WebDriver_Functions.WaitForTextPresentIn(By.id("dialog-confirm"), "Account has been created");
	        String AccountNumbers = DriverFactory.getInstance().getDriver().findElement(By.id("dialog-confirm")).getText();
	        Helper_Functions.PrintOut("AccountNumberstest:   " + AccountNumbers + "     -- " + Payment, false);
			AccountNumbers = AccountNumbers.replace("Account has been created successfully and Account Numbers are ", "");
			AccountNumbers = AccountNumbers.replaceAll(" ", "");
			
			Account_Data Account_Details[] = new Account_Data[NumAccounts];
			StringTokenizer tok = new StringTokenizer(AccountNumbers, ",", true);

			for (int i = 0 ; i < NumAccounts; i++) {
				Account_Details[i] = new Account_Data(Account_Info);
				String token = tok.nextToken();
				Account_Details[i].Account_Number = token;
			    Account_Details[i].Account_Type = Account_Type;
			    Account_Details[i].Tax_ID_One = StateTax;
			    Account_Details[i].Tax_ID_Two = CountryTax;
			    
			    if (Payment.contentEquals("Invoice")) {
			    	//load the dummy invoice numbers.
			    	Account_Details[i].Invoice_Number_A = Environment.DummyInvoiceOne;
			    	Account_Details[i].Invoice_Number_B = Environment.DummyInvoiceTwo;
			    }else {
			    	Account_Details[i].Credit_Card_CVV = CreditCard[2];
			    	Account_Details[i].Credit_Card_Expiration_Month = CreditCard[3];
			    	Account_Details[i].Credit_Card_Expiration_Year = "20" + CreditCard[4];
			    	Account_Details[i].Credit_Card_Number = CreditCard[1];
			    	Account_Details[i].Credit_Card_Type = CreditCard[0].toUpperCase();
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
	
	public static boolean writeAccountsToExcel(Account_Data Account_Info[], String fileName, String sheetName) throws Exception{
		try {
			Helper_Functions.Excellock.lock();
			//Read the spreadsheet that needs to be updated
			FileInputStream fsIP= new FileInputStream(new File(fileName));  
			//Access the workbook                  
			HSSFWorkbook wb = new HSSFWorkbook(fsIP);
			//Access the worksheet, so that we can update / modify it. 
			HSSFSheet worksheet = wb.getSheetAt(0);
			for(int i = 1; i< wb.getNumberOfSheets() + 1;i++) {
				//PrintOut("CurrentSheet: " + worksheet.getSheetName(), false);  //for debugging if getting errors with sheet not found
				if (worksheet.getSheetName().contentEquals(sheetName)) {
					break;
				}
				worksheet = wb.getSheetAt(i);
			} 
			
			
			int RowtoWrite = 0;
			for(int i = 0; i < Account_Info.length; i++) {
				while (worksheet.getRow(RowtoWrite) != null) {
					RowtoWrite++;
				}
				worksheet.createRow(RowtoWrite);
				String AccountInformation[] = new String[] {
					Account_Info[i].Level, 
					Account_Info[i].FirstName,
					Account_Info[i].LastName,
					Account_Info[i].Shipping_Address_Info.Address_Line_1, 
					Account_Info[i].Shipping_Address_Info.Address_Line_2,
					Account_Info[i].Shipping_Address_Info.City,
					Account_Info[i].Shipping_Address_Info.State,
					Account_Info[i].Shipping_Address_Info.State_Code,
					Account_Info[i].Shipping_Address_Info.Phone_Number,
					Account_Info[i].Shipping_Address_Info.Zip,
					Account_Info[i].Shipping_Address_Info.Country_Code,
					Account_Info[i].Shipping_Address_Info.Region,
					Account_Info[i].Shipping_Address_Info.Country,
					Account_Info[i].Billing_Address_Info.Address_Line_1,
					Account_Info[i].Billing_Address_Info.Address_Line_2,
					Account_Info[i].Billing_Address_Info.City,
					Account_Info[i].Billing_Address_Info.State,
					Account_Info[i].Billing_Address_Info.State_Code,
					Account_Info[i].Billing_Address_Info.Phone_Number,
					Account_Info[i].Billing_Address_Info.Zip,
					Account_Info[i].Billing_Address_Info.Country_Code,
					Account_Info[i].Billing_Address_Info.Region,
					Account_Info[i].Billing_Address_Info.Country,
					Account_Info[i].Account_Number,
					Account_Info[i].Credit_Card_Type,
					Account_Info[i].Credit_Card_Number,
					Account_Info[i].Credit_Card_CVV,
					Account_Info[i].Credit_Card_Expiration_Month,
					Account_Info[i].Credit_Card_Expiration_Year,
					Account_Info[i].Invoice_Number_A,
					Account_Info[i].Invoice_Number_B,
					Account_Info[i].Account_Type,
					Account_Info[i].Tax_ID_One,
					Account_Info[i].Tax_ID_Two
				};
				try {
					for (int j = 0; j < AccountInformation.length + 1; j++) {
						if (worksheet.getRow(RowtoWrite).getCell(j) == null) {//if cell not present create it
							worksheet.getRow(RowtoWrite).createCell(j);
						}
						worksheet.getRow(RowtoWrite).getCell(j).setCellValue(AccountInformation[j]);
					}
				}catch(Exception e) {}

				RowtoWrite++;
			}

			//Close the InputStream  
			fsIP.close(); 
			//Open FileOutputStream to write updates
			FileOutputStream output_file =new FileOutputStream(new File(fileName));  
			//write changes
			wb.write(output_file);
			//close the stream
			output_file.close();
			wb.close();
		}catch (Exception e) {
			throw e;
		}finally {
			Helper_Functions.Excellock.unlock();
		}
		return true;
	}
	
	public static boolean writeUsersToExcel(Account_Data Account_Info[], String fileName, String sheetName) throws Exception{
		try {
			Helper_Functions.Excellock.lock();
			//Read the spreadsheet that needs to be updated
			FileInputStream fsIP= new FileInputStream(new File(fileName));  
			//Access the workbook                  
			HSSFWorkbook wb = new HSSFWorkbook(fsIP);
			//Access the worksheet, so that we can update / modify it. 
			HSSFSheet worksheet = wb.getSheetAt(0);
			for(int i = 1; i< wb.getNumberOfSheets() + 1;i++) {
				//PrintOut("CurrentSheet: " + worksheet.getSheetName(), false);  //for debugging if getting errors with sheet not found
				if (worksheet.getSheetName().contentEquals(sheetName)) {
					break;
				}
				worksheet = wb.getSheetAt(i);
			} 
			
			
			int RowtoWrite = 0;
			for(int i = 0; i < Account_Info.length; i++) {
				while (worksheet.getRow(RowtoWrite) != null) {
					RowtoWrite++;
				}
				worksheet.createRow(RowtoWrite);
				String AccountInformation[] = new String[] {
					Account_Info[i].Level, 
					Account_Info[i].Shipping_Address_Info.Address_Line_1, 
					Account_Info[i].Shipping_Address_Info.Address_Line_2,
					Account_Info[i].Shipping_Address_Info.City,
					Account_Info[i].Shipping_Address_Info.State,
					Account_Info[i].Shipping_Address_Info.State_Code,
					Account_Info[i].Shipping_Address_Info.Phone_Number,
					Account_Info[i].Shipping_Address_Info.Zip,
					Account_Info[i].Shipping_Address_Info.Country_Code,
					Account_Info[i].Shipping_Address_Info.Region,
					Account_Info[i].Shipping_Address_Info.Country,
					Account_Info[i].Billing_Address_Info.Address_Line_1,
					Account_Info[i].Billing_Address_Info.Address_Line_2,
					Account_Info[i].Billing_Address_Info.City,
					Account_Info[i].Billing_Address_Info.State,
					Account_Info[i].Billing_Address_Info.State_Code,
					Account_Info[i].Billing_Address_Info.Phone_Number,
					Account_Info[i].Billing_Address_Info.Zip,
					Account_Info[i].Billing_Address_Info.Country_Code,
					Account_Info[i].Billing_Address_Info.Region,
					Account_Info[i].Billing_Address_Info.Country,
					Account_Info[i].Account_Number,
					Account_Info[i].Credit_Card_Type,
					Account_Info[i].Credit_Card_Number,
					Account_Info[i].Credit_Card_CVV,
					Account_Info[i].Credit_Card_Expiration_Month,
					Account_Info[i].Credit_Card_Expiration_Year,
					Account_Info[i].Invoice_Number_A,
					Account_Info[i].Invoice_Number_B,
					Account_Info[i].Account_Type,
					Account_Info[i].Tax_ID_One,
					Account_Info[i].Tax_ID_Two
				};
				try {
					for (int j = 0; j < AccountInformation.length + 1; j++) {
						if (worksheet.getRow(RowtoWrite).getCell(j) == null) {//if cell not present create it
							worksheet.getRow(RowtoWrite).createCell(j);
						}
						worksheet.getRow(RowtoWrite).getCell(j).setCellValue(AccountInformation[j]);
					}
				}catch(Exception e) {}

				RowtoWrite++;
			}

			//Close the InputStream  
			fsIP.close(); 
			//Open FileOutputStream to write updates
			FileOutputStream output_file =new FileOutputStream(new File(fileName));  
			//write changes
			wb.write(output_file);
			//close the stream
			output_file.close();
			wb.close();
		}catch (Exception e) {
			throw e;
		}finally {
			Helper_Functions.Excellock.unlock();
		}
		return true;
	}
}