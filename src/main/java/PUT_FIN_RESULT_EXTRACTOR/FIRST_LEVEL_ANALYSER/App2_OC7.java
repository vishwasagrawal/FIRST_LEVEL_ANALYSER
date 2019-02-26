package PUT_FIN_RESULT_EXTRACTOR.FIRST_LEVEL_ANALYSER;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import org.apache.commons.io.FileUtils;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
//import io.github.bonigarcia.wdm.WebDriverManager;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

public class App2_OC7 {

	private static final SimpleDateFormat sdf = new SimpleDateFormat("_dd_MM_yyyy_HH_mm_ss");
	private static String src = null;	
	private static String dst = null;

	public static void main(String[] args) throws Exception 
	{
		{
			String SysClient = "CCF-715";
			String DomainName ="";
			String Username = "BPC_EXPERT";
			String Password = "Welcome1!";
			String Saml2disabled = "yes";

		/*	ValidateInputInfo(args);
			SysClient=args[0];
			Username =args[1];
			Password =args[2];
			Saml2disabled =args[3];*/

			if(Saml2disabled.equalsIgnoreCase("YES")) {
				DomainName =SysClient.indexOf("-")>=0 ? "wdf.sap.corp/ui#" : "s4hana.ondemand.com/ui?saml2=disabled#";
			}
			else {
				//DomainName =SysClient.indexOf("-")>=0 ? "wdf.sap.corp/ui#" : "s4hana.ondemand.com/ui?saml2=disabled#"; 
				DomainName =SysClient.indexOf("-")>=0 ? "wdf.sap.corp/ui#" : "s4hana.ondemand.com/ui#";

			}
			String SystemURL = "https://"+SysClient+"."+DomainName+"CloudSolution-manage&/h4screen=test";
			WebDriver driver=null;

			System.out.println("Analysis Started !!!");
			driver=CreateChromeDriverObject(SystemURL);
			CopyTemplateWorkbookAndCreateNewWorkbook(SysClient);
			LoginToWebApplication(driver,Username,Password);
			FetchStatusFromApplication(driver);	
			DestroyAnyDriverObject(driver);
			CalculateSummaryData();
		}
	}
	private static void ValidateInputInfo(String[] args) 
	{
		if(args.length==4) 
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

	public static void ExplicitWaitForInvisibility(WebDriver driver, String xpath) throws Exception
	{
		(new WebDriverWait(driver,500)).until(ExpectedConditions.invisibilityOfElementLocated(By.xpath(xpath)));
	}

	private static void DestroyAnyDriverObject(WebDriver driver)
	{
		System.out.println("**********Entering into DestroyAnyDriverObject**********");
		driver.close();
		driver.quit();	
		System.out.println("==========Exiting from DestroyAnyDriverObject==========");
	}

	private static void FetchStatusFromApplication(WebDriver driver) throws Exception 
	{
		//Load the workbook and worksheet
		System.out.println("**********Entering into FetchStatusFromApplication**********");
		FileInputStream inputStream = new FileInputStream(new File(dst));
		XSSFWorkbook wb = (XSSFWorkbook) WorkbookFactory.create(inputStream);
		XSSFSheet sheet1 = wb.getSheetAt(0);

		TimeUnit.SECONDS.sleep(5);
		driver.findElement(By.id("masterPageId--segBtn1Id-button")).click();	
		TimeUnit.SECONDS.sleep(2);

		//Total script count
		int FirstRow = 9;
		int LastRow = sheet1.getPhysicalNumberOfRows();

		System.out.println("Total PUT scripts are: " +(LastRow-FirstRow+1));
		for(int i=FirstRow;i<=LastRow;i++) 
		{
			int internalFetchingIndex = i-1;
			String data0 = sheet1.getRow(internalFetchingIndex).getCell(1).getStringCellValue();
			driver.findElement(By.id("masterPageId--searchField-I")).sendKeys(data0);
			TimeUnit.SECONDS.sleep(2);
			driver.findElement(By.id("masterPageId--searchField-search")).click();
			//	TimeUnit.SECONDS.sleep(8);
			ExplicitWaitForPresence(driver, ".//ul[@id='masterPageId--list-listUl']//li[1]//div[3]//span[1]");


			String TempStatus = null;
			String Status = null;
			//String[] Status1 = null;

			if (!driver.findElements(By.xpath(".//ul[@id='masterPageId--list-listUl']//li[1]//div[3]//span[1]")).isEmpty())	{

				for(int z=1;z<=5;z++)
				{
					driver.findElement(By.xpath(".//ul[@id='masterPageId--list-listUl']//li[1]//div[3]//span[1]")).click();
					if(driver.findElement(By.xpath(".//ul[@id='masterPageId--list-listUl']//li[1]//div[3]//span[1]")).isEnabled())
					{break;}
					else
						TimeUnit.SECONDS.sleep(2);
					continue;					
				}


				//TempStatus = driver.findElement(By.xpath(".//ul[@id='masterPageId--list-listUl']//li[1]//div[3]//div[2]")).getText();
				TempStatus = driver.findElement(By.xpath(".//ul[@id='masterPageId--list-listUl']//li[1]//div[3]//div[2]//div[1]")).getText();
				//	Status1 = TempStatus.split("\\r?\\n"); //split in array timestamp and status values.
				String FailedURL= null;

				// Fetch URL for the failed scripts
				if(TempStatus.equalsIgnoreCase("Failed"))
				{
					TimeUnit.SECONDS.sleep(5);
					driver.findElement(By.xpath("//*[@class='sapMITBContent']//div[1]//tbody")).click();
					TimeUnit.SECONDS.sleep(2);
					FailedURL = driver.getCurrentUrl();
				}
				// End for Fetch URL for the failed scripts
				//.//ul[@id='masterPageId--list-listUl']//li[1]//div[3]//div[2]: status
				//.//ul[@id='masterPageId--list-listUl']//li[1]//div[3]//div[1]//div[1]//div[1]: time

				//String ExeTime = driver.findElement(By.xpath(".//ul[@id='masterPageId--list-listUl']//li[1]//div[3]//div[1]//div[1]//div[1]")).getText();
				String ExeTime = driver.findElement(By.xpath(".//ul[@id='masterPageId--list-listUl']//li[1]//div[3]//div[1]//div[2]//span[1]")).getText();
				TimeUnit.SECONDS.sleep(2);

				//Write Excel function
				sheet1.getRow(internalFetchingIndex).createCell(2).setCellValue(TempStatus);
				sheet1.getRow(internalFetchingIndex).createCell(3).setCellValue(ExeTime);
				sheet1.getRow(internalFetchingIndex).createCell(4).setCellValue(FailedURL);
				System.out.println("Status: " +data0+ " - " +TempStatus+ "."+" Executed at " +ExeTime+"." ); //print in console status of each script
			}

			else if (driver.findElement(By.xpath(".//ul[@id='masterPageId--list-listUl']//li[1]//div[1]")).isEnabled())	{
				WebDriverWait wait1 = new WebDriverWait(driver, 30);
				wait1.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(".//ul[@id='masterPageId--list-listUl']//li[1]//div[1]")));
				Status = driver.findElement(By.xpath(".//ul[@id='masterPageId--list-listUl']//li[1]//div[1]")).getText();
				TimeUnit.SECONDS.sleep(2);
				//Write Excel function
				sheet1.getRow(internalFetchingIndex).createCell(2).setCellValue(Status);
				System.out.println("Status for the script " +data0+ " is " +TempStatus+ "." ); //print in console status of script with NO DATA
			}
			else {
				System.out.println("Script does not exists");
			}

			FileOutputStream outputStream = new FileOutputStream(new File(dst));
			wb.write(outputStream);
			driver.findElement(By.id("masterPageId--searchField-reset")).click();
		}
		System.out.println("==========Exiting from FetchStatusFromApplication==========");
	}

	private static void CalculateSummaryData() throws Exception 
	{
		System.out.println("**********Entering into CalculateSummaryData**********");
		FileInputStream inputStream = new FileInputStream(new File(dst));
		XSSFWorkbook wb = (XSSFWorkbook) WorkbookFactory.create(inputStream);
		XSSFSheet sheet1 = wb.getSheetAt(0);
	//	XSSFCellStyle sheet2 = wb.createCellStyle();

		int FirstRow = 9;
		int LastRow = sheet1.getPhysicalNumberOfRows();

		int Scnt=0, Fcnt=0, Ncnt=0, Incnt=0, Nocnt=0,Cncnt=0;
		for (int i = FirstRow; i <=LastRow; i++) 
		{
			int internalFetchingIndex = i-1;
			String Status = sheet1.getRow(internalFetchingIndex).getCell(2).getStringCellValue();
			if(Status.equalsIgnoreCase("Success")) 
				{ Scnt = Scnt+1;
				//sheet2.setFillBackgroundColor(IndexedColors.GREEN.getIndex());
				//sheet2.setFillPattern(FillPatternType.BIG_SPOTS); 
				}

			else if(Status.equalsIgnoreCase("failed"))
				{Fcnt = Fcnt+1; 
				//sheet2.setFillBackgroundColor(IndexedColors.RED.getIndex());
				//sheet2.setFillPattern(FillPatternType.BIG_SPOTS); 
				}
								
			else if(Status.equalsIgnoreCase("Not executable"))
				Ncnt = Ncnt+1;
			else if(Status.equalsIgnoreCase("In Process"))
				Incnt = Incnt+1;
			else if(Status.equalsIgnoreCase("No data"))
				Nocnt = Nocnt+1;
			else if(Status.equalsIgnoreCase("Canceled"))
				Cncnt = Cncnt+1;
			else {continue;}
		}
		sheet1.getRow(0).createCell(2).setCellValue(Scnt);
		sheet1.getRow(1).createCell(2).setCellValue(Fcnt);
		sheet1.getRow(2).createCell(2).setCellValue(Ncnt);
		sheet1.getRow(3).createCell(2).setCellValue(Incnt);
		sheet1.getRow(4).createCell(2).setCellValue(Nocnt);
		sheet1.getRow(5).createCell(2).setCellValue(Cncnt);

		inputStream.close(); 
		FileOutputStream outputStream = new FileOutputStream(new File(dst));
		wb.write(outputStream);
		outputStream.close();
		wb.close();
		System.out.println("==========Exiting from CalculateSummaryData==========");
		System.out.println("##### Summary from the Analyser #####");
		System.out.println("Success: " +Scnt);
		System.out.println("Failed: " +Fcnt);
		System.out.println("Not executable: " +Ncnt);
		System.out.println("In Process: " +Incnt);		
		System.out.println("No data: " +Nocnt);
		System.out.println("Canceled: " +Cncnt);
		System.out.println("Analysis Completed. Please check the latest PUT results @ C:\\PUT_Daily_Execution_Status");		
	}

	private static void LoginToWebApplication(WebDriver driver, String username, String password) throws Exception 
	{
		System.out.println("**********Entering into LoginToWebApplication**********");
		try {
			driver.findElement(By.id("USERNAME_FIELD-inner")).sendKeys(username);
			driver.findElement(By.id("PASSWORD_FIELD-inner")).sendKeys(password);
			driver.findElement(By.id("LOGIN_LINK")).click();
		} catch (Exception e) {
			driver.findElement(By.id("j_username")).sendKeys(username);
			driver.findElement(By.id("j_password")).sendKeys(password);
			driver.findElement(By.id("logOnFormSubmit")).click();			
		}

		//App2_OC7.AuthPopUp(driver);
		WebDriverWait wait = new WebDriverWait(driver, 50);
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("masterPageId--segBtn1Id-button")));
		System.out.println("Login Operation was Successful...");
		System.out.println("==========Exiting from LoginToWebApplication==========");
	}

	private static void CopyTemplateWorkbookAndCreateNewWorkbook(String sysClient) throws IOException 
	{
		System.out.println("**********Entering into CopyTemplateWorkbookAndCreateNewWorkbook**********");
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		String var= sdf.format(timestamp);
		//copy file
		src = "C:\\PUT_Daily_Execution_Status\\PUT_Results_Template.xlsx";	
		dst = "C:\\PUT_Daily_Execution_Status\\PUT_Results_"+sysClient + "_" +var+ ".xlsx";
		FileUtils.copyFile(new File(src), new File(dst));
		System.out.println("New Workbook created Successfully...");
		System.out.println("==========Exiting from CopyTemplateWorkbookAndCreateNewWorkbook==========");
	}

	public static WebDriver CreateChromeDriverObject(String url)
	{
		System.out.println("**********Entering into CreateChromeDriverObject**********");
		//	WebDriverManager.chromedriver().setup();
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
		System.out.println("Browser Maximized");
		System.out.println("==========Exiting from CreateChromeDriverObject==========");
		return driver;
	}

	//To close Authentication popup in customer systems
	public static void AuthPopUp(WebDriver driver) {
		try {
			WebDriverWait wait1 = new WebDriverWait(driver, 50);
			wait1.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@id='SAMLDialog-footer']")));
			if(driver.findElement(By.xpath("//*[@id='SAMLDialog-footer']")).isEnabled()) 
			{
				driver.findElement(By.xpath("//*[@id='SAMLDialog-footer']//button//span//span")).click();
				//log.info("Authentication window popup = Closed");
			}			
		}
		catch (Exception e) {}
	}

	public static void ExplicitWaitForPresence(WebDriver driver, String xpath) throws Exception {
		(new WebDriverWait(driver,
				500)).until(ExpectedConditions.presenceOfElementLocated(By.xpath(xpath)));
		TimeUnit.SECONDS.sleep(5);
	}
}