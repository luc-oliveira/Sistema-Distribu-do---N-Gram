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
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.file.Files;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.JOptionPane;

import Processamento.GeraMatrizMaster;
import Processamento.Gram;



public class Mestre implements Runnable {
	// Constants
	public static final int SERVER_RECEIVE_PORT = 25000, SERVER_SEND_PORT = 25001, RECEIVE_DATAGRAM = 25003;
	public static final int FILE_BUFFER = 2048;
	volatile static List<File> Files;
	private static final String FILES_LOCATION = "/CAMINHO/DA/PASTA/COM/ARQUIVOS/A/SEREM/PROCESSADOS";
	private Socket conexao;
	static Integer Arquivo = 0;
	private final static String nomeMatrizPrincipal = "geradoMatriz.txt";
	static SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
	Date hora = Calendar.getInstance().getTime(); 
	String horaInicial = sdf.format(hora);
	
	public static void main(String[] args) {
		ExecutorService thrs = Executors.newFixedThreadPool(10);
		File folder = new File(FILES_LOCATION);
		Files = Arrays.asList(folder.listFiles());
		//um singleton do bom
		GeraMatrizMaster m = GeraMatrizMaster.getInstance();
		
		boolean sentinela = true;
		thrs.execute(new keepAlive());
		try {
			ServerSocket a = new ServerSocket(SERVER_RECEIVE_PORT);
			
			while(sentinela){
				Socket conexao = a.accept();
				System.out.println("Escravo conectado: " + conexao.getInetAddress().getHostName()); 
				Mestre Mast = new  Mestre(conexao);
				thrs.execute(Mast);
				if(Arquivo > Files.size()){
					sentinela = false;
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public Mestre(Socket s){
		this.conexao = s;
	}
		

	@Override
	public void run() {
		Integer ArquivosNoDiretorio = Files.size();
		GeraMatrizMaster m = GeraMatrizMaster.getInstance();
		while(Arquivo < ArquivosNoDiretorio){
			if(enviarMsgEscravo(conexao,"ARQUIVO")){		
				String msg = receberMsgEscravo(conexao);
				if(msg != null){
			        if(msg.equals("FREE")){
			        	//SE TA LIVRE EU MANDO UM ARQUIVO PRA ELE PROCESSAR
			        	if(mandaArquivo(conexao)){
			        		File res = receberArquivo(conexao);
			        		File result = new File("resultados/"+res.getName());
			        		try {
								m.atualizaMestre(result);
								m.geraMatrizPrincipal("resultado-final/");
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
			        	}else{
			        		System.out.println("Ocorreu um erro ao enviar o arquivo.");
			        	}
			        }else{
			        	System.out.println("Nao veio msg");
			        }
				}
			}
		}
		if(Arquivo == ArquivosNoDiretorio){
			enviarMsgEscravo(conexao,"FIM");
			System.out.println("Todos os arquivos processados.");
			Date horaFin = Calendar.getInstance().getTime(); 
			String horaFim = sdf.format(horaFin);
			criarArquivoComTempoGastoParaProcessarBase(horaInicial, horaFim);
		}
		
		try {
			conexao.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private synchronized void criarArquivoComTempoGastoParaProcessarBase(String horaInicial, String horaFinal){
		try {
			FileWriter arquivo = new FileWriter(new File("resultado-final/tempo.txt"));
			arquivo.write("Come�ou a processar:" + horaInicial+ System.lineSeparator());
			arquivo.write("Terminou de processar: "+ horaFinal);
			arquivo.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static synchronized File retornaUmArquivoDaLista(){
		return Arquivo < Files.size() ? Files.get(Arquivo++) : null; 
	}
	
	private boolean enviarMsgEscravo(Socket conexao, String msg){
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
	
	private String receberMsgEscravo(Socket conexao){
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
	
	private boolean mandaArquivo(Socket conexao){
		try {
			OutputStream saidaData = conexao.getOutputStream();
        	BufferedOutputStream saidaBuffer = new BufferedOutputStream(saidaData, FILE_BUFFER);
			DataOutputStream saidaArquiv = new DataOutputStream(saidaBuffer);
			// mando arquivo
			byte[] buffer = new byte[FILE_BUFFER];
			//Seleciono o pr�ximo arquivo da lista de arquivos do diret�rio
			File file = retornaUmArquivoDaLista();
			BufferedInputStream fileReader = new BufferedInputStream(new FileInputStream(file), FILE_BUFFER);
			saidaArquiv.writeUTF(file.getName());
			saidaArquiv.writeLong(file.length());
			int byteCount = 0;
			while ((byteCount = fileReader.read(buffer, 0, FILE_BUFFER)) != -1)
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
	
	private File receberArquivo(Socket conexao){
		try {
			InputStream inp = conexao.getInputStream();
			BufferedInputStream bufferInput = new BufferedInputStream(inp);
			DataInputStream input = new DataInputStream(bufferInput);			
			new File("resultados/").mkdir();
			String nomeArquivo = input.readUTF();
			long tamArquivo = input.readLong();
			BufferedOutputStream fileWriter = new BufferedOutputStream(new FileOutputStream("resultados/" + nomeArquivo));
			for (int readBytes = 0; readBytes < tamArquivo; readBytes++)
				fileWriter.write(bufferInput.read());	
			fileWriter.flush();
			fileWriter.close();
			File resul = new File(nomeArquivo);
			return resul;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
