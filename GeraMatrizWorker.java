package Processamento;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ListIterator;

public class GeraMatrizWorker {
	
	private final String nomeArquivoSaida = "gerado.txt";
	
	// localEntrada - Arquivo a ser processado
	
	// localSaida   - Local do arquivo resultante do processamento (matriz).
	//				  Formato exemplo: "C:\\Users\\Luana"
	//				  Ir� criar      : "C:\\Users\\Luana\\nomeArquivoSaida.txt"
	public File ProcessarArquivo(File localEntrada, String localSaida) throws IOException{
		
		try{
			FileInputStream entrada = new FileInputStream(localEntrada);
			InputStreamReader entradaFormatada = new InputStreamReader(entrada);
			
			String caractereAtual;
			char caractereA;
			String caractereProximo;
			char caractereP;
			
			Gram elementoGram;
			ArrayList<Gram> listaGrams;
			HashMap<String, ArrayList<Gram>> hashGrams = new HashMap<String, ArrayList<Gram>>();
			
			int bytes = entradaFormatada.read();
			caractereA = (char) bytes;
			caractereAtual = Character.toString(caractereA);
			
			if (entradaFormatada.ready()){
				caractereP = (char) entradaFormatada.read();
				caractereProximo = Character.toString(caractereP);
			} else {
				caractereProximo = " ";
			}
		
			
			do {
					if (caractereAtual.equals("\n")){
						caractereAtual = " ";
					}
					
					if (caractereProximo.equals("\n")){
						caractereProximo = " ";
					}
				
					listaGrams = new ArrayList<Gram>();
					String chave = caractereAtual;
					if (!hashGrams.containsKey(chave)){
		
						// Acrescenta elementos n�o existentes no hashmap
						elementoGram = new Gram();
						elementoGram.setDestino(caractereProximo);
						elementoGram.incrementaFrequencia();
						
						listaGrams.add(elementoGram);
						 
						hashGrams.put(chave, listaGrams);
						 
					 } else {
						 
						 ArrayList<Gram> array = hashGrams.get(caractereAtual);
						 boolean acrescentaGram = false;
						 
						// Inicio o iterator para percorrer o array atual da chave (caracter origem) do hashmap
						 ListIterator<Gram> iterator = array.listIterator(); 
						 while (iterator.hasNext()) {
							 Gram elementoGramAux = (Gram) iterator.next();
							 
							 	// Se o destino j� existir, apenas incremento a frequencia do Gram existente
								 if (elementoGramAux.getDestino().equals(caractereProximo)){
									 elementoGramAux.incrementaFrequencia();
									 acrescentaGram = false;
									 break;
								 } else {
								// Se o destino ainda n�o existir, preciso acrescentar um novo Gram
									 acrescentaGram = true;
								 }	 
						 }
						 // Acrescentando um novo Gram
						 if (acrescentaGram){
							 elementoGram = new Gram();
							 elementoGram.setDestino(caractereProximo);
							 elementoGram.incrementaFrequencia();
							 iterator.add(elementoGram);
						 }
					 }
					caractereAtual = caractereProximo;
					bytes = entradaFormatada.read();
					if (bytes != -1){
						caractereP = (char) bytes;
						caractereProximo = Character.toString(caractereP);
					} else {
						caractereProximo = " ";
					}
				} while (bytes != -1);
				
				new File(localSaida).mkdirs();
				
				File file = new File(localSaida + "\\" + nomeArquivoSaida);
				FileWriter raf = new FileWriter(file);
				
				//codigo teste s� para saber o resultado
				for (String key : hashGrams.keySet()) {
					ArrayList<Gram> array = hashGrams.get(key);
					String chave = key;
					Iterator<Gram> it = array.iterator();
					 
					while (it.hasNext()){
						Gram elementoGramAux = (Gram) it.next();
						String texto =  chave + "," + elementoGramAux.getDestino() + "," + elementoGramAux.getFrequencia();
						raf.append(texto + "\r\n");
					}
				}
				raf.close();
		
			entradaFormatada.close();
			return file;
			
		} catch (Exception e){
			e.printStackTrace();
		};
		
		return null;
	}
}

	
	
	
	

