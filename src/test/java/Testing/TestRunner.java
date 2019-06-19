package Testing;

import org.testng.TestNG;
import SupportClasses.TestNG_TestListener;
import SupportClasses.Environment;
import SupportClasses.TestNG_ReportListener;

public class TestRunner {

	static TestNG testNg;
	
	@SuppressWarnings("deprecation")
	public static void main(String[] args) {
		
		//Listener that will adds functionality before and after a test
		TestNG_TestListener testLstn = new TestNG_TestListener();
		//Listener that will generate a report based on the testing results.
		TestNG_ReportListener repLstn = new TestNG_ReportListener();
		
		Environment.getInstance().setLevel("2");
		
		testNg = new TestNG();
		
		testNg.setTestClasses(new Class[] {
				ADAT_Application.ADAT_SmokeTest.class, 
				MFAC_Application.MFAC.class});
		
		testNg.addListener(testLstn);
		testNg.addListener(repLstn);
		
		testNg.run();
	}
}
