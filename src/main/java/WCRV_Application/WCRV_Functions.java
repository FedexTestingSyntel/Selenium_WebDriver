package WCRV_Application;

import static org.junit.Assert.assertEquals;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import SupportClasses.DriverFactory;
import SupportClasses.Helper_Functions;
import SupportClasses.WebDriver_Functions;

public class WCRV_Functions{
	public static String svcGroupIndicator[][] = {{"INTRA_COUNTRY","D"},{"EXPORT","E"},{"IMPORT","I"},{"IMPORT_INBOUND","II"},{"LEGACY_THIRD_PARTY","LT"},{"GLOBAL_THIRD_PARTY","GT"}};
	//public static String svcGroupIndicator[][] = {{"INTRA_COUNTRY","D"},{"EXPORT","E"},{"IMPORT","I"},{"GLOBAL_THIRD_PARTY","GT"}};
	
	//{Service Code,   Brief service name,      full service name}
	public static String serviceBaseCode[][] = {
			{"SL","Sp", "SmartPost"},
			{"SP", "SpDom", "SmartPostDom"}, //Added on 01/18/19
			{"SB", "SpBPM", "SmartPost Bound Printed Matter"}, //Added on 01/18/19
			{"SM", "SpMed", "SmartPost Media"},//Added on 01/18/19
			{"FE","Fec", "NEED TO ADD NAME"},
			{"FP","fp", "NEED TO ADD NAME"},
			{"92","Gd", "Ground"},
			{"90","Hd", "Home Delivery"},
			{"88","SD", "Intra-MX SD-Letter"},
			{"87","SDci", "Intra-MX SD City LTR"},
			{"86","IEF", "Economy Freight"},
			//{"84","IPddf", "Priority DirectDistribution Freight"},   //distribution services removed from list after defect 470145 
			{"83","3Df", "3Day Freight"},
			{"80","2Df", "2Day Freight"},
			{"70","1Df", "1Day Freight"},
			{"49","2DAm", "2Day AM"},
			{"39","Ff", "First Overnight® Freight"},
			{"26", "Econ", "FedEx Economy"},
			{"24","Ndnoon", "FedEx Next Day by 12 Noon, GB to GB"},
			{"20","Es", "Express Saver"},
			//{"18","IPdd", "Priority DirectDistribution"},   //distribution services removed from list after defect 470145 
			//{"17","IEdd", "Economy DirectDistribution"},   //distribution services removed from list after defect 470145 
			{"06","Fo", "First Overnight/International"},
			{"05","So", "Standard Overnight"},
			{"04","Iec", "International Economy"},
			{"03","2Da", "2Day"},
			{"01","Po", "Priority Overnight/International"}};     
	
	public static String[] WCRV_Generate(String CountryCode, String User, String Password) throws Exception{
		Random rand = new Random();
		int numServicetoSelect = rand.nextInt(3) + 1;
		return WCRV_Generate(CountryCode, User, Password, "any", numServicetoSelect);
	}
	
