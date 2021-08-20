package client;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import shared.SocketAtomicBroadcastBuffer;
import shared.TextMessage;

public class WorkingThread extends Thread {

	private Socket server;
	private SocketAtomicBroadcastBuffer<String> sharedBuff;
	private String host;

	public WorkingThread(Socket server) {
		this.server = server;
	}

	@Override
	public void run() {
		try (Socket socket = this.server;
				ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
				ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());) {
			TextMessage msg = new TextMessage("server.Client");
			out.writeObject(msg);
			int id = in.readInt();
			System.out.println("Moj id je " + id);
			sharedBuff = new SocketAtomicBroadcastBuffer<>(socket, in, out);
			while (true) {
				String stock = sharedBuff.get(id);
				System.out.println(stock + " " + id);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
