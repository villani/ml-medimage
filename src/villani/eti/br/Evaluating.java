package villani.eti.br;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.TreeMap;

import mulan.classifier.MultiLabelLearnerBase;
import mulan.classifier.lazy.BRkNN;
import mulan.classifier.lazy.MLkNN;
import mulan.classifier.transformation.ClassifierChain;
import mulan.classifier.transformation.LabelPowerset;
import mulan.data.InvalidDataFormatException;
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

import villani.eti.br.util.*;

public class Evaluating {

	public static void run(String id, LogBuilder log,
			TreeMap<String, String> entradas) {

		log.write(" - Receiving the input parameters.");
		boolean ehd = Boolean.parseBoolean(entradas.get("ehd"));
		boolean lbp = Boolean.parseBoolean(entradas.get("lbp"));
		boolean sift = Boolean.parseBoolean(entradas.get("sift"));
		boolean gabor = Boolean.parseBoolean(entradas.get("gabor"));
		boolean mlknn = Boolean.parseBoolean(entradas.get("mlknn"));
		boolean brknn = Boolean.parseBoolean(entradas.get("brknn"));
		boolean chain = Boolean.parseBoolean(entradas.get("chain"));
		boolean lp = Boolean.parseBoolean(entradas.get("lp"));
		int ns = Integer.parseInt(entradas.get("ns"));
		String rotulos = id + "irma-structure.xml"; 
		String[] tecnicas = {"Ehd", "Gabor", "Lbp", "Sift"};
		String[] classificadores = {"BRkNN", "Chain", "LP", "MLkNN"};

		for (String tecnica : tecnicas) {

			if (tecnica.equals("Ehd") && !ehd) continue;
			if (tecnica.equals("Gabor") && !gabor) continue;
			if (tecnica.equals("Lbp") && !lbp) continue;
			if (tecnica.equals("Sift") && !sift) continue;

			for (String classificador : classificadores) {

				if (classificador.equals("BRkNN") && !brknn) continue;
				if (classificador.equals("Chain") && !chain) continue;
				if (classificador.equals("LP") && !lp) continue;
				if (classificador.equals("MLkNN") && !mlknn) continue;
				

				for (int i = 0; i < ns; i++) {

					String base = id + tecnica + "-Sub" + i + ".arff";
					
					
					MultiLabelInstances trainingSet = null;
					try {
						log.write(" - Instantiating multi-label set to train " + base);
						trainingSet = new MultiLabelInstances(base, rotulos);
					} catch (InvalidDataFormatException idfe) {
						log.write(" - Failure in the data format to instantiate multi-label set to train " + base + ": " + idfe.getMessage());
						System.exit(0);
					}

					log.write(" - Instantiating classifier " + classificador);
					MultiLabelLearnerBase mlLearner = null;
					if (classificador.equals("MLkNN")) mlLearner = new MLkNN(); // default k=10
					if (classificador.equals("BRkNN")) mlLearner = new BRkNN(); // default k=10
					if (classificador.equals("Chain")) {
						RandomForest classifier = new RandomForest();
						mlLearner = new ClassifierChain(classifier);
					}
					if (classificador.equals("LP")) {
						IBk classifier = new IBk(10);
						mlLearner = new LabelPowerset(classifier);
					}
					

					try {
						log.write(" - Creating model of the " + mlLearner.getClass() + " from train set " + base);
						mlLearner.build(trainingSet);
					} catch (Exception e) {
						log.write(" - Failure to create the classifier model: " + e.getMessage());
						System.exit(0);
					}

					log.write(" - Instantiating evaluator.");
					Evaluator avaliador = new Evaluator();

					log.write(" - Instantiating measures list.");
					ArrayList<Measure> medidas = new ArrayList<Measure>();
					medidas.add(new HammingLoss());
					medidas.add(new SubsetAccuracy());
					medidas.add(new ExampleBasedPrecision());
					medidas.add(new ExampleBasedRecall());
					medidas.add(new ExampleBasedFMeasure());
					medidas.add(new ExampleBasedAccuracy());
					medidas.add(new ExampleBasedSpecificity());
					int numOfLabels = trainingSet.getNumLabels();
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
						
						if(j == i) continue;

						String baseTeste = tecnica + "-Sub" + j + ".arff";

						MultiLabelInstances testSet = null;
						
						try {
							log.write(" - Instantiating multi-label set to test " + baseTeste);
							testSet = new MultiLabelInstances(id + baseTeste, rotulos);
						} catch (InvalidDataFormatException idfe) {
							log.write(" - Failure in the data format to instantiate multi-label set to test " + baseTeste + ": " + idfe.getMessage());
							System.exit(0);
						}

						log.write(" - Evaluating model created by classifier " + mlLearner.getClass());
						Evaluation avaliacao = null;
						try {
							avaliacao = avaliador.evaluate(mlLearner, testSet, medidas);
						} catch (IllegalArgumentException iae) {
							log.write(" - Invalid parameters: " + iae.getMessage());
							System.exit(0);
						} catch (Exception e) {
							log.write(" - Failure to evaluate the model: " + e.getMessage());
							System.exit(0);
						}

						log.write(" - Saving evaluation results...");
						File resultado = new File(id + classificador + "-" + tecnica + "-Treino" + i + "-Teste" + j + ".csv");
						try {
							FileWriter escritor = new FileWriter(resultado);
							escritor.write(avaliacao.toString());
							escritor.close();
						} catch (IOException ioe) {
							log.write(" - Failure to save evaluation results: " + ioe.getMessage());
							System.exit(0);
						}

					}

				}

			}

		}

	}
}