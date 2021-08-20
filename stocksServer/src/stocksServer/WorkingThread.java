package stocksServer;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;

import shared.TextMessage;

public class WorkingThread extends Thread {

	private Socket server;

	private HashMap<String, Integer> stocks = new HashMap<>();

	public WorkingThread(Socket server) {
		this.server = server;
	}

	@Override
	public void run() {
		try (Socket socket = this.server;
				ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
				ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());) {
			TextMessage msg = new TextMessage("server.StocksServer");
			out.writeObject(msg);
			while (true) {
				String className = ((TextMessage) in.readObject()).getBody();
				Worker w = (Worker) Class.forName(className).getConstructor().newInstance();
				this.stocks = w.work(out, in, this.stocks);
				TextMessage okmsg = new TextMessage("OK DONE");
				out.writeObject(okmsg);
				System.out.println("Zavrsio");
				for (Entry<String, Integer> pair : this.stocks.entrySet()) {
					String key = pair.getKey();
					Integer value = pair.getValue();
					System.out.println(key + " " + value + "\n");
					Date date = new Date();
					System.out.println("Vreme: " + new Timestamp(date.getTime()));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
