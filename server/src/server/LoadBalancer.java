package server;

import static server.Server.balanceNumber;
import static server.Server.needBalancing;
import static server.Server.numberOfStocksServers;
import static server.Server.stocks;
import static server.Server.stocksOn;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

import shared.StocksMessage;

public class LoadBalancer extends Thread {

	private static AtomicBoolean finish = new AtomicBoolean(false);

	private int id;

	private ObjectOutputStream out;
	private ObjectInputStream in;
	private Semaphore mutex;

	public LoadBalancer(int id, ObjectOutputStream out, ObjectInputStream in, Semaphore mutex) {
		super();

		this.id = id;
		this.in = in;
		this.out = out;
		this.mutex = mutex;
	}

	private void balance() throws InterruptedException, IOException, ClassNotFoundException {
		synchronized (needBalancing) {
			while (!needBalancing.get()) {
				System.out.println("cekam balansiranje " + id);
				needBalancing.wait();
			}
			finish.set(false);
			System.out.println("krenuo balansiranje " + id);
			HashMap<String, Integer> hm = new HashMap<>();
			List<String> myStocks = stocksOn.get(id);
			for (String stock : myStocks) {
				hm.put(stock, stocks.get(stock));
			}
			StocksMessage stocksMsg = new StocksMessage(hm);
			mutex.acquire();
			String msg = "set";
			out.writeObject(msg);
			out.flush();
			out.writeObject(stocksMsg);
			out.flush();
			msg = (String) in.readObject();
			if (!"OK DONE".equals(msg)) {
				System.err.println("NESTO NIJE OK PREKIDAM PROGRAM");
				mutex.release();
				return;
			}
			mutex.release();
//			System.out.println("nit " + id + " je zavrsila balansiranje sa brojevima: " + balanceNumber.get() + " "
//					+ numberOfStocksServers.get());
			if (balanceNumber.incrementAndGet() >= numberOfStocksServers.get()) {
				needBalancing.set(false);
				balanceNumber.set(0);
				needBalancing.notifyAll();
				synchronized (finish) {
					finish.set(true);
					finish.notifyAll();
				}
				System.out.println("zavrseno balansiranje");
			}
		}
		synchronized (finish) {
			while (!finish.get())
				finish.wait();
		}
	}

	@Override
	public void run() {
		while (true) {
			try {
				balance();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				mutex.release();
				return;
			} catch (ClassNotFoundException | IOException e) {
				e.printStackTrace();
				mutex.release();
				return;
			}
		}
	}

}
