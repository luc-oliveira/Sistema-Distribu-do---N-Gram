package lucas.luana;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class keepAlive implements Runnable {
	private static Integer sleeptime = 30000;
	
	public void run(){
		try {
			encontrarEscravos();
			Thread.sleep(sleeptime);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(Mestre.Files.size() > Mestre.Arquivo){
			run();
		}
		
	}
	
	public static void encontrarEscravos(){

		try {
			System.out.println("Procurando máquinas para enviar arquivos a serem processados.");
			Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();

			while (interfaces.hasMoreElements()) {
				NetworkInterface networkInterface = interfaces.nextElement();

				if (networkInterface.isLoopback())
					continue;

				for (InterfaceAddress interfaceAddress : networkInterface
						.getInterfaceAddresses()) {
					InetAddress broadcast = interfaceAddress.getBroadcast();
					if (broadcast == null)
						continue;

					// Aqui vamos checar se o escravo responde a mensagem que enviamos
					DatagramSocket sk = new DatagramSocket();
					sk.setBroadcast(true);

					byte[] buffer = new byte[1]; // Buffer com nada
					DatagramPacket packet = new DatagramPacket(buffer, 1,
							broadcast, Escravo.ESCRAVO_RECEIVE_PORT);

					sk.send(packet);
					sk.close();
				}
			}

		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
