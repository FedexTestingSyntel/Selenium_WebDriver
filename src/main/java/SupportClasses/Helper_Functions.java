package SupportClasses;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Pattern;
import org.openqa.selenium.By;
import Data_Structures.Account_Data;
import Data_Structures.User_Data;
import jxl.Sheet;
import jxl.Workbook;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;

public class Helper_Functions{
	public static String MyEmail = "accept@gmail.com", myPhone = "9011111111", myPassword = "Test1234";
	public static String MyFakeEmail = "accept@fedex.com";
	public static String BaseDirectory = System.getProperty("user.dir").substring(0, System.getProperty("user.dir").lastIndexOf("\\") + 1);
	public static String FileSaveDirectory = BaseDirectory + "EclipseScreenshots";
	public static String DataDirectory = BaseDirectory + "Data";
	public static String TestingData = DataDirectory + "\\TestingData.xls";
	public final static Lock Excellock = new ReentrantLock();//prevent excel ready clashes
	public static String Passed = "Passed", Failed = "Fail", Skipped = "Skipped";
	
	public static ArrayList<String[]> ContactList = new ArrayList<String[]>(), CreditCardList = new ArrayList<String[]>(), EnrollmentList = new ArrayList<String[]>(), TaxInfoList = new ArrayList<String[]>();
    
	public static String getCallerClassName() { 
        StackTraceElement[] stElements = Thread.currentThread().getStackTrace();
        for (int i=1; i<stElements.length; i++) {
            StackTraceElement ste = stElements[i];
            if (!ste.getClassName().equals(Helper_Functions.class.getName()) && ste.getClassName().indexOf("java.lang.Thread")!=0) {
                return ste.getClassName();
            }
        }
        return null;
     }
	
	public static void WriteToFile(String Text, String Path){	
		//this is only applicable if configured for tests that print the summery table.
		if (Text.contains("Passed: ") && Text.contains(" - Failed: ")) {
			String Results = Text.substring(Text.indexOf("Passed: "), Text.length());
			if (Results.length() < 28 && Results.contains(" - Failed: ")) {//check in case pulled to many characters
				Results = Results.replace("Passed: ", " - P");
				Results = Results.replace(" - Failed: ", "F");
				Results = Results.replaceAll("\r", "");
				Results = Results.replaceAll("\n", "");
				Path = Path.replace(".txt", "");
				Path += Results;
			}
		}
		if (!Path.contains(".txt")) {
			Path+= ".txt";
		}
		File newTextFile = new File(Path);
		FileWriter fw;
		try {
			String Folder = Path.substring(0, Path.lastIndexOf("\\"));
			if (!(new File(Folder)).exists()) {
				new File(Folder).mkdir();
			}
			newTextFile.createNewFile();
			fw = new FileWriter(newTextFile);
			fw.write(Text);
	        fw.close();
		} catch (Exception e) {  
			PrintOut("Failure writing to file.", true);
			e.printStackTrace();
		}
	}
	
	public static void MoveOldLogs(){
    	String main_Source = FileSaveDirectory;
    	File main_dir = new File(main_Source);
    	if(main_dir.isDirectory()) {
    	    File[] content_main = main_dir.listFiles();
    	    for(int j = 0; j < content_main.length; j++) {
    	    	try {
    	        	String PathSource = content_main[j].getPath();
    	        	String PathDestination = content_main[j].getPath() + File.separator + "Old";
    	        	File source_dir = new File(PathSource);
    	        	File destination_dir = new File(PathDestination);
    	        	int year = Calendar.getInstance().get(Calendar.YEAR);
    	        	int month = Calendar.getInstance().get(Calendar.MONTH) + 1;
    	        	int day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
    	        	if(source_dir.isDirectory()) {
    	        	    File[] content_subfolder = source_dir.listFiles();
    	        	    for(int i = 0; i < content_subfolder.length; i++) {
    	        	    	if (content_subfolder[i].isDirectory()){
    	        	    		break; //do not need to seach within subfolders.
    	        	    	}
    	        	    	
    	        	    	try {
    	        	        	if (!destination_dir.exists()) {// if the directory does not exist, create it
    	        	        	    PrintOut("Creating directory: " + destination_dir.getName(), true);
    	        	        	    try{
    	        	        	    	destination_dir.mkdir();
    	        	        	        PrintOut(PathDestination + " DIR created", true);  
    	        	        	    } catch(SecurityException se){}        
    	        	        	}
    	        	        	BasicFileAttributes attr = Files.readAttributes(Paths.get(content_subfolder[i].getPath()), BasicFileAttributes.class);
    	        	        	String creationtime = " " + attr.creationTime();
    	        	        	if (!creationtime.contains(Integer.toString(year) + "-" + String.format("%02d", month) + "-" + String.format("%02d", day))){//if the file was created before today
    	        	        		String localPathDestination = PathDestination + File.separator + creationtime.substring(1, 8);
    	        	        		File old_month_dir = new File(localPathDestination);
    	        	        		if (!old_month_dir.exists()) {// if the directory does not exist for the old month then create it
    	            	        	    PrintOut("Creating directory: " + old_month_dir.getName(), true);
    	            	        	    try{
    	            	        	    	old_month_dir.mkdir();
    	            	        	        PrintOut(localPathDestination + " DIR created", true);  
    	            	        	    } catch(SecurityException se){}
    	            	        	}
    	        	        		//Files.move(from, to, CopyOption... options).
    	        	        		Files.move(Paths.get(content_subfolder[i].getPath()), Paths.get(content_subfolder[i].getPath().replace(PathSource, localPathDestination)), StandardCopyOption.REPLACE_EXISTING);
    	        	        	}
    	    				}catch (Exception e) {}
    	        	    }
    	        	}
				}catch (Exception e) {}
    	    }//end for finding each individual app
    	}
    }//end MoveOldLogs
	  
