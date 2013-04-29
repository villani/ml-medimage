package br.eti.villani.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

import mulan.data.InvalidDataFormatException;
import mulan.data.MultiLabelInstances;

/**
 * The mulan learners needs a XML file beyond ARFF file to instantiante a multi-label dataset.
 * This class creates these two files in one step.
 * @author VILLANI, L. and PRATI, R. C.
 *
 */
public class RelationBuilder {
	
	private String nameRelation;
	private FileWriter arff;
	private File xml;
	private Scanner reader;
	private int numAttributes;
	private boolean attributesSaved;
	private MultiLabelInstances dataset;
	
	/**
	 * Creates the ARFF file and XML file with the same name.
	 * @param nameRelation The relation name that used to ARFF file and XML file.
	 * @throws IOException If one from files can not be manipulated.
	 */
	public RelationBuilder(String nameRelation) throws IOException{
		
		this.nameRelation = nameRelation;
		this.xml = new File(this.nameRelation + ".xml");
		this.reader = new Scanner(this.xml);
		this.arff = new FileWriter(this.nameRelation + ".arff");
		this.numAttributes = 0;
		this.attributesSaved = false;
		
		this.arff.write("@relation " + this.nameRelation + "\n\n");
	}
	
	/**
	 * Create the ARFF file and XML file with different names. 
	 * @param nameRelation The ARFF file name
	 * @param nameXml The XML file name
	 * @throws IOException If the files can not be manipulated.
	 */
	public RelationBuilder(String nameRelation, String nameXml) throws IOException{
		
		this.nameRelation = nameRelation;
		this.xml = new File(nameXml + ".xml");
		this.reader = new Scanner(this.xml);
		this.arff = new FileWriter(this.nameRelation + ".arff");
		this.numAttributes = 0;
		this.attributesSaved = false;
		
		this.arff.write("@relation " + this.nameRelation + "\n\n");
	}
	
	/**
	 * Defines a attribute to relation.
	 * @param attributeName Attribute name of the instance.
	 * @param type Attribute type.
	 * @throws Exception If occur a problem when writing the attribute.
	 */
	public void defineAttribute(String attributeName, String type) throws Exception{
		if(!this.attributesSaved){
			this.arff.write("@attribute " + attributeName + " " + type + "\n");
			this.numAttributes++;
		} else throw new Exception("The attributes list already was saved");
	}
	
	/**
	 * Gets the quantity of attributes in the relation.
	 * @return Quantity of attributes.
	 */
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
	
	/**
	 * Inserts a instance in relation.
	 * @param instance The instance values.
	 * @throws Exception If occur a problem when writing the instance.
	 */
	public void insertData(String instance) throws Exception{
		if(this.attributesSaved){
			this.arff.write(instance + "\n");
		} else throw new Exception("The attributes list still was not saved");
	}
	
	/**
	 * Saves the relation created and gets a multi-label set from this data.
	 * @return The multi-label set created.
	 * @throws IOException If occur a problem when saving the relation.
	 * @throws InvalidDataFormatException If the data were saved in wrong format.
	 */
	public MultiLabelInstances saveRelation() throws IOException, InvalidDataFormatException{
		this.arff.close();
		this.dataset = new MultiLabelInstances(this.nameRelation + ".arff", this.xml.getAbsolutePath());
		return this.dataset;
	}

}
