package br.eti.villani.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

/**
 * This class builds the xml file based in the content of the txt file provided by Image Retrieval in Medical Applications (IRMA). 
 * The txt file contains the IRMA code structure used in various medical applications.
 * To download the current version of txt file, access 
 * <a href="http://irma-project.org/irma_code_formular_en.php">IRMA Code download</a>.
 * That xml file is need in multi-label application that uses the <a href="http://mulan.sourceforge.net/">mulan framework</a>.
 * @author VILLANI, L. and PRATI, R. C.
 *
 */
public class XmlIrmaCodeBuilder {
	
	/**
	 * The name of the source file that has the IRMA code structure. 
	 */
	private String fileName; 
	
	/**
	 * The name of the XML file created.
	 */
	private String xmlName;
	
	/**
	 * The object to manipulate the content of the source file with the IRMA code structure.
	 */
	private File src;
	
	/**
	 * The object to read the content of the source file.
	 */
	private Scanner reader;
	
	/**
	 * The object to write the content of the xml file.
	 */
	private FileWriter xml;
	
	/**
	 * The vector with the axes of the IRMA code. 
	 */
	private char[] irmaCode = {'T','D','A','B'}; 
	
	/**
	 * Builds the xml file based in content of the txt source file.
	 * @param fileName The name of the source file that has the IRMA code structure. 
	 * That file must be within project directory that uses the XmlIrmaCodeBuilder.
	 * @throws FileNotFoundException If source file not found.  
	 * @throws IOException If the xml file cannot be manipulated.
	 */
	public XmlIrmaCodeBuilder(String fileName, String xmlName) throws FileNotFoundException, IOException {
		
		this.fileName = fileName;
		this.xmlName = xmlName;
		this.src = new File(this.fileName);
		this.reader = new Scanner(this.src);
		this.xml = new FileWriter(this.xmlName + ".xml");
		
		this.xml.write("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
		this.xml.write("<labels xmlns=\"http://mulan.sourceforge.net/labels\">");
		int index = -1;
		String code = "";
		String codePrevious = "";
		while(this.reader.hasNextLine()){
			String line = this.reader.nextLine();
			if(line.contains("****")) {
				index++;
				continue;
			}
			if(line.contains("[")){
				code = irmaCode[index] + line.substring(line.indexOf("[") + 1, line.indexOf("]"));
				insertNode(codePrevious, code);
				codePrevious = code;
			}
		}
		close(code);
	}
	
	/**
	 * Inserts within xml file the node with the code extracted from the txt source file.
	 * Uses the code length inserted previously and the code length that will inserted to identify the level that the node will be inserted. 
	 * @param codePrevious
	 * @param code
	 * @throws IOException If the xml file cannot be manipulated.
	 */
	private void insertNode(String codePrevious, String code) throws IOException{
		if(!codePrevious.equals("")){
			if(codePrevious.length() == code.length()) this.xml.write("</label>\n");
			if(codePrevious.length() > code.length()){
				int i = codePrevious.length() - code.length();
				while(i >= 0){
					this.xml.write("</label>\n");
					i--;
				}
			}
		}
		this.xml.write("\n<label name=\"" + code + "\">");
	}
	
	/**
	 * Finalizes the nodes opened and saves the xml file.
	 * @param code
	 * @throws IOException If the xml file cannot be manipulated.
	 */
	private void close(String code) throws IOException{
		for(int i = code.length(); i > 1; i--){
			this.xml.write("</label>");
		}
		this.xml.write("</labels>");
		this.xml.close();
	}
	
	
	/**
	 * Checks if the xml file was created.
	 * @return true if the xml file was created with the sucess, and false otherwise.
	 */
	public boolean hasXml(){
		File xml = new File(xmlName + ".xml");
		return xml.exists();
	}


	
	/**
	 * Tests the XmlIrmaCodeBuilder code.
	 * @param args The name of the source file that has the IRMA code structure.
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try{
			XmlIrmaCodeBuilder xicb = new XmlIrmaCodeBuilder(args[0], args[1]);
			if(xicb.hasXml()) System.out.println("The xml is available.");;
		} catch(Exception e){
			System.out.println("Fail: [" + e.getMessage() + "]");
		}	
	}

}
