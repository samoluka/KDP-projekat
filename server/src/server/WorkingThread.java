package server;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import shared.TextMessage;

public class WorkingThread extends Thread {

	private Socket client;
	private int interval;
	private int id;

	public WorkingThread(Socket client, int id) {
		super();
		this.client = client;
		this.id = id;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void run() {
		try (Socket socket = this.client;
				ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
				ObjectInputStream in = new ObjectInputStream(socket.getInputStream());) {
			System.out.println("ClientLP: " + socket.getLocalPort());
			System.out.println("ClientP: " + socket.getPort());
			System.out.println("ClientA: " + socket.getInetAddress());
			System.out.println("ClientLA: " + socket.getLocalAddress());

			String className = ((TextMessage) in.readObject()).getBody();
			System.out.println(className);
			Worker w = (Worker) Class.forName(className).getConstructor().newInstance();
			w.work(client, out, in, id);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
