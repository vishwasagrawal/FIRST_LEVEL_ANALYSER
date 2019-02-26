package ImageToText;

import java.util.ArrayList;
import java.util.List;


public class RunJar 
{
	public static void main(String[] args) throws Exception  {

		try{
			List<String> command = new ArrayList<String>();

			command.add("java");
			command.add("-jar");
			command.add("C:\\Users\\I344380\\Desktop\\PostingChecks_sikuli.jar");
			//command.add("_SAPI342598");
			//command.add("6GSCJdG9Y]9z]L4C#dc4");
			
			ProcessBuilder builder = new ProcessBuilder(command);		    
			Process process = builder.start();
			System.exit(0);

		}catch(Exception e){
			System.out.println("Executer threw a SQLException : " + e);
			e.printStackTrace();			
		}
		System.out.println("Exec FINISHED");

	}
}