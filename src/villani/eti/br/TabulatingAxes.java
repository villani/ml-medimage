package villani.eti.br;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;
import java.util.TreeMap;

import villani.eti.br.util.*;

public class TabulatingAxes {

	public static void run(String id, LogBuilder log,
			TreeMap<String, String> entradas) throws IOException {

		log.write(" - Receiving the input parameters.");
		boolean brknn = Boolean.parseBoolean(entradas.get("brknn"));
		boolean chain = Boolean.parseBoolean(entradas.get("chain"));
		boolean lp = Boolean.parseBoolean(entradas.get("lp"));
		boolean mlknn = Boolean.parseBoolean(entradas.get("mlknn"));
		boolean ehd = Boolean.parseBoolean(entradas.get("ehd"));
		boolean gabor = Boolean.parseBoolean(entradas.get("gabor"));
		boolean lbp = Boolean.parseBoolean(entradas.get("lbp"));
		boolean sift = Boolean.parseBoolean(entradas.get("sift"));
		int ns = Integer.parseInt(entradas.get("ns"));
		boolean hamming = Boolean.parseBoolean(entradas.get("hamming"));
		boolean microf = Boolean.parseBoolean(entradas.get("microf"));
		boolean average = Boolean.parseBoolean(entradas.get("average"));
		String[] classificadores = { "MLkNN", "BRkNN", "LP", "Chain" };
		String[] tecnicas = { "Ehd", "Gabor", "Lbp", "Sift" };
		String[] eixos = { "T", "D", "A", "B" };
		String[] medidas = { "Hamming Loss", "Micro-averaged F-Measure", "Average Precision" };

		for (String classificador : classificadores) {

			if (classificador.equals("BRkNN") && !brknn) continue;
			if (classificador.equals("Chain") && !chain) continue;
			if (classificador.equals("LP") && !lp) continue;
			if (classificador.equals("MLkNN") && !mlknn) continue;

			for (String tecnica : tecnicas) {

				if (tecnica.equals("Ehd") && !ehd) continue;
				if (tecnica.equals("Gabor") && !gabor) continue;
				if (tecnica.equals("Lbp") && !lbp) continue;
				if (tecnica.equals("Sift") && !sift) continue;

				for (String eixo : eixos) {

					for (int i = 0; i < ns; i++) {

						for (int j = 1; j < ns; j++) {

							if (j == i)
								continue;

							String nomeArquivo = id + classificador + "-" + tecnica + "-" + eixo + "-Treino" + i + "-Teste" + j + ".csv";
							log.write(" - Opening file " + nomeArquivo);
							File arquivo = new File(nomeArquivo);
							Scanner leitor = new Scanner(arquivo);
							TreeMap<String, String> resultado = new TreeMap<String, String>();

							log.write(" - Getting the measure values.");
							while (leitor.hasNextLine()) {

								String linha = leitor.nextLine();
								String[] result = linha.split(": ");
								resultado.put(result[0], result[1]);

							}

							leitor.close();

							for (String medida : medidas) {

								if (medida.equals("Hamming Loss") && !hamming) continue;
								if (medida.equals("Micro-averaged F-Measure") && !microf) continue;
								if (medida.equals("Average Precision") && !average) continue;

								String linha = classificador + "-" + tecnica + "-" + eixo + "-Treino" + i + "-Teste" + j + ";" + resultado.get(medida) + "\n";
								String nomeCsv = id + medida + "-" + classificador + "-" + tecnica + "-" + eixo + ".csv";
								log.write(" - Saving measure " + medida + " in the file " + nomeCsv);
								File csv = new File(nomeCsv);
								FileWriter escritor = new FileWriter(csv, true);
								escritor.write(linha);
								escritor.flush();
								escritor.close();

							}
						}
					}
				}
			}

		}

	}
}
