package villani.eti.br;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.TreeMap;

import mulan.classifier.MultiLabelLearnerBase;
import mulan.classifier.lazy.BRkNN;
import mulan.classifier.lazy.MLkNN;
import mulan.classifier.transformation.ClassifierChain;
import mulan.classifier.transformation.LabelPowerset;
import mulan.data.InvalidDataFormatException;
import mulan.data.LabelsMetaDataImpl;
import mulan.data.MultiLabelInstances;
import mulan.evaluation.Evaluation;
import mulan.evaluation.Evaluator;
import mulan.evaluation.measure.AveragePrecision;
import mulan.evaluation.measure.Coverage;
import mulan.evaluation.measure.ErrorSetSize;
import mulan.evaluation.measure.ExampleBasedAccuracy;
import mulan.evaluation.measure.ExampleBasedFMeasure;
import mulan.evaluation.measure.ExampleBasedPrecision;
import mulan.evaluation.measure.ExampleBasedRecall;
import mulan.evaluation.measure.ExampleBasedSpecificity;
import mulan.evaluation.measure.HammingLoss;
import mulan.evaluation.measure.IsError;
import mulan.evaluation.measure.Measure;
import mulan.evaluation.measure.MicroFMeasure;
import mulan.evaluation.measure.MicroPrecision;
import mulan.evaluation.measure.MicroRecall;
import mulan.evaluation.measure.OneError;
import mulan.evaluation.measure.RankingLoss;
import mulan.evaluation.measure.SubsetAccuracy;
import weka.classifiers.lazy.IBk;
import weka.classifiers.trees.RandomForest;
import weka.core.Instances;

import villani.eti.br.util.*;

public class EvaluatingAxes {

