package villani.eti.br.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class IrmaCode {
	
	private String nameXmlIrmaCode;
	private File xml;
	private Scanner reader;
	private ArrayList<String> structureIrmaCode;
	
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
	 * @param code
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
	 * @param binaryCode
	 * @return
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

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		try{
			IrmaCode ic = new IrmaCode("example");
			String binaryCode = ic.toBinary("1121-117-720-452");
			String irmaCodeFormat = ic.toIrmaProjectFormat(binaryCode);
			System.out.println(binaryCode);
			System.out.println(irmaCodeFormat);
		} catch(Exception e){
			System.out.println("Fail: [" + e.getMessage() + "]");
		}

	}

}
