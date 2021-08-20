package server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import shared.MonitorAtomicBroadcastBuffer;
import shared.StocksMessage;
import shared.TextMessage;

public class StocksServer implements Worker {

	private void rebalance(ConcurrentHashMap<String, Integer> map, AtomicBoolean needBalancing,
			ConcurrentHashMap<Integer, List<String>> stocksOn, AtomicInteger numberOfStocksServers) {
		HashMap<Integer, List<String>> hs = new HashMap<Integer, List<String>>();
		List<String> stocks = new LinkedList<>();
		stocks.addAll(map.keySet());
		int ind = 0;
		int n = numberOfStocksServers.get();
		int l = stocks.size() / n;
		int ostatak = stocks.size() - l * n;

		for (Integer key : stocksOn.keySet()) {
			int a = 0;
			if (ostatak > 0) {
				a = 1;
				ostatak--;
			}
			hs.put(key, stocks.subList(ind, ind + l + a));
			ind += (l + a);
		}
		stocksOn.putAll(hs);
	}

	@Override
	public void work(Socket client, ObjectOutputStream out, ObjectInputStream in,
			ConcurrentHashMap<String, Integer> map, int id, AtomicBoolean needBalancing,
			ConcurrentHashMap<Integer, List<String>> stocksOn, AtomicInteger balanceNumber,
			AtomicInteger numberOfStocksServers, ConcurrentHashMap<Integer, AtomicBoolean> needUpdate,
			MonitorAtomicBroadcastBuffer<String> buff) throws InterruptedException {

		AtomicBoolean nu = new AtomicBoolean(false);
		needUpdate.put(id, nu);
//		UpdateStocks us = new UpdateStocks(nu, stocksOn, map, out, in, id, buff);
//		us.start();
		needBalancing.set(false);
		synchronized (needBalancing) {
			numberOfStocksServers.incrementAndGet();
			stocksOn.put(id, new LinkedList<String>());
			rebalance(map, needBalancing, stocksOn, numberOfStocksServers);
			needBalancing.set(true);
			needBalancing.notifyAll();
		}
		LoadBalancer lb = new LoadBalancer(needBalancing, balanceNumber, stocksOn, map, id, numberOfStocksServers, out,
				in);
		lb.start();
		synchronized (needBalancing) {
			while (needBalancing.get()) {
				needBalancing.wait();
			}
		}
		int waitingTime = 10000;
		while (true) {
			synchronized (needBalancing) {
				while (needBalancing.get()) {
					needBalancing.wait();
				}
			}
			TextMessage msg = new TextMessage("stocksServer.GetWorker");
			try {
				out.writeObject(msg);
				HashMap<String, Integer> hs = ((StocksMessage) in.readObject()).getBody();
				map.putAll(hs);
				msg = (TextMessage) in.readObject();
				if (!"OK DONE".equals(msg.getBody())) {
					System.err.println("NESTO NIJE OK PREKIDAM PROGRAM");
					return;
				}
				waitingTime = 10000;
			} catch (IOException | ClassNotFoundException e) {
				// e.printStackTrace();
				System.err.println("Server nedostupan " + id);
				if (waitingTime == 0) {
					System.err.println("Server ugasen " + id);
					numberOfStocksServers.decrementAndGet();
					lb.interrupt();
					lb.join();
					synchronized (needBalancing) {
						stocksOn.remove(id);
						rebalance(map, needBalancing, stocksOn, numberOfStocksServers);
						needBalancing.set(true);
						needBalancing.notifyAll();
					}
					break;
				}
				waitingTime /= 2;
			} finally {
				System.err.println("cekam vreme: " + waitingTime);
				Thread.sleep(waitingTime);
			}
		}
		System.out.println("GASIM STOCKSERVER NIT " + id);
	}

}
