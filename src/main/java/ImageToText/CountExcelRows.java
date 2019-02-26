package ImageToText;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class CountExcelRows 
{
	public static String downloadFilepath = "C:\\CountExcelData";
	public static int ExcelRowCount;
	public static void main(String[] args) throws Exception 
	{
		{
			String SysClient = "HC7-715";
			String Username = "BPC_EXPERT";
			String Password = "Welcome1!";

			/*ValidateInputInfo(args);
			SysClient=args[0];
			Username =args[1];
			Password =args[2];*/

			String SystemURL = "https://"+SysClient+"."+"wdf.sap.corp/ui#CloudSolution-manage";
			String SystemURLAdmin = "https://"+SysClient+"."+"wdf.sap.corp/ui#CloudSolution-administer";
			WebDriver driver=null;

			driver=CreateChromeDriverObject(SystemURL);			
			LoginToWebApplication(driver,Username,Password);
			FetchDataFromApplication(driver, SystemURLAdmin);
			DownloadExcel(driver);
			FetchValidationDataFromExcelSheet("ConfigurationItems.csv");
			CreateSheetAndUpdate(driver);
			DestroyAnyDriverObject(driver);
		}
	}

	private static void CreateSheetAndUpdate(WebDriver driver) throws Exception 
	{
		try {
			File fileName = new File("C:\\CountExcelData\\Details.xlsx");
			FileOutputStream fos = new FileOutputStream(fileName);
			XSSFWorkbook  workbook = new XSSFWorkbook();
			XSSFSheet sheet = workbook.createSheet("Details");
			Row row = sheet.createRow(0);   
			Cell cell0 = row.createCell(0);
			cell0.setCellValue("Phase");
			
			Row row1 = sheet.createRow(1);
			Cell cell1 = row1.createCell(1);
			cell1.setCellValue("Country");       
			
			Row row2 = sheet.createRow(2);
			Cell cell2 = row2.createCell(2);
			cell2.setCellValue("Configuration Items");

			workbook.write(fos);
			fos.flush();
			fos.close();
		} catch (Exception e) {
		
			e.printStackTrace();
		}
		
	}

	private static void FetchDataFromApplication(WebDriver driver, String SystemURLAdmin) 
	{
		WebDriverWait wait = new WebDriverWait(driver, 50);
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@id='application-CloudSolution-manage-component---HomePage--phaseEvaluateTitle-title']")));
		String Phase = driver.findElement(By.xpath("//*[@id='application-CloudSolution-manage-component---HomePage--phaseEvaluateTitle-title']")).getText();
		String Country = driver.findElement(By.xpath("//*[@id='application-CloudSolution-manage-component---HomePage--countryCode']")).getText();
		System.out.println("System Phase: "+Phase + "\n"+"Default country: " +Country);
		driver.navigate().to(SystemURLAdmin);
		
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(".//span[contains(text(),'Configuration Items')]")));
		String ConfigItems = driver.findElement(By.xpath(".//span[contains(text(),'Configuration Items (')]")).getText();
		System.out.println("Configurations Items: "+ConfigItems.trim().replaceAll("[a-zA-Z()]", ""));
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

	public static WebDriver CreateChromeDriverObject(String url)
	{
		System.out.println("**********Entering into CreateChromeDriverObject**********");
		String exePath = "C:\\PUT_Daily_Execution_Status\\chromedriver.exe";
		System.setProperty("webdriver.chrome.driver", exePath);	

		HashMap<String, Object> chromePrefs = new HashMap<String, Object>();
		chromePrefs.put("profile.default_content_settings.popups", 0);
		chromePrefs.put("download.default_directory", downloadFilepath);
		ChromeOptions options = new ChromeOptions();
		options.setExperimentalOption("prefs", chromePrefs);
		DesiredCapabilities cap = DesiredCapabilities.chrome();
		cap.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
		cap.setCapability(ChromeOptions.CAPABILITY, options);
		WebDriver driver = new ChromeDriver(cap);


		HashMap<String, Object> chromeOptionsMap = new HashMap<String, Object>();
		options.addArguments("--test-type");
		options.addArguments("--incognito");

		cap.setCapability(ChromeOptions.CAPABILITY, chromeOptionsMap);
		driver.navigate().to(url);
		driver.manage().window().maximize();
		System.out.println("Browser Maximized");
		System.out.println("==========Exiting from CreateChromeDriverObject==========");
		return driver;
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

		//ExplicitWaitForInvisibility(driver,"//DIV[@id='__xmlview1--fin.ar.lineitems.display.TableItems-busyIndicator']");
		TimeUnit.SECONDS.sleep(10);		
		System.out.println("Login Operation was Successful...");
		System.out.println("==========Exiting from LoginToWebApplication==========");
	}

	private static void DestroyAnyDriverObject(WebDriver driver)
	{
		System.out.println("**********Entering into DestroyAnyDriverObject**********");
		driver.close();
		driver.quit();	
		System.out.println("==========Exiting from DestroyAnyDriverObject==========");
	}

	public static void ExplicitWaitForInvisibility(WebDriver driver, String xpath) throws Exception
	{
		(new WebDriverWait(driver,500)).until(ExpectedConditions.invisibilityOfElementLocated(By.xpath(xpath)));
	}

	private static void DownloadExcel(WebDriver driver) throws Exception 
	{
		System.out.println("**********Entering into DownloadExcel**********");
		try {
			TimeUnit.SECONDS.sleep(2);
			//ExplicitWaitForInvisibility(driver,"//DIV[@id='__xmlview1--fin.ar.lineitems.display.TableItems-busyIndicator']");
			WebDriverWait wait = new WebDriverWait(driver, 100);
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//BDI[text()='Download']")));
			TimeUnit.SECONDS.sleep(10);
			driver.findElement(By.xpath("//BDI[text()='Download']")).click();
			TimeUnit.SECONDS.sleep(5);
			System.out.println("File download is successful.");
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("**********Exiting into DownloadExcel**********");
	}

	public static void FetchValidationDataFromExcelSheet(String FileName) throws Exception 
	{
		System.out.println("**********Entering into FetchValidationDataFromExcelSheet**********");
		try {
			BufferedReader reader = Files.newBufferedReader(Paths.get(downloadFilepath + "\\" + FileName));
			CSVParser csvParser = new CSVParser(reader,CSVFormat.DEFAULT);
			int csvRecords = csvParser.getRecords().size();
			System.out.println("Total Number of records are: " +csvRecords);
		} catch (Exception e) {			
			e.printStackTrace();
		}
		System.out.println("**********Exiting into FetchValidationDataFromExcelSheet**********");
	}
}