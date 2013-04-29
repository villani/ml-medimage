package br.eti.villani.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Converts the IRMA code to binary code and binary code to IRMA code.
 * @author VILLANI, L. and PRATI, R. C.
 *
 */
public class IrmaCode {
	
	private String nameXmlIrmaCode;
	private File xml;
	private Scanner reader;
	private ArrayList<String> structureIrmaCode;
	
	
	/**
	 * Instantiate a IrmaCode object.
	 * @param nameXmlIrmaCode The patch to XML file with the structure IRMA.
	 * @throws FileNotFoundException
	 */
	public IrmaCode(String nameXmlIrmaCode) throws FileNotFoundException{
		this.nameXmlIrmaCode = nameXmlIrmaCode + ".xml";
		this.xml = new File(this.nameXmlIrmaCode);
		this.reader = new Scanner(this.xml);
		this.structureIrmaCode = new ArrayList<String>();
		
		while(this.reader.hasNextLine()){
			String line = this.reader.nextLine();
			if(line.contains("label name")){
				this.structureIrmaCode.add(line.substring(line.indexOf("=\"") + 2, line.indexOf("\">")));
			}
		}
	}
	
	/**
	 * Converts to binary the IRMA code in format "1111-123-123-123".
	 * @param code The IRMA code.
	 * @return
	 */
	public String toBinary(String code){
		ArrayList<String> irmaCode = new ArrayList<String>();
		irmaCode.add("T" + code.charAt(0));
		irmaCode.add("T" + code.charAt(0) + code.charAt(1));
		irmaCode.add("T" + code.charAt(0) + code.charAt(1) + code.charAt(2));
		irmaCode.add("T" + code.charAt(0) + code.charAt(1) + code.charAt(2) + code.charAt(3));
		irmaCode.add("D" + code.charAt(5));
		irmaCode.add("D" + code.charAt(5) + code.charAt(6));
		irmaCode.add("D" + code.charAt(5) + code.charAt(6) + code.charAt(7));
		irmaCode.add("A" + code.charAt(9));
		irmaCode.add("A" + code.charAt(9) + code.charAt(10));
		irmaCode.add("A" + code.charAt(9) + code.charAt(10) + code.charAt(11));
		irmaCode.add("B" + code.charAt(13));
		irmaCode.add("B" + code.charAt(13) + code.charAt(14));
		irmaCode.add("B" + code.charAt(13) + code.charAt(14) + code.charAt(15));
		
		String binaryCode = "";
		
		for(String c: this.structureIrmaCode){
			binaryCode += (irmaCode.contains(c))?"1":"0";
			binaryCode += ",";
		}
		System.out.println();
		return binaryCode.substring(0, binaryCode.length() - 1);
	}
	
	/**
	 * Convert to IRMA code format a binary code separated by comma: "1,1,0,1,0,1..."
	 * @param binaryCode Binary code.
	 * @return Code in IRMA format.
	 */
	public String toIrmaProjectFormat(String binaryCode){
		
		String[] exists = binaryCode.split(",");
		String axis = "T";
		String codeIrma = "";
		
		for(int i = 0; i <  this.structureIrmaCode.size(); i++){
			if(exists[i].equals("1")) {
				String aux = this.structureIrmaCode.get(i);
				if(!aux.substring(0, 1).equals(axis)) {
					codeIrma +="-";
					axis = aux.substring(0, 1);
				}
				aux = aux.substring(aux.length() - 1);
				codeIrma += aux;
			}
		}

		return codeIrma;
	}

}
