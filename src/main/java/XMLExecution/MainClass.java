package XMLExecution;

import java.util.ArrayList;
import java.util.List;
import org.testng.TestNG;

public class MainClass {
	/*
	 * import org.testng.TestListenerAdapter;
	@SuppressWarnings("deprecation")
	public static void main(String[] args) {
		TestListenerAdapter tla = new TestListenerAdapter();
		TestNG testng = new TestNG();
		testng.setTestClasses(new Class[] { MFAC_Application.MFAC.class, ADAT_Application.ADAT_SmokeTest.class});
		testng.addListener(tla);
		testng.run();
		}
	*/
	
	public static class mainClass {
	    public static void main(String[] args){
	    List<String> file = new ArrayList<String>();
	    file.add("C:\\Users\\5159473\\Documents\\GitHub\\Selenium_WebDriver\\src\\main\\java\\ADAT_Application.xml");
	    file.add("C:\\Users\\5159473\\Documents\\GitHub\\Selenium_WebDriver\\src\\main\\java\\MFAC_Application.xml");
	    TestNG testNG = new TestNG();
	    testNG.setTestSuites(file);
	    testNG.run();}
	}
}