	//Service can be intra, notintra, export, import, third, or any.     //not case sensitive
	//intra = domestic, notintra = international
 	public static String[] WCRV_Generate(String CountryCode, String User, String Password, String Service, int numServicetoSelect) throws Exception{
 		Service = Service.toUpperCase();
 		String RateSheetID = "";
		try {
			WebDriver_Functions.Login(User, Password);
				
			//to load the cookies for the given country
			WebDriver_Functions.ChangeURL("WDPA", CountryCode, false);
			WebDriver_Functions.ChangeURL("WCRV", CountryCode, false);
			
			//Early exist if use does not have access
			if (WebDriver_Functions.CheckBodyText("This user does not have access to the FedEx Rate Sheet Tool.")) {
				throw new Exception("UserId does not have access.");
			}
			
			//wait for WCRV page to load
			WebDriver_Functions.WaitPresent(By.id("wcrv-profile"));

			try{
				WebDriver_Functions.WaitPresent(By.id("account"));
			}catch (Exception e){
				Helper_Functions.PrintOut("Account number section did not load.", true);
				throw e;
			}
			
			//select the first account number from the account list box if needed
			try{
				WebDriver_Functions.Select(By.id("account"), "1", "i");
			}catch (Exception e){}

			//wait for the shipping address label to load
			WebDriver_Functions.WaitPresent(By.cssSelector("#wcrv-address-control > label"));
			
			String AccountCountry = WebDriver_Functions.GetValue(By.id("accountCountry"));//This is saved for use in confirmation page.
			String State = "";
			try{
				State = WebDriver_Functions.GetText(By.cssSelector("#wcrv-address-control > div > table > tbody:nth-child(3) > tr:nth-child(1) > td"));
				State = State.substring(State.lastIndexOf(",") - 2 , State.lastIndexOf(","));
			}catch (Exception e){}
			
			WebDriver_Functions.WaitForText(By.cssSelector("div.fx-inner-grid.fx-cf > h5"), " FedEx Services available from your country TO other countries.");
			
			//wait for the service element to be loaded  //random.nextInt(max - min + 1) + min
			Random rand = new Random();
			String RateSheetName = "";//the code of the services
			int gi = 0, sbc = 0, i = 0;
			for(int attempt = 0; i < numServicetoSelect; attempt++){
				try{
					if (attempt < 20){// try X number of times to randomly select a service.
						gi = rand.nextInt(svcGroupIndicator.length);
						sbc = rand.nextInt(serviceBaseCode.length);
					}else if (attempt == 20){// After the X time start systematically choosing services.
						gi = 0; sbc = 0;
					}else{
						if (sbc == serviceBaseCode.length - 1){
							gi += 1;	
							sbc = 0;
						}else if (gi == svcGroupIndicator.length){
							Helper_Functions.PrintOut("No services can be selected", true);
							throw new Exception();
						}else{
							sbc += 1;
						}
					}

					boolean try_to_select = false;
					if (Service.contentEquals("ANY")) {
						try_to_select = true;
					}else if (svcGroupIndicator[gi][0].contains(Service)) {
						try_to_select = true;
					}
					
					if(try_to_select && WCRV_SelectService(svcGroupIndicator[gi][0], serviceBaseCode[sbc][0])){
						//DebugOut("trying gi" + gi + "    sbc" + sbc);
						if (RateSheetName == ""){
							RateSheetName = svcGroupIndicator[gi][1] + serviceBaseCode[sbc][1];
						}else{
							RateSheetName += "." + svcGroupIndicator[gi][1] + serviceBaseCode[sbc][1] ;
						}
						i++;
						Helper_Functions.PrintOut("Selected " + svcGroupIndicator[gi][0] + " " + serviceBaseCode[sbc][2] + " " + i +" of " + numServicetoSelect, true);
					}
				}catch(Exception e){
					Helper_Functions.PrintOut(" No " + svcGroupIndicator[gi][0] + " " + serviceBaseCode[sbc][2], true);
				}
			}//end for i
				
			Select selLanguage = new Select(DriverFactory.getInstance().getDriver().findElement(By.id("wcrv-language")));
			selLanguage.selectByValue("EN");//change language to English
			
				
			try{//check that the default currency loads
				WebDriver_Functions.WaitForTextNot(By.id("wcrv-currency"), "Select or enter");
				Select selCurrency = new Select(DriverFactory.getInstance().getDriver().findElement(By.id("wcrv-currency")));
				selCurrency.selectByValue("USD");
			}catch (Exception e){
				Helper_Functions.PrintOut("Default currency did not load.", true);
				Select selCurrency = new Select(DriverFactory.getInstance().getDriver().findElement(By.id("wcrv-currency")));
				selCurrency.selectByValue("USD");
			}

			DriverFactory.getInstance().getDriver().findElement(By.id("rateSheetName")).clear();
			DriverFactory.getInstance().getDriver().findElement(By.id("rateSheetName")).sendKeys(RateSheetName);
			if (rand.nextInt(5) > 4){
				 DriverFactory.getInstance().getDriver().findElement(By.id("rateSheetProfileName")).clear();
				 String Time = Helper_Functions.CurrentDateTime();
				 DriverFactory.getInstance().getDriver().findElement(By.id("rateSheetProfileName")).sendKeys(RateSheetName + Time);
				 Helper_Functions.PrintOut("Profile " + RateSheetName + Time + " has been attempted", true);
			}
			
			RateSheetName =  WebDriver_Functions.GetValue(By.id("rateSheetName"));//in case the full name may not fit field
			WebDriver_Functions.takeSnapShot(RateSheetName + ".png");
			Helper_Functions.PrintOut("RateSheet " + RateSheetName + " has been attempted", true);
			WebDriver_Functions.Click(By.id("wcrv-submit"));
			WebDriver_Functions.WaitForText(By.cssSelector("h1"), "Rate Sheet Request Confirmation");
			WebDriver_Functions.WaitForText(By.xpath("//div[@id='modalWindowSuccess']/div/div/div/div/label[4]"), RateSheetName);
			RateSheetID = WebDriver_Functions.GetText(By.xpath("//div[@id='modalWindowSuccess']/div/div/div/div[2]/label[2]/b")).substring(2);
			//.substring(2) is to remove the "# " of the rate sheet id.
			Helper_Functions.PrintOut(RateSheetName + " was successfully submitted with ID of " + RateSheetID, true);
			WebDriver_Functions.takeSnapShot(RateSheetName + "Conf.png");

			boolean RateSheetHistory = false;
			try {
				WebDriver_Functions.Click(By.id("success-ok"));
				WebDriver_Functions.WaitPresent(By.cssSelector("th.sorting"));
				if (AccountCountry.contentEquals("US") && State.contentEquals("PR")){
					//ITG 166046 - ID 1509566
					WebDriver_Functions.WaitForText(By.xpath("//*[@id='manageTable']/tbody/tr[1]/td[3]"), "PR");
				}else{
					WebDriver_Functions.WaitForText(By.xpath("//*[@id='manageTable']/tbody/tr[1]/td[3]"), AccountCountry);
				}
				
				WebDriver_Functions.WaitForText(By.xpath("//*[@id='manageTable']/tbody/tr[1]/td[5]"), RateSheetID);
				WebDriver_Functions.WaitForText(By.xpath("//table[@id='manageTable']/tbody/tr/td[8]"), "Pending");
				WebDriver_Functions.takeSnapShot(RateSheetName + "RSHT.png");
				RateSheetHistory = true;
			}catch (Exception RSH) {
				Helper_Functions.PrintOut("Not able to validate Rate Sheet history table.");
			}
			
		
			return new String[] {User, RateSheetName, RateSheetID, "History Table: " + RateSheetHistory};
			//need to create a new thread here to wait 10mins then check if rate sheet is completed then download.
		} catch (Exception e) {
			Helper_Functions.PrintOut("Not able to complete WCRV_Generate", true);
			throw new Exception(RateSheetID + e.getLocalizedMessage());
		}
 	}//end WCRV_Generate
 
