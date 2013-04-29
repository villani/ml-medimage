package villani.eti.br;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.TreeMap;
import java.util.Vector;

import villani.eti.br.util.*;

public class Preparing {
	
	public static void run(String id, LogBuilder log, TreeMap<String, String> entradas){
		
		log.write(" - Receiving the input parameters.");
		String caminho = entradas.get("caminho");
		int ns = Integer.parseInt(entradas.get("ns"));
		int ni = Integer.parseInt(entradas.get("ni"));
		
		log.write(" - Getting the main image set.");
		File pasta = new File(caminho);
		File[] listaDeImagens = pasta.listFiles();
		Vector<File> cp = new Vector<File>(listaDeImagens.length);

		log.write(" - Removing from set images TIFF format.");
		for(File imagem: listaDeImagens){
			String ext = imagem.getName().split("\\.")[1];
			if(!ext.equals("tif")) cp.add(imagem);
		}
		
		log.write(" - Forming main set with " + cp.size() + " images.");
		
		log.write(" - Forming " + ns + " subsets with " + ni + " images.");
		for(int i = 0; i < ns; i++){
			
			File[] subconjunto = new File[ni];
			
			for(int j = 0; j < ni; j++){
				subconjunto[j] = cp.remove((int)(cp.size()*Math.random()));
				cp.trimToSize();
			}
			
			File subinfile = new File(id+"Sub" + i + ".lst");
			FileWriter escritor = null;
			try {
				escritor = new FileWriter(subinfile);
				for(File imagem: subconjunto) escritor.write(imagem.getAbsolutePath() + "\n");
				escritor.close();
			} catch (IOException e) {
				log.write("Failure to create file " + subinfile + ": " + e.getMessage());
				System.exit(0);
			}
			
			 
		}
		
		
	}

}
