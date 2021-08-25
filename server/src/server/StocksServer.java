package server;

import static server.Server.balanceNumber;
import static server.Server.lookingFor;
import static server.Server.needBalancing;
import static server.Server.numberOfStocksServers;
import static server.Server.serverStockMutex;
import static server.Server.stocks;
import static server.Server.stocksChanges;
import static server.Server.stocksOn;
import static server.Server.transactionsActive;
import static server.Server.transactionsOn;
import static server.Server.workerStreamMap;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import shared.Pair;

public class StocksServer implements Worker {

	private Semaphore mutex = new Semaphore(1);
	private int id;
	int waitingTime = 500;

	private void rebalance() {
		HashMap<Integer, List<String>> hs = new HashMap<Integer, List<String>>();
		HashMap<Integer, List<String>> ts = new HashMap<Integer, List<String>>();
		List<String> stocksList = new LinkedList<>();
//		List<String> transactionList = new LinkedList<>();
		stocksList.addAll(stocks.keySet());
//		transactionList.addAll(transactionsActive.values());
		int ind = 0;
		int n = numberOfStocksServers.get();
		int l = stocksList.size() / n;
		int ostatak = stocksList.size() - l * n;

		for (Integer key : stocksOn.keySet()) {
			int a = 0;
			if (ostatak > 0) {
				a = 1;
				ostatak--;
			}
			List<String> subListStocks = stocksList.subList(ind, ind + l + a);
			hs.put(key, subListStocks);
			List<String> subListTransaction = transactionsActive.parallelStream()
					.filter((s) -> (subListStocks.contains(s.split(";")[1]))).collect(Collectors.toList());
			ts.put(key, subListTransaction);
			ind += (l + a);
		}
		stocksOn.putAll(hs);
		transactionsOn.putAll(ts);
		System.out.println("Javljam balanserima");
		needBalancing.set(true);
		balanceNumber.set(0);
		needBalancing.notifyAll();
	}

	@Override
	public void work(Socket client, ObjectOutputStream out, ObjectInputStream in, int id) throws InterruptedException {
		this.id = id;
		synchronized (needBalancing) {
			needBalancing.set(false);
			numberOfStocksServers.incrementAndGet();
			stocksOn.put(id, new LinkedList<String>());
			serverStockMutex.put(id, mutex);
			workerStreamMap.put(id, new Pair<>(in, out));
			transactionsOn.put(id, new LinkedList<>());
			if (lookingFor.get() == 0)
				rebalance();
		}
		AtomicBoolean available = new AtomicBoolean(true);
		LoadBalancer lb = new LoadBalancer(id, out, in, mutex);
		lb.setDaemon(true);
		lb.start();

		while (true) {
			synchronized (needBalancing) {
				while (needBalancing.get()) {
					needBalancing.wait();
				}
			}
			mutex.acquire();
			try {
				String msg = "get";
				out.writeObject(msg);
				out.flush();
				String[] mess = ((String) in.readObject()).split("\t");
				HashMap<String, Integer> hs = new HashMap<String, Integer>();
				for (String str : mess) {
					hs.put(str.split(";")[0], Integer.parseInt(str.split(";")[1]));
				}
				out.writeObject("OK DONE");
				out.flush();
				calculateChanges(hs);
				stocks.putAll(hs);
				if (!available.get()) {
					available.set(true);
				}
				waitingTime = 500;
			} catch (IOException | ClassNotFoundException e) {
				System.err.println("Server nedostupan " + id);
				if (available.get()) {
					lookingFor.incrementAndGet();
					available.set(false);
				}
				if (waitingTime == 0) {
					removeServer();
					return;
				}
				waitingTime /= 2;
			} finally {
				// System.err.println("cekam vreme: " + waitingTime);
				mutex.release();
				Thread.sleep(waitingTime);
			}
		}
	}

	private void calculateChanges(HashMap<String, Integer> hs) {
		for (Entry<String, Integer> e : hs.entrySet()) {
			int lastPrice = stocks.get(e.getKey()) != null ? stocks.get(e.getKey()) : -1;
			if (lastPrice != -1) {
				Double change = (e.getValue() * 1.0 / lastPrice) - 1.0;
				if (change != 0.0)
					stocksChanges.put(e.getKey(), change);
			}
		}
	}

	private void removeServer() {
		System.err.println("Server ugasen " + id);
		numberOfStocksServers.decrementAndGet();
		lookingFor.decrementAndGet();
		serverStockMutex.remove(id);
		workerStreamMap.remove(id);
		System.out.println("zavrsio nit");
		synchronized (needBalancing) {
			stocksOn.remove(id);
			if (lookingFor.get() == 0) {
				System.out.println("radim rebalans nit " + id);
				rebalance();
			}
		}
		System.out.println("GASIM STOCKSERVER NIT " + id);
	}

}
