package villani.eti.br.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;

/**
 * Registers in a log file the run of a experiment. 
 * @author Leonardo Villani
 *
 */
public class LogBuilder {
	
	private File file;
	private FileWriter writer;
	
	/**
	 * Creates a log file within the program run folder.
	 * @param fileName the log file name.
	 */
	public LogBuilder(String fileName){
		file = new File(fileName);
		try {
			writer = new FileWriter(file);
			writer.write(Calendar.getInstance().getTime() + ": LOG INITIALIZED\n");
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Appends the text within the log file.
	 * @param log text that will be registered.
	 */
	public void write(String log){
		try {
			writer = new FileWriter(file, true);
			log = Calendar.getInstance().getTime() + ": " + log + "\n";
			System.out.println(log);
			writer.write(log);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}
	
	/**
	 * Finalizes the log file. 
	 */
	public void close(){
		write("LOG FINALIZED\n");
	}

}
