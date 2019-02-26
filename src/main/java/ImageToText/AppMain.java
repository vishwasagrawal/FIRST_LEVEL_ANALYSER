package ImageToText;

import org.apache.log4j.Logger;

public class AppMain {

	private static Logger logger = Logger.getLogger(AppMain.class);

	public static void main(String[] args) {
		String xlsLoc = "C:\\CountExcelData\\", csvLoc = "C:\\CountExcelData\\ConfigurationItems.csv", fileLoc = "";

		fileLoc = CSVToExcel.convertCsvToXls(xlsLoc, csvLoc);
		logger.info("File Location Is?= " + fileLoc);
	}
}