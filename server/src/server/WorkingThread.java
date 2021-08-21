package server;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import shared.MonitorAtomicBroadcastBuffer;
import shared.TextMessage;

public class WorkingThread extends Thread {

	private Socket client;
	private int interval;
	private ConcurrentHashMap<String, Integer> stocks;
	private int id;
	private AtomicBoolean needBalancing;
	private ConcurrentHashMap<Integer, List<String>> stocksOn;
	private AtomicInteger balanceNumber;
	private AtomicInteger numberOfStocksServers;
	private ConcurrentHashMap<Integer, AtomicBoolean> needUpdate;
	private MonitorAtomicBroadcastBuffer<String> buff;
	private AtomicInteger lf;

	public WorkingThread(Socket client, ConcurrentHashMap<String, Integer> stocks, int id, AtomicBoolean needBalancing,
			ConcurrentHashMap<Integer, List<String>> stocksOn, AtomicInteger balanceNumber,
			AtomicInteger numberOfStocksServers, ConcurrentHashMap<Integer, AtomicBoolean> needUpdate,
			MonitorAtomicBroadcastBuffer<String> buff, AtomicInteger lf) {
		super();
		this.client = client;
		this.stocks = stocks;
		this.id = id;
		this.needBalancing = needBalancing;
		this.stocksOn = stocksOn;
		this.balanceNumber = balanceNumber;
		this.numberOfStocksServers = numberOfStocksServers;
		this.needUpdate = needUpdate;
		this.buff = buff;
		this.lf = lf;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void run() {
		try (Socket socket = this.client;
				ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
				ObjectInputStream in = new ObjectInputStream(socket.getInputStream());) {
			System.out.println("ClientLP: " + socket.getLocalPort());
			System.out.println("ClientP: " + socket.getPort());
			System.out.println("ClientA: " + socket.getInetAddress());
			System.out.println("ClientLA: " + socket.getLocalAddress());

			String className = ((TextMessage) in.readObject()).getBody();
			System.out.println(className);
			Worker w = (Worker) Class.forName(className).getConstructor().newInstance();
			w.work(client, out, in, stocks, id, needBalancing, stocksOn, balanceNumber, numberOfStocksServers,
					needUpdate, buff, lf);
		} catch (Exception e) {
			// e.printStackTrace();
		}
	}
}
