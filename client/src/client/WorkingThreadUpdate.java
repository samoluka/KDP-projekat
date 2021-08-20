package client;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;

import shared.TextMessage;

public class WorkingThreadUpdate extends Thread {
	private Socket server;
	private int interval;

	public WorkingThreadUpdate(Socket server, int interval) {
		this.server = server;
		this.interval = interval;
	}

	@Override
	public void run() {
		try (Socket socket = this.server;
				ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
				ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());) {
			TextMessage msg = new TextMessage("server.ClientUpdateStocks");
			out.writeObject(msg);
			while (true) {
				Scanner sc = new Scanner(System.in);
				String str = sc.nextLine();
				msg = new TextMessage(str);
				out.writeObject(msg);
				System.out.println("Poslao");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