 	public static void WCRV_Profile(String CountryCode, String User, String Password){
 		CountryCode = CountryCode.toUpperCase();
	
		//String SCPath = Helper_Functions.CurrentDateTime() + " L" + Level + " WCRV ";
		
		try {
			WebDriver_Functions.Login(User, Password);
					
			// launch the browser and direct it to the Base URL
			WebDriver_Functions.ChangeURL("WDPA", CountryCode, true);//to load the cookies for the given country
			WebDriver_Functions.ChangeURL("WCRV", CountryCode, true);
			//wait for WCRV page to load
			WebDriver_Functions.WaitPresent(By.id("wcrv-profile"));
			try{
				WebDriver_Functions.WaitPresent(By.id("account"));
			}catch (Exception e){
				Helper_Functions.PrintOut("Account numbers did not load", true);
				throw new Exception();
			}
			
			//check the profile drop down
			try{
				WebDriver_Functions.Click(By.id("wcrv-profile"));
				Select selectProfile = new Select(DriverFactory.getInstance().getDriver().findElement(By.id("wcrv-profile")));
				String firstprofilename = selectProfile.getFirstSelectedOption().getText();
				if (!firstprofilename.matches("No profiles")){
					selectProfile.selectByIndex(1);//select the first profile. The 0 element will be "Select or enter"
					String strSelectedProfile = selectProfile.getFirstSelectedOption().getText();
					//wait for the profile to load
					WebDriver_Functions.WaitClickable(By.id("wcrv-submit"));
					try{
						assertEquals("", WebDriver_Functions.GetText(By.id("rateSheetProfileName")));
					}catch (Exception e) {
						Helper_Functions.PrintOut("    180586 - ID 1575048", true);
					}
					
					try{//check that the default language loads
						WebDriver_Functions.WaitForTextNot(By.id("wcrv-language"), "Select or enter");
					}catch (Exception e){Helper_Functions.PrintOut("Default language did not load.", true);}
					Select selLanguage = new Select(DriverFactory.getInstance().getDriver().findElement(By.id("wcrv-language")));
					selLanguage.selectByValue("EN");//change language to English
					
					try{//check that the default currency loads
						WebDriver_Functions.WaitForTextNot(By.id("wcrv-currency"), "Select or enter");
					}catch (Exception e){Helper_Functions.PrintOut("Default currency did not load.", true);}
					Select selCurrency = new Select(DriverFactory.getInstance().getDriver().findElement(By.id("wcrv-currency")));
					selCurrency.selectByValue("USD");
					
					DriverFactory.getInstance().getDriver().findElement(By.id("rateSheetProfileName")).clear();
					DriverFactory.getInstance().getDriver().findElement(By.id("rateSheetProfileName")).sendKeys(strSelectedProfile);
					WebDriver_Functions.Click(By.id("wcrv-submit"));
					try{
						WebDriver_Functions.WaitForText(By.xpath("//*[@id='wcrv-options-control']/div/div/div/div[6]/div[2]/div/div"), "!\nAlert: This profile name already exists. Any options you have chosen will overwrite previous selections for this profile. If you want to create a new profile, please enter a new name.");
					}catch (Exception e) {
						Helper_Functions.PrintOut("180586 - ID 1575044 - Alert message for when submitting profile name already saved.", true);
						WebDriver_Functions.ChangeURL("WCRV", CountryCode, true);
						WebDriver_Functions.WaitPresent(By.id("account"));
					}
					selectProfile.selectByIndex(0);
					DriverFactory.getInstance().getDriver().findElement(By.id("rateSheetProfileName")).clear();
				}
			}catch (Exception e){Helper_Functions.PrintOut("    Not able to complete profile drop down", true);}
			
			//select the profiles tab
			WebDriver_Functions.Click(By.cssSelector("#profileTab > a > b"));
			
			try{
				assertEquals("Profile", WebDriver_Functions.GetText(By.xpath("//*[@id='profileTab']/a/b")));
			}catch (Exception e) {Helper_Functions.PrintOut("180586 - ID 1574983 - Profile tab label not correct", true);}
			
			try{
				assertEquals("Rate Sheet Profile", WebDriver_Functions.GetText(By.xpath("//*[@id='deleteProfile']/h2")));
			}catch (Exception e) {Helper_Functions.PrintOut("180586 - ID 1574994 - Profile Tab -> Page Title not correct", true);}
			
			String strCustomerServiceLink = "";
			try{
				WebDriver_Functions.WaitForText(By.id("serviceText"), "For questions about your rate sheets, please contact your local Customer Service Center.");
				try{
					strCustomerServiceLink = WebDriver_Functions.GetValue(By.id("generalError_custsvc_link"));
					//http://www.fedex.com/<locale>/contact/ "  https://wwwdrt.idev.fedex.com/en-us/customer-support.html
					if (!strCustomerServiceLink.contains(CountryCode.toLowerCase())){
						throw new Exception();
					}
				}catch (Exception e) {Helper_Functions.PrintOut("180586 - ID 1575058 - link within help text not correct", true);}
			}catch (Exception e) {Helper_Functions.PrintOut("180586 - ID 1575058 - help text at bottom of profile tab", true);}
			
			try{
				WebDriver_Functions.WaitPresent(By.id("profile"));
				WebDriver_Functions.WaitPresent(By.xpath("//*[@id='deleteProfile']/h5"));
				WebDriver_Functions.WaitForText(By.xpath("//*[@id='deleteProfile']/h5"), "Too many profiles? Use this page to delete unused profiles. Deleted profiles will no longer be available for selection when you generate a new rate sheet.");
			}catch (Exception e){
				if (WebDriver_Functions.GetText(By.tagName("body")).contains("There are no profiles saved for this account.")){
					Helper_Functions.PrintOut("Completed Profiles testing since user had no profiles", true);
					return;
				}
				Helper_Functions.PrintOut("180586 - ID 1575056", true);
			}
			
			//did not test 180586 - ID 1574995testing Functional > Profile Tab > Profile List Container need to manually test
			
			try{
				WebDriver_Functions.WaitForText(By.id("deleteButton"), "Delete Profile");
			}catch (Exception e) {Helper_Functions.PrintOut("180586 - ID 1566758", true);}

			try{ 
				WebDriver_Functions.Click(By.xpath("//*[@id='profile']"));
				String DeletedProfile = WebDriver_Functions.GetText(By.cssSelector("td.rowData"));
				WebDriver_Functions.Click(By.cssSelector("#deleteButton > button"));
				//profile should have just been deleted, select the profile tab again to refresh
				WebDriver_Functions.Click(By.cssSelector("#profileTab > a > b"));
				WebDriver_Functions.WaitClickable(By.xpath("//*[@id='profile']"));
				Assert.assertNotSame(DeletedProfile, WebDriver_Functions.GetValue(By.xpath("//*[@id='profile']")));
			}catch (Exception e) {
				Helper_Functions.PrintOut("180586 - ID <<Add ID>> Not able to delete profile", true);
			}
			
			try{
				if (strCustomerServiceLink != null){
					DriverFactory.getInstance().getDriver().get(strCustomerServiceLink);
					String ps = DriverFactory.getInstance().getDriver().getPageSource();
					if (ps.contains("Error 404") || ps.contains("page not found") || ps.contains("this page is not available")){
	        			throw new Exception();
	        		}
				}
			}catch (Exception e) {Helper_Functions.PrintOut("180586 - ID 1575058 - Customer service link is help text not working correctly", true);}
			
			Helper_Functions.PrintOut("Completed WCRV_Profile with no issues", true);
		} catch (Exception e) {
			Helper_Functions.PrintOut("Not able to complete WCRV_Profile", true);
			WCRV_CheckException(e);
		}	
 }//end WCRV_Profile
 	
