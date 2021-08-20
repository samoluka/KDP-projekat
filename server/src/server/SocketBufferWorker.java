package server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import shared.AtomicBroadcastBuffer;

public class SocketBufferWorker extends Thread {
	private Socket client;
	private AtomicBroadcastBuffer<String> buffer;
	private ObjectOutputStream out;
	private ObjectInputStream in;
	private int id;

	public SocketBufferWorker(AtomicBroadcastBuffer<String> buffer, Socket client, ObjectInputStream in,
			ObjectOutputStream out, int id) {
		this.in = in;
		this.out = out;
		this.client = client;
		this.buffer = buffer;
		this.id = id;
	}

	@Override
	public void run() {
		while (true) {
			try {
				String operation = (String) in.readObject();
				switch (operation) {
				case "put":
					out.writeObject("OK DONE");
					out.flush();
					String msg = (String) in.readObject();
					buffer.put(msg);
					out.writeObject("OK");
					break;

				case "get":
					String item = buffer.get(id);
					out.writeObject(item);
					break;

				default:
					String ret = String.format("*** Operation %s not supported.", operation);
					System.out.println(ret);
					out.writeObject(ret);
					break;
				}
			} catch (ClassNotFoundException | IOException e) {
				// System.err.println("OVDE PUCA");
				// e.printStackTrace();
				buffer.removeListener(id);
				System.out.println("DISKONEKTOVAN KLIJENT " + id);
				return;
			}
		}
	}

}
