package villani.eti.br;

import ij.ImagePlus;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.Vector;

import javax.imageio.ImageIO;

import mpi.cbg.fly.Feature;
import mpi.cbg.fly.Filter;
import mpi.cbg.fly.FloatArray2D;
import mpi.cbg.fly.FloatArray2DSIFT;
import mpi.cbg.fly.ImageArrayConverter;
import mulan.data.InvalidDataFormatException;
import net.semanticmetadata.lire.imageanalysis.Gabor;
import net.semanticmetadata.lire.imageanalysis.mpeg7.EdgeHistogramImplementation;
import weka.clusterers.SimpleKMeans;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import de.florianbrucker.ml.lbp.LBPModel;
import de.florianbrucker.ml.lbp.LBPParameters;

import villani.eti.br.util.*;

public class Building {

	public static void run(String id, LogBuilder log, TreeMap<String, String> entradas) {

		log.write(" - Receiving the input parameters.");
		boolean hasEHD = Boolean.parseBoolean(entradas.get("ehd"));
		boolean hasLBP = Boolean.parseBoolean(entradas.get("lbp"));
		int bins = Integer.parseInt(entradas.get("bins"));
		int vizinhos = Integer.parseInt(entradas.get("vizinhos"));
		int raio = Integer.parseInt(entradas.get("raio"));
		boolean hasSIFT = Boolean.parseBoolean(entradas.get("sift"));
		int histoSize = Integer.parseInt(entradas.get("histoSize"));
		boolean hasGabor = Boolean.parseBoolean(entradas.get("gabor"));
		String irma = entradas.get("irma");
		String rotulos = entradas.get("rotulos");
		int ns = Integer.parseInt(entradas.get("ns"));
		int ni = Integer.parseInt(entradas.get("ni"));

		log.write(" - Getting labels set IRMA.");
		XmlIrmaCodeBuilder xicb;
		try {
			log.write(" - Creating XML file with the IRMA code structure.");
			xicb = new XmlIrmaCodeBuilder(irma, id+"irma-structure");
			if (xicb.hasXml()) log.write(" - XML file with IRMA code structure created successfully.");
		} catch (IOException e) {
			log.write(" - Failure to obtain relation image name/ IRMA code: " + e.getMessage());
			System.exit(0);
		}
		
		log.write(" - Getting the relation image name/ IRMA code from file: " + rotulos);
		File relacaoImagemCodigo = new File(rotulos);
		TreeMap<String, String> relacao = new TreeMap<String, String>();
		Scanner leitor = null; 
		try {
			leitor = new Scanner(relacaoImagemCodigo);
		} catch (FileNotFoundException fnfe) {
			log.write(" - Failure to read the file to obtain relation " + rotulos + ": " + fnfe.getMessage());
			System.exit(0);
		}
		while (leitor.hasNextLine()) {
			String[] campos = leitor.nextLine().split(";");
			relacao.put(campos[0], campos[1]);
		}
		leitor.close();

		log.write(" - Creating object that converts IRMA code to binary and that needs XML created previously.");
		IrmaCode conversor = null;
		try {
			conversor = new IrmaCode(id+"irma-structure");
		} catch (FileNotFoundException fnfe) {
			log.write(" - Failure to read the XML file " + id + "irma-structure.xml: " + fnfe.getMessage());
			System.exit(0);
		}

		if (hasEHD) {
			for (int i = 0; i < ns; i++) {
				log.write(" - Creating the EHD ARFF base to the subset " + i);
				
				String dataset = id + "Ehd-Sub" + i;
				RelationBuilder instanciasEHD = null;
				try{
					instanciasEHD = new RelationBuilder(dataset, id + "irma-structure");
					for (int j = 0; j < 80; j++) instanciasEHD.defineAttribute("ehd" + j, "numeric");
					log.write(" - Saving the attributes list and including the labels list from XML.");
					instanciasEHD.saveAttributes();
				} catch(IOException ioe){
					log.write(" - Failure to create or saving the attribute list and labels list: " + ioe.getMessage());
					System.exit(0);
				} catch(Exception e){
					log.write(" - Failure to define a attribute: " + e.getMessage());
					System.exit(0);
				}

				log.write(" - Getting EHD features for each image.");
				ArrayList<File> imagens = new ArrayList<File>(ni);
				File subconjunto = new File(id + "Sub" + i + ".lst");
				leitor = null;
				try {
					leitor = new Scanner(subconjunto);
				} catch (FileNotFoundException fnfe) {
					log.write(" - Failure to read the file " + subconjunto.getName() + " to obtain EHD: " + fnfe.getMessage());
					System.exit(0);
				}
				
				while (leitor.hasNextLine()) imagens.add(new File(leitor.nextLine()));
				for (File imagem : imagens) {
					EdgeHistogramImplementation extrator = null;
					try {
						extrator = new EdgeHistogramImplementation(ImageIO.read(imagem));
					} catch (IOException ioe) {
						log.write(" - Failure to read image " + imagem.getName() + " to extract features: " + ioe.getMessage());
						System.exit(0);
					}
					int[] ehd = extrator.setEdgeHistogram();
					String amostra = "";
					for (int e : ehd) amostra += e + ",";
					String nomeImg = imagem.getName().split("\\.")[0];
					amostra += conversor.toBinary(relacao.get(nomeImg));
					try {
						instanciasEHD.insertData(amostra);
					} catch (Exception ex) {
						log.write(" - Failure to insert sample " + amostra + ": " + ex.getMessage());
						System.exit(0);
					}
				}

				try {
					instanciasEHD.saveRelation();
				} catch (InvalidDataFormatException idfe){
					log.write(" - Failure in the format to save relation: " + idfe.getMessage());
				} catch (IOException ioe) {
					log.write(" - Failure to save relation: " + ioe.getMessage());
				}
				log.write(" - New samples set saved in: " + dataset + ".arff");
			}
		}

		if (hasLBP){
			for (int i = 0; i < ns; i++) {
				log.write(" - Creating the LBP ARFF base to the subset " + i);
				
				String dataset = id + "Lbp-Sub" + i;
				RelationBuilder instanciasLBP = null;
				try{
					instanciasLBP = new RelationBuilder(dataset, id);
					for (int j = 0; j < bins; j++) instanciasLBP.defineAttribute("lbp" + j, "numeric");
					log.write("- Saving the attributes list and including labels list from XML.");
					instanciasLBP.saveAttributes();
				} catch(IOException ioe){
					log.write(" - Failure to create or save the attributes list and labels list: " + ioe.getMessage());
					System.exit(0);
				} catch(Exception e){
					log.write(" - Failure to define a attribute: " + e.getMessage());
					System.exit(0);
				}
				
				log.write(" - Getting LBP features for each image.");
				ArrayList<File> imagens = new ArrayList<File>(ni);
				File subconjunto = new File(id + "Sub" + i + ".lst");
				leitor = null;
				try {
					leitor = new Scanner(subconjunto);
				} catch (FileNotFoundException fnfe) {
					log.write(" - Failure to read the file " + subconjunto.getName() + " to obtain LBP: " + fnfe.getMessage());
					System.exit(0);
				}
				
				while (leitor.hasNextLine()) imagens.add(new File(leitor.nextLine()));
				for (File imagem : imagens) {
					LBPParameters p = new LBPParameters(vizinhos, raio, bins);
					LBPModel extrator = null;
					try {
						extrator = new LBPModel(p, imagem);
					} catch (IOException ioe) {
						log.write(" - Failure to read image " + imagem.getName() + " to extract features: " + ioe.getMessage());
						System.exit(0);
					}
					float[] histLBP = extrator.subModels[0].patternHist;
					String amostra = "";
					for (float lbp : histLBP) amostra += lbp + ",";
					String nomeImg = imagem.getName().split("\\.")[0];
					amostra += conversor.toBinary(relacao.get(nomeImg));
					try {
						instanciasLBP.insertData(amostra);
					} catch (Exception ex) {
						log.write(" - Failure to insert sample " + amostra + ": " + ex.getMessage());
						System.exit(0);
					}
				}

				try {
					instanciasLBP.saveRelation();
				} catch (InvalidDataFormatException idfe){
					log.write(" - Failure in the format to save relation: " + idfe.getMessage());
				} catch (IOException ioe) {
					log.write(" - Failure to save relation: " + ioe.getMessage());
				}
				log.write(" - New samples set saved in: " + dataset + ".arff");
			}
		}
		
		if (hasSIFT){
			for (int i = 0; i < ns; i++) {
				log.write(" - Creating SIFT ARFF base to subset " + i);
				
				String dataset = id + "Sift-Sub" + i;
				RelationBuilder instanciasSIFT = null;
				try{
					instanciasSIFT = new RelationBuilder(dataset, id);
					for(int j = 0; j < histoSize; j++) instanciasSIFT.defineAttribute("histSIFT" + j, "numeric");
					log.write(" - Saving the attributes list and including the labels list from XML.");
					instanciasSIFT.saveAttributes();
				} catch(IOException ioe){
					log.write(" - Failure to create or save attributes list and labels list: " + ioe.getMessage());
					System.exit(0);
				} catch(Exception e){
					log.write(" - Failure to define a attribute: " + e.getMessage());
					System.exit(0);
				}
				
				log.write(" - Getting SIFT features to the images.");
				ArrayList<File> imagens = new ArrayList<File>(ni);
				File subconjunto = new File(id + "Sub" + i + ".lst");
				leitor = null;
				try {
					leitor = new Scanner(subconjunto);
				} catch (FileNotFoundException fnfe) {
					log.write(" - Failure to read the file " + subconjunto.getName() + " to obtain SIFT: " + fnfe.getMessage());
					System.exit(0);
				}
				
				while (leitor.hasNextLine()) imagens.add(new File(leitor.nextLine()));
				
				// Início - Identificação dos pontos-chave para cálculo do histograma SIFT
				ArrayList<String> idPontos = new ArrayList<String>();
				ArrayList<Attribute> listaDeAtributos = new ArrayList<Attribute>();
				for(int j = 0; j < 128; j++) listaDeAtributos.add(new Attribute("feat" + j));
				Instances instancias = new Instances("sift",listaDeAtributos,10);
				for(File imagem : imagens){
					ImagePlus ip = new ImagePlus(imagem.getAbsolutePath());
					FloatArray2DSIFT sift = new FloatArray2DSIFT(4,8);
					FloatArray2D fa = ImageArrayConverter.ImageToFloatArray2D(ip.getProcessor().convertToFloat());
					Filter.enhance(fa, 1.0f);
					float initial_sigma = 1.6f;
					fa = Filter.computeGaussianFastMirror(fa, (float)Math.sqrt(initial_sigma * initial_sigma - 0.25));
					sift.init(fa, 3, initial_sigma, 64, 1024);
					Vector<Feature> pontosChave = sift.run(1024);
					for(Feature ponto : pontosChave){
						idPontos.add(imagem.getName());
						Instance instancia = new DenseInstance(128);
//						instancia.setDataset(instancias);
						for(int j = 0; j < ponto.descriptor.length; j++) instancia.setValue(j, ponto.descriptor[j]);
						instancias.add(instancia);
					}
				}
				SimpleKMeans km = new SimpleKMeans();
				int[] atribuicoes = null;
				try{
					km.setNumClusters(histoSize);
					km.setOptions(new String[]{"-O","-fast"});
					km.buildClusterer(instancias);
					atribuicoes = km.getAssignments();
				} catch(Exception e){
					log.write(" - Failure in key points clustering to SIFT histogram: " + e.getMessage());
					System.exit(0);
				}
				TreeMap<String,int[]> histoSIFT = new TreeMap<String, int[]>();
				for(int j = 0; j < atribuicoes.length; j++){
					String img = idPontos.get(j); 
					if(! histoSIFT.containsKey(img)) histoSIFT.put(img, new int[histoSize]);
					histoSIFT.get(img)[atribuicoes[i]]++;
				}
				// Fim - Identificação dos pontos-chave para cálculo do histograma SIFT
				
				for (File imagem : imagens) {					
	
					String amostra = "";
					int[] histograma = histoSIFT.get(imagem.getName());
					for(int h : histograma) amostra += h + ",";					
					String nomeImg = imagem.getName().split("\\.")[0];					
					amostra += conversor.toBinary(relacao.get(nomeImg));
					try {
						instanciasSIFT.insertData(amostra);
					} catch (Exception ex) {
						log.write(" - Failure to insert sample " + amostra + ": " + ex.getMessage());
						System.exit(0);
					}
					
				}

				try {
					instanciasSIFT.saveRelation();
				} catch (InvalidDataFormatException idfe){
					log.write(" - Failure in the format to save relation: " + idfe.getMessage());
				} catch (IOException ioe) {
					log.write(" - Failure to save relation: " + ioe.getMessage());
				}
				log.write(" - New samples set saved in: " + dataset + ".arff");
			}
		}
		
		if (hasGabor){
			for (int i = 0; i < ns; i++) {
				log.write(" - Creating Gabor ARFF base to subset " + i);
				
				String dataset = id + "Gabor-Sub" + i;
				RelationBuilder instanciasGabor = null;
				try{
					instanciasGabor = new RelationBuilder(dataset, id);
					for (int j = 0; j < 60; j++) instanciasGabor.defineAttribute("gabor" + j, "numeric");
					log.write(" - Saving the attributes list and including the labels list from XML.");
					instanciasGabor.saveAttributes();
				} catch(IOException ioe){
					log.write(" - Failure to create or save the attributes list and labels list: " + ioe.getMessage());
					System.exit(0);
				} catch(Exception e){
					log.write(" - Failure to define a attribute: " + e.getMessage());
					System.exit(0);
				}
				
				log.write(" - Getting Gabor features for each image.");
				ArrayList<File> imagens = new ArrayList<File>(ni);
				File subconjunto = new File(id + "Sub" + i + ".lst");
				leitor = null;
				try {
					leitor = new Scanner(subconjunto);
				} catch (FileNotFoundException fnfe) {
					log.write(" - Failure to read the file " + subconjunto.getName() + " to obtain Gabor: " + fnfe.getMessage());
					System.exit(0);
				}
				
				while (leitor.hasNextLine()) imagens.add(new File(leitor.nextLine()));
				for (File imagem : imagens) {
					
					Gabor extrator = new Gabor();
					double[] histGabor = null;
					try {
						histGabor = extrator.getFeature(ImageIO.read(imagem));
					} catch (IOException ioe) {
						log.write(" - Failure to obtain Gabor to the image " + imagem.getName() + ": " + ioe.getMessage());
					}
					String amostra = "";
					for (double g : histGabor) amostra += g + ",";
					
					String nomeImg = imagem.getName().split("\\.")[0];
					amostra += conversor.toBinary(relacao.get(nomeImg));
					
					try {
						instanciasGabor.insertData(amostra);
					} catch (Exception ex) {
						log.write(" - Failure to insert sample " + amostra + ": " + ex.getMessage());
						System.exit(0);
					}
				}

				try {
					instanciasGabor.saveRelation();
				} catch (InvalidDataFormatException idfe){
					log.write(" - Failure in the format to save relation: " + idfe.getMessage());
				} catch (IOException ioe) {
					log.write(" - Failure to save relation: " + ioe.getMessage());
				}
				log.write(" - New samples set saved in: " + dataset + ".arff");
			}
		}

	}

}
