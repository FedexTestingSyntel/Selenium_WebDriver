package INET_Application;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import Data_Structures.Account_Data;
import Data_Structures.Address_Data;
import Data_Structures.Enrollment_Data;
import Data_Structures.User_Data;
import SupportClasses.DriverFactory;
import SupportClasses.Environment;
import SupportClasses.Helper_Functions;
import SupportClasses.WebDriver_Functions;
import edu.emory.mathcs.backport.java.util.Arrays;

public class INET_Shipment {
	static String LevelsToTest = "3";
	static String CountryList[][];
	
	@BeforeClass
	public void beforeClass() {
		Environment.SetLevelsToTest(LevelsToTest);
		CountryList = Environment.getCountryList("smoke");
	}
	
	@DataProvider (parallel = true)
	public static Iterator<Object[]> dp(Method m) {
		List<Object[]> data = new ArrayList<Object[]>(); 

		for (int i=0; i < Environment.LevelsToTest.length(); i++) {
			String Level = String.valueOf(Environment.LevelsToTest.charAt(i));
			int intLevel = Integer.parseInt(Level);
			switch (m.getName()) { //Based on the method that is being called the array list will be populated.
	    	case "WFCL_Reset_Password_Email":
	    		User_Data User_Info_Array[] = User_Data.Get_UserIds(intLevel);
	    		for (String[] Country: CountryList) {
	    			for (User_Data User_Info: User_Info_Array) {
	    				if (User_Info.Address_Info.Country_Code.contentEquals(Country[0]) && !User_Info.ACCOUNT_NUMBER.isEmpty()) {
	    					data.add( new Object[] {Level, User_Info});
	    					break;
	    				}
	    			}
				}
	    		break;
			}
		}	
		return data.iterator();
	}
	