 	public static void WCRV_CheckcountryServices(String CountryCode, String User, String Password){
		//String SCPath = Helper_Functions.CurrentDateTime() + " L" + Level + " WCRV ";
		String All_Services = "";
		try {
			Helper_Functions.PrintOut("Starting WCRV_CheckcountryServices", true);
			
			WebDriver_Functions.Login(User, Password);
			WebDriver_Functions.ChangeURL("WDPA", CountryCode, true);//to load the cookies for the given country
			WebDriver_Functions.ChangeURL("WCRV", CountryCode, true);
			//wait for WCRV page to load
			WebDriver_Functions.WaitPresent(By.id("wcrv-profile"));
			WebDriver_Functions.WaitPresent(By.id("account"));
			Select selectAccount = new Select(DriverFactory.getInstance().getDriver().findElement(By.id("account")));
			List<WebElement> AccountsCount = selectAccount.getOptions();   
			
			//loop through all available accounts. the 0 element will be "Select or enter" and the last two elements will be "----" and "Account(s) not enabled for rate sheets" so minus 2
			for (int i = 1; i < AccountsCount.size() - 2; i++){
				Actions actions = new Actions(DriverFactory.getInstance().getDriver());
				//select the first account number from the account list box if needed
				try{
					actions.moveToElement(DriverFactory.getInstance().getDriver().findElement(By.id("account"))).perform();
					selectAccount.selectByIndex(i);//select the first account. The 0 element will be "Select or enter"
				}catch (Exception e){}

			    //wait for the shipping address label to load
				WebDriver_Functions.WaitPresent(By.cssSelector("#wcrv-address-control > label"));
				actions.moveToElement(DriverFactory.getInstance().getDriver().findElement(By.cssSelector("div.fx-inner-grid.fx-cf > h5"))).perform();
				WebDriver_Functions.WaitForText(By.cssSelector("div.fx-inner-grid.fx-cf > h5"), " FedEx Services available from your country TO other countries.");
				WebDriver_Functions.WaitPresent(By.xpath("//*[@id='EFXE']/h5"));//the "FedEx Express" label in the export section
				
				try{//This will check if the domestic area is collapsed and if so expand it.
					if (!DriverFactory.getInstance().getDriver().findElement(By.id("domesticSelectAll")).isDisplayed()){
						WebDriver_Functions.Click(By.cssSelector("#domesticServices > span"));
						Helper_Functions.PrintOut("Domestic area was not displayed by default.", true);
					}
				}catch (Exception e){Helper_Functions.PrintOut("Failure in displaying domestic area.", true);}
				try{//This will check if the international area is collapsed and if so expand it.
					if (!DriverFactory.getInstance().getDriver().findElement(By.id("exportSelectAll")).isDisplayed()){
						WebDriver_Functions.Click(By.cssSelector("#internationalServices > span"));
						Helper_Functions.PrintOut("International area was not displayed by default.", true);
					}
				}catch (Exception e){Helper_Functions.PrintOut("Failure in displaying international area.", true);}

				DriverFactory.getInstance().getDriver().manage().timeouts().implicitlyWait(DriverFactory.WaitTimeOut, TimeUnit.MICROSECONDS);
				for (int gi = 0; gi < svcGroupIndicator.length; gi++){
					for(int sbc = 0; sbc < serviceBaseCode.length;sbc++){
						try{
							WebElement element = null;
							//select the correct tab and position screen
							if (gi == 0 ){
								element = DriverFactory.getInstance().getDriver().findElement(By.id("wcrv-services-control"));
								//move screen to above the Domestic Services section
								actions.moveToElement(DriverFactory.getInstance().getDriver().findElement(By.xpath("//*[@id='domesticServices']/b"))).perform();
							}else{
								if (gi == 1){
									element = DriverFactory.getInstance().getDriver().findElement(By.id("exportE"));
								}else if(gi == 2 || gi == 3){
									element = DriverFactory.getInstance().getDriver().findElement(By.id("importI"));
								}else if (gi == 4 || gi == 5){
									element = DriverFactory.getInstance().getDriver().findElement(By.id("tPartyTP"));
								}
								actions.moveToElement(DriverFactory.getInstance().getDriver().findElement(By.xpath("//*[@id='internationalServices']/b"))).perform();
							}

							//move the screen to the service 
							actions.moveToElement(element).perform();
							element.click();
							
							//check if service is present
							By byXpath = By.xpath("//input[(@svcgroupindicator='" + svcGroupIndicator[gi][0] + "') and (@value = '" + serviceBaseCode[sbc][0] + "')]");
							element = DriverFactory.getInstance().getDriver().findElement(byXpath);
							actions.moveToElement(element).perform();
							element.click();
							//// know the service is on the screen
							String AccountCountryCode = WebDriver_Functions.GetText(By.xpath("//*[@id='wcrv-address-control']/div/table/tbody[1]/tr[1]/td")); 
							if (All_Services.isEmpty()){
								All_Services = "{\"" + AccountCountryCode + "\", \"" + svcGroupIndicator[gi][0] + "\", \"" + serviceBaseCode[sbc][0] + "\"}";
							}else{
								All_Services += ", {\"" + AccountCountryCode + "\", \"" + svcGroupIndicator[gi][0] + "\", \"" + serviceBaseCode[sbc][0] + "\"}";
							}
							Helper_Functions.PrintOut(svcGroupIndicator[gi][0] + " " + serviceBaseCode[sbc][2]  + " is present.", true);
						}catch(Exception e){
							//to debug to see the different services that cannot be selected
							Helper_Functions.PrintOut("No " + svcGroupIndicator[gi][0] + " " + serviceBaseCode[sbc][2] , true);
						}
					}//end for sbc
				}//end for gi
			}//end for i
		All_Services+= ", ";
		Helper_Functions.PrintOut(All_Services, true);	
		} catch (Exception e) {
			//Helper_Functions.PrintOut("General failure in WCRV_CheckcountryServices");
			WCRV_CheckException(e);
		};
 	}//end WCRV_CheckcountryServices

