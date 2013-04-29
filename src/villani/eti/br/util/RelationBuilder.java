package villani.eti.br.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

import mulan.data.InvalidDataFormatException;
import mulan.data.MultiLabelInstances;

public class RelationBuilder {
	
	private String nameRelation;
	private FileWriter arff;
	private File xml;
	private Scanner reader;
	private int numAttributes;
	private boolean attributesSaved;
	private MultiLabelInstances dataset;
	
	public RelationBuilder(String nameRelation) throws FileNotFoundException, IOException{
		
		this.nameRelation = nameRelation;
		this.xml = new File(this.nameRelation + ".xml");
		this.reader = new Scanner(this.xml);
		this.arff = new FileWriter(this.nameRelation + ".arff");
		this.numAttributes = 0;
		this.attributesSaved = false;
		
		this.arff.write("@relation " + this.nameRelation + "\n\n");
	}
	
	public RelationBuilder(String nameRelation, String nameXml) throws FileNotFoundException, IOException{
		
		this.nameRelation = nameRelation;
		this.xml = new File(nameXml + ".xml");
		this.reader = new Scanner(this.xml);
		this.arff = new FileWriter(this.nameRelation + ".arff");
		this.numAttributes = 0;
		this.attributesSaved = false;
		
		this.arff.write("@relation " + this.nameRelation + "\n\n");
	}
	
	public void defineAttribute(String attributeName, String type) throws Exception{
		if(!this.attributesSaved){
			this.arff.write("@attribute " + attributeName + " " + type + "\n");
			this.numAttributes++;
		} else throw new Exception("The attributes list already was saved");
	}
	
	public int numAttributes(){
		return this.numAttributes;
	}
	
	public void saveAttributes() throws IOException{
		while(this.reader.hasNextLine()){
			String line = this.reader.nextLine();
			if(line.contains("label name")){
				String attribute = line.substring(line.indexOf("=\"") + 2, line.indexOf("\">"));
				this.arff.write("@attribute " + attribute + " {0,1}\n");
				this.numAttributes++;
			}
		}
		this.arff.write("\n@data\n");
		this.attributesSaved = true;
	}
	
	public void insertData(String instance) throws Exception{
		if(this.attributesSaved){
			this.arff.write(instance + "\n");
		} else throw new Exception("The attributes list still was not saved");
	}
	
	public MultiLabelInstances saveRelation() throws IOException, InvalidDataFormatException{
		this.arff.close();
		this.dataset = new MultiLabelInstances(this.nameRelation + ".arff", this.xml.getAbsolutePath());
		return this.dataset;
	}

}
