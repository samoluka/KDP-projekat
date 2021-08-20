package server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import shared.StocksMessage;
import shared.TextMessage;

public class LoadBalancer extends Thread {

	private AtomicBoolean needBalance;
	private ConcurrentHashMap<Integer, List<String>> stocksOn;
	private ConcurrentHashMap<String, Integer> stocks;
	private int id;
	private AtomicInteger balanceNumber;
	private AtomicInteger numberOfStocksServers;
	private ObjectOutputStream out;
	private ObjectInputStream in;

	public LoadBalancer(AtomicBoolean needBalance, AtomicInteger balanceNumber,
			ConcurrentHashMap<Integer, List<String>> stocksOn, ConcurrentHashMap<String, Integer> stocks, int id,
			AtomicInteger numberOfStocksServers, ObjectOutputStream out, ObjectInputStream in) {
		super();
		this.needBalance = needBalance;
		this.stocksOn = stocksOn;
		this.stocks = stocks;
		this.id = id;
		this.balanceNumber = balanceNumber;
		this.numberOfStocksServers = numberOfStocksServers;
		this.in = in;
		this.out = out;
	}

	private void balance() throws InterruptedException {
		synchronized (needBalance) {
			while (!needBalance.get()) {
				needBalance.wait();
			}
			System.out.println("krenuo balansiranje " + id);
			HashMap<String, Integer> hm = new HashMap<>();
			List<String> myStocks = this.stocksOn.get(id);
			for (String stock : myStocks) {
				hm.put(stock, stocks.get(stock));
			}
			StocksMessage stocksMsg = new StocksMessage(hm);
			try {
				TextMessage msg = new TextMessage("stocksServer.SetWorker");
				out.writeObject(msg);
				out.writeObject(stocksMsg);
				msg = (TextMessage) in.readObject();
				if (!"OK DONE".equals(msg.getBody())) {
					System.err.println("NESTO NIJE OK PREKIDAM PROGRAM");
					return;
				}
			} catch (IOException | ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return;
			}
			if (balanceNumber.incrementAndGet() == this.numberOfStocksServers.get()) {
				this.needBalance.set(false);
				this.balanceNumber.set(0);
				needBalance.notifyAll();
				System.out.println("zavrseno balansiranje");
			}
			needBalance.wait();
		}
	}

	@Override
	public void run() {
		while (true) {
			try {
				balance();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				// e.printStackTrace();
				return;
			}
		}
	}

}