	public static void Wait(long Seconds) {
		try {
			Thread.sleep(Seconds * 1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
 	
	//String[] {Streetline1 - 0, Streetline2 - 1, City - 2, State - 3, StateCode - 4, postalCode - 5, countryCode - 6};
	public static String[] AccountDetails(String AccountNumber){
		String Streetline1 = "", Streetline2 = "", City ="", State ="", StateCode = "", postalCode = "", countryCode = "";
		if (Environment.getInstance().getLevel().contentEquals("")) {
			PrintOut("WARNING Environement level has not been set. Loading L3 for now.\n\n\n", false);
			Environment.getInstance().setLevel("3");
		}
		String TempLevel = "L" + Environment.getInstance().getLevel();
		if (!"L1L2L3L4".contains(TempLevel)){
			PrintOut("Invalid Level to find account number detials, checking account vs L3", true);
			TempLevel = "L3";
		}
 		try {
 			PrintOut("Account number " + AccountNumber + " recieved from " + Thread.currentThread().getStackTrace()[2].getMethodName(), true);
 			WebDriver_Functions.ChangeURL("JSP", "US", false);
 			WebDriver_Functions.Type(By.name("contactAccountNumber"), AccountNumber);
 			WebDriver_Functions.Type(By.name("contactAccountOpCo"), "FX");

 		    //selects the correct radio button for the level, only works for 1,2,3,4
 			WebDriver_Functions.Click(By.xpath("//input[(@name='contactLevel') and (@value = '" + TempLevel + "')]"));
			WebDriver_Functions.Click(By.name("contactAccountSubmit"));
			String SourceText = DriverFactory.getInstance().getDriver().getPageSource();
			
			int intStartingPoint;
			//PrintOut(SourceText.replaceAll("\n", ""));
			String StartingPoint = "&lt;streetLine&gt;";
			if (SourceText.indexOf(StartingPoint) < 0){
				StartingPoint = "&lt;customer:streetLine&gt;";
			}
			for (int i=0;i<3;i++){ //instead of three if you set to 2 the shipping address will be returned.
				SourceText = SourceText.substring(SourceText.indexOf(StartingPoint) + StartingPoint.length(), SourceText.length());
				//PrintOut(SourceText.replaceAll("\n", ""));
			}
			String start = "name=\"";
			String end  = "\" value=\"";
			Streetline1 = SourceText.substring(SourceText.indexOf(start) + start.length(), SourceText.indexOf(end));
			
			StartingPoint = "additionalLine1&gt;";//save if the account number has an address line1 value
			if(SourceText.indexOf(StartingPoint) > 0){
				intStartingPoint = SourceText.indexOf(StartingPoint) + StartingPoint.length();
				SourceText = SourceText.substring(intStartingPoint, SourceText.length() - intStartingPoint);
				Streetline2 = SourceText.substring(SourceText.indexOf(start) + start.length(), SourceText.indexOf(end));
			}
			
			StartingPoint = "geoPoliticalSubdivision2&gt";
			intStartingPoint = SourceText.indexOf(StartingPoint) + StartingPoint.length();
			SourceText = SourceText.substring(intStartingPoint, SourceText.length() - intStartingPoint);
			if (intStartingPoint > StartingPoint.length()){
				City = SourceText.substring(SourceText.indexOf(start) + start.length(), SourceText.indexOf(end));
			}
			
			StartingPoint = "geoPoliticalSubdivision3&gt";			
			intStartingPoint = SourceText.indexOf(StartingPoint) + StartingPoint.length();
			SourceText = SourceText.substring(intStartingPoint, SourceText.length() - intStartingPoint);
			if (intStartingPoint > StartingPoint.length()){
				StateCode = SourceText.substring(SourceText.indexOf(start) + start.length(), SourceText.indexOf(end));
			}
			
			StartingPoint = "postalCode&gt";
			intStartingPoint = SourceText.indexOf(StartingPoint) + StartingPoint.length();
			SourceText = SourceText.substring(intStartingPoint, SourceText.length() - intStartingPoint);
			if (intStartingPoint > StartingPoint.length()){
				postalCode = SourceText.substring(SourceText.indexOf(start) + start.length(), SourceText.indexOf(end));
			}
			
			
			StartingPoint = "countryCode&gt";
			intStartingPoint = SourceText.indexOf(StartingPoint) + StartingPoint.length();
			SourceText = SourceText.substring(intStartingPoint, SourceText.length() - intStartingPoint);
			countryCode = SourceText.substring(SourceText.indexOf(start) + start.length(), SourceText.indexOf(end));
			
			if (postalCode.length() > 5 && countryCode.contentEquals("US")){
				postalCode = postalCode.substring(0, 5);
			}
			
			//remove any special characters
			String AccountDetails[] = {Streetline1, Streetline2, City, State, StateCode, postalCode, countryCode};
			for (int i = 0; i < AccountDetails.length; i++){
				String nfdNormalizedString = Normalizer.normalize(AccountDetails[i], Normalizer.Form.NFD); 
				Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
				AccountDetails[i] = pattern.matcher(nfdNormalizedString).replaceAll("");
			}
			if (countryCode.length() > 5 || countryCode.length() == 0){return null;}
			PrintOut("AccountDetails: " + Arrays.toString(AccountDetails), true);
			return AccountDetails;
 		}catch (Exception e){
 			e.printStackTrace();
 			PrintOut("Not able to fully retrieve address: " + Streetline1 + " " + Streetline2+ " " + City+ " " + State+ " " + StateCode+ " " +postalCode+ " " + countryCode, true);
			return LoadAddress("US");
 		}
 	}//end AccountDetails
    
	public static String CurrentDateTime() {
		Date curDate = new Date();
    	SimpleDateFormat Dateformatter = new SimpleDateFormat("MMddyy");
    	SimpleDateFormat Timeformatter = new SimpleDateFormat("HHmmss");
    	return Dateformatter.format(curDate) + "T" + Timeformatter.format(curDate);
	}
	
	public static String CurrentDateTime(boolean t) {
		Date curDate = new Date();
    	SimpleDateFormat DateTime= new SimpleDateFormat("MM-dd-yy HH:mm:ss:SS");
    	return DateTime.format(curDate);
	}
	
	public static String ValidPhoneNumber(String CountryCode){
		switch (CountryCode) {
    		case "SG":  
    			return "64579846";  //verified on 2/22/19
    		default:
    			return myPhone;
		}//end switch CountryCode
	}
	
	public static void PrintOut(String Text){
		//if boolean not sent will default to not printing timestamp
		PrintOut(Text, false);
	}
	
	public static void PrintOut(String Text, boolean TimeStamp){
		long ThreadID = Thread.currentThread().getId();
		
		if (TimeStamp) {
			Text = CurrentDateTime() + ": " + Text;
			System.out.println(ThreadID + " " + Text); 
		}else {
			System.out.println(Text);
		}
		if (Text == null) {
			Text = "";
		}
		Text = Text.replaceAll("\n", System.lineSeparator());
		ThreadLogger.getInstance().UpdateLogs(Text);//Store the all values that are printed for a given thread.
	}
	
	public static void WriteUserToExcel(String UserID, String Password) {
		int intLevel = Integer.valueOf(Environment.getInstance().getLevel());
		String Data[][] = new String[][] {{"SSO_LOGIN_DESC", UserID}, {"USER_PASSWORD_DESC", Password}};
		try {
			WriteToExcel(DataDirectory + "\\TestingData.xls", "L" + intLevel, Data, -1); 
		}catch (Exception e) {
			Helper_Functions.PrintOut("Unable to update TestingData.xml. Attempting backup sheet.");
			WriteToExcel(DataDirectory + "\\TestingDataBackup.xls", "L" + intLevel, Data, -1);
		}
		
	}
	
	public static void WriteUserToExcel(User_Data User_Info) {
		int intLevel = Integer.valueOf(Environment.getInstance().getLevel());
		String Data[][] = User_Data.Get_User_Data_String_Array(User_Info);
		WriteToExcel(DataDirectory + "\\TestingData.xls", "L" + intLevel, Data, -1); 
	}
	
	
	//{Card Type - 0, Card Number - 1, CVV - 2, Expiration Month - 3, Expiration year - 4}
	public static String[] LoadCreditCard(String CardDetails){
		//Note, this is not designed to run in prod and test levels at the same time.
		String Level = Environment.getInstance().getLevel();
		
		CardDetails = CardDetails.toUpperCase();
		PrintOut("LoadCreditCard Received: " + CardDetails, true);
		String CreditCard[] = null;

		if (CardDetails.contains("I")) {//if requesting invalid credit card
			CreditCard = new String[] {"Visa", "400555444444416", "111", "12", "20"};
			PrintOut("LoadCreditCard is Returning Invalid Card: " + Arrays.toString(CreditCard), true);
			return CreditCard;
		}

		if (CreditCardList.isEmpty() && Level.contentEquals("7")) {
			CreditCardList = getExcelData(DataDirectory + "\\CreditCardDetails.xls", "Prod"); 

		}else if (CreditCardList.isEmpty()) {
			CreditCardList = getExcelData(DataDirectory + "\\CreditCardDetails.xls", "Test"); 
		}
		
		for (int i = 0; i < CreditCardList.size(); i++) {
			CreditCard = CreditCardList.get(i);
			if (CardDetails.length() == 1 && CreditCard[0].contains(CardDetails)) {
				PrintOut("CreditCard: " + Arrays.toString(CreditCard), false);
				return CreditCard;
			}else if (CardDetails.length() <= 16 && CardDetails.length() >= 15 && CreditCard[1].contains(CardDetails)){ //if a credit card number was sent
				try{
					CreditCard = CreditCardList.get(i + 1);//return the next card id possible
				}catch(Exception e) {
					CreditCard = CreditCardList.get(i - 1);//else return the previous card
				}
				PrintOut("CreditCard: " + Arrays.toString(CreditCard), false);
				return CreditCard;
			}else if (CardDetails.length() == 4 && CreditCard[1].contains(CardDetails)) {
				PrintOut("CreditCard: " + Arrays.toString(CreditCard), false);
				return CreditCard;
			}
		}
		
		PrintOut("Unable to find desired card. LoadCreditCard is Returning: " + Arrays.toString(CreditCardList.get(0)), true);
		return CreditCardList.get(0);
	}//end LoadCreditCard
	
	public static String[] LoadAddress(String CountryCode){
		return LoadAddress(CountryCode, "", "");
	}
	//{Streetline1 - 0, Streetline2 - 1, City - 2, State - 3, StateCode - 4, postalCode - 5, CountryCode - 6, ShareID - 7};
	public static String[] LoadAddress(String CountryCode, String CodeState, String Address1){
		CountryCode = CountryCode.toUpperCase();
		CodeState = CodeState.toUpperCase();
		//PrintOut("LoadAddress Received: _" + CountryCode + "_ _" + CodeState + "_", true);
		String ReturnAddress[] = null;
		if (Environment.getInstance().getLevel().contentEquals("7") && CountryCode.contentEquals("US")){
			ReturnAddress = new String[] {"10 FEDEX PKWY 2nd FL", "", "COLLIERVILLE", "Tennessee", "TN", "38017", "US"};
			PrintOut("Since testing production sending back 10 FEDEX PKWY 2nd FL address.   " + Arrays.toString(ReturnAddress), true);
			return ReturnAddress;
		}
		if (ContactList.isEmpty()) {
			ContactList = getExcelData(DataDirectory + "\\AddressDetails.xls", "Countries"); 
		}
		
		for(int i = 0; i< ContactList.size(); i++) {
			if (ContactList.get(i)[6].contentEquals(CountryCode)) {
				if (CodeState == "") {
					ReturnAddress = ContactList.get(i);
					break;
				}else if (CodeState != "" && ContactList.get(i)[4].contentEquals(CodeState)) {
					if (Address1 == "") {
						ReturnAddress = ContactList.get(i);
						break;
					}else if (Address1 != "" && ContactList.get(i)[0].contentEquals(Address1)) {
						ReturnAddress = ContactList.get(i);
						break;
					}
				}
			}
		}
		
		if (ReturnAddress != null) {
			PrintOut("Address: " + Arrays.toString(ReturnAddress), false);
			return ReturnAddress;
		}
		
		PrintOut("Unable to return requested address.", true);
		throw new Error("Address not loaded");
	}//end LoadAddress
	
	//////////////remove this later and use the Enrollment_Data ED[] = Environment.getEnrollmentDetails(intLevel);
	public static String[] LoadEnrollmentIDs(String CountryCode){
		PrintOut("LoadEnrollmentIDs Received: _" + CountryCode, true);
		if (EnrollmentList.isEmpty()) {
			EnrollmentList = getExcelData(DataDirectory + "\\EnrollmentIds.xls", "EnrollmentIds"); 
		}
		
		String EnrollmentID[] = null;
		for(String Enrollment[] : EnrollmentList) {
			if (Enrollment[1].contentEquals(CountryCode)) {
				EnrollmentID = Enrollment;
				break;
			}
		}
		PrintOut("LoadEnrollmentIDs returning: _" + Arrays.toString(EnrollmentID), true);
		return EnrollmentID;//enrollment for the given country not found
	}
	
//////////////remove this later and use the Enrollment_Data ED[] = Environment.getEnrollmentDetails(intLevel);
	public static String[][] LoadAllEnrollmentIDs(String CountryCode){
		PrintOut("LoadAllEnrollmentIDs Received: _" + CountryCode, true);
		if (EnrollmentList.isEmpty()) {
			EnrollmentList = getExcelData(DataDirectory + "\\EnrollmentIds.xls", "EnrollmentIds"); 
		}
		
		ArrayList<String[]> CountrySpecific = new ArrayList<String[]>(EnrollmentList);
		
		for (int i = 0; i < CountrySpecific.size(); i++) {
			if (!CountrySpecific.get(i)[1].contentEquals(CountryCode)) {
				CountrySpecific.remove(i);
				i--;
			}
		}

		String[][] array = new String[CountrySpecific.size()][5];
		for (int i = 0; i < CountrySpecific.size(); i++) {
		    array[i] = CountrySpecific.get(i);
		}
		
		PrintOut("LoadEnrollmentIDs returning: " + CountrySpecific.size() + " ids.", true);
		return array;
	}
	
	
	public static String Check_Country_Region(String CountryCode) {
		for (String CountrCode[] : ContactList) {
			if (CountrCode[6].contentEquals(CountryCode.toUpperCase())) {
				return CountrCode[7];
			}
		}
		return "Country Not Found";
	}
	
	//{First Name - 0, Middle Name - 1, Last Name - 2}
	public static String[] LoadDummyName(String Base, String Level){
		final String[] numNames = {"", "one","two","three","four","five","six","seven"};
	    String DummyName[] = {"F" + numNames[Integer.valueOf(Level)] + Base + getRandomString(7), "M", "L" + getRandomString(7)};
		PrintOut("Full Name: " + Arrays.toString(DummyName), false);
		return DummyName;
	}
	
	public static ArrayList<String[]> getTaxInfo(String CountryCode){
		if (TaxInfoList.isEmpty()) {
			TaxInfoList = getExcelData(DataDirectory + "\\TaxData.xls", "TaxIds"); 
		}
		
		ArrayList<String[]> countrySpecificTaxInfo = new ArrayList<String[]>();
		for(String TaxInfo[] : TaxInfoList) {
			if (TaxInfo[0] != null && TaxInfo[0].contentEquals(CountryCode)) {
				countrySpecificTaxInfo.add(TaxInfo);
			}
		}
		if (countrySpecificTaxInfo.isEmpty()) {
			countrySpecificTaxInfo.add(null);
		}
		return countrySpecificTaxInfo;
	}
	
	
	
	public static String getRandomString(int Length) {
        //String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
		String SALTCHARS = "abcdefghijklmnopqrstuvwxyz";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() < Length) { // length of the random string.
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }
        String saltStr = salt.toString();
        return saltStr;
    }
	
	public static String LoadUserID(String Base){
		String User = Base + CurrentDateTime() + getRandomString(4);
		return User;
	}
	
	//returns the transaction id from a string
	public static String ParseTransactionId(String s) {
		if(s.contains("transactionId")) {
			String TransactionIdStart = "transactionId\":\"";
			int TransactionIdEnd;
			for (TransactionIdEnd = s.indexOf(TransactionIdStart) + TransactionIdStart.length(); TransactionIdEnd < s.length(); TransactionIdEnd++) {
				if (s.substring(TransactionIdEnd, TransactionIdEnd + 1).contentEquals("\"")) {
					break;
				}
			}
			return s.substring(s.indexOf(TransactionIdStart) + TransactionIdStart.length(), TransactionIdEnd);
		}
		return null;
	}
	
	public static String ParseValueFromResponse(String s, String ident) {
		if(s.contains(ident)) {
			String TransactionIdStart = ident + "\":\"";
			int TransactionIdEnd;
			for (TransactionIdEnd = s.indexOf(TransactionIdStart) + TransactionIdStart.length(); TransactionIdEnd < s.length(); TransactionIdEnd++) {
				if (s.substring(TransactionIdEnd, TransactionIdEnd + 1).contentEquals("\"")) {
					break;
				}
			}
			return s.substring(s.indexOf(TransactionIdStart) + TransactionIdStart.length(), TransactionIdEnd);
		}
		return null;
	}
	
	//will take in the level and country code
	//check the listed excel fild for the given country and level and return account number if possible.
	public static String getExcelFreshAccount(String Level, String CountryCode, boolean SingleAccount){
		CountryCode = CountryCode.toUpperCase();
		ArrayList<String[]> data = getExcelData(DataDirectory + "\\AddressDetails.xls", "Accounts");
		
		int intLevel = Integer.parseInt(Level);
		//Here ar ethe assumed headers of the excel file.
		//[Address_Line_1, Address_Line_2, City, State, State_Code, Zip, Country_Code, Region, Country, L1_Account, L2_Account, L3_Account, L4_Account, L5_Account, L6_Account, L7_Account]
		if (!data.get(0)[6].contentEquals("Country_Code") || !data.get(0)[8 + intLevel].contentEquals("L" + Level + "_Account")){
			PrintOut("WARNING, excel file does not match with expected columns", false);
		}
		String Account = null;
		for (String CountryArray[] : data){
			//if the correct line for the country and there are account numbers loaded.
			if (CountryArray[6].contentEquals(CountryCode) && !CountryArray[8 + intLevel].isEmpty()) {//position 9 in the L1 accounts
				Account = CountryArray[8 + intLevel];
				if (Account.contains(",") && SingleAccount) {
					Account = Account.substring(0, Account.indexOf(","));//multiple account numbers could be stored separated with a ","
					Account = Account.replaceAll(" ", "");
				}
				break;
			}
		}
		return Account;
	}
	
	public static ArrayList<String[]> getExcelData(String filePath, String sheetName) {
		//Note, may face issues if the file is an .xlsx, save it as a xls and works

		ArrayList<String[]> data = new ArrayList<>();
		try {
			FileInputStream fs = new FileInputStream(filePath);
			Workbook wb = Workbook.getWorkbook(fs);
			Sheet sh = wb.getSheet(sheetName);
			
			int totalNoOfCols = sh.getColumns();
			int totalNoOfRows = sh.getRows();
			
			for (int i= 0 ; i < totalNoOfRows; i++) { //change to start at 1 if want to ignore the first row.
				String buffer[] = new String[totalNoOfCols];
				boolean EmptyRow = true;
				int EmptyCount = 0;
				for (int j=0; j < totalNoOfCols; j++) {
					String CellContents = sh.getCell(j, i).getContents();
					if (CellContents == null || CellContents.contentEquals("")) {
						EmptyCount++;
						CellContents = "";
					}else {
						EmptyRow = false;
					}
					buffer[j] = CellContents;
				}
				if (!EmptyRow && EmptyCount < totalNoOfCols) {
					data.add(buffer);
				}
			}
			wb.close();
			fs.close();
			
		}catch (Exception e) {
			System.err.println("filePath: " + filePath + "   sheetName: " + sheetName);
			e.printStackTrace();
		}
		return data;
	}
	
	public static boolean RemoveAccountFromAccount_Numbers(String Level, String Account_to_Delete) {
		try {
			Excellock.lock();
			String fileName = DataDirectory + "\\AddressDetails.xls";
			String sheetName = "Account_Numbers";
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
			
			//find what row contains the level and account number
			DataFormatter formatter = new DataFormatter();
			HSSFRow keyRow = worksheet.getRow(0);
			int LevelColumn = -1, Account_NumberColumn = -1;
			for (int key = 0; key < keyRow.getLastCellNum(); key++) {
				if (formatter.formatCellValue(keyRow.getCell(key)).contentEquals("Level")) {
					LevelColumn = key;
				}else if (formatter.formatCellValue(keyRow.getCell(key)).contentEquals("Account_Number")) {
					Account_NumberColumn = key;
				}
			}
			
			if (LevelColumn == -1 || Account_NumberColumn == -1) {
				PrintOut("Warning, Identifiers not found: LevelColumn=" + LevelColumn + ", Account_NumberColumn=" + Account_NumberColumn, false);
			}
			
			for (int j = 0 ; j < worksheet.getLastRowNum(); j++) {
				try {
					HSSFRow removingRow = worksheet.getRow(j);
					if(removingRow != null){
						
						String Lvl = formatter.formatCellValue(removingRow.getCell(LevelColumn));
						String Account = formatter.formatCellValue(removingRow.getCell(Account_NumberColumn));
						if (Lvl.contentEquals(Level) && Account.contentEquals(Account_to_Delete)) {
							worksheet.removeRow(removingRow);
							break;
						}
					}	
				}catch (Exception e) {}
				
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
			PrintOut("WARNING, Unble to remove account from excel.", true);
			return false;
		}finally {
			Excellock.unlock();
		}
		PrintOut("Account number removed from testing file. " + Account_to_Delete, true);
		return true;
	}
	
	public static boolean RemoveAccountFromAccount_Numbers(String Billing_Address_Line_1) {
		try {
			Excellock.lock();
			String fileName = DataDirectory + "\\AddressDetails.xls";
			String sheetName = "Account_Numbers";
			//Read the spreadsheet that needs to be updated
			FileInputStream fsIP= new FileInputStream(new File(fileName));  
			//Access the workbook                  
			HSSFWorkbook wb = new HSSFWorkbook(fsIP);
			//Access the worksheet, so that we can update / modify it. 
			HSSFSheet worksheet = wb.getSheetAt(0);
			DataFormatter formatter = new DataFormatter();
			
			for(int i = 1; i< wb.getNumberOfSheets() + 1;i++) {
				//PrintOut("CurrentSheet: " + worksheet.getSheetName(), false);  //for debugging if getting errors with sheet not found
				if (worksheet.getSheetName().contentEquals(sheetName)) {
					break;
				}
				worksheet = wb.getSheetAt(i);
			}
			//find what row contains billing address line 1
			HSSFRow keyRow = worksheet.getRow(0);
			int keyColumn = 0;
			for (int key = 0; key < keyRow.getLastCellNum(); key++) {
				if (formatter.formatCellValue(keyRow.getCell(key)).contentEquals("Billing_Address_Line_1")) {
					keyColumn = key;
					break;
				}
			}
			
			for (int j = 0 ; j < worksheet.getLastRowNum(); j++) {
				try {
					HSSFRow removingRow = worksheet.getRow(j);
					if(removingRow != null){
						String AddressLineOne = formatter.formatCellValue(removingRow.getCell(keyColumn));
						if (AddressLineOne.contentEquals(Billing_Address_Line_1)) {
							worksheet.removeRow(removingRow);
						}
					}	
				}catch (Exception e) {}
				
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
			PrintOut("WARNING, Unble to remove account from excel.", true);
			return false;
		}finally {
			Excellock.unlock();
		}
		return true;
	}
	
	public static boolean writeExcelData(String fileName, String sheetName, String CellData, int RowtoWrite, int ColumntoWrite){
		try {
			Excellock.lock();
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
			
			// declare a Cell object
			Cell cell = null; 
			// Access the second cell in second row to update the value
			if (worksheet.getRow(RowtoWrite) == null) {//if row not present create it
				worksheet.createRow(RowtoWrite);
			}
			if (worksheet.getRow(RowtoWrite).getCell(ColumntoWrite) == null) {//if cell not present create it
				worksheet.getRow(RowtoWrite).createCell(ColumntoWrite);
			}
			cell = worksheet.getRow(RowtoWrite).getCell(ColumntoWrite);
			
			// Get current cell value value and overwrite the value
			cell.setCellValue(CellData);
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
			PrintOut("WARNING, Unable to write to Excel.", true);
			return false;
		}finally {
			Excellock.unlock();
		}
		return true;
	}

	public static boolean WriteToExcel(String FileName, String SheetName, String Data[][], int KeyPosition) {
		HSSFWorkbook wb = null;
		boolean FileUpdated = false;
		try {
			Excellock.lock();
			//Read the spreadsheet that needs to be updated
			FileInputStream fsIP= new FileInputStream(new File(FileName));  
			//Access the workbook                  
			wb = new HSSFWorkbook(fsIP);
			//Access the work sheet, so that we can update / modify it. 
			HSSFSheet worksheet = wb.getSheetAt(0);
			for(int i = 1; i< wb.getNumberOfSheets() + 1;i++) {
				//PrintOut("CurrentSheet: " + worksheet.getSheetName(), false);  //for debugging if getting errors with sheet not found
				if (worksheet.getSheetName().contentEquals(SheetName)) {
					break;
				}
				worksheet = wb.getSheetAt(i);
			}
			
			//Check the column headers for the key position
			int KeyColumn = -1;
			HSSFRow IdentifierRow = null;
			IdentifierRow = worksheet.getRow(0);
			if (KeyPosition != -1) { //Only when making an update
				for (int i = 0; i < worksheet.getLastRowNum() + 1; i++) {
					if (IdentifierRow.getCell(i) != null) {
						String Check_Cell = IdentifierRow.getCell(i).getStringCellValue();
						if (Check_Cell.contentEquals(Data[KeyPosition][0])) {
							KeyColumn = i;
							break;
						}
					}
				}
				//header not found
				if (KeyColumn == -1) {
					throw new Exception("Key Not Found");
				}
			}
			
			for (int j = 1; j < worksheet.getLastRowNum() + 1; j++) {
				//Updating values
				//System.out.print("j: " + j + "   ");//for debug
				if (worksheet.getRow(j) != null && worksheet.getRow(j).getCell(KeyColumn) != null) { //worksheet.getRow(j).getCell(KeyColumn).getStringCellValue().contentEquals(Data[KeyPosition][1]
					//format the cell just in case it is not a string.
					DataFormatter formatter = new DataFormatter();
					String val = formatter.formatCellValue(worksheet.getRow(j).getCell(KeyColumn));
					
					if (val.contentEquals(Data[KeyPosition][1])){
						for (int k = 0; k < Data.length; k++) {
							if (worksheet.getRow(j).getCell(k) == null) {//if cell not present create it
								worksheet.getRow(j).createCell(k);
							}
							for(int l = 0; l < IdentifierRow.getPhysicalNumberOfCells(); l++) {
								if (IdentifierRow.getCell(l) != null && IdentifierRow.getCell(l).getStringCellValue().contentEquals(Data[k][0])) {
									Cell cell = null; 
									if (worksheet.getRow(j).getCell(l) == null) {//if cell not present create it
										worksheet.getRow(j).createCell(l);
									}
									cell = worksheet.getRow(j).getCell(l);
									cell.setCellValue(Data[k][1]);
									//break;
								}
							}
						}
					}
				}else if (KeyPosition == -1){//when making an addition
					j = worksheet.getLastRowNum() + 1;
					for (int k = 0; k < Data.length; k++) {
						if (worksheet.getRow(j) == null) {//if cell not present create it
							worksheet.createRow(j);
						}
						if (worksheet.getRow(j).getCell(k) == null) {//if cell not present create it
							worksheet.getRow(j).createCell(k);
						}
						for(int l = 0; l < IdentifierRow.getPhysicalNumberOfCells(); l++) {
							if (IdentifierRow.getCell(l) != null && IdentifierRow.getCell(l).getStringCellValue().contentEquals(Data[k][0])) {
								Cell cell = null; 
								if (worksheet.getRow(j).getCell(l) == null) {//if cell not present create it
									worksheet.getRow(j).createCell(l);
								}
								cell = worksheet.getRow(j).getCell(l);
								cell.setCellValue(Data[k][1]);
								break;
							}
						}
					}
				}

			}
			//Close the InputStream  
			fsIP.close(); 
			//Open FileOutputStream to write updates
			FileOutputStream output_file =new FileOutputStream(new File(FileName));  
			//write changes
			wb.write(output_file);
			//close the stream
			output_file.close();
			
			FileUpdated = true;
		}catch (Exception e) {
			try {
				wb.close();
			} catch (IOException e1) {}
			e.printStackTrace();
		}finally {
			Excellock.unlock();
		}
		return FileUpdated;
	}

	public static boolean WriteToExcel(String FileName, String SheetName, String Data[][], int KeyPosition[]) {
		HSSFWorkbook wb = null;
		boolean FileUpdated = false;
		try {
			Excellock.lock();
			//Read the spreadsheet that needs to be updated
			FileInputStream fsIP= new FileInputStream(new File(FileName));  
			//Access the workbook                  
			wb = new HSSFWorkbook(fsIP);
			//Access the work sheet, so that we can update / modify it. 
			HSSFSheet worksheet = wb.getSheetAt(0);
			for(int i = 1; i< wb.getNumberOfSheets() + 1;i++) {
				//PrintOut("CurrentSheet: " + worksheet.getSheetName(), false);  //for debugging if getting errors with sheet not found
				if (worksheet.getSheetName().contentEquals(SheetName)) {
					break;
				}
				worksheet = wb.getSheetAt(i);
			}
			
			//Check the column headers for the key position
			int KeyColumn[] = new int[KeyPosition.length];
			HSSFRow IdentifierRow = null;
			IdentifierRow = worksheet.getRow(0);
			DataFormatter formatter = new DataFormatter();
			//Only when making an update
			if (KeyPosition[0] != -1) { 
				for (int Keys = 0 ; Keys < KeyPosition.length; Keys++) {
					for (int i = 0; i < worksheet.getLastRowNum() + 1; i++) {
						if (IdentifierRow.getCell(i) != null) {
							String Check_Cell = formatter.formatCellValue(IdentifierRow.getCell(i));

							if (Check_Cell.contentEquals(Data[KeyPosition[Keys]][0])) {
								KeyColumn[Keys] = i;
							}
						}
					}
					//header not found
					if (KeyColumn[Keys] == -1) {
						throw new Exception("Key Not Found, " + Data[KeyPosition[Keys]][1]);
					}
				}
				
				for (int j = 1; j < worksheet.getLastRowNum() + 1; j++) {
					//Updating values
					//System.out.print("j: " + j + "   ");//for debug
					if (worksheet.getRow(j) != null) { //worksheet.getRow(j).getCell(KeyColumn).getStringCellValue().contentEquals(Data[KeyPosition][1]
						boolean MakeUpdate = true;
						for (int CellCheck = 0; CellCheck < KeyPosition.length; CellCheck++) {
							if (worksheet.getRow(j).getCell(KeyColumn[CellCheck]) != null) {
								//format the cell just in case it is not a string.
								String val = formatter.formatCellValue(worksheet.getRow(j).getCell(KeyColumn[CellCheck]));

								if (!val.contentEquals(Data[KeyPosition[CellCheck]][1])){
									MakeUpdate = false;
								}
							}else if (worksheet.getRow(j).getCell(KeyColumn[CellCheck]) == null) {
								MakeUpdate = false;
							}
						}
						
						if (MakeUpdate){
							for (int k = 0; k < Data.length; k++) {
								if (worksheet.getRow(j).getCell(k) == null) {//if cell not present create it
									worksheet.getRow(j).createCell(k);
								}
								for(int l = 0; l < IdentifierRow.getPhysicalNumberOfCells(); l++) {
									if (IdentifierRow.getCell(l) != null && IdentifierRow.getCell(l).getStringCellValue().contentEquals(Data[k][0])) {
										Cell cell = null; 
										if (worksheet.getRow(j).getCell(l) == null) {//if cell not present create it
											worksheet.getRow(j).createCell(l);
										}
										cell = worksheet.getRow(j).getCell(l);
										cell.setCellValue(Data[k][1]);
									}
								}
							}
							break; //now that update made stop looking for value to update.
						}
					}
				}
			}else {
				//adding a new row
				int j = worksheet.getLastRowNum() + 1;
				for (int k = 0; k < Data.length; k++) {
					if (worksheet.getRow(j) == null) {//if row not present create it
						worksheet.createRow(j);
					}
					if (worksheet.getRow(j).getCell(k) == null) {//if cell not present create it
						worksheet.getRow(j).createCell(k);
					}
					for(int l = 0; l < IdentifierRow.getPhysicalNumberOfCells(); l++) {
						if (IdentifierRow.getCell(l) != null && IdentifierRow.getCell(l).getStringCellValue().contentEquals(Data[k][0])) {
							Cell cell = null; 
							if (worksheet.getRow(j).getCell(l) == null) {//if cell not present create it
								worksheet.getRow(j).createCell(l);
							}
							cell = worksheet.getRow(j).getCell(l);
							cell.setCellValue(Data[k][1]);
							break;
						}
					}
				}
			}
			//Close the InputStream  
			fsIP.close(); 
			//Open FileOutputStream to write updates
			FileOutputStream output_file =new FileOutputStream(new File(FileName));  
			//write changes
			wb.write(output_file);
			//close the stream
			output_file.close();
			
			FileUpdated = true;
		}catch (Exception e) {
			try {
				wb.close();
			} catch (IOException e1) {}
			e.printStackTrace();
		}finally {
			Excellock.unlock();
		}
		return FileUpdated;
	}

	//{{Phone number country code, Phone Number}, {...}{Mobile...}, { ...}{Fax...}, {}{email address}}}
	public static String[][] LoadPhone_Mobile_Fax_Email(String CountryCode){
		CountryCode = CountryCode.toUpperCase();
		PrintOut("LoadPhone_Mobile_Fax Received: " + CountryCode, true);
		String PhoneCode, Phone = myPhone, Mobile = myPhone, Fax = myPhone, Email = MyEmail;
		switch (CountryCode) {
        	case "US": 
        		PhoneCode = "1"; break;
        	default: 
        		PrintOut("No country code sent to LoadAddress call. Returning US Dummy Values.", true);
        		PhoneCode = "1";
		 }//end switch
		String Details[][] = {{PhoneCode, Phone} , {PhoneCode, Mobile}, {PhoneCode, Fax}, {"", Email}};
		PrintOut("LoadPhone_Mobile_Fax Returning: " + Arrays.deepToString(Details), true);
		return Details;
	}
	
	public static boolean isInteger(String s, int radix) {
	    if(s.isEmpty()) return false;
	    for(int i = 0; i < s.length(); i++) {
	        if(i == 0 && s.charAt(i) == '-') {
	            if(s.length() == 1) return false;
	            else continue;
	        }
	        if(Character.digit(s.charAt(i),radix) < 0) return false;
	    }
	    return true;
	}
	
	public static String ScreenshotBase() {
		Date curDate = new Date();
    	SimpleDateFormat Dateformatter = new SimpleDateFormat("MMddyy");
    	SimpleDateFormat Timeformatter = new SimpleDateFormat("HHmmssSSS");
    	String Time = Dateformatter.format(curDate) + "T" + Timeformatter.format(curDate);
		return Time + " L" + Environment.getInstance().getLevel() + " ";
	}
	
	public static Account_Data getFreshAccount(String Level, String CountryCode){
		Account_Data D[] = Environment.getAccountDetails(Level);
		CountryCode = CountryCode.toUpperCase();
		for (Account_Data Current: D) {
			if (Current != null && Current.Billing_Address_Info.Country_Code != null && Current.Billing_Address_Info.Country_Code.contentEquals(CountryCode)) {
				return Current;
			}
		}
		return null;
	}
	
	public static Account_Data getAddressDetails(String Level, String CountryCode){
		CountryCode = CountryCode.toUpperCase();
		Account_Data D[] = Environment.getAddressDetails();
		for (Account_Data Current: D) {
			if (Current != null && Current.Billing_Address_Info.Country_Code != null && Current.Billing_Address_Info.Country_Code.contentEquals(CountryCode)) {
				Current.Level = Level;
				//Will load the dummy values for the name associated with address.
				final String[] numNames = {"", "one","two","three","four","five","six","seven"};
				Current.FirstName = "F" + numNames[Integer.valueOf(Level)] + getRandomString(7);
				Current.MiddleName = "M";
				Current.LastName = "L" + getRandomString(7);
				return new Account_Data(Current);
			}
		}
		return null;
	}
	

}//End Class


/*
//Way to get X number of random unique numbers
List<Integer> range = IntStream.range(min, max).boxed().collect(Collectors.toCollection(ArrayList::new));
Collections.shuffle(range);
*/