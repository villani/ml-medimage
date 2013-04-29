package villani.eti.br;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Calendar;
import java.util.Scanner;
import java.util.TreeMap;

import villani.eti.br.util.*;

public class Main {

	public static LogBuilder log;
	public static TreeMap<String,String> entradas;
	public static String id;
	public static boolean direct;
	public static boolean axes;
	

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{

		Calendar data = Calendar.getInstance();
		id = "Exe" + 
				data.get(Calendar.YEAR) + 
				(data.get(Calendar.MONTH)+1) + 
				data.get(Calendar.DAY_OF_MONTH) +
				data.get(Calendar.HOUR_OF_DAY) +
				data.get(Calendar.MINUTE);
		
		File diretorio = new File(id);
		
		if(!diretorio.isDirectory()) diretorio.mkdir();
		
		id = diretorio + "/";

		log = new LogBuilder(id + "Running.log");

		log.write("Starting " + id + ":");

		log.write("Getting the system inputs from conf.ini.");
		init();

		log.write("Checking inputs:");
		for(String key : entradas.keySet()){
			log.write(" - " + key + ": " + entradas.get(key));
		}
		
		log.write("Starting Step One - Data preparing...");
		Preparing.run(id, log, entradas);
		
		log.write("Starting Step Two - Building bases in ARFF format...");
		Building.run(id, log, entradas);
		
		if(direct){
			log.write("Starting Step Three - Evaluating classifiers to direct annotation...");
			Evaluating.run(id, log, entradas);

			log.write("Starting Step Four - Organizing results of the evaluating of the classifiers to direct annotation...");
			Tabulating.run(id, log, entradas);
		} else {
			log.write("Without direct classification.");
			log.write("Scapeding steps three and four...");
		}
		
		if(axes){
			log.write("Starting Step Five - Building bases separate by axes...");
			BuildingAxes.run(id, log, entradas);
			
			log.write("Starting Step Six - Evaluating classifiers to classification by axes...");
			EvaluatingAxes.run(id, log, entradas);
			
			log.write("Starting Step Seven - Organizing results of the evaluating of the classifier to annotation by axes...");
			TabulatingAxes.run(id, log, entradas);		
		} else {
			log.write("Without classification by axes.");
			log.write("Scapeding steps five, six and seven...");
		}
				
		log.write("Finalizing experiments.");
		log.close();

	}

	public static void init(){

		File conf = new File("conf.ini");
		Scanner leitor = null;

		try {
			leitor = new Scanner(conf);
		} catch (FileNotFoundException e) {
			log.write("Failure to receive the inputs system: " + e.getMessage());
			System.exit(0);
		}

		entradas = new TreeMap<String,String>();

		while(leitor.hasNextLine()){
			String linha = leitor.nextLine();
			String parametros[] = linha.split("=");
			if(parametros.length < 2) continue;
			entradas.put(parametros[0], parametros[1]);
		}
		
		direct = Boolean.parseBoolean(entradas.get("direct"));
		axes = Boolean.parseBoolean(entradas.get("axes"));

		log.write("Inputs obtained.");
		leitor.close();
	}

}
