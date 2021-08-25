package server;

import static server.Server.buff;
import static server.Server.serverStockMutex;
import static server.Server.stocks;
import static server.Server.stocksChanges;
import static server.Server.stocksOn;
import static server.Server.transactionsActive;
import static server.Server.transactionsFinished;
import static server.Server.workerStreamMap;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;

import shared.ChangeMessage;
import shared.Pair;
import shared.StocksMessage;

public class Client implements Worker {

	private static int x = 1000;
	private static int idTransaction = 1;
	private static int idT = 0;

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
					StocksMessage sm1 = new StocksMessage(hs);
					HashMap<String, Double> cs = new HashMap<>();
					cs.putAll(stocksChanges);
					ChangeMessage sm2 = new ChangeMessage(cs);
					out.writeObject(sm1);
					out.writeObject(sm2);
					out.flush();
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
		String username = (String) in.readObject();
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
		String msg;
		Semaphore m;
		int serverId = 0;

		while (true) {
			try {
				operation = (String) in.readObject();
				System.out.println(operation);
				mutex.acquire();
				switch (operation) {
//				case "get":
//					String item = buff.get(id);
//					out.writeObject(item);
//					break;
				case "buy":
				case "sell":
					String[] offer = ((String) in.readObject()).split(";");
					String t = operation + ";" + String.join(";", offer) + ";" + idTransaction++ + ";" + username;
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
					stream.getSecond().writeObject(t);
					stream.getSecond().flush();
					msg = (String) stream.getFirst().readObject();
					okCode = (String) stream.getFirst().readObject();
					m.release();
					if (!"OK DONE".equals(okCode)) {
						out.writeObject("GRESKA");
						out.flush();
						break;
					}
					transactionsActive.add(t);
					Set<String> idSet = Arrays.asList(msg.split(";")).stream().collect(Collectors.toSet());
					List<String> fullList = transactionsActive.parallelStream()
							.filter(((String s) -> s.split(";").length > 4 && idSet.contains(s.split(";")[4])))
							.collect(Collectors.toList());
					transactionsActive.removeAll(fullList);
					transactionsFinished.addAll(fullList);
					out.writeObject("Ok");
					out.flush();
					break;
				case "cancel":
					idT = in.readInt();
					Optional<String> stockOpt = transactionsActive.stream().filter(
							(String s) -> Integer.parseInt(s.split(";")[4]) == idT && s.split(";")[5].equals(username))
							.findFirst();
					if (stockOpt.isEmpty()) {
						out.writeObject("GRESKA");
						out.flush();
						break;
					}
					String stock = stockOpt.get().split(";")[1];
					stream = null;
					for (Entry<Integer, List<String>> pair : stocksOn.entrySet()) {
						if (pair.getValue().contains(stock)) {
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
					stream.getSecond().writeInt(idT);
					stream.getSecond().flush();
					m.release();
					okCode = (String) stream.getFirst().readObject();
					if (!"OK DONE".equals(okCode)) {
						out.writeObject("GRESKA");
						out.flush();
						break;
					}
					transactionsActive.removeIf((s) -> Integer.parseInt(s.split(";")[4]) == idT);
					out.writeObject("Canceled");
					out.flush();
					break;
				case "refresh":
					out.writeObject("transactions");
					out.writeObject(String.join("\t", transactionsActive));
					out.writeObject(String.join("\t", transactionsFinished));
					out.flush();
					okCode = (String) in.readObject();
					if (!"OK DONE".equals(okCode)) {
						System.err.println("greskaa na refresovanju transakcija");
					}
					break;
				case "status":
					idT = in.readInt();
					out.writeObject("status");
					int status = -1;
					if (transactionsActive.parallelStream()
							.anyMatch((s) -> s.split(";").length > 4 && Integer.parseInt(s.split(";")[4]) == idT))
						status = 1;
					else if (transactionsFinished.parallelStream()
							.anyMatch((s) -> s.split(";").length > 4 && Integer.parseInt(s.split(";")[4]) == idT))
						status = 2;
					out.writeInt(status);
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
