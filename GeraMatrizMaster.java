package Processamento;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Scanner;


// Instanciar no in�cio do c�digo do mestre apenas, nenhuma outra vez, pois o mestre possui um �nico hashmap.
public class GeraMatrizMaster {
	private static final GeraMatrizMaster INSTANCE = new GeraMatrizMaster();

	  private GeraMatrizMaster() {
	  }

	  public static GeraMatrizMaster getInstance() {
	    return INSTANCE;
	  }
	
	private final String nomeMatrizPrincipal = "geradoMatriz.txt";
	
	private HashMap<String, ArrayList<Gram>> hashGrams = new HashMap<String, ArrayList<Gram>>();
	
	// localEntrada    - Arquivo a ser acrescentado na matriz principal do mestre
	//					 Obs.: � o arquivo que o escravo devolveu depois de realizar processamento

	public synchronized void atualizaMestre(File entrada) throws IOException{
		
		try {
			ArrayList<Gram> listaGrams;
			Gram elementoGram;
			Scanner sc = new Scanner(entrada);
			sc.useDelimiter("[\\s,\r\n]+");
			String[] linha;
			String linhaAtual;
			
			if (sc.hasNextLine()){
				while (sc.hasNextLine()){
					linhaAtual = sc.nextLine();
					linha = new String[3];
					linha =  linhaAtual.split(",");
					
					String origem = linha[0];
					String destino = linha[1];
					String frequencia = linha[2];
					
					listaGrams = new ArrayList<Gram>();
					
					if (!hashGrams.containsKey(origem)){

						elementoGram = new Gram();
						elementoGram.setDestino(destino);
						elementoGram.somaFrequencia(Long.parseLong(frequencia));
						
						listaGrams.add(elementoGram);
						 
						hashGrams.put(origem, listaGrams);
						 
					 } else {
						 //estou sonouserr
						 ArrayList<Gram> array = hashGrams.get(origem);
						 boolean acrescentaGram = false;
						// Inicio o iterator para percorrer o array atual da chave do hashmap
						 ListIterator<Gram> iterator = array.listIterator(); 
						 while (iterator.hasNext()) {
							 Gram elementoGramAux = (Gram) iterator.next();
								 if (elementoGramAux.getDestino().equals(destino)){
									 elementoGramAux.somaFrequencia(Long.parseLong(frequencia));
									 acrescentaGram = false;
									 break;
								 } else {
									 acrescentaGram = true;
								 }	 
						 }
						 if (acrescentaGram){
							 elementoGram = new Gram();
							 elementoGram.setDestino(destino);
							 elementoGram.somaFrequencia(Long.parseLong(frequencia));
							 iterator.add(elementoGram);
						 }
					 }

				}
				sc.close();
			}
		} catch (Exception e){
			e.printStackTrace();
		}
		
	}
	
	// localMatriz   - Local onde voc� quer que a matriz principal fique, se n�o existir ele cria
	//				   Exemplo: "C:\\Users\\Luana"
	//				   Criar� : "C:\\Users\\Luana\\geradoMatriz.txt"
	public void geraMatrizPrincipal(String localMatriz) throws IOException{
		
		try {
			// Cria se n�o existe o local
			new File(localMatriz).mkdirs();
			
			File file = new File(localMatriz + "\\" + nomeMatrizPrincipal);
			FileWriter raf = new FileWriter(file);
			
			//codigo teste s� para saber o resultado
			for (String key : this.hashGrams.keySet()) {
				ArrayList<Gram> array = this.hashGrams.get(key);
				String chave = key;
				Iterator<Gram> it = array.iterator();
				 
				while (it.hasNext()){
					Gram elementoGramAux = (Gram) it.next();
					String texto =  chave + "," + elementoGramAux.getDestino() + "," + elementoGramAux.getFrequencia();
					raf.append(texto + "\r\n");
				}
			}
			raf.close();
		} catch (Exception e){
			e.printStackTrace();
		}
	}
}
	
