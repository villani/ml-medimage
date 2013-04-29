package br.eti.villani;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Calendar;
import java.util.Scanner;
import java.util.TreeMap;

import br.eti.villani.util.LogBuilder;

/**
 * This framework provide a way to use the features extract techniques and multi-labels learners to automatic classification of two-dimensional medical images.
 * Its methods were tested with the images from IRMA-Project available in:
 * <ul>
 *  <li>
 *   <a href="http://irma-project.org/">http://irma-project.org/</a>
 *  </li>
 * </ul>
 * This framework uses the java libraries from:
 * <ul>
 *  <li>
 *   <a href="http://rsbweb.nih.gov/ij/">ImageJ - Image Processing and Analysis in Java</a>
 *  </li>
 *  <li>
 *   <a href="http://www.florianbrucker.de/index.php?p=lbp">Texture Classification Using Local Binary Patterns (LBP features)</a>
 *  </li>
 *  <li>
 *   <a href="http://www.semanticmetadata.net/lire/">LIRE - Lucene Image REtrieval (EHD and Gabor features)</a>
 *  </li>
 *  <li>
 *   <a href="http://fly.mpi-cbg.de/~saalfeld/Projects/javasift.html">Stephan Saalfeld - ImageJ Plugins - JavaSIFT (SIFT features)/</a>
 *  </li>
 *  <li>
 *   <a href="http://math.nist.gov/javanumerics/jama/">JAMA - A Java Matrix Package (package needed in JavaSIFT)</a>
 *  </li>
 *  <li>
 *   <a href="http://mulan.sourceforge.net/">Mulan - A Java Library for Multi-Label Learning</a>
 *  </li>
 *  <li>
 *   <a href="http://www.cs.waikato.ac.nz/ml/weka">Weka 3 - Data Mining Software in Java</a>
 *  </li>
 *  <li>
 *   <a href="http://jdom.org/">Java-based solution for XML data from Java code.</a>
 *  </li>
 * </ul> 
 * 
 * If you need more information, please, contact informatica@villani.et.ibr or ronaldo.prati@ufabc.edu.br.
 * 
 * @author VILLANI, L. and PRATI, R. C.
 *
 */
public class Main {

	private static LogBuilder log;
	private static TreeMap<String,String> entradas;
	private static String id;
	private static boolean direct;
	private static boolean axes;
	

	/**
	 * Gets the inputs for the run of the steps follows and registers in a file the experiment logs.
	 * @param args No parameters needed.
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

	/**
	 * Reads the experiment setup from the file CONF.INI. 
	 */
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