	@Test(dataProvider = "dp")
	public static void INET_Create_Shipment(String Level, User_Data User_Info){
		try {
			INET_Create_Shipment("Unsure fix to send correct later", User_Info, User_Info.Address_Info, User_Info.Address_Info);
		}catch (Exception e) {
			Assert.fail(e.getMessage());
		}
	}//end OADR_ApplyDiscount
	
/////////////Need to convert the below to user address data and user data.
	public static void INET_Create_Shipment(String Service, User_Data User_Info, Address_Data Origin_Address_Info, Address_Data Destination_Address_Info) throws Exception{
		try {
			// launch the browser and direct it to the Base URL
			WebDriver_Functions.Login(User_Info);
			WebDriver_Functions.ChangeURL("INET", User_Info.Address_Info.Country_Code, false);
			WebDriver_Functions.Click(By.id("module.from._headerEdit"));
			for (int i = 0; i < 2; i++){
				String Loc = "to";
				Address_Data Address_Info = Destination_Address_Info;
				if (i == 1){
					Loc = "from";
					Address_Info = Origin_Address_Info;
				}
				//wait for country to load
				WebDriver_Functions.WaitForTextPresentIn(By.id(Loc + "Data.countryCode"), "A");

	 			WebDriver_Functions.Select(By.id(Loc + "Data.countryCode"), Address_Info.Country_Code, "v");
	 			
	 			WebDriver_Functions.WaitForTextPresentIn(By.id(Loc + "Data.countryCode"), "A");
				WebDriver_Functions.WaitPresent(By.id(Loc + "Data.contactName"));
				String ContactName = Helper_Functions.getRandomString(8);//enter filler name
				
				WebDriver_Functions.Type(By.id(Loc + "Data.companyName"), "Comp " + ContactName);
				WebDriver_Functions.Type(By.id(Loc + "Data.addressLine1"), Address_Info.Address_Line_1);
				WebDriver_Functions.Type(By.id(Loc + "Data.addressLine2"), Address_Info.Address_Line_2);
				
				if (!Address_Info.Zip.isEmpty()){
					//had to change method of input due to issues with error message when zip is cleared
					JavascriptExecutor myExecutor = ((JavascriptExecutor) DriverFactory.getInstance().getDriver());
			    	WebElement zipcode = DriverFactory.getInstance().getDriver().findElement(By.id(Loc + "Data.zipPostalCode"));
			    	myExecutor.executeScript("arguments[0].value='"+ Address_Info.Zip + "';", zipcode);
				}
				//enter city 
				WebDriver_Functions.Type(By.id(Loc + "Data.city"), Address_Info.City);
				WebDriver_Functions.Select(By.id(Loc + "Data.stateProvinceCode"), Address_Info.State_Code, "v");
				WebDriver_Functions.Type(By.id(Loc + "Data.phoneNumber"), Helper_Functions.myPhone);
			}
			WebDriver_Functions.Type(By.id("psd.mps.row.weight.0"), "1");
			
			//Service type
		    WebElement dropdown = DriverFactory.getInstance().getDriver().findElement(By.id("psdData.serviceType"));
		    //dropdown.click();
		    List<WebElement> options = dropdown.findElements(By.tagName("option"));
		    String optTxt = null;
		    for(WebElement option : options){
		    	optTxt = option.getText();
		        if(optTxt.contains(Service)){
		            break;
		        }
		    }
		    WebDriver_Functions.Select(By.id("psdData.serviceType"), optTxt, "t");
			if (Service.contentEquals("Priority")){
				WebDriver_Functions.WaitPresent(By.id("psd.mps.row.weight.0"));
				WebDriver_Functions.Type(By.id("psd.mps.row.weight.0"), "10");
				WebDriver_Functions.Select(By.id("psdData.packageType"), "FedEx Box", "v");
				if (optTxt.contains("International")){
					WebDriver_Functions.Click(By.id("commodityData.packageContents.products"));
					WebDriver_Functions.Type(By.id("commodityData.totalCustomsValue") ,"10");
				}
				//Pickup/Drop-off Modal
				WebDriver_Functions.Click(By.id("pdm.initialChoice.schedulePickup"));
				WebDriver_Functions.WaitPresent(By.id("pickupAddress.collapse"));
				WebDriver_Functions.WaitPresent(By.id("packageInfo.collapse"));
			}else if (Service.contentEquals("Ground")){
				WebDriver_Functions.Select(By.id("psdData.packageType"), "2", "i");//Barrel
				WebDriver_Functions.Select(By.id("commodityData.shipmentPurposeCode"), "3", "i");//Gift
				WebDriver_Functions.Type(By.id("commodityData.totalCustomsValue") ,"10");
				//Pickup/Drop-off Modal
				WebDriver_Functions.Click(By.id("pdm.initialChoice.schedulePickup"));
				WebDriver_Functions.WaitPresent(By.id("pickupAddress.collapse"));
				WebDriver_Functions.WaitPresent(By.id("packageInfo.collapse"));
			}else if (Service.contentEquals("Freight")){
				//Package type is set to "Your Packaging"
				WebDriver_Functions.WaitPresent(By.id("psd.mps.row.dimensions.0"));
				WebDriver_Functions.Type(By.id("psd.mps.row.weight.0") ,"200");
				
				//wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("psd.mps.row.dimensions.0")));
				WebDriver_Functions.Select(By.id("psd.mps.row.dimensions.0"), "manual", "v");
				//wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("psd.mps.row.dimensionLength.0")));
				//wait.until(ExpectedConditions.elementToBeClickable(By.id("psd.mps.row.dimensionLength.0")));
				
				WebDriver_Functions.Type(By.id("psd.mps.row.dimensionLength.0") ,"8");
				WebDriver_Functions.Type(By.id("psd.mps.row.dimensionWidth.0") ,"4");
				WebDriver_Functions.Type(By.id("psd.mps.row.dimensionHeight.0") ,"3");
				
				//Pickup/Drop-off Modal
				WebDriver_Functions.Click(By.id("pdm.initialChoice.schedulePickup"));
				///actions.moveToElement(driver.findElement(By.id("module.from._headerTitle"))).perform();
				///wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("dropoff.viewMoreLocations.link")));
				///wait.until(ExpectedConditions.elementToBeClickable(By.id("pdm.initialChoice.schedulePickup"))).click();
				WebDriver_Functions.WaitPresent(By.id("pickupAddress.collapse"));
				WebDriver_Functions.WaitPresent(By.id("packageInfo.collapse"));
				//edit the pickup information
				WebDriver_Functions.Click(By.id("packageInfo.edit.plus"));
				WebDriver_Functions.WaitPresent(By.id("pdm.personWithSkidNumber"));
				WebDriver_Functions.Type(By.id("pdm.personWithSkidNumber"), "John");

				WebDriver_Functions.Select(By.id("pdm.dimProfile"), "manual", "v");
				//wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("pdm.dimLength")));
				//wait.until(ExpectedConditions.elementToBeClickable(By.id("pdm.dimLength")));
				WebDriver_Functions.Type(By.id("pdm.dimLength") ,"8");
				WebDriver_Functions.Type(By.id("pdm.dimWidth") ,"4");
				WebDriver_Functions.Type(By.id("pdm.dimHeight") ,"3");
				//Lift gate
				WebDriver_Functions.Select(By.id("pdm.truckType"), "L", "v");
				//Trailer size = 28
				WebDriver_Functions.Select(By.id("pdm.truckSize"), "28", "v");
			}
			
			WebDriver_Functions.takeSnapShot("Shipment.png");
			WebDriver_Functions.Click(By.id("completeShip.ship.field"));
			
			//Enter product/commodity information
			if (optTxt.contains("International")){
				try{
					WebDriver_Functions.Select(By.id("commodityData.chosenProfile.profileID"), "add", "v");
					WebDriver_Functions.WaitPresent(By.id("commodityData.chosenProfile.description"));
					WebDriver_Functions.Type(By.id("commodityData.chosenProfile.description") ,"Generic Description");
					WebDriver_Functions.Select(By.id("commodityData.chosenProfile.unitOfMeasure"), "1", "i");
					WebDriver_Functions.Type(By.id("commodityData.chosenProfile.quantity") ,"10");
					WebDriver_Functions.Type(By.id("commodityData.chosenProfile.commodityWeight") ,"50");

					WebDriver_Functions.Select(By.id("commodityData.chosenProfile.manufacturingCountry"), "1", "i");
					WebDriver_Functions.Click(By.id("commodity.button.addCommodity"));
					WebDriver_Functions.WaitForText(By.id("commodity.summaryTable._contents._row1._col2"), "Generic Description");
					WebDriver_Functions.takeSnapShot("product_commodity information.png");
					WebDriver_Functions.Click(By.id("completeShip.ship.field"));
				}catch (Exception e){}
			}else{
				//Confirm shipping details
				WebDriver_Functions.WaitPresent(By.id("confirm.ship.field"));
				WebDriver_Functions.Click(By.id("completeShip.ship.field"));
			}
			
			//confirmation page
			WebDriver_Functions.WaitPresent(By.id("trackingNumber"));

			Helper_Functions.PrintOut("Need to finish", true);

			if (WebDriver_Functions.isPresent(By.id("label.alert.unsuccessfulPickupSchedule"))){
				Helper_Functions.PrintOut(WebDriver_Functions.GetText(By.id("label.alert.unsuccessfulPickupSchedule")), true);
			}
			
		}catch (Exception e){
			throw e;
		}
	}//end WDPAShipment
  
	
}
