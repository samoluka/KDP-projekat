package server;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import shared.MonitorAtomicBroadcastBuffer;

public class UpdateStocks extends Thread {

	private AtomicBoolean needUpdate;
	private ConcurrentHashMap<Integer, List<String>> stocksOn;
	private ConcurrentHashMap<String, Integer> stocks;
	private ObjectOutputStream out;
	private ObjectInputStream in;
	private int id;
	private MonitorAtomicBroadcastBuffer<String> buff;

	public UpdateStocks(AtomicBoolean needUpdate, ConcurrentHashMap<Integer, List<String>> stocksOn,
			ConcurrentHashMap<String, Integer> stocks, ObjectOutputStream out, ObjectInputStream in, int id,
			MonitorAtomicBroadcastBuffer<String> buff) {
		super();
		this.needUpdate = needUpdate;
		this.stocksOn = stocksOn;
		this.stocks = stocks;
		this.out = out;
		this.in = in;
		this.id = id;
		this.buff = buff;
	}

//	private void update() {
//		synchronized (needUpdate) {
//			while (!needUpdate.get()) {
//				try {
//					needUpdate.notifyAll();
//					needUpdate.wait();
//				} catch (InterruptedException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
//			HashMap<String, Integer> hm = new HashMap<>();
//			List<String> myStocks = this.stocksOn.get(id);
//			for (String stock : myStocks) {
//				hm.put(stock, stocks.get(stock) * 2);
//			}
//			StocksMessage stocksMsg = new StocksMessage(hm);
//			try {
//				TextMessage msg = new TextMessage("stocksServer.SetWorker");
//				out.writeObject(msg);
//				out.writeObject(stocksMsg);
//				msg = (TextMessage) in.readObject();
//				if (!"OK DONE".equals(msg.getBody())) {
//					System.err.println("NESTO NIJE OK PREKIDAM PROGRAM");
//					return;
//				}
//			} catch (IOException | ClassNotFoundException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//				return;
//			}
//			needUpdate.set(false);
//		}
//	}

	@Override
	public void run() {
		while (true) {
			for (Entry<String, Integer> pair : stocks.entrySet()) {
				// stocks.put(pair.getKey(), pair.getValue() * 2);
				buff.put(pair.getKey() + ";" + pair.getValue());
				stocks.put(pair.getKey(), (int) (pair.getValue() * 1.1));
			}
			// System.out.println("UPDATE STOCKS");
			try {
				sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