	/**
	 * @param id
	 * @param log
	 * @param entradas
	 */
	public static void run(String id, LogBuilder log,
			TreeMap<String, String> entradas) {

		log.write(" - Receiving the input parameters.");
		boolean ehd = Boolean.parseBoolean(entradas.get("ehd"));
		boolean gabor = Boolean.parseBoolean(entradas.get("gabor"));
		boolean lbp = Boolean.parseBoolean(entradas.get("lbp"));
		boolean sift = Boolean.parseBoolean(entradas.get("sift"));		
		boolean brknn = Boolean.parseBoolean(entradas.get("brknn"));
		boolean chain = Boolean.parseBoolean(entradas.get("chain"));
		boolean lp = Boolean.parseBoolean(entradas.get("lp"));
		boolean mlknn = Boolean.parseBoolean(entradas.get("mlknn"));
		String[] tecnicas = { "Ehd", "Gabor", "Lbp", "Sift"};
		int ns = Integer.parseInt(entradas.get("ns"));
		String[] eixos = { "T", "D", "A", "B" };
		String[] classificadores = {"BRkNN", "Chain", "LP", "MLkNN"};

		for (String tecnica : tecnicas) {

			if (tecnica.equals("Ehd") && !ehd) continue;
			if (tecnica.equals("Gabor") && !gabor) continue;
			if (tecnica.equals("Lbp") && !lbp) continue;
			if (tecnica.equals("Sift") && !sift) continue;

			for (int i = 0; i < ns; i++) {

				for (String eixo : eixos) {

					for (String classificador : classificadores) {

						if (classificador.equals("BRkNN") && !brknn) continue;
						if (classificador.equals("Chain") && !chain) continue;
						if (classificador.equals("LP") && !lp) continue;
						if (classificador.equals("MLkNN") && !mlknn) continue;

						String nomeTreino = id + tecnica + "-Sub" + i + "-" + eixo;

						Instances instanciasTreino = null;
						try {
							log.write(" - Unserializing the train instances from " + nomeTreino);
							FileInputStream instanciasFIS = new FileInputStream(nomeTreino + ".bsi");
							ObjectInputStream instanciasOIS = new ObjectInputStream(instanciasFIS);
							instanciasTreino = (Instances) instanciasOIS.readObject();
							instanciasOIS.close();
							instanciasFIS.close();
						} catch (Exception e) {
							log.write(" - Failure to unserialize instances: " + e.getMessage());
							System.exit(0);
						}

						LabelsMetaDataImpl rotulosTreino = null;
						try {
							log.write(" - Unserialize respective label structure.");
							FileInputStream rotulosFIS = new FileInputStream(nomeTreino + ".labels");
							ObjectInputStream rotulosOIS = new ObjectInputStream(rotulosFIS);
							rotulosTreino = (LabelsMetaDataImpl) rotulosOIS.readObject();
							rotulosOIS.close();
							rotulosFIS.close();
						} catch (Exception e) {
							log.write(" - Failure to unserialize labels: " + e.getMessage());
							System.exit(0);
						}

						MultiLabelInstances baseTreino = null;
						try {
							log.write(" - Instantiating multi-label train set");
							baseTreino = new MultiLabelInstances(instanciasTreino, rotulosTreino);
						} catch (InvalidDataFormatException idfe) {
							log.write(" - Failure in the data format to instantiate multi-label set: " + idfe.getMessage());
							System.exit(0);
						}

						MultiLabelLearnerBase mlLearner = null;
						log.write(" - Instantiating classifier " + classificador);
						if (classificador.equals("BRkNN")) mlLearner = new BRkNN(); // default k=10
						if (classificador.equals("Chain")) {
							RandomForest classifier = new RandomForest();
							mlLearner = new ClassifierChain(classifier);
						}
						if (classificador.equals("LP")) {
							IBk kNN = new IBk(10);
							mlLearner = new LabelPowerset(kNN);
						}
						if (classificador.equals("MLkNN")) mlLearner = new MLkNN(); // default k=10

						try {
							log.write(" - Creating " + classificador + " model from train set " + nomeTreino);
							mlLearner.build(baseTreino);
						} catch (Exception e) {
							log.write(" - Failure to create the classifier model: " + e.getMessage());
							System.exit(0);
						}

						log.write(" - Instantiating evaluator.");
						Evaluator avaliador = new Evaluator();

						log.write(" - Instantiating measure list.");
						ArrayList<Measure> medidas = new ArrayList<Measure>();
						medidas.add(new HammingLoss());
						medidas.add(new SubsetAccuracy());
						medidas.add(new ExampleBasedPrecision());
						medidas.add(new ExampleBasedRecall());
						medidas.add(new ExampleBasedFMeasure());
						medidas.add(new ExampleBasedAccuracy());
						medidas.add(new ExampleBasedSpecificity());
						int numOfLabels = baseTreino.getNumLabels();
						medidas.add(new MicroPrecision(numOfLabels));
						medidas.add(new MicroRecall(numOfLabels));
						medidas.add(new MicroFMeasure(numOfLabels));
						medidas.add(new AveragePrecision());
						medidas.add(new Coverage());
						medidas.add(new OneError());
						medidas.add(new IsError());
						medidas.add(new ErrorSetSize());
						medidas.add(new RankingLoss());

						for (int j = 0; j < ns; j++) {
							
							if( i == j ) continue;

							String nomeTeste = id + tecnica + "-Sub" + j + "-" + eixo;

							Instances instanciasTeste = null;
							try {
								log.write(" - Unserealizing test instances from " + nomeTeste);
								FileInputStream instanciasFIS = new FileInputStream(nomeTeste + ".bsi");
								ObjectInputStream instanciasOIS = new ObjectInputStream(instanciasFIS);
								instanciasTeste = (Instances) instanciasOIS.readObject();
								instanciasOIS.close();
								instanciasFIS.close();
							} catch (Exception e) {
								log.write(" - Failure to unserealize instances: " + e.getMessage());
								System.exit(0);
							}

							LabelsMetaDataImpl rotulosTeste = null;
							try {
								log.write(" - Unserealize respective label structure.");
								FileInputStream rotulosFIS = new FileInputStream(nomeTeste + ".labels");
								ObjectInputStream rotulosOIS = new ObjectInputStream(rotulosFIS);
								rotulosTeste = (LabelsMetaDataImpl) rotulosOIS.readObject();
								rotulosOIS.close();
								rotulosFIS.close();
							} catch (Exception e) {
								log.write(" - Failure to unserealize labels: " + e.getMessage());
								System.exit(0);
							}

							MultiLabelInstances baseTeste = null;
							try {
								log.write(" - Instantiating multi-label test set.");
								baseTeste = new MultiLabelInstances(instanciasTeste, rotulosTeste);
							} catch (InvalidDataFormatException idfe) {
								log.write(" - Failure in the data format to instantiate multi-label set: " + idfe.getMessage());
								System.exit(0);
							}

							log.write(" - Evaluating the model created by classifier " + classificador);
							Evaluation avaliacao = null;
							try {
								avaliacao = avaliador.evaluate(mlLearner, baseTeste, medidas);
							} catch (IllegalArgumentException iae) {
								log.write(" - Invalid parameters used: " + iae.getMessage());
								System.exit(0);
							} catch (Exception e) {
								log.write(" - Failure to evaluate the model: " + e.getMessage());
								System.exit(0);
							}

							log.write(" - Saving evaluation result.");
							File resultado = new File(id + classificador + "-" + tecnica + "-" + eixo + "-Treino" + i + "-Teste" + j + ".csv");
							try {
								FileWriter escritor = new FileWriter(resultado);
								escritor.write(avaliacao.toString());
								escritor.close();
							} catch (IOException ioe) {
								log.write(" - Failure to save evaluation result: " + ioe.getMessage());
								System.exit(0);
							}

						}

					}

				}

			}

		}

	}
}