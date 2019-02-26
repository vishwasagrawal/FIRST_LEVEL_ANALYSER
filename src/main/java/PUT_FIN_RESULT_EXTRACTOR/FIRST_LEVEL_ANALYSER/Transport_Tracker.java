package PUT_FIN_RESULT_EXTRACTOR.FIRST_LEVEL_ANALYSER;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import org.apache.poi.ss.formula.IStabilityClassifier;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class Transport_Tracker {
	static WebDriver driver=null;
	public static void main(String[] args) throws Exception 
	{
		{
			String SystemURL ="https://slc.wdf.sap.corp/cgi-bin/tmsshow_web/trtrace.pl";
			//String TRno ="HC4K045159";
			String TRno ="HC4K045257";
			
			String Destination = "HC7";

			
			/*ValidateInputInfo(args);
			TRno=args[0];
			Destination =args[1];*/

			WebDriver driver=null;
			System.out.println("Checking the Transport status: " +TRno +" in "+Destination);
			driver=CreateChromeDriverObject(SystemURL);
			FetchTRStatusFromApplication(driver, TRno, Destination);	
			DestroyAnyDriverObject(driver);
		}
	}

	private static void ValidateInputInfo(String[] args) 
	{
		if(args.length==2) 
		{
			for(int i=0;i<args.length;i++)
				if(args[i].trim().length()== 0)
				{
					System.out.println( (i+1) +" Input parameter is either null or invalid...");
					System.exit(0);
				}
		}
		else 
		{
			System.out.println("Insufficient parameter provided");
			System.exit(0);
		}
	}

	private static void DestroyAnyDriverObject(WebDriver driver)
	{		
		driver.close();
		driver.quit();	
	}


	private static void FetchTRStatusFromApplication(WebDriver driver, String TRno, String Destination) throws Exception 
	{
		driver.findElement(By.xpath("//*[contains(@name,'TRKORR')]")).sendKeys(TRno);
		driver.findElement(By.xpath("//*[contains(@name,'TSID')]")).sendKeys(Destination);
		driver.findElement(By.xpath("//*[contains(@value,'GO')]")).click();
		//	ExplicitWaitForPresence(driver, ".//*[contains(@onclick," + Destination + ")]");
		TimeUnit.SECONDS.sleep(3);

		if(isAlertPresent(driver)) 
		{
			DestroyAnyDriverObject(driver);
			throw new java.lang.RuntimeException("Expected transport is yet to reach. Have patience!!!");			
		}
		else 
		{
			if (!driver.findElements(By.xpath(".//*[contains(@style,'background-color:lightgreen')]")).isEmpty()) {
				System.out.println("Expected transport reached. Proceeding for PUT Mass Execution!!!");
			}
			else if (driver.findElement(By.xpath(".//*[contains(@style,'background-color:yellow')]")).isEnabled()) {
				System.out.println("Transport is yet to reach. Have patience!!!");
				DestroyAnyDriverObject(driver);
				//throw new java.lang.RuntimeException("Expected transport is yet to reach. Have patience!!!");
				throw new Exception("Expected transport is yet to reach. Have patience");
			} else {
				//System.out.println("Invalid data");
				DestroyAnyDriverObject(driver);
				throw new Exception("Expected transport is yet to reach. Have patience");
			}
		}

	}

	/*public static void checkAlert(WebDriver driver) throws Exception
	{		
		WebDriverWait wait = new WebDriverWait(driver, 5);
		try
		{
			if(wait.until(ExpectedConditions.alertIsPresent())!=null) 
			{
				Alert alert = driver.switchTo().alert();
				alert.accept();
				System.out.println("Expected transport is yet to reach. Have patience!!!");			
				throw new Exception("Expected transport is yet to reach. Have patience!!!");

			}
		}
		catch(Exception e)
		{
			//System.out.println("Checking !!!");
			//ExplicitWaitForPresence(driver, ".//*[contains(@onclick," + Destination + ")]");
			if (!driver.findElements(By.xpath(".//*[contains(@style,'background-color:lightgreen')]")).isEmpty()) 
			{
				System.out.println("Expected transport reached. Proceeding for PUT Mass Execution!!!");
			}

			else if (driver.findElement(By.xpath(".//*[contains(@style,'background-color:yellow')]")).isEnabled()) 
			{
				System.out.println("Transport is yet to reach. Have patience!!!");
				throw new java.lang.RuntimeException("Expected transport is yet to reach. Have patience!!!");

			} else {


			}			 
		}	
		finally 
		{
			DestroyAnyDriverObject(driver);
			System.exit(1);
		}
	}*/

	public static boolean isAlertPresent(WebDriver driver){
		boolean foundAlert = false;
		WebDriverWait wait = new WebDriverWait(driver, 5 /*timeout in seconds*/);
		try {
			wait.until(ExpectedConditions.alertIsPresent());
			foundAlert = true;
		} catch (TimeoutException eTO) {
			foundAlert = false;
		}
		return foundAlert;
	}

	@SuppressWarnings("deprecation")
	public static WebDriver CreateChromeDriverObject(String url)
	{

		String exePath = "C:\\PUT_Daily_Execution_Status\\chromedriver.exe";
		System.setProperty("webdriver.chrome.driver", exePath);	
		ChromeOptions options = new ChromeOptions();
		HashMap<String, Object> chromeOptionsMap = new HashMap<String, Object>();
		options.addArguments("--test-type");
		options.addArguments("--incognito");

		DesiredCapabilities cap = DesiredCapabilities.chrome();
		cap.setCapability(ChromeOptions.CAPABILITY, chromeOptionsMap);
		cap.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
		cap.setCapability(ChromeOptions.CAPABILITY, options);

		WebDriver driver = new ChromeDriver(cap);
		driver.navigate().to(url);
		driver.manage().window().maximize();		
		return driver;
	}
}