package villani.eti.br;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.HashSet;
import java.util.TreeMap;

import mulan.data.InvalidDataFormatException;
import mulan.data.LabelsMetaDataImpl;
import mulan.data.MultiLabelInstances;
import weka.core.Instances;

import villani.eti.br.util.*;

public class BuildingAxes {

	public static void run(String id, LogBuilder log, TreeMap<String, String> entradas){

		log.write(" - Receiving the input parameters.");
		boolean ehd = Boolean.parseBoolean(entradas.get("ehd"));
		boolean lbp = Boolean.parseBoolean(entradas.get("lbp"));
		boolean sift = Boolean.parseBoolean(entradas.get("sift"));
		boolean gabor = Boolean.parseBoolean(entradas.get("gabor"));
		int ns = Integer.parseInt(entradas.get("ns"));
		String[] tecnicas = {"Ehd","Lbp", "Sift","Gabor"};
		String[] eixos = {"T","D","A","B"};
		String rotulos = id + "irma-structure.xml";

		for(String tecnica: tecnicas){
			if(tecnica.equals("Ehd") && !ehd) continue;
			if(tecnica.equals("Lbp") && !lbp) continue;
			if(tecnica.equals("Sift") && !sift) continue;
			if(tecnica.equals("Gabor") && !gabor) continue;
			for(String eixo: eixos){
				for(int i = 0; i < ns; i++){

					String base = id + tecnica + "-Sub" + i + ".arff";
					String subBase = id + tecnica + "-Sub" + i + "-" + eixo;
					MultiLabelInstances mlData = null;

					try{
						
						log.write(" - Instantiating multi-label set from base " + base);
						mlData = new MultiLabelInstances(base, rotulos);						
					} catch(InvalidDataFormatException idfe){
						
						log.write(" - Failure in the format to create multi-label set: " + idfe.getMessage());
						System.exit(0);
						
					}

					log.write(" - Creating labels filter.");
					LabelsMetaDataImpl estruturaRotulos = (LabelsMetaDataImpl)mlData.getLabelsMetaData();
					HashSet<String> filtros = new HashSet<String>();
					for(String rotulo: estruturaRotulos.getLabelNames()){
						if(!rotulo.startsWith(eixo)) filtros.add(rotulo);
					}
					
					log.write(" - Removing from instances the labels that no belong to axis " + eixo);
					Instances instancias = mlData.getDataSet();
					for(String filtro: filtros){
						instancias.deleteAttributeAt(instancias.attribute(filtro).index());
					}
					
					
					try{
						// Remove os atributos rótulos de outros eixos
						log.write(" - Readjusting the labels structure to new instances labels set.");
						mlData = mlData.reintegrateModifiedDataSet(instancias);
						
					} catch(InvalidDataFormatException idfe){
						
						log.write(" - Failure to readjust multi-label set: " + idfe.getMessage() + " : " + idfe.getCause());
						System.exit(0);
						
					}

					try{
						
						log.write(" - Serialing samples of the technique " + tecnica + " to the axis " + eixo);
						FileOutputStream amostrasFOS = new FileOutputStream(subBase + ".bsi");
						ObjectOutputStream amostrasOOS = new ObjectOutputStream(amostrasFOS);
						amostrasOOS.writeObject(mlData.getDataSet());
						amostrasOOS.flush();
						amostrasOOS.close();
						amostrasFOS.flush();
						amostrasFOS.close();
						
					} catch(Exception e){
						
						log.write(" - Failure to serialize to dataset: " + e.getMessage());
						System.exit(0);
						
					}

					try{
						
						log.write(" - Serializing labels structure to the respective set.");
						FileOutputStream rotulosFOS = new FileOutputStream(subBase + ".labels");
						ObjectOutputStream rotulosOOS = new ObjectOutputStream(rotulosFOS);
						rotulosOOS.writeObject(mlData.getLabelsMetaData());
						rotulosOOS.flush();
						rotulosOOS.close();
						rotulosFOS.flush();
						rotulosFOS.close();
						
					} catch(Exception e){
						
						log.write(" - Failure to serialize labels structure of the multi-label set: " + e.getMessage());
						System.exit(0);
						
					}

				}
			}
		}
	}
}
