package server;

import static server.Server.buff;
import static server.Server.serverStockMutex;
import static server.Server.stocks;
import static server.Server.stocksOn;
import static server.Server.transactionsActive;
import static server.Server.workerStreamMap;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.Semaphore;

import shared.Pair;
import shared.StocksMessage;

public class Client implements Worker {

	private static int x = 1000;

	private ObjectOutputStream out;
	private ObjectInputStream in;
	private int id;
	private Semaphore mutex = new Semaphore(1);

	private Thread t = new Thread(new Runnable() {

		@Override
		public void run() {
			while (true) {
				try {
					mutex.acquire();
					out.writeObject("stocks");
					out.flush();
					HashMap<String, Integer> hs = new HashMap<>();
					hs.putAll(stocks);
					StocksMessage sm = new StocksMessage(hs);
					out.writeObject(sm);
					mutex.release();
					Thread.sleep(x);
				} catch (InterruptedException | IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return;
				}
			}
		}
	});

	@Override
	public void work(Socket client, ObjectOutputStream out, ObjectInputStream in, int id)
			throws IOException, Exception {

		buff.addListener(id);
		out.writeInt(id);
		out.flush();
		this.in = in;
		this.out = out;
		this.id = id;
		t.setDaemon(true);
		t.start();
//		SocketBufferWorker sbw = new SocketBufferWorker(buff, client, in, out, id);
//		sbw.run();
		Pair<ObjectInputStream, ObjectOutputStream> stream = null;
		String okCode;
		String operation;
		Semaphore m;
		int serverId = 0;
		while (true) {
			try {
				operation = (String) in.readObject();
				System.out.println(operation);
				mutex.acquire();
				switch (operation) {
				case "get":
					String item = buff.get(id);
					out.writeObject(item);
					break;
				case "buy":
				case "sell":
					String[] offer = ((String) in.readObject()).split(";");
					stream = null;
					serverId = 0;
					for (Entry<Integer, List<String>> pair : stocksOn.entrySet()) {
						if (pair.getValue().contains(offer[0])) {
							stream = workerStreamMap.get(pair.getKey());
							serverId = pair.getKey();
							break;
						}
					}
					if (stream == null) {
						System.err.println("GRESKAAAA");
						out.writeObject("GRESKAAA");
						out.flush();
						break;
					}
					m = serverStockMutex.get(serverId);
					m.acquire();
					stream.getSecond().writeObject(operation);
					stream.getSecond().writeObject(String.join(";", offer) + ";" + id);
					stream.getSecond().flush();
					okCode = (String) stream.getFirst().readObject();
					m.release();
					if (!"OK DONE".equals(okCode)) {
						out.writeObject("GRESKA");
						out.flush();
						break;
					}
					transactionsActive.put(id, operation + ";" + String.join(";", offer) + ";"
							+ LocalDateTime.now().toString() + ";" + id);
					out.writeObject("Ok");
					out.flush();
					break;
				case "cancel":
					System.out.println("cancel");
					String[] transaction = transactionsActive.get(id).split(";");
					stream = null;
					for (Entry<Integer, List<String>> pair : stocksOn.entrySet()) {
						if (pair.getValue().contains(transaction[1])) {
							stream = workerStreamMap.get(pair.getKey());
							serverId = pair.getKey();
							break;
						}
					}
					if (stream == null) {
						System.err.println("GRESKAAAA");
						out.writeObject("GRESKAAA");
						out.flush();
						break;
					}
					m = serverStockMutex.get(serverId);
					m.acquire();
					stream.getSecond().writeObject("cancel");
					stream.getSecond().writeObject(String.join(";", transaction) + ";" + id);
					stream.getSecond().flush();
					m.release();
					okCode = (String) stream.getFirst().readObject();
					if (!"OK DONE".equals(okCode)) {
						out.writeObject("GRESKA");
						out.flush();
						break;
					}
					transactionsActive.remove(id);
					out.writeObject("Canceled");
					out.flush();
					break;
				default:
//					String ret = String.format("*** Operation %s not supported.", operation);
//					System.out.println(ret);
//					out.writeObject(ret);
					break;
				}
				mutex.release();
			} catch (ClassNotFoundException | IOException e) {
				// System.err.println("OVDE PUCA");
				e.printStackTrace();
				buff.removeListener(id);
				System.out.println("DISKONEKTOVAN KLIJENT " + id);
				return;
			}
		}
	}

}