 	private static void WCRV_CheckException(Exception e){
		DriverFactory.getInstance().getDriver().manage().timeouts().implicitlyWait(DriverFactory.WaitTimeOut, TimeUnit.MICROSECONDS);
		try{
			Helper_Functions.PrintOut("General failure in WCRV: " + Thread.currentThread().getStackTrace()[2].getMethodName(), true);
			if(WebDriver_Functions.isPresent(By.xpath("//*[@id='fx-error-banner']/p"))){
				Helper_Functions.PrintOut((DriverFactory.getInstance().getDriver().findElement(By.xpath("//*[@id='fx-error-banner']/p"))).getAttribute("innerText"), true);
			}else if (WebDriver_Functions.isPresent(By.xpath("//*[@id='wcrv-confirmation-control']/div[1]/div[1]/h1"))){
				Helper_Functions.PrintOut((DriverFactory.getInstance().getDriver().findElement(By.xpath("//*[@id='wcrv-confirmation-control']/div[1]/div[1]/div/div[1]/label"))).getAttribute("innerText"), true);
			}else{
				e.printStackTrace();
			}
		}catch (Exception e2){Helper_Functions.PrintOut("General failure in WCRV_CheckException.", true);}
		DriverFactory.getInstance().getDriver().manage().timeouts().implicitlyWait(DriverFactory.WaitTimeOut, TimeUnit.SECONDS);
 	}
 	
