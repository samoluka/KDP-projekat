package server;

import static server.Server.buff;
import static server.Server.stocks;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashMap;
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
		while (true) {
			try {
				operation = (String) in.readObject();
				mutex.acquire();
				switch (operation) {
//				case "get":
//					String item = buff.get(id);
//					out.writeObject(item);
//					break;
				case "buy":
				case "sell":
					String[] offer = ((String) in.readObject()).split(";");
//					stream = null;
//					for (Entry<Integer, List<String>> pair : stocksOn.entrySet()) {
//						if (pair.getValue().contains(offer[0])) {
//							stream = workerStreamMap.get(pair.getKey());
//						}
//					}
//					if (stream == null) {
//						System.err.println("GRESKAAAA");
//						break;
//					}
//					stream.getSecond().writeObject(operation);
//					stream.getSecond().writeObject(String.join(";", offer) + ";" + id);
//					stream.getSecond().flush();
//					okCode = (String) stream.getFirst().readObject();
//					if (!"OK".equals(okCode)) {
//						out.writeObject("GRESKA");
//						out.flush();
//						break;
//					}
//					transactionsActive.put(id,
//							operation + ";" + String.join(";", offer) + ";" + LocalDateTime.now().toString());
					out.writeObject("Ok");
					out.flush();
					break;
				case "cancel":
					System.out.println("cancel");
//					String[] transaction = transactionsActive.get(id).split(";");
//					stream = null;
//					for (Entry<Integer, List<String>> pair : stocksOn.entrySet()) {
//						if (pair.getValue().contains(transaction[1])) {
//							stream = workerStreamMap.get(pair.getKey());
//						}
//					}
//					if (stream == null) {
//						System.err.println("GRESKAAAA");
//						break;
//					}
//					stream.getSecond().writeObject("cancel");
//					stream.getSecond().writeObject(String.join(";", transaction) + ";" + id);
//					stream.getSecond().flush();
//					okCode = (String) stream.getFirst().readObject();
//					if (!"OK".equals(okCode)) {
//						out.writeObject("GRESKA");
//						out.flush();
//						break;
//					}
//					transactionsActive.remove(id);
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
