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
			ConcurrentHashMap<Integer, List<String>> stocksOn, AtomicInteger numberOfStocksServers,
			AtomicInteger balanceNumber) {
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
		System.out.println("Javljam balanserima");
		needBalancing.set(true);
		balanceNumber.set(0);
		needBalancing.notifyAll();
	}

	@Override
	public void work(Socket client, ObjectOutputStream out, ObjectInputStream in,
			ConcurrentHashMap<String, Integer> map, int id, AtomicBoolean needBalancing,
			ConcurrentHashMap<Integer, List<String>> stocksOn, AtomicInteger balanceNumber,
			AtomicInteger numberOfStocksServers, ConcurrentHashMap<Integer, AtomicBoolean> needUpdate,
			MonitorAtomicBroadcastBuffer<String> buff, AtomicInteger lookingFor) throws InterruptedException {

//		AtomicBoolean nu = new AtomicBoolean(false);
//		needUpdate.put(id, nu);
//		UpdateStocks us = new UpdateStocks(nu, stocksOn, map, out, in, id, buff);
//		us.start();

		synchronized (needBalancing) {
			needBalancing.set(false);
			numberOfStocksServers.incrementAndGet();
			stocksOn.put(id, new LinkedList<String>());
			if (lookingFor.get() == 0)
				rebalance(map, needBalancing, stocksOn, numberOfStocksServers, balanceNumber);
//			if (lookingFor.get() == 0) {
//				needBalancing.set(true);
//				needBalancing.notifyAll();
//			}
		}
		AtomicBoolean available = new AtomicBoolean(true);
		LoadBalancer lb = new LoadBalancer(needBalancing, balanceNumber, stocksOn, map, id, numberOfStocksServers, out,
				in, available, lookingFor);
		lb.setDaemon(false);
		lb.start();
//		synchronized (needBalancing) {
//			while (needBalancing.get()) {
//				needBalancing.wait();
//			}
//		}
		int waitingTime = 500;
		while (true) {
			synchronized (needBalancing) {
				while (needBalancing.get()) {
					needBalancing.wait();
				}
			}
			// System.out.println("Za nit " + id + " saljem poruku");
			TextMessage msg = new TextMessage("stocksServer.GetWorker");
			try {
				out.writeObject(msg);
				HashMap<String, Integer> hs = ((StocksMessage) in.readObject()).getBody();
				map.putAll(hs);
				msg = (TextMessage) in.readObject();
				if (!"OK DONE".equals(msg.getBody())) {
					System.err.println("NESTO NIJE OK PREKIDAM PROGRAM");
//					available.set(false);
//					if (lb.isAlive()) {
//						lb.interrupt();
//						lb.join();
//					}
//					synchronized (needBalancing) {
//						stocksOn.remove(id);
//						rebalance(map, needBalancing, stocksOn, numberOfStocksServers);
//						needBalancing.set(true);
//						needBalancing.notifyAll();
//					}
					return;
				}
				if (!available.get()) {
					available.set(true);
					if (lookingFor.decrementAndGet() == 0) {
					}
//						lookingFor.notifyAll();
					// numberOfStocksServers.incrementAndGet();
				}
				waitingTime = 500;
			} catch (IOException | ClassNotFoundException e) {
				// e.printStackTrace();
				System.err.println("Server nedostupan " + id);
				if (available.get()) {
					// numberOfStocksServers.decrementAndGet();
					lookingFor.incrementAndGet();
				}
				available.set(false);
				if (waitingTime == 0) {
					System.err.println("Server ugasen " + id);
					numberOfStocksServers.decrementAndGet();
					lookingFor.decrementAndGet();
					System.out.println("zavrsio nit");
					synchronized (needBalancing) {
						stocksOn.remove(id);
						if (lookingFor.get() == 0) {
							System.out.println("radim rebalans nit " + id);
							rebalance(map, needBalancing, stocksOn, numberOfStocksServers, balanceNumber);
//							balanceNumber.set(0);
//							needBalancing.set(true);
//							needBalancing.notifyAll();
						}
					}
					System.out.println("GASIM STOCKSERVER NIT " + id);
					return;
				}
				waitingTime /= 2;
			} finally {
				// System.err.println("cekam vreme: " + waitingTime);
				Thread.sleep(waitingTime);
			}
		}
	}

}