 	private static boolean WCRV_SelectService(String svgGroup, String servBaseCode){
		DriverFactory.getInstance().getDriver().manage().timeouts().implicitlyWait(DriverFactory.WaitTimeOut, TimeUnit.MICROSECONDS);
 		try{
 			WebElement element = null;
 			Actions actions = new Actions(DriverFactory.getInstance().getDriver());
 			//select the correct tab and position screen
 			if (svgGroup.contentEquals("INTRA_COUNTRY")){
 				element = DriverFactory.getInstance().getDriver().findElement(By.id("wcrv-services-control"));
 				//move screen to above the Domestic Services section
 				actions.moveToElement(DriverFactory.getInstance().getDriver().findElement(By.xpath("//*[@id='domesticServices']/b"))).perform();
 			}else{
 				if (svgGroup.contentEquals("EXPORT")){
 					element = DriverFactory.getInstance().getDriver().findElement(By.id("exportE"));
 				}else if (svgGroup.contentEquals("IMPORT")|| svgGroup.contentEquals("IMPORT_INBOUND")){
 					element = DriverFactory.getInstance().getDriver().findElement(By.id("importI"));
 				}else if (svgGroup.contentEquals("LEGACY_THIRD_PARTY")|| svgGroup.contentEquals("GLOBAL_THIRD_PARTY")){
 					element = DriverFactory.getInstance().getDriver().findElement(By.id("tPartyTP"));
 				}
 				actions.moveToElement(DriverFactory.getInstance().getDriver().findElement(By.xpath("//*[@id='internationalServices']/b"))).perform();
 			}

 			//move the screen to the service 
 			actions.moveToElement(element).perform();
 			element.click();
					
 			//check if service is present
 			By byXpath = By.xpath("//input[(@svcgroupindicator='" + svgGroup + "') and (@value = '" + servBaseCode + "')]");
 			element = DriverFactory.getInstance().getDriver().findElement(byXpath);
 			actions.moveToElement(element).perform();
 			element.click();

 			Helper_Functions.PrintOut(svgGroup + " " + servBaseCode  + " has been selected.", true);
 			
			//populate the location area
			WebElement FromElement = null, ToElement = null; 
			By ByFromElement = null, ByToElement = null;
			if (svgGroup.contentEquals("IMPORT_INBOUND") || svgGroup.contentEquals("LEGACY_THIRD_PARTY")){
				if (svgGroup.contentEquals("IMPORT_INBOUND")){
					WebDriver_Functions.WaitForText(By.id("importLoc1"), "");
					FromElement = DriverFactory.getInstance().getDriver().findElement(By.id("importLoc1"));
					ByFromElement = By.id("importLoc1");
				}else if (svgGroup.contentEquals("LEGACY_THIRD_PARTY")){
					WebDriver_Functions.WaitForText(By.id("thirdpartyFromLoc1"), "");
					FromElement = DriverFactory.getInstance().getDriver().findElement(By.id("thirdpartyFromLoc1"));
					ByFromElement = By.id("thirdpartyFromLoc1");
					WebDriver_Functions.WaitForText(By.id("thirdpartyToLoc1"), "");
					ToElement = DriverFactory.getInstance().getDriver().findElement(By.id("thirdpartyToLoc1"));
					ByToElement = By.id("thirdpartyToLoc1");
				}
				try{
					String AccountCountryCode =WebDriver_Functions.GetText(By.xpath("//*[@id='wcrv-address-control']/div/table/tbody[1]/tr[1]/td")); 
					String CountryCodes[] = {"US", "CA", "IN"};
					for (int k = 0; k < CountryCodes.length; k++){
						if (!CountryCodes[k].contentEquals(AccountCountryCode) && FromElement.getAttribute("value").isEmpty()){//From location cannot use same location as the account country
							FromElement.clear();
							String ZipCode = Helper_Functions.LoadAddress(CountryCodes[k])[5];
							FromElement.sendKeys(ZipCode);//enter the zip code
							//this part will select the first address that is loaded by google addresses
							Thread.sleep(1000);
							FromElement.sendKeys(Keys.DOWN);
							FromElement.sendKeys(Keys.RETURN);
							WebDriver_Functions.WaitForTextNotPresentIn(ByFromElement, ZipCode);
							//DriverFactory.getInstance().getDriverWait().until(ExpectedConditions.not(ExpectedConditions.textToBePresentInElement(FromElement, ZipCode)));
							Helper_Functions.PrintOut("From location: " + FromElement.getAttribute("value"), true);
							if (ToElement== null){
								break;
							}
						}else if (!CountryCodes[k].contentEquals(AccountCountryCode) && ToElement.getAttribute("value").isEmpty()){//To location cannot use same location as the account country
							ToElement.clear();
							String ZipCode = Helper_Functions.LoadAddress(CountryCodes[k])[5];
							ToElement.sendKeys(ZipCode);//enter the zip code
							//this part will select the first address that is loaded by google addresses
							Thread.sleep(1000);
							ToElement.sendKeys(Keys.DOWN);
							ToElement.sendKeys(Keys.RETURN);
							WebDriver_Functions.WaitForTextNotPresentIn(ByToElement, ZipCode);
							//DriverFactory.getInstance().getDriverWait().until(ExpectedConditions.not(ExpectedConditions.textToBePresentInElement(ToElement, ZipCode)));
							Helper_Functions.PrintOut("To location: " + ToElement.getAttribute("value"), true);
							break;
						}
					}//end for k
				}catch (Exception e2){
					Helper_Functions.PrintOut("Exception when populationg locaiton fields", true);
					return false;
				}
			}else if (svgGroup.contentEquals("INTRA_COUNTRY")){
				//enter a valid location for the country. ITG166046 - ID1532820
				String DA_DZ_Countries[] = {"CA", "MX", "BR", "CO", "GB", "FR"};
				String AccountCountry = WebDriver_Functions.GetValue(By.id("accountCountry"));
				if (Arrays.asList(DA_DZ_Countries).contains(AccountCountry)){
					String ZipCode = Helper_Functions.LoadAddress(AccountCountry)[5];
					FromElement = DriverFactory.getInstance().getDriver().findElement(By.id("domesticLoc1"));
					FromElement.sendKeys(ZipCode);
					Thread.sleep(1000);
					FromElement.sendKeys(Keys.DOWN);
					FromElement.sendKeys(Keys.RETURN);
					//DriverFactory.getInstance().getDriverWait().until(ExpectedConditions.not(ExpectedConditions.textToBePresentInElement(FromElement, ZipCode)));
					WebDriver_Functions.WaitForTextNotPresentIn(ByFromElement, ZipCode);
					Helper_Functions.PrintOut("From location: " + FromElement.getAttribute("value"), true);
				}
			}
 		}catch(Exception e){
 			//Helper_Functions.PrintOut(svgGroup + " " + servBaseCode  + " is not present to be selected.", true);
 			return false;
 		}
 		DriverFactory.getInstance().getDriver().manage().timeouts().implicitlyWait(DriverFactory.WaitTimeOut, TimeUnit.SECONDS);
 		return true;
 	}//end WCRV_SelectService

