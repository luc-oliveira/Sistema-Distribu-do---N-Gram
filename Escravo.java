package lucas.luana;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Scanner;

import javax.swing.JOptionPane;

import Processamento.Gram;






public class Escravo {
	public static final int ESCRAVO_RECEIVE_PORT = 30100, ESCRAVO_SEND_PORT = 30110;
	private static InetAddress masterAdress;
	private final static String nomeArquivoSaida = "gerado.txt";
	
	public static void main(String[] args) {
		try {
			//ESPERO O MESTRE MANDA UM BROADCAST PRA PODER PENDURAR NELE.
			DatagramSocket dsocket = new DatagramSocket(ESCRAVO_RECEIVE_PORT);
			byte[] buffer = new byte[1];
			DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
			dsocket.receive(packet);

			masterAdress = packet.getAddress();
//			/System.out.println("Recebido! Address do emissor: " + masterAdress);
			dsocket.close();
			Socket conexao = new Socket(masterAdress, Mestre.SERVER_RECEIVE_PORT);
			if(conexao != null){
				System.out.println("Mestre conectado : "+ conexao.getInetAddress().getHostName());
			}
			String msgM;
			do{
				msgM = receberMsgMestre(conexao);
				if(msgM.equals("ARQUIVO")){
					if(enviarMsgMestre(conexao,"FREE")){
						//MANDEI MSG DE QUE ESTOU LIVRE, AGORA TENTO RECEBER O ARQUIVO	
						File result = receberArquivo(conexao);
						File resultado = ProcessarArquivo(result,"resultado/");
						if(mandaArquivo(conexao,resultado)){
							//System.out.println("Arquivo processado e enviado.");
						}
					}
				}
			}while(msgM.equals("ARQUIVO"));
			if(msgM.equals("FIM")){
				System.out.println("Processado todos os arquivos");
				conexao.close();
			}	
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static String receberMsgMestre(Socket conexao){
		InputStream entradaData;
		String dados = null;
		try {
			entradaData = conexao.getInputStream();
			DataInputStream in = new DataInputStream(entradaData);
			dados = in.readUTF();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return dados;
	}
	
	
	private static boolean enviarMsgMestre(Socket conexao, String msg){
		OutputStream saidaData;
		try {
			saidaData = conexao.getOutputStream();
			DataOutputStream out = new DataOutputStream(saidaData);
			out.writeUTF(msg);
			out.flush();
			return true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		return false;
	}
	
	private static boolean mandaArquivo(Socket conexao, File file){
		try {
			OutputStream saidaData = conexao.getOutputStream();
        	BufferedOutputStream saidaBuffer = new BufferedOutputStream(saidaData, Mestre.FILE_BUFFER);
			DataOutputStream saidaArquiv = new DataOutputStream(saidaBuffer);
			//DataOutputStream saida = new DataOutputStream(saidaData);
			// mando arquivo
			byte[] buffer = new byte[Mestre.FILE_BUFFER];
			BufferedInputStream fileReader = new BufferedInputStream(new FileInputStream(file), Mestre.FILE_BUFFER);
			saidaArquiv.writeUTF(file.getName());
			saidaArquiv.writeLong(file.length());
			int byteCount = 0;
			while ((byteCount = fileReader.read(buffer, 0, Mestre.FILE_BUFFER)) != -1)
                saidaBuffer.write(buffer, 0, byteCount);
            fileReader.close();
            saidaBuffer.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	private static File ProcessarArquivo(File localEntrada, String localSaida) throws IOException{
		
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
	
	
	private static File receberArquivo(Socket conexao){
		try {
			InputStream inp = conexao.getInputStream();
			BufferedInputStream bufferInput = new BufferedInputStream(inp);
			DataInputStream input = new DataInputStream(bufferInput);
			
			new File("files/").mkdir();
			//System.out.println("Come�ando a receber arquivos do emissor...");
			
			String nomeArquivo = input.readUTF();
			long tamArquivo = input.readLong();
		
			//System.out.println("Recebendo arquivo: " + nomeArquivo + ", Tamanho: " + tamArquivo / 1000 + " KB...");

			BufferedOutputStream fileWriter = new BufferedOutputStream(new FileOutputStream("files/" + nomeArquivo));

			for (int readBytes = 0; readBytes < tamArquivo; readBytes++)
				fileWriter.write(bufferInput.read());
			
			fileWriter.flush();
			fileWriter.close();
			System.out.println("Arquivo recebido: " + nomeArquivo + ", Tamanho: " + tamArquivo / 1000 + " KB");
			File recebido = new File("files/"+nomeArquivo);
			return recebido;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			//return "Arquivo n�o recebido, ocorreu um erro.";
		}
		return null;
	}
}