 	public static boolean WCRV_CheckPermission(String CountryCode, String User, String Password) throws Exception{;
		try {
			WebDriver_Functions.Login(User, Password);
				
			//to load the cookies for the given country
			WebDriver_Functions.ChangeURL("WDPA", CountryCode, false);
			WebDriver_Functions.ChangeURL("WCRV", CountryCode, false);
			
			//Early exist if use does not have access
			if (WebDriver_Functions.CheckBodyText("This user does not have access to the FedEx Rate Sheet Tool.")) {
				throw new Exception("UserId does not have access.");
			}
			
			//wait for WCRV page to load
			WebDriver_Functions.WaitPresent(By.id("wcrv-profile"));
			return true;
		} catch (Exception e) {
			return false;
		}
 	}//end WCRV_Generate

 	public static String WCRV_Check_Help_Links(String CountryCode) throws Exception{
 		String mainWindowHandle = DriverFactory.getInstance().getDriver().getWindowHandle();
 		try {
 			//to load the cookies for the given country
 	 		WebDriver_Functions.ChangeURL("WDPA", CountryCode, false);
 	 		WebDriver_Functions.ChangeURL("WCRV", CountryCode, false);
 	 		
 	 		WebDriver_Functions.WaitClickable(By.id("help"));
 	 		WebDriver_Functions.Click(By.id("help"));
 	 		
 	 		//view the new help file
 			for (String childWindowHandle : DriverFactory.getInstance().getDriver().getWindowHandles()) {
 				//If window handle is not main window handle then close it 
 				if(!childWindowHandle.equals(mainWindowHandle)){
 					DriverFactory.getInstance().getDriver().switchTo().window(childWindowHandle);
 					try {
 						WebDriver_Functions.WaitForTextNot(By.tagName("body"), (""));
 	 					if (!WebDriver_Functions.CheckBodyText("Generate a Rate Sheet")) {
 	 						throw new Exception("Text not present in help file.");
 	 					}
 					}catch (Exception e){
 						throw e;
 					}finally {
 						// Close child windows
 	 					DriverFactory.getInstance().getDriver().close(); 
 					}
 				}
 			}//end for child window
 		}catch (Exception e) {
 			String ECRV_ENABLED_COUNTRIES_LOCALE= "es_AR,en_AR,en_AW,de_AT,en_AT,en_BS,en_BB,en_BM,en_BQ,pt_BR,en_BR,en_VG,en_KY,es_CL,en_CL,es_CO,en_CO,en_CW,da_DK,en_DK,es_DO,en_DO,en_EE,en_FI,fi_FI,de_DE,en_DE,en_GD,fr_GP,en_GP,it_IT,en_IT,en_JM,en_LV,en_LT,fr_MQ,en_MQ,en_NO,no_NO,en_PL,pl_PL,es_ES,en_ES,en_KN,en_LC,en_SX,en_VC,sv_SE,en_SE,en_TT,en_TC,es_UY,en_UY,en_VI,es_VE,en_VE,en_BE,fr_BE,nl_BE,es_CR,en_CR,cs_CZ,en_CZ,fr_FR,en_FR,es_GT,en_GT,en_HU,hu_HU,en_LU,nl_NL,en_NL,es_PA,en_PA,sl_SI,en_SI,en_CH,de_CH,fr_CH,it_CH,fr_CA,en_CA,en_IE,en_GB,en_MX,es_MX,en_BH,en_IN,en_KW,en_AE,ar_AE,en_BW,en_MW,en_MZ,en_NA,en_ZA,en_SZ,en_ZM,en_US,es_US,en_PR,es_PR,en_AU,zh_CN,en_CN,en_GU,tc_HK,en_HK,zh_HK,ja_JP,en_JP,en_MO,en_MY,en_NZ,en_PH,en_SG,ko_KR,en_KR,tc_TW,en_TW,zh_TW,en_TH,th_TH,en_VN,en_ID,";
		 	if (ECRV_ENABLED_COUNTRIES_LOCALE.contains(CountryCode + ",")) {         		
		 		throw new Exception(CountryCode + " Help page not loading correctly.");
		 	}else {
		 		throw new Exception(CountryCode + " Help page not loading correctly. May not be WCRV enabled.");
		 	}
 		}finally {
 			DriverFactory.getInstance().getDriver().switchTo().window(mainWindowHandle);
 		}
 		
	 	return "Help File is appearing for " + CountryCode;
 	}	
}